package com.teaera.teaeracafe.net.Request;

import com.teaera.teaeracafe.net.Model.OrderDetailsInfo;

import java.util.ArrayList;

/**
 * Created by admin on 26/10/2017.
 */

public class RefundOrdersRequest {

    final String userId;
    final String orderId;
    final String subTotal;
    final String rewardsCredit;
    final String total;
    final String tax;
    final String taxAmount;
    final String rewards;
    final String redeem;
    final ArrayList<OrderDetailsInfo> refundItems;

    public RefundOrdersRequest(String userId, String orderId, String subTotal, String rewardsCredit, String total, String tax, String taxAmount, String rewards, ArrayList<OrderDetailsInfo> refundItems,String redeem) {
        this.userId = userId;
        this.orderId = orderId;
        this.subTotal = subTotal;
        this.rewardsCredit = rewardsCredit;
        this.total = total;
        this.tax = tax;
        this.taxAmount = taxAmount;
        this.rewards = rewards;
        this.refundItems = refundItems;
        this.redeem=redeem;
    }
}
