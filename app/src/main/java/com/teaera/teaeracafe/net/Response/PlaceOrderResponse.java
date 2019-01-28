package com.teaera.teaeracafe.net.Response;

import com.teaera.teaeracafe.net.Model.OrderInfo;

/**
 * Created by admin on 19/12/2017.
 */

public class PlaceOrderResponse extends BaseResponse {
    private OrderInfo orderInfo;

    public OrderInfo getOrderInfo() {
        return orderInfo;
    }

    public void setOrderInfo(OrderInfo orderInfo) {
        this.orderInfo = orderInfo;
    }
}
