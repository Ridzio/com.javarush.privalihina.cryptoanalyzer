package com.javarush.cryptoanalyzer.privalikhina;

import com.javarush.cryptoanalyzer.privalikhina.app.Application;
import com.javarush.cryptoanalyzer.privalikhina.controller.MainController;
import com.javarush.cryptoanalyzer.privalikhina.entity.Result;
import com.javarush.cryptoanalyzer.privalikhina.view.ConsoleView;
import com.javarush.cryptoanalyzer.privalikhina.view.View;

public class EntryPoint {
    public static void main(String[] args) {
        View view = new ConsoleView();
        MainController mainController = new MainController(view);
        Application application = new Application(mainController);


        Result result = application.run();
        if (result == null) {
            System.out.println("ОШИБКА: function.execute вернул null");
        } else {
            application.printResult(result);
        }
    }
}
