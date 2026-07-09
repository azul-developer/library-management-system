package com.liz.library.domain.message;

public final class MessageCodes {

    private MessageCodes() {
    }

    public static final String INVALID_TOTAL_COPIES = "INVALID_TOTAL_COPIES";
    public static final String DUPLICATED = "DUPLICATED";
    public static final String INCONSISTENCY = "INCONSISTENCY";
    public static final String BOOK_ALREADY_EXISTS = "BOOK_ALREADY_EXISTS";
    public static final String BOOK_NOT_AVAILABLE = "BOOK_NOT_AVAILABLE";
    public static final String BOOK_NOT_FOUND = "BOOK_NOT_FOUND";
    public static final String INVALID_EMAIL = "INVALID_EMAIL";
    public static final String EMAIL_REQUIRED = "EMAIL_REQUIRED";
    public static final String EMAIL_ALREADY_EXISTS = "EMAIL_ALREADY_EXISTS";
    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String ROLE_NOT_FOUND = "ROLE_NOT_FOUND";
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String LOAN_SERVICE_ERROR = "LOAN_SERVICE_ERROR";
    public static final String LOAN_SERVICE_UNAVAILABLE = "LOAN_SERVICE_UNAVAILABLE";
}
