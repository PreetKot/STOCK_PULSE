package com.stockpulse.web;

public class ConflictException extends BusinessException {
    public ConflictException(String errorCode, String message) {
        super(errorCode, message);
    }
}