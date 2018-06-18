package com.teaera.teaeracafe.net.Request;

/**
 * Created by admin on 01/02/2018.
 */

public class LoadBalanceRequest {
    final String userId;
    final String amount;
    final String nonce;

    public LoadBalanceRequest(String userId, String amount, String nonce) {
        this.userId = userId;
        this.amount = amount;
        this.nonce = nonce;

    }
}
