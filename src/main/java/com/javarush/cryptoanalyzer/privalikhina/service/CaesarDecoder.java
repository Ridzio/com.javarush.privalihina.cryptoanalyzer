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

            String alphabet = CryptoAlphabet.ALL_SYMBOLS;
            int alphabetLength = alphabet.length();

            try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                 FileWriter writer = new FileWriter(outputFile)) {

                int character;
                while ((character = reader.read()) != -1) {
                    char ch = (char) character;
                    int index = alphabet.indexOf(ch);
                    if (index != -1) {
                        int newIndex = (index - shift + alphabetLength) % alphabetLength;
                        writer.write(alphabet.charAt(newIndex));
                    } else {
                        writer.write(ch); // оставить символ, если его нет в алфавите
                    }
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
        String alphabet = CryptoAlphabet.ALL_SYMBOLS;
        int len = alphabet.length();

        for (char ch : input.toCharArray()) {
            int index = alphabet.indexOf(ch);
            if (index != -1) {
                int newIndex = (index - shift + len) % len;
                result.append(alphabet.charAt(newIndex));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }
}

