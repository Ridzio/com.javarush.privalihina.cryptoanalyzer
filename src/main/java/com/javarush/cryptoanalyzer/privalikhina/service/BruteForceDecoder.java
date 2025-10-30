
package com.javarush.cryptoanalyzer.privalikhina.service;

import com.javarush.cryptoanalyzer.privalikhina.constants.CryptoAlphabet;
import com.javarush.cryptoanalyzer.privalikhina.entity.Result;
import com.javarush.cryptoanalyzer.privalikhina.exception.ApplicationException;
import com.javarush.cryptoanalyzer.privalikhina.repository.ResultCode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

public class BruteForceDecoder implements Function {

    private static final String BEST_FOLDER_NAME = "result_best_shift";
    private static final String FILE_TEMPLATE = "decoded_shift_%d.txt";
    private static final String BEST_FILE_TEMPLATE = "best_decoded_shift_%d.txt";

    @Override
    public Result execute(String[] parameters) {
        // parameters: [inputFile, outputsDirPath] (ConsoleView supplies mode,input,outputs)
        String inputFile = parameters[0];
        String outputsDirArg = parameters[1];

        try {
            Path inputPath = Path.of(inputFile).toAbsolutePath();
            String content = Files.readString(inputPath, StandardCharsets.UTF_8);

            Path requestedOutputsDir = Path.of(outputsDirArg).toAbsolutePath();
            Path outputsDir;

            // Если указанная папка уже существует — создаём уникальную папку рядом, чтобы не перезаписывать старые результаты
            if (Files.exists(requestedOutputsDir)) {
                // если это файл — используем его parent
                if (Files.isRegularFile(requestedOutputsDir)) {
                    Path parent = requestedOutputsDir.getParent() != null ? requestedOutputsDir.getParent() : Paths.get(".");
                    outputsDir = createUniqueOutputDir(parent, requestedOutputsDir.getFileName().toString());
                } else {
                    // это директория — создаём уникальную подпапку внутри неё (base name = имя директории)
                    outputsDir = createUniqueOutputDir(requestedOutputsDir.getParent() != null ? requestedOutputsDir.getParent() : Paths.get("."),
                            requestedOutputsDir.getFileName().toString());
                }
            } else {
                // если не существует — просто создаём её
                Files.createDirectories(requestedOutputsDir);
                outputsDir = requestedOutputsDir;
            }

            // Теперь у нас есть пустая папка outputsDir, в которую будем писать
            int alphabetSize = CryptoAlphabet.ALL_SYMBOLS.length();

            for (int shift = 0; shift < alphabetSize; shift++) {
                String decoded = CaesarDecoder.decode(content, shift);
                Path outFile = outputsDir.resolve(String.format(FILE_TEMPLATE, shift));
                Files.writeString(outFile, decoded, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }

            // Оценим все файлы в outputsDir и найдём лучший
            int bestShift = -1;
            String bestDecoded = "";
            int bestScore = Integer.MIN_VALUE;

            try (Stream<Path> stream = Files.list(outputsDir)) {
                for (Path p : (Iterable<Path>) stream::iterator) {
                    if (!Files.isRegularFile(p)) continue;
                    String fileName = p.getFileName().toString();
                    if (!fileName.startsWith("decoded_shift_") || !fileName.endsWith(".txt")) continue;

                    String decodedText = Files.readString(p, StandardCharsets.UTF_8);
                    int score = rateText(decodedText);

                    if (score > bestScore) {
                        bestScore = score;
                        bestDecoded = decodedText;
                        try {
                            String num = fileName.substring("decoded_shift_".length(), fileName.length() - ".txt".length());
                            bestShift = Integer.parseInt(num);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }

            if (bestShift < 0) {
                return new Result(ResultCode.ERROR, new ApplicationException("BruteForce did not produce any output files", null));
            }

            // Сохраняем лучший результат в подпапку outputsDir/result_best_shift
            Path bestDir = outputsDir.resolve(BEST_FOLDER_NAME);
            if (!Files.exists(bestDir)) {
                Files.createDirectories(bestDir);
            }
            Path bestOut = bestDir.resolve(String.format(BEST_FILE_TEMPLATE, bestShift));
            Files.writeString(bestOut, bestDecoded, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // Сообщения для пользователя
            System.out.println("Brute force completed successfully. Best shift = " + bestShift);
            System.out.println("All variants are in: " + outputsDir.toAbsolutePath());
            System.out.println("Best result saved to: " + bestOut.toAbsolutePath());

            return new Result(ResultCode.OK, bestDecoded, bestShift);

        } catch (IOException e) {
            e.printStackTrace();
            return new Result(ResultCode.ERROR, new ApplicationException("IO error during BruteForce", e));
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(ResultCode.ERROR, new ApplicationException("BruteForce operation finished with exception", e));
        }
    }

    /**
     * Создаёт уникальную папку baseName или baseName_1, baseName_2 ... в parent.
     * Возвращает созданную Path.
     */
    private Path createUniqueOutputDir(Path parent, String baseName) throws IOException {
        Path candidate = parent.resolve(baseName);
        if (!Files.exists(candidate)) {
            Files.createDirectories(candidate);
            return candidate;
        }
        int counter = 1;
        while (true) {
            Path cand = parent.resolve(baseName + "_" + counter);
            if (!Files.exists(cand)) {
                Files.createDirectories(cand);
                return cand;
            }
            counter++;
        }
    }

    /**
     * Оценка "читаемости" текста: суммарное количество вхождений слов из COMMON_WORDS.
     */
    private int rateText(String text) {
        int score = 0;
        if (text == null || text.isEmpty()) return score;
        String lower = text.toLowerCase();
        for (String word : CryptoAlphabet.COMMON_WORDS) {
            if (word == null || word.isEmpty()) continue;
            int idx = 0;
            while ((idx = lower.indexOf(word, idx)) != -1) {
                score++;
                idx += word.length();
            }
        }
        return score;
    }
}