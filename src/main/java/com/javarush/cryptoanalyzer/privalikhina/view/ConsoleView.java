package com.javarush.cryptoanalyzer.privalikhina.view;

import com.javarush.cryptoanalyzer.privalikhina.entity.Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import static com.javarush.cryptoanalyzer.privalikhina.constants.ApplicationCompletionConstants.EXCEPTION;
import static com.javarush.cryptoanalyzer.privalikhina.constants.ApplicationCompletionConstants.SUCCESS;
import static com.javarush.cryptoanalyzer.privalikhina.constants.FunctionCodeConstants.*;

public class ConsoleView implements View {

    private static final String INVALID_CHARS = "[/\\\\:*?\"<>|]";

    @Override
    public String[] getParameters() {
        Scanner scanner = new Scanner(System.in);

        // Выбор режима
        System.out.println("Выберите режим работы:");
        System.out.println("1 - Зашифровать");
        System.out.println("2 - Расшифровать");
        System.out.println("3 - Брутфорс");
        System.out.print("Введите номер режима: ");
        String modeInput = scanner.nextLine().trim();

        String mode;
        switch (modeInput) {
            case "1" -> mode = ENCODE;
            case "2" -> mode = DECODE;
            case "3" -> mode = BRUTEFORCE;
            default -> {
                System.out.println("Неверный режим. Будет использована неподдерживаемая функция.");
                return new String[]{UNSUPPORTED_FUNCTION};
            }
        }

        // Проверка существования входного файла
        String inputFilePath;
        while (true) {
            System.out.print("Введите путь к входному файлу: ");
            inputFilePath = scanner.nextLine().trim();
            if (Files.exists(Path.of(inputFilePath)) && Files.isRegularFile(Path.of(inputFilePath))) {
                break;
            } else {
                System.out.println("Файл не найден. Пожалуйста, убедитесь, что путь указан корректно.");
            }
        }

        // Ввод пути к выходному файлу/папке
        System.out.print("Введите путь к выходному файлу или папке: ");
        String outputPathInput = scanner.nextLine().trim();

        // Формируем корректный путь к выходному файлу
        String outputFilePath = resolveOutputFilePath(outputPathInput, mode);

        if (outputFilePath == null) {
            System.out.println("Не удалось определить корректный путь для выходного файла.");
            return new String[]{UNSUPPORTED_FUNCTION};
        }

        if (mode.equals(BRUTEFORCE)) {
            return new String[]{mode, inputFilePath, outputFilePath};
        } else {
            // Для encode/decode нужно запросить сдвиг
            System.out.print("Введите сдвиг (целое число): ");
            String shift = scanner.nextLine().trim();
            return new String[]{mode, inputFilePath, outputFilePath, shift};
        }
    }

    /**
     * Если введён путь к папке — создаёт дефолтный файл внутри.
     * Если путь к файлу — проверяет корректность имени.
     * Возвращает полный путь к выходному файлу.
     */
    private String resolveOutputFilePath(String pathInput, String mode) {
        if (pathInput.isEmpty()) {
            // Если ничего не ввели — используем дефолтные имена и папку текущую
            return defaultFileNameForMode(mode);
        }

        Path path = Paths.get(pathInput);

        if (Files.exists(path)) {
            if (Files.isDirectory(path)) {
                // Если это папка — добавляем дефолтное имя файла
                Path filePath = path.resolve(defaultFileNameForMode(mode));
                createFolderIfNotExists(path.toString());
                return filePath.toString();
            } else if (Files.isRegularFile(path)) {
                // Это файл — проверяем имя
                if (isValidFileName(path.getFileName().toString())) {
                    createFolderIfNotExists(path.getParent().toString());
                    return path.toString();
                } else {
                    System.out.println("Имя файла содержит недопустимые символы.");
                    return null;
                }
            } else {
                System.out.println("Путь не является ни файлом, ни папкой.");
                return null;
            }
        } else {
            // Если файла/папки нет — пробуем понять, это папка или файл по расширению
            if (pathInput.endsWith(".txt")) {
                // Вроде как файл, но его нет — создадим папку, если нужно, и вернём путь
                Path parent = path.getParent();
                if (parent != null) {
                    createFolderIfNotExists(parent.toString());
                }
                if (isValidFileName(path.getFileName().toString())) {
                    return path.toString();
                } else {
                    System.out.println("Имя файла содержит недопустимые символы.");
                    return null;
                }
            } else {
                // Возможно папка, создаём её
                createFolderIfNotExists(path.toString());
                Path filePath = path.resolve(defaultFileNameForMode(mode));
                return filePath.toString();
            }
        }
    }

    private boolean isValidFileName(String fileName) {
        return !fileName.matches(".*" + INVALID_CHARS + ".*");
    }

    private String defaultFileNameForMode(String mode) {
        return switch (mode) {
            case ENCODE -> "input.txt";
            case DECODE -> "output.txt";
            case BRUTEFORCE -> "best.txt";
            default -> "result.txt";
        };
    }

    private void createFolderIfNotExists(String folderPath) {
        Path folder = Paths.get(folderPath);
        if (!Files.exists(folder)) {
            try {
                Files.createDirectories(folder);
                System.out.println("Папка создана: " + folder.toAbsolutePath());
            } catch (IOException e) {
                System.out.println("Не удалось создать папку: " + folder.toAbsolutePath());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void printResult(Result result) {
        switch (result.getResultCode()) {
            case OK -> {
                System.out.println(SUCCESS);
                if (result.getDecodedText() != null && !result.getDecodedText().isEmpty()) {
                    System.out.println("Самый удачный сдвиг: " + result.getBestShift());
                    System.out.println("Результат расшифровки:");
                    System.out.println(result.getDecodedText());
                }
            }
            case ERROR -> {
                if (result.getApplicationException() != null) {
                    System.out.println(EXCEPTION + result.getApplicationException().getMessage());
                    result.getApplicationException().printStackTrace();
                } else {
                    System.out.println(EXCEPTION + "Неизвестная ошибка");
                }
            }
        }
    }
}