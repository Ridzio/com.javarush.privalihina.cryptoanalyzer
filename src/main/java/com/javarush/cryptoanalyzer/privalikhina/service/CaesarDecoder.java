package com.javarush.cryptoanalyzer.privalikhina.service;

import com.javarush.cryptoanalyzer.privalikhina.constants.CryptoAlphabet;
import com.javarush.cryptoanalyzer.privalikhina.entity.Result;
import com.javarush.cryptoanalyzer.privalikhina.exception.ApplicationException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

import static com.javarush.cryptoanalyzer.privalikhina.repository.ResultCode.ERROR;
import static com.javarush.cryptoanalyzer.privalikhina.repository.ResultCode.OK;

public class CaesarDecoder implements Function {
    @Override
    public Result execute(String[] parameters) {
        try {
            String inputFile = parameters[0];
            String outputFile = parameters[1];
            int shift = Integer.parseInt(parameters[2]);

            try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                 FileWriter writer = new FileWriter(outputFile)) {

                int character;
                while ((character = reader.read()) != -1) {
                    char ch = (char) character;
                    writer.write(unshiftChar(ch, shift));
                }
            }

            return new Result(OK);

        } catch (Exception e) {
            return new Result(ERROR, new ApplicationException("Decode operation failed", e));
        }
    }

    // Используется в BruteForceDecoder
    public static String decode(String input, int shift) {
        StringBuilder result = new StringBuilder();
        for (char ch : input.toCharArray()) {
            result.append(unshiftChar(ch, shift));
        }
        return result.toString();
    }

    /**
     * Сдвигает символ только в пределах его алфавитного набора:
     * - русские строчные (RUS_LOWER)
     * - русские заглавные (RUS_UPPER)
     * - цифры (DIGITS)
     * Иначе возвращает символ без изменений (пробелы, пунктуация и пр.).
     */
    private static char unshiftChar(char ch, int shift) {
        String rusLower = CryptoAlphabet.RUS_LOWER;
        String rusUpper = CryptoAlphabet.RUS_UPPER;
        String digits = CryptoAlphabet.DIGITS;

        if (rusLower.indexOf(ch) >= 0) {
            int idx = rusLower.indexOf(ch);
            int newIdx = Math.floorMod(idx - shift, rusLower.length());
            return rusLower.charAt(newIdx);
        } else if (rusUpper.indexOf(ch) >= 0) {
            int idx = rusUpper.indexOf(ch);
            int newIdx = Math.floorMod(idx - shift, rusUpper.length());
            return rusUpper.charAt(newIdx);
        } else if (digits.indexOf(ch) >= 0) {
            int idx = digits.indexOf(ch);
            int newIdx = Math.floorMod(idx - shift, digits.length());
            return digits.charAt(newIdx);
        } else {
            // пробелы, знаки пунктуации и любые другие символы не трогаем
            return ch;
        }
    }
}

