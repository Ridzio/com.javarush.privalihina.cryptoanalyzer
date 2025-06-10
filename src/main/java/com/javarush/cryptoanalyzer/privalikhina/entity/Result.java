package com.javarush.cryptoanalyzer.privalikhina.entity;

import com.javarush.cryptoanalyzer.privalikhina.exception.ApplicationException;
import com.javarush.cryptoanalyzer.privalikhina.repository.ResultCode;

public class Result {
    private final ResultCode resultCode;
    private final ApplicationException applicationException;

    // Добавляемые поля
    private String decodedText;
    private int bestShift;

    // Основной конструктор
    public Result(ResultCode resultCode) {
        this.resultCode = resultCode;
        this.applicationException = null;
    }

    public Result(ResultCode resultCode, ApplicationException applicationException) {
        this.resultCode = resultCode;
        this.applicationException = applicationException;
    }

    // Новый конструктор с результатами брутфорса
    public Result(ResultCode resultCode, String decodedText, int bestShift) {
        this.resultCode = resultCode;
        this.applicationException = null;
        this.decodedText = decodedText;
        this.bestShift = bestShift;
    }

    // Геттеры
    public ResultCode getResultCode() {
        return resultCode;
    }

    public ApplicationException getApplicationException() {
        return applicationException;
    }

    public String getDecodedText() {
        return decodedText;
    }

    public int getBestShift() {
        return bestShift;
    }

    // Сеттеры (опционально)
    public void setDecodedText(String decodedText) {
        this.decodedText = decodedText;
    }

    public void setBestShift(int bestShift) {
        this.bestShift = bestShift;
    }
}
