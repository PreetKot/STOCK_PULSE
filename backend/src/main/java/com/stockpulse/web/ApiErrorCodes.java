package com.stockpulse.web;

public class ApiErrorCodes {
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String INVALID_REQUEST = "INVALID_REQUEST";
    public static final String INVALID_DECISION = "INVALID_DECISION";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String DUPLICATE_SKU = "DUPLICATE_SKU";
    public static final String PENDING_SUGGESTION_EXISTS = "PENDING_SUGGESTION_EXISTS";
    public static final String SUGGESTION_ALREADY_FINALIZED = "SUGGESTION_ALREADY_FINALIZED";
    public static final String STOCK_CONFLICT = "STOCK_CONFLICT";
    public static final String PRODUCT_DELETE_CONFLICT = "PRODUCT_DELETE_CONFLICT";

    private ApiErrorCodes() {}
}