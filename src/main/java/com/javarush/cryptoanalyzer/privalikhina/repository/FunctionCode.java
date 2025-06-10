package com.javarush.cryptoanalyzer.privalikhina.repository;

import com.javarush.cryptoanalyzer.privalikhina.service.Decode;
import com.javarush.cryptoanalyzer.privalikhina.service.Encode;
import com.javarush.cryptoanalyzer.privalikhina.service.Function;
import com.javarush.cryptoanalyzer.privalikhina.service.UnsupportedFunction;
import com.javarush.cryptoanalyzer.privalikhina.service.BruteForceDecoder;

public enum FunctionCode {
    ENCODE(new Encode()),
    DECODE(new Decode()),
    BRUTEFORCE(new BruteForceDecoder()),
    UNSUPPORTED_FUNCTION(new UnsupportedFunction());

    private final Function function;

    FunctionCode(Function function) {
        this.function = function;
    }

    public Function getFunction() {
        return function;
    }
}