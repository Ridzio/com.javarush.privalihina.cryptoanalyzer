
package com.javarush.cryptoanalyzer.privalikhina.service;

import com.javarush.cryptoanalyzer.privalikhina.constants.CryptoAlphabet;
import com.javarush.cryptoanalyzer.privalikhina.entity.Result;
import com.javarush.cryptoanalyzer.privalikhina.exception.ApplicationException;
import com.javarush.cryptoanalyzer.privalikhina.repository.ResultCode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Stream;

public class BruteForceDecoder implements Function {

    private static final String BEST_FOLDER_NAME = "result_best_shift";
    private static final String FILE_TEMPLATE = "decoded_shift_%d.txt";
    private static final String BEST_FILE_TEMPLATE = "best_decoded_shift_%d.txt";

    @Override
    public Result execute(String[] parameters) {
        String inputFile = parameters[0];
        String outputsDirArg = parameters[1];

        try {
            Path inputPath = Path.of(inputFile).toAbsolutePath();
            String content = Files.readString(inputPath, StandardCharsets.UTF_8);

            Path requestedOutputsDir = Path.of(outputsDirArg).toAbsolutePath();
            Path outputsDir = resolveOutputDir(requestedOutputsDir);

            int alphabetSize = CryptoAlphabet.ALL_SYMBOLS.length();

            // 1) генерируем все варианты
            for (int shift = 0; shift < alphabetSize; shift++) {
                String decoded = CaesarDecoder.decode(content, shift);
                Path outFile = outputsDir.resolve(String.format(FILE_TEMPLATE, shift));
                Files.writeString(outFile, decoded, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }

            // 2) оцениваем варианты
            int bestShift = -1;
            String bestDecoded = "";
            double bestScore = Double.NEGATIVE_INFINITY;

            try (Stream<Path> stream = Files.list(outputsDir)) {
                for (Path p : (Iterable<Path>) stream::iterator) {
                    if (!Files.isRegularFile(p)) continue;
                    String fileName = p.getFileName().toString();
                    if (!fileName.startsWith("decoded_shift_") || !fileName.endsWith(".txt")) continue;

                    String decodedText = Files.readString(p, StandardCharsets.UTF_8);
                    double score = rateText(decodedText);

                    try {
                        String num = fileName.substring("decoded_shift_".length(),
                                fileName.length() - ".txt".length());
                        int currentShift = Integer.parseInt(num);

                        if (score > bestScore) {
                            bestScore = score;
                            bestDecoded = decodedText;
                            bestShift = currentShift;
                        } else if (Math.abs(score - bestScore) < 1e-9) {
                            // Если одинаковый скор — выбираем меньший сдвиг
                            int canonBest = Math.floorMod(bestShift, alphabetSize);
                            int canonCur = Math.floorMod(currentShift, alphabetSize);
                            if (canonCur < canonBest) {
                                bestDecoded = decodedText;
                                bestShift = currentShift;
                            }
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }

            if (bestShift < 0)
                return new Result(ResultCode.ERROR, new ApplicationException("Не удалось определить лучший сдвиг", null));

            // Канонизируем bestShift: теперь 37 и 4 станут одинаковыми и будут представлены минимальным эквивалентом
            bestShift = Math.floorMod(bestShift, alphabetSize);

            // 3) сохранить лучший результат
            Path bestDir = outputsDir.resolve(BEST_FOLDER_NAME);
            Files.createDirectories(bestDir);
            Path bestOut = bestDir.resolve(String.format(BEST_FILE_TEMPLATE, bestShift));
            Files.writeString(bestOut, bestDecoded, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println("Brute force завершён успешно. Лучший сдвиг = " + bestShift);
            System.out.println("Результаты: " + outputsDir.toAbsolutePath());
            System.out.println("Лучший результат сохранён: " + bestOut.toAbsolutePath());

            return new Result(ResultCode.OK, bestDecoded, bestShift);

        } catch (Exception e) {
            e.printStackTrace();
            return new Result(ResultCode.ERROR, new ApplicationException("Ошибка при BruteForce", e));
        }
    }

    /**
     * Создаёт директорию для вывода, не оставляя пустых папок.
     */
    private Path resolveOutputDir(Path baseDir) throws IOException {
        if (!Files.exists(baseDir)) {
            Files.createDirectories(baseDir);
            return baseDir;
        }

        // Если папка существует и не пуста, создаём _1, _2, ...
        if (Files.isDirectory(baseDir) && Files.list(baseDir).findAny().isPresent()) {
            int counter = 1;
            Path newDir;
            do {
                newDir = baseDir.resolveSibling(baseDir.getFileName() + "_" + counter);
                counter++;
            } while (Files.exists(newDir));
            Files.createDirectories(newDir);
            return newDir;
        }

        return baseDir;
    }

    /**
     * Оценка читаемости текста.
     * χ² считается по всем символам, которые входят в CryptoAlphabet.ALL_SYMBOLS,
     * с ожидаемым распределением, где буквы используют LETTER_FREQ, а пробел/цифры/пунктуация получают небольшую массу.
     * Дополнительные сигналы: биграммы (включая пробелы) и энтропия — небольшие бонусы.
     */
    private double rateText(String text) {
        if (text == null || text.isEmpty()) return Double.NEGATIVE_INFINITY;

        String allSymbols = CryptoAlphabet.ALL_SYMBOLS;
        int totalLen = Math.max(1, text.length());

        // 1) наблюдаемые частоты по всем символам (в том числе пробел/пунктуация/цифры)
        Map<Character, Integer> observed = new HashMap<>();
        for (char c : text.toCharArray()) {
            if (allSymbols.indexOf(c) >= 0) {
                observed.put(c, observed.getOrDefault(c, 0) + 1);
            } else {
                // если встретился незарегистрированный символ — учитываем его как прочие (игнорируем в χ²)
            }
        }

        // 2) ожидаемое распределение (нормировано на 1.0) по всем символам
        Map<Character, Double> expectedDist = buildExpectedDistribution(allSymbols);

        // 3) χ² по всем символам (нормируем по длине текста)
        double chi = 0.0;
        for (int i = 0; i < allSymbols.length(); i++) {
            char symbol = allSymbols.charAt(i);
            double expectedProb = expectedDist.getOrDefault(symbol, 0.0);
            double expectedCount = expectedProb * totalLen;
            double obsCount = observed.getOrDefault(symbol, 0);
            double diff = obsCount - expectedCount;
            chi += (diff * diff) / (expectedCount + 1e-6); // +epsilon чтобы избежать деления на 0
        }
        double chiNormalized = chi / Math.max(1, totalLen);

        // 4) биграммы (учитываем пробелы и пунктуацию)
        Set<String> bigrams = new HashSet<>();
        String lower = text.toLowerCase();
        for (int i = 0; i < lower.length() - 1; i++) {
            bigrams.add(lower.substring(i, i + 2));
        }
        double bigramDiversity = (double) bigrams.size() / Math.max(1, lower.length());

        // 5) энтропия по всем символам
        double entropy = 0.0;
        for (Map.Entry<Character, Integer> e : observed.entrySet()) {
            double p = (double) e.getValue() / totalLen;
            entropy -= p * (Math.log(p) / Math.log(2));
        }

        // 6) комбинируем сигналы в итоговый скор
        // Весовые коэффициенты можно подгонять по задачам; я предлагаю такие значения как стартовые.
        double wChi = 1.0;        // χ² (меньше — лучше)
        double wBigram = 40.0;    // биграммы — сильный признак читаемости
        double wEntropy = 5.0;    // энтропия — дополнительный признак
        double wChiScale = 150.0; // масштаб для перевода chiNormalized в адекватную шкалу

        // чем меньше χ² -> лучше => используем обратную функцию
        double chiScore = 1.0 / (1.0 + chiNormalized / wChiScale);

        double score = wChi * chiScore + wBigram * bigramDiversity + wEntropy * entropy;

        // небольшой штраф за очень маленькую долю букв (если текст состоит в основном из цифр/символов)
        int letterCount = 0;
        for (char c : text.toLowerCase().toCharArray()) {
            if (CryptoAlphabet.RUS_LOWER.indexOf(c) >= 0 || CryptoAlphabet.RUS_UPPER.indexOf(c) >= 0) letterCount++;
        }
        double letterRatio = (double) letterCount / Math.max(1, totalLen);
        if (letterRatio < 0.15) {
            score -= 50.0 * (0.15 - letterRatio); // штраф
        }

        return score;
    }

    /**
     * Построение ожидаемого распределения вероятностей по всем символам ALL_SYMBOLS.
     * Идея:
     * - все буквы (нижний/верхний) получают веса, основанные на CryptoAlphabet.LETTER_FREQ;
     * - пробел (WHITESPACE) получает вес spaceMass;
     * - цифры и пунктуация получают равномерные небольшие массы.
     *
     * Возвращаем Map<Character, Double> где суммы значений = 1.0
     */
    private Map<Character, Double> buildExpectedDistribution(String allSymbols) {
        // Настраиваемые массы (эмпирически)
        double lettersMass = 0.78;    // общая доля букв
        double spaceMass = 0.12;      // доля пробелов (и возможно других whitespace)
        double digitsMass = 0.05;     // доля всех цифр вместе
        double punctMass = 1.0 - (lettersMass + spaceMass + digitsMass); // остальное - пунктуация

        // Соберём базовые множители
        Map<Character, Double> result = new HashMap<>();

        // 1) буквы (нижний и верхний)
        // суммарная частота в LETTER_FREQ уже ~1.0 for lowercase set,
        // используем её и распределим lettersMass между строчными и заглавными одинаково
        double letterLowerMass = lettersMass / 2.0;
        double letterUpperMass = lettersMass / 2.0;

        double sumLowerFreq = CryptoAlphabet.LETTER_FREQ.values().stream().mapToDouble(Double::doubleValue).sum();
        // распределяем по строчным
        for (Map.Entry<Character, Double> e : CryptoAlphabet.LETTER_FREQ.entrySet()) {
            char lower = e.getKey();
            double freq = e.getValue() / sumLowerFreq; // нормируем на 1
            result.put(lower, freq * letterLowerMass);
            // заглавная версия
            char upper = Character.toUpperCase(lower);
            result.put(upper, freq * letterUpperMass);
        }

        // 2) пробелы / whitespace (в CryptoAlphabet WHITESPACE обычно = " ")
        String whitespace = CryptoAlphabet.WHITESPACE;
        if (whitespace == null || whitespace.isEmpty()) whitespace = " ";
        double perWhitespace = spaceMass / whitespace.length();
        for (char c : whitespace.toCharArray()) {
            result.put(c, perWhitespace);
        }

        // 3) цифры
        String digits = CryptoAlphabet.DIGITS;
        if (digits == null || digits.isEmpty()) digits = "0123456789";
        double perDigit = digitsMass / digits.length();
        for (char d : digits.toCharArray()) {
            result.put(d, perDigit);
        }

        // 4) пунктуация — все оставшиеся символы из ALL_SYMBOLS, которые ещё не внесены
        List<Character> puncts = new ArrayList<>();
        for (char c : allSymbols.toCharArray()) {
            if (!result.containsKey(c)) puncts.add(c);
        }
        double perPunct = puncts.isEmpty() ? 0.0 : punctMass / puncts.size();
        for (char c : puncts) {
            result.put(c, perPunct);
        }

        // 5) небольшая нормировка (чтобы сумма = 1.0)
        double sum = result.values().stream().mapToDouble(Double::doubleValue).sum();
        if (sum <= 0) {
            // fallback: равномерное распределение
            double u = 1.0 / allSymbols.length();
            Map<Character, Double> uniform = new HashMap<>();
            for (char c : allSymbols.toCharArray()) uniform.put(c, u);
            return uniform;
        }
        double invSum = 1.0 / sum;
        result.replaceAll((k, v) -> v * invSum);
        return result;
    }
}