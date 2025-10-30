package com.javarush.cryptoanalyzer.privalikhina;


import com.javarush.cryptoanalyzer.privalikhina.app.Application;
import com.javarush.cryptoanalyzer.privalikhina.controller.MainController;
import com.javarush.cryptoanalyzer.privalikhina.entity.Result;
import com.javarush.cryptoanalyzer.privalikhina.view.ConsoleView;
import com.javarush.cryptoanalyzer.privalikhina.view.View;

import java.util.Scanner;

public class EntryPoint {
    public static void main(String[] args) {
        View view = new ConsoleView();
        MainController mainController = new MainController(view);
        Application application = new Application(mainController);
        Scanner scanner = new Scanner(System.in);

        boolean continueProgram = true;

        while (continueProgram) {
            // Запуск основного цикла программы
            Result result = application.run();

            if (result == null) {
                System.out.println("ОШИБКА: function.execute вернул null");
            } else {
                application.printResult(result);
            }

            // Предложение продолжить
            System.out.print("\nХотите продолжить? (да/нет): ");
            String answer = scanner.nextLine().trim().toLowerCase();

            if (!answer.equals("да") && !answer.equals("yes")) {
                continueProgram = false;
                System.out.println("Выход из программы. До свидания!");
            } else {
                System.out.println("\n----------------------------------------\n");
            }
        }

        scanner.close();
    }
}
