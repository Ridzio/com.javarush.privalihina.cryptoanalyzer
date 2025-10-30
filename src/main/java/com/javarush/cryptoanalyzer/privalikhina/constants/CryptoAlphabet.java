package com.javarush.cryptoanalyzer.privalikhina.constants;

import java.util.Map;

public class CryptoAlphabet {
    public static final String RUS_LOWER = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя";
    public static final String RUS_UPPER = RUS_LOWER.toUpperCase();
    public static final String DIGITS = "0123456789";
    public static final String PUNCTUATION = ".,”’:-!?";
    public static final String WHITESPACE = " ";

    public static final String ALL_SYMBOLS =
            RUS_LOWER + RUS_UPPER + DIGITS + PUNCTUATION + WHITESPACE;

    // Частоты букв в русском языке (приблизительно)
    public static final Map<Character, Double> LETTER_FREQ = Map.ofEntries(
            Map.entry('о', 0.1097), Map.entry('е', 0.0845), Map.entry('а', 0.0801),
            Map.entry('и', 0.0735), Map.entry('н', 0.0670), Map.entry('т', 0.0626),
            Map.entry('с', 0.0547), Map.entry('р', 0.0473), Map.entry('в', 0.0454),
            Map.entry('л', 0.0440), Map.entry('к', 0.0349), Map.entry('м', 0.0321),
            Map.entry('д', 0.0298), Map.entry('п', 0.0281), Map.entry('у', 0.0262),
            Map.entry('я', 0.0201), Map.entry('ы', 0.0190), Map.entry('ь', 0.0174),
            Map.entry('г', 0.0169), Map.entry('з', 0.0165), Map.entry('б', 0.0159),
            Map.entry('ч', 0.0144), Map.entry('й', 0.0121), Map.entry('х', 0.0097),
            Map.entry('ж', 0.0094), Map.entry('ш', 0.0073), Map.entry('ю', 0.0064),
            Map.entry('ц', 0.0048), Map.entry('щ', 0.0036), Map.entry('э', 0.0032),
            Map.entry('ф', 0.0026), Map.entry('ъ', 0.0004), Map.entry('ё', 0.0004)
    );
}
