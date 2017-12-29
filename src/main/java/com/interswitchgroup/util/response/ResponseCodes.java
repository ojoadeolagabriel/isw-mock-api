package com.interswitchgroup.util.response;

public enum ResponseCodes {
    SUCCESS("90000", "Success"),
    NOT_PERMITTED("10405", "Not permitted to perform action"),
    NOT_FOUND("10404", "Resource not found"),
    NOT_PROCESSABLE("10422", "Request not processable");

    public String code, message;
    ResponseCodes(String code, String message){
        this.code = code; this.message = message;
    }
}
