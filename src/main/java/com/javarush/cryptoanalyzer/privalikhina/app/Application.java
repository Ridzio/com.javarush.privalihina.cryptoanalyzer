package com.javarush.cryptoanalyzer.privalikhina.app;

import com.javarush.cryptoanalyzer.privalikhina.controller.MainController;
import com.javarush.cryptoanalyzer.privalikhina.entity.Result;
import com.javarush.cryptoanalyzer.privalikhina.repository.FunctionCode;
import com.javarush.cryptoanalyzer.privalikhina.service.Function;

import static com.javarush.cryptoanalyzer.privalikhina.constants.FunctionCodeConstants.*;

public class Application {
    private final MainController mainController;

    public Application(MainController mainController) {
        this.mainController = mainController;
    }

    //public Result run() {
//        /**
//         * Возвращает параметры запуска программы.
//         * Индексы массива:
//         * 0 - режим (например: "encrypt", "decrypt")
//         * 1 - путь к входному файлу
//         * 2 - путь к выходному файлу
//         *
//         * @return массив строк с параметрами
//         */
//        String[] parameters = mainController.getView().getParameters();
//      String mode = parameters[0]; //java класс, который будет выполнять программу
//      Function function = getFunction(mode);
//        // 🔽 ВСТАВЬ ЭТОТ БЛОК
//        Result result = function.execute(parameters);
//        if (result == null) {
//            System.out.println("DEBUG: function.execute(parameters) вернул null!");
//        }
//
//        return result;
//    }

    public Result run() {
        String[] parameters = mainController.getView().getParameters();
        String mode = parameters[0];

        Function function = getFunction(mode);

        // Переставим параметры для передачи в Encode/Decode
        // Ожидается: [inputFile, outputFile, shift]
        String[] functionParameters = new String[]{parameters[1], parameters[2], parameters[3]};

        Result result = function.execute(functionParameters);

        if (result == null) {
            System.out.println("DEBUG: function.execute(parameters) вернул null!");
        }

        return result;
    }

    private Function getFunction(String mode) {
        return switch (mode){
            case ENCODE -> FunctionCode.valueOf(ENCODE).getFunction();
            case DECODE -> FunctionCode.valueOf(DECODE).getFunction();
            default -> FunctionCode.valueOf(UNSUPPORTED_FUNCTION).getFunction();
        };
    }

    public void printResult(Result result) {
        mainController.getView().printResult(result);

    }
}
