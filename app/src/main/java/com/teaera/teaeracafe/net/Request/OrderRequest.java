package com.teaera.teaeracafe.net.Request;


import com.teaera.teaeracafe.net.Model.CartInfo;

import java.util.ArrayList;

/**
 * Created by admin on 28/09/2017.
 */

public class OrderRequest {
    final String userId;
    final String locationId;
    final String subTotal;
    final String rewardsCredit;
    final String tax;
    final String taxAmount;
    final String totalPrice;
    final String rewards;
    final String redeem;
    final String waitingTime;
    final ArrayList<CartInfo> cartInfos;
    final String nonce;

    public OrderRequest(String userId, String locationId, String subTotal, String rewardsCredit, String tax, String taxAmount, String totalPrice, String rewards, String redeem, String waitingTime, ArrayList<CartInfo> cartInfos, String nonce) {
        this.userId = userId;
        this.locationId = locationId;
        this.subTotal = subTotal;
        this.rewardsCredit = rewardsCredit;
        this.tax = tax;
        this.taxAmount = taxAmount;
        this.totalPrice = totalPrice;
        this.rewards = rewards;
        this.redeem = redeem;
        this.waitingTime = waitingTime;
        this.cartInfos = cartInfos;
        this.nonce = nonce;
    }
}
