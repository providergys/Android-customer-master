package com.teaera.teaeracafe.net.Request;

/**
 * Created by admin on 01/02/2018.
 */

public class UpdateTokenRequest {
    final String userId;
    final String token;

    public UpdateTokenRequest(String userId, String token) {
        this.userId = userId;
        this.token = token;
    }
}
