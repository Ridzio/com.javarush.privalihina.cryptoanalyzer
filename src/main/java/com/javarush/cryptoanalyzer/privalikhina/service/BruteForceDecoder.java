
package com.javarush.cryptoanalyzer.privalikhina.service;

import com.javarush.cryptoanalyzer.privalikhina.constants.CryptoAlphabet;
import com.javarush.cryptoanalyzer.privalikhina.entity.Result;
import com.javarush.cryptoanalyzer.privalikhina.exception.ApplicationException;
import com.javarush.cryptoanalyzer.privalikhina.repository.ResultCode;

import java.nio.file.Files;
import java.nio.file.Path;

public class BruteForceDecoder implements Function {

    public Result execute(String[] parameters) {
        String inputFile = parameters[0];
        String outputFile = parameters[1];

        try {
            String content = Files.readString(Path.of(inputFile));

            int bestShift = 0;
            String bestDecoded = "";
            int bestScore = -1;

            for (int shift = 1; shift < CryptoAlphabet.ALL_SYMBOLS.length(); shift++) {
                String decoded = CaesarDecoder.decode(content, shift);
                int score = rateText(decoded);

                if (score > bestScore) {
                    bestScore = score;
                    bestDecoded = decoded;
                    bestShift = shift;
                }
            }

            // Записываем в файл, предполагая, что путь корректен
            Files.writeString(Path.of(outputFile), bestDecoded);

            System.out.println("Brute force completed successfully. Best shift = " + bestShift);
            return new Result(ResultCode.OK, bestDecoded, bestShift);

        } catch (Exception e) {
            e.printStackTrace();
            return new Result(ResultCode.ERROR, new ApplicationException("BruteForce operation finished with exception", e));
        }
    }

    private int rateText(String text) {
        int score = 0;
        for (String word : CryptoAlphabet.COMMON_WORDS) {
            score += text.split(word, -1).length - 1;
        }
        return score;
    }
}