package com.javarush.cryptoanalyzer.privalikhina.constants;

public class CryptoAlphabet {
    public static final String RUS_LOWER = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя";
    public static final String RUS_UPPER = RUS_LOWER.toUpperCase();
    public static final String ENG_LOWER = "abcdefghijklmnopqrstuvwxyz";
    public static final String ENG_UPPER = ENG_LOWER.toUpperCase();
    public static final String DIGITS = "0123456789";
    public static final String PUNCTUATION = ".,!?;:()\"'«»—-[]{}<>@#$%^&*/\\|=+_`~";
    public static final String WHITESPACE = " \t\n\r";

    public static final String ALL_SYMBOLS =
            RUS_LOWER + RUS_UPPER +
                    ENG_LOWER + ENG_UPPER +
                    DIGITS + PUNCTUATION + WHITESPACE;

    public static final String[] COMMON_WORDS = {
            "было", "тогда", "очень", "больше", "всегда", "жизни", "другой",
            "потому", "можно", "после", "человек", "время", "день", "года",
            "должен", "может", "сейчас", "ничего", "здесь", "был", "еще"
    };
}
