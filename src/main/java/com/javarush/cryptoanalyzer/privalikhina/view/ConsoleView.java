package com.javarush.cryptoanalyzer.privalikhina.view;

import com.javarush.cryptoanalyzer.privalikhina.entity.Result;

import java.util.Scanner;

import static com.javarush.cryptoanalyzer.privalikhina.constants.ApplicationCompletionConstants.EXCEPTION;
import static com.javarush.cryptoanalyzer.privalikhina.constants.ApplicationCompletionConstants.SUCCESS;
import static com.javarush.cryptoanalyzer.privalikhina.constants.FunctionCodeConstants.*;

public class ConsoleView implements View {

    @Override
    public String[] getParameters() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Выберите режим работы:");
        System.out.println("1 - Зашифровать");
        System.out.println("2 - Расшифровать");
        System.out.print("Введите номер режима: ");
        String modeInput = scanner.nextLine().trim();

        String mode;
        switch (modeInput) {
            case "1" -> mode = ENCODE;
            case "2" -> mode = DECODE;
            default -> {
                System.out.println("Неверный режим. Будет использована неподдерживаемая функция.");
                mode = UNSUPPORTED_FUNCTION;
            }
        }

        System.out.print("Введите путь к входному файлу: ");
        String inputFilePath = scanner.nextLine().trim();

        System.out.print("Введите путь к выходному файлу: ");
        String outputFilePath = scanner.nextLine().trim();

        System.out.print("Введите сдвиг (целое число): ");
        String shift = scanner.nextLine().trim();

        return new String[]{mode, inputFilePath, outputFilePath, shift};
    }

    @Override
    public void printResult(Result result) {
        switch (result.getResultCode()) {
            case OK -> System.out.println(SUCCESS);
            case ERROR -> {
                System.out.println(EXCEPTION + result.getApplicationException().getMessage());
                result.getApplicationException().printStackTrace(); // 👈 печатает подробности
            }
        }
    }
}