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

        // Режим работы
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
                System.out.println("Неверный режим.");
                return new String[]{UNSUPPORTED_FUNCTION};
            }
        }

        // Проверка пути входного файла
        String inputFilePath;
        while (true) {
            System.out.print("Введите путь к входному файлу: ");
            inputFilePath = scanner.nextLine().trim();
            if (Files.exists(Path.of(inputFilePath)) && Files.isRegularFile(Path.of(inputFilePath))) {
                break;
            } else {
                System.out.println("Файл не найден. Убедитесь, что путь указан корректно.");
            }
        }

        // Ввод пути к выходу
        System.out.print("Введите путь к выходному файлу или папке (Enter — по умолчанию рядом с входным файлом): ");
        String outputPathInput = scanner.nextLine().trim();

        String outputFilePath = resolveOutputFilePath(outputPathInput, mode, inputFilePath);
        if (outputFilePath == null) {
            System.out.println("Не удалось определить корректный путь для выходного файла/папки.");
            return new String[]{UNSUPPORTED_FUNCTION};
        }

        Path outputPath = Paths.get(outputFilePath);

        // Для брутфорса — не создаём ничего заранее
        if (mode.equals(BRUTEFORCE)) {
            return new String[]{mode, inputFilePath, outputPath.toString()};
        }

        // Для encode/decode — создаём файл, если не существует
        try {
            if (!Files.exists(outputPath)) {
                if (outputPath.getParent() != null) {
                    createFolderIfNotExists(outputPath.getParent().toString());
                }
                Files.createFile(outputPath);
                System.out.println("Создан файл: " + outputPath.toAbsolutePath());
            }
        } catch (IOException e) {
            System.out.println("Ошибка при создании файла: " + outputPath.toAbsolutePath());
            e.printStackTrace();
            return new String[]{UNSUPPORTED_FUNCTION};
        }

        // Ввод сдвига
        System.out.print("Введите сдвиг (целое число): ");
        String shift = scanner.nextLine().trim();

        return new String[]{mode, inputFilePath, outputPath.toString(), shift};
    }

    /**
     * Определяет итоговый путь для вывода.
     * Для BRUTEFORCE возвращает путь к уникальной директории.
     * Для ENCODE/DECODE — путь к файлу.
     */
    private String resolveOutputFilePath(String pathInput, String mode, String inputFilePath) {
        Path inputFile = Paths.get(inputFilePath);
        Path parentDir = inputFile.getParent() != null ? inputFile.getParent() : Paths.get(".");

        // BRUTEFORCE: возвращаем уникальный путь к папке
        if (mode.equals(BRUTEFORCE)) {
            if (pathInput.isEmpty()) {
                return createUniqueDir(parentDir, BRUTEFORCE_FOLDER_BASE).toString();
            }

            Path provided = Paths.get(pathInput);
            if (Files.exists(provided)) {
                if (Files.isDirectory(provided)) {
                    return createUniqueDir(provided, BRUTEFORCE_FOLDER_BASE).toString();
                } else {
                    Path parent = provided.getParent() != null ? provided.getParent() : parentDir;
                    return createUniqueDir(parent, BRUTEFORCE_FOLDER_BASE).toString();
                }
            } else {
                // Создаём папку рядом с входным, если путь не существует
                Path targetFolder = parentDir.resolve(pathInput);
                return createUniqueDir(targetFolder, BRUTEFORCE_FOLDER_BASE).toString();
            }
        }

        // ENCODE / DECODE
        if (pathInput.isEmpty()) {
            return makeUniqueFilePath(parentDir.resolve(defaultFileNameForMode(mode)), inputFile);
        }

        Path path = Paths.get(pathInput);

        if (Files.exists(path)) {
            if (Files.isDirectory(path)) {
                Path fileCandidate = path.resolve(defaultFileNameForMode(mode));
                return makeUniqueFilePath(fileCandidate, inputFile);
            } else if (Files.isRegularFile(path)) {
                if (isValidFileName(path.getFileName().toString())) {
                    return makeUniqueFilePath(path, inputFile);
                } else {
                    System.out.println("Имя файла содержит недопустимые символы.");
                    return null;
                }
            } else {
                System.out.println("Путь не является ни файлом, ни директорией.");
                return null;
            }
        } else {
            if (pathInput.endsWith(".txt")) {
                Path parentOfPath = path.getParent();
                if (parentOfPath != null) createFolderIfNotExists(parentOfPath.toString());
                if (isValidFileName(path.getFileName().toString())) return path.toString();
                System.out.println("Имя файла содержит недопустимые символы.");
                return null;
            } else {
                // Пользователь ввёл что-то вроде "5" — создаём рядом с входным
                Path targetFolder = parentDir.resolve(pathInput);
                createFolderIfNotExists(targetFolder.toString());
                Path candidate = targetFolder.resolve(defaultFileNameForMode(mode));
                return makeUniqueFilePath(candidate, inputFile);
            }
        }
    }

    /** Проверка имени файла */
    private boolean isValidFileName(String fileName) {
        return !fileName.matches(".*" + INVALID_CHARS + ".*");
    }

    /** Базовые имена файлов */
    private String defaultFileNameForMode(String mode) {
        return switch (mode) {
            case ENCODE -> "input.txt";
            case DECODE -> "output.txt";
            default -> "result.txt";
        };
    }

    /** Создаёт директорию при необходимости */
    private void createFolderIfNotExists(String folderPath) {
        Path folder = Paths.get(folderPath);
        if (!Files.exists(folder)) {
            try {
                Files.createDirectories(folder);
                System.out.println("Создана папка: " + folder.toAbsolutePath());
            } catch (IOException e) {
                System.out.println("Ошибка при создании папки: " + folder.toAbsolutePath());
                e.printStackTrace();
            }
        }
    }

    /** Создаёт уникальное имя файла (если совпадает с входным или уже существует) */
    private String makeUniqueFilePath(Path base, Path inputFile) {
        Path candidate = base;
        int counter = 1;
        while (Files.exists(candidate) ||
                candidate.toAbsolutePath().normalize().equals(inputFile.toAbsolutePath().normalize())) {
            String newName = base.getFileName().toString().replace(".txt", "_" + counter + ".txt");
            candidate = base.getParent().resolve(newName);
            counter++;
        }
        return candidate.toString();
    }

    /** Возвращает путь к уникальной директории (baseName, baseName_1, ...) */
    private Path createUniqueDir(Path parentOrAbsolute, String baseName) {
        Path parent = Files.isDirectory(parentOrAbsolute) ? parentOrAbsolute : parentOrAbsolute.getParent();
        if (parent == null) parent = Paths.get(".");
        Path base = parent.resolve(baseName);

        if (!Files.exists(base)) return base;

        int counter = 1;
        while (true) {
            Path candidate = parent.resolve(baseName + "_" + counter);
            if (!Files.exists(candidate)) return candidate;
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
                System.out.print(EXCEPTION);
                if (result.getApplicationException() != null) {
                    System.out.println(result.getApplicationException().getMessage());
                    result.getApplicationException().printStackTrace();
                } else {
                    System.out.println("Неизвестная ошибка.");
                }
            }
        }
    }
}