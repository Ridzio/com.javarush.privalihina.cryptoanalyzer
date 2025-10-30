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
    private static final String BRUTEFORCE_FOLDER_BASE = "bruteforce_outputs";

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
        System.out.print("Введите путь к выходному файлу или папке (по умолчанию создается файл или папка рядом с входным файлом): ");
        String outputPathInput = scanner.nextLine().trim();

        // Формируем путь к выходному файлу или папке
        String outputFilePath = resolveOutputFilePath(outputPathInput, mode, inputFilePath);

        if (outputFilePath == null) {
            System.out.println("Не удалось определить корректный путь для выходного файла/папки.");
            return new String[]{UNSUPPORTED_FUNCTION};
        }

        Path outputPath = Paths.get(outputFilePath);

        // Для BRUTEFORCE: не создаём папку здесь — BruteForceDecoder создаст её и заполнит
        if (mode.equals(BRUTEFORCE)) {
            return new String[]{mode, inputFilePath, outputPath.toString()};
        }

        // Для ENCODE/DECODE — создаём файл, если его нет
        try {
            if (!Files.exists(outputPath)) {
                // Убедимся, что родительская папка существует
                if (outputPath.getParent() != null) {
                    createFolderIfNotExists(outputPath.getParent().toString());
                }
                Files.createFile(outputPath);
                System.out.println("Файл создан по пути: " + outputPath.toAbsolutePath());
            }
        } catch (IOException e) {
            System.out.println("Не удалось создать файл: " + outputPath.toAbsolutePath());
            e.printStackTrace();
            return new String[]{UNSUPPORTED_FUNCTION};
        }

        // Для encode/decode нужно запросить сдвиг
        System.out.print("Введите сдвиг (целое число): ");
        String shift = scanner.nextLine().trim();
        return new String[]{mode, inputFilePath, outputPath.toString(), shift};
    }

    /**
     * Формирует путь к выходу.
     * Для BRUTEFORCE возвращает уникальный путь к директории (не создаёт её).
     * Для ENCODE/DECODE — возвращает путь к файлу (с уникализацией при совпадении).
     */
    private String resolveOutputFilePath(String pathInput, String mode, String inputFilePath) {
        Path inputFile = Paths.get(inputFilePath);
        Path parent = inputFile.getParent() != null ? inputFile.getParent() : Paths.get(".");

        // --- BRUTEFORCE special handling (return unique dir path, do not create) ---
        if (mode.equals(BRUTEFORCE)) {
            if (pathInput.isEmpty()) {
                Path unique = createUniqueDir(parent, BRUTEFORCE_FOLDER_BASE);
                return unique.toString();
            }

            Path provided = Paths.get(pathInput);
            if (Files.exists(provided)) {
                if (Files.isDirectory(provided)) {
                    // вернём уникальную подпапку внутри указанной директории (не создаём)
                    return createUniqueDir(provided, BRUTEFORCE_FOLDER_BASE).toString();
                } else if (Files.isRegularFile(provided)) {
                    // если указали файл — используем его родительскую папку для создания уникальной папки
                    Path pParent = provided.getParent() != null ? provided.getParent() : Paths.get(".");
                    return createUniqueDir(pParent, BRUTEFORCE_FOLDER_BASE).toString();
                } else {
                    System.out.println("Указанный путь для брутфорса не является директорией/файлом.");
                    return null;
                }
            } else {
                // если путь не существует — считаем, что это папка (вернём уникальный путь)
                return createUniqueDir(provided, BRUTEFORCE_FOLDER_BASE).toString();
            }
        }

        // --- ENCODE / DECODE handling ---
        if (pathInput.isEmpty()) {
            String defaultFileName = defaultFileNameForMode(mode); // input.txt / output.txt
            Path outputPath = parent.resolve(defaultFileName);

            // Если имя совпадает с входным или файл уже существует — уникализируем
            int counter = 1;
            while (Files.exists(outputPath) || outputPath.toAbsolutePath().normalize().equals(inputFile.toAbsolutePath().normalize())) {
                String newFileName = defaultFileName.replace(".txt", "_" + counter + ".txt");
                outputPath = parent.resolve(newFileName);
                counter++;
            }
            return outputPath.toString();
        }

        Path path = Paths.get(pathInput);

        if (Files.exists(path)) {
            if (Files.isDirectory(path)) {
                createFolderIfNotExists(path.toString());
                Path candidate = path.resolve(defaultFileNameForMode(mode));
                // уникализируем, если совпадает с input
                int counter = 1;
                while (Files.exists(candidate) || candidate.toAbsolutePath().normalize().equals(inputFile.toAbsolutePath().normalize())) {
                    String newFileName = defaultFileNameForMode(mode).replace(".txt", "_" + counter + ".txt");
                    candidate = path.resolve(newFileName);
                    counter++;
                }
                return candidate.toString();
            } else if (Files.isRegularFile(path)) {
                if (isValidFileName(path.getFileName().toString())) {
                    createFolderIfNotExists(path.getParent().toString());
                    // если указанный файл совпадает с входным — уникализируем имя рядом с входным
                    if (path.toAbsolutePath().normalize().equals(inputFile.toAbsolutePath().normalize())) {
                        String defaultFileName = defaultFileNameForMode(mode);
                        Path alt = parent.resolve(defaultFileName);
                        int counter = 1;
                        while (Files.exists(alt)) {
                            String newFileName = defaultFileName.replace(".txt", "_" + counter + ".txt");
                            alt = parent.resolve(newFileName);
                            counter++;
                        }
                        return alt.toString();
                    }
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
            // Если путь не существует
            if (pathInput.endsWith(".txt")) {
                Path parentOfPath = path.getParent();
                if (parentOfPath != null) createFolderIfNotExists(parentOfPath.toString());
                if (isValidFileName(path.getFileName().toString())) return path.toString();
                System.out.println("Имя файла содержит недопустимые символы.");
                return null;
            } else {
                // считаем, что это папка: создаём папку (тут создаём, т.к. пользователь явно указал её)
                createFolderIfNotExists(path.toString());
                Path candidate = path.resolve(defaultFileNameForMode(mode));
                int counter = 1;
                while (Files.exists(candidate) || candidate.toAbsolutePath().normalize().equals(inputFile.toAbsolutePath().normalize())) {
                    String newFileName = defaultFileNameForMode(mode).replace(".txt", "_" + counter + ".txt");
                    candidate = path.resolve(newFileName);
                    counter++;
                }
                return candidate.toString();
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

    /**
     * Возвращает уникальный путь для директории baseName (baseName, baseName_1, baseName_2 ...)
     * НЕ создаёт директорию — только вычисляет первый свободный путь.
     */
    private Path createUniqueDir(Path parentOrAbsolute, String baseName) {
        Path parentToUse = parentOrAbsolute;

        // Если указан путь к файлу (contains dot) — используем его parent
        String fname = parentOrAbsolute.getFileName().toString();
        if (fname.contains(".") && !fname.endsWith(".")) {
            if (parentOrAbsolute.getParent() != null) {
                parentToUse = parentOrAbsolute.getParent();
            } else {
                parentToUse = Paths.get(".");
            }
        }

        if (parentToUse == null) parentToUse = Paths.get(".");

        Path base = parentToUse.resolve(baseName);
        if (!Files.exists(base)) {
            return base;
        }

        int counter = 1;
        while (true) {
            Path candidate = parentToUse.resolve(baseName + "_" + counter);
            if (!Files.exists(candidate)) {
                return candidate;
            }
            counter++;
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