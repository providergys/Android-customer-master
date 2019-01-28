package com.teaera.teaeracafe.net.Response;

/**
 * Created by admin on 01/08/2017.
 */

public class BaseResponse {

    private static String success = "false";
    private static String failed = "true";
    private String error;
    private String message;

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public Boolean isError() {
        return this.error != success;
    }
}
