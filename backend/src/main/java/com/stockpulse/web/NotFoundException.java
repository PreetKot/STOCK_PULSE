package com.stockpulse.web;

public class NotFoundException extends BusinessException {
    public NotFoundException(String resourceType, String identifier) {
        super(ApiErrorCodes.NOT_FOUND, resourceType + " not found with identifier: " + identifier);
    }
}