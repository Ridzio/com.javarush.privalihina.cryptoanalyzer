package com.javarush.cryptoanalyzer.privalikhina.controller;

import com.javarush.cryptoanalyzer.privalikhina.view.View;

public class MainController {
    private View view;

    public MainController(View view) {
        this.view = view;
    }

    public View getView() {
        return view;
    }
}
