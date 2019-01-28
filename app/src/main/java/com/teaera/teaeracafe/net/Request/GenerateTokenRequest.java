package com.teaera.teaeracafe.net.Request;

/**
 * Created by admin on 25/12/2017.
 */

public class GenerateTokenRequest {
    final String customerId;

    public GenerateTokenRequest(String customerId) {
        this.customerId = customerId;
    }
}
