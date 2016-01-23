package com.imslbd.grossary;

/**
 * Created by shahadat on 1/23/16.
 */
public enum StatusCodes {
    OK(1, "OK"),
    USER_NOT_FOUND_ERROR(2000, "User not found."),
    PASSWORD_DOES_NOT_MATCH_ERROR(2001, "Password does not match.");

    private final int code;
    private final String message;

    StatusCodes(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int statusCode() {
        return code;
    }

    public String message() {
        return message;
    }
}
