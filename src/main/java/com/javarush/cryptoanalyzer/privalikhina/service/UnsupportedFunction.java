package com.javarush.cryptoanalyzer.privalikhina.service;

import com.javarush.cryptoanalyzer.privalikhina.entity.Result;
import com.javarush.cryptoanalyzer.privalikhina.exception.ApplicationException;
import com.javarush.cryptoanalyzer.privalikhina.repository.ResultCode;

public class UnsupportedFunction implements Function {
    @Override
    public Result execute(String[] parameters) {
        //TODO UnsupportedFunction execute method
        return new Result(ResultCode.ERROR, new ApplicationException("Unsupported function."));
      }
}
