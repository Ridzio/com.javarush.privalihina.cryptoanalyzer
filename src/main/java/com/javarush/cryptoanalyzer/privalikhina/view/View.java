package com.javarush.cryptoanalyzer.privalikhina.view;

import com.javarush.cryptoanalyzer.privalikhina.entity.Result;

public interface View {
   String[] getParameters();

   void printResult(Result result);
}
