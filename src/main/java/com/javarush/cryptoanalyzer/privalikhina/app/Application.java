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

//    public Result run() {
//        String[] parameters = mainController.getView().getParameters();
//        String mode = parameters[0];
//
//        Function function = getFunction(mode);
//
//        String[] functionParameters;
//
//        if (BRUTEFORCE.equals(mode)) {
//            // Ожидается: parameters = [mode, inputFilePath, saveFolder]
//            if (parameters.length < 3) {
//                System.out.println("Ошибка: недостаточно параметров для режима брутфорс");
//                return null;
//            }
//
//            String inputFilePath = parameters[1];
//            String saveFolder = parameters[2];
//
//            // Формируем имя выходного файла для расшифровки: например, originalName_decoded.txt
//            String inputFileName = Path.of(inputFilePath).getFileName().toString();
//            String outputFileName = inputFileName + "_decoded.txt";
//            String outputFilePath = saveFolder;
//
//            functionParameters = new String[]{inputFilePath, outputFilePath};
//
//        } else if (ENCODE.equals(mode) || DECODE.equals(mode)) {
//            // Для encode/decode: [mode, inputFilePath, outputFilePath, shift]
//            if (parameters.length < 4) {
//                System.out.println("Ошибка: недостаточно параметров для режима шифрования/дешифрования");
//                return null;
//            }
//            functionParameters = new String[]{parameters[1], parameters[2], parameters[3]};
//        } else {
//            // Для неподдерживаемых режимов - просто передаем все параметры без первого
//            functionParameters = new String[parameters.length - 1];
//            System.arraycopy(parameters, 1, functionParameters, 0, functionParameters.length);
//        }
//
//        Result result = function.execute(functionParameters);
//
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

        String[] functionParameters;

        if (BRUTEFORCE.equals(mode)) {
            // Ожидается: parameters = [mode, inputFilePath, outputFilePath (полный путь)]
            if (parameters.length < 3) {
                System.out.println("Ошибка: недостаточно параметров для режима брутфорс");
                return null;
            }

            String inputFilePath = parameters[1];
            String outputFilePath = parameters[2];

            functionParameters = new String[]{inputFilePath, outputFilePath};

        } else if (ENCODE.equals(mode) || DECODE.equals(mode)) {
            // Для encode/decode: [mode, inputFilePath, outputFilePath, shift]
            if (parameters.length < 4) {
                System.out.println("Ошибка: недостаточно параметров для режима шифрования/дешифрования");
                return null;
            }
            functionParameters = new String[]{parameters[1], parameters[2], parameters[3]};
        } else {
            // Для неподдерживаемых режимов - просто передаем все параметры без первого
            functionParameters = new String[parameters.length - 1];
            System.arraycopy(parameters, 1, functionParameters, 0, functionParameters.length);
        }

        Result result = function.execute(functionParameters);

        if (result == null) {
            System.out.println("DEBUG: function.execute(parameters) вернул null!");
        }

        return result;
    }

    private Function getFunction(String mode) {
        return switch (mode) {
            case ENCODE -> FunctionCode.valueOf(ENCODE).getFunction();
            case DECODE -> FunctionCode.valueOf(DECODE).getFunction();
            case BRUTEFORCE -> FunctionCode.valueOf(BRUTEFORCE).getFunction();
            default -> FunctionCode.valueOf(UNSUPPORTED_FUNCTION).getFunction();
        };
    }

    public void printResult(Result result) {
        mainController.getView().printResult(result);
    }
}