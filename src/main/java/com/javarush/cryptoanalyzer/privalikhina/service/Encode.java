package com.javarush.cryptoanalyzer.privalikhina.service;

import com.javarush.cryptoanalyzer.privalikhina.entity.Result;
import com.javarush.cryptoanalyzer.privalikhina.exception.ApplicationException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

import static com.javarush.cryptoanalyzer.privalikhina.constants.CryptoAlphabet.*;
import static com.javarush.cryptoanalyzer.privalikhina.repository.ResultCode.ERROR;
import static com.javarush.cryptoanalyzer.privalikhina.repository.ResultCode.OK;

public class Encode implements Function {
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
                    writer.write(shiftChar(ch, shift));
                }
            }

            return new Result(OK);

        } catch (Exception e) {
            return new Result(ERROR, new ApplicationException("Encode operation finished with exception", e));
        }
    }

    private char shiftChar(char ch, int shift) {
        if (RUS_LOWER.indexOf(ch) != -1) {
            return RUS_LOWER.charAt((RUS_LOWER.indexOf(ch) + shift) % RUS_LOWER.length());
        } else if (RUS_UPPER.indexOf(ch) != -1) {
            return RUS_UPPER.charAt((RUS_UPPER.indexOf(ch) + shift) % RUS_UPPER.length());
        } else if (ENG_LOWER.indexOf(ch) != -1) {
            return ENG_LOWER.charAt((ENG_LOWER.indexOf(ch) + shift) % ENG_LOWER.length());
        } else if (ENG_UPPER.indexOf(ch) != -1) {
            return ENG_UPPER.charAt((ENG_UPPER.indexOf(ch) + shift) % ENG_UPPER.length());
        } else if (DIGITS.indexOf(ch) != -1) {
            return DIGITS.charAt((DIGITS.indexOf(ch) + shift) % DIGITS.length());
        } else {
            return ch;
        }
    }
}
