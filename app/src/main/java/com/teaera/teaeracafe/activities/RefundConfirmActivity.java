package com.teaera.teaeracafe.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.teaera.teaeracafe.R;
import com.teaera.teaeracafe.adapter.RefundOrderAdapter;
import com.teaera.teaeracafe.app.Application;
import com.teaera.teaeracafe.net.Model.OrderDetailsInfo;
import com.teaera.teaeracafe.net.Model.OrderInfo;
import com.teaera.teaeracafe.net.Request.RefundOrdersRequest;
import com.teaera.teaeracafe.net.Response.RefundOrdersResponse;
import com.teaera.teaeracafe.preference.UserPrefs;
import com.teaera.teaeracafe.utils.Constants;
import com.teaera.teaeracafe.utils.DialogUtils;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RefundConfirmActivity extends BaseActivity implements View.OnClickListener, RefundOrderAdapter.OnQuantityChangeListener, DialogInterface.OnClickListener {

    private TextView rewardTextView;
    private TextView subtotalTextView;
    private TextView creditTextView;
    private TextView taxTextView;
    private TextView totalTextView;
    private ListView itemListView;

    private RefundOrderAdapter refundOrderAdapter;

    private OrderInfo orderInfo;
    private ArrayList<OrderDetailsInfo> orderDetails = new ArrayList<OrderDetailsInfo>();
    private ArrayList<Integer> quantityArray = new ArrayList<Integer>();

    private DialogInterface.OnClickListener okListener;
    private float storeTax;
    private int rewards = 0;

    private String totalPriceStr = "0.00";
    private String subTotalStr = "0.00";
    private String redeemCreditStr = "0.00";
    private String taxAmountStr = "0.00";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refund_confirm);

        okListener = this;
        orderInfo = (OrderInfo) getIntent().getSerializableExtra("order");

        orderDetails = (ArrayList<OrderDetailsInfo>) getIntent().getSerializableExtra("orderDetails");
        System.out.println("ddddddddddddddddddddddd 2" + orderDetails);
        for (int i = 0; i < orderDetails.size(); i++) {
            int quantity = Integer.parseInt(orderDetails.get(i).getQuantity()) - Integer.parseInt(orderDetails.get(i).getRefundQuantity());
            quantityArray.add(quantity);
            orderDetails.get(i).setQuantity(Integer.toString(quantity));
        }
        System.out.println("ddddddddddddddddddddddd 3" + orderDetails);
        init();
    }

    private void init() {
        rewardTextView = findViewById(R.id.rewardTextView);
        subtotalTextView = findViewById(R.id.subtotalTextView);
        creditTextView = findViewById(R.id.creditTextView);
        taxTextView = findViewById(R.id.taxTextView);
        totalTextView = findViewById(R.id.totalTextView);

        itemListView = findViewById(R.id.itemListView);
        refundOrderAdapter = new RefundOrderAdapter(this, orderDetails);
        itemListView.setAdapter(refundOrderAdapter);

        // initial data
        totalPriceStr = orderInfo.getTotalPrice();
        subTotalStr = orderInfo.getSubTotal();
        redeemCreditStr = orderInfo.getRewardsCredit();
        taxAmountStr = orderInfo.getTaxAmount();
        rewards = Integer.parseInt(orderInfo.getRewards());

        Button refundButton = findViewById(R.id.refundButton);
        refundButton.setOnClickListener(this);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(this);

        updateOrderInfo();
    }

    private void updateOrderInfo() {

        totalTextView.setText("$" + totalPriceStr);
        subtotalTextView.setText("$" + subTotalStr);
        creditTextView.setText("-$" + redeemCreditStr);
        taxTextView.setText("-$" + taxAmountStr);
        rewardTextView.setText(String.format("-%d", rewards));
    }

    private void updateRefundOrderDetails() {
        refundOrderAdapter.updateCategories(orderDetails);
        refundOrderAdapter.notifyDataSetChanged();
    }

    private void refundOrders() {
        showLoader(R.string.empty);
        //  orderInfo.getRedeem()
//        Application.getServerApi().refundOrders(new RefundOrdersRequest(UserPrefs.getUserInfo(this).getId(), orderInfo.getId(), orderInfo.getSubTotal(), orderInfo.getRewardsCredit(), orderInfo.getTotalPrice(), orderInfo.getTax(), orderInfo.getTaxAmount(), orderInfo.getRewards(), orderDetails)).enqueue(new Callback<RefundOrdersResponse>(){
        try {
            Application.getServerApi().refundOrders("application/json", new RefundOrdersRequest(UserPrefs.getUserInfo(this).getId(), orderInfo.getId(), subTotalStr, redeemCreditStr, totalPriceStr, orderInfo.getTax(), taxAmountStr, Integer.toString(rewards), orderDetails, orderInfo.getRedeem())).enqueue(new Callback<RefundOrdersResponse>() {

                @Override
                public void onResponse(Call<RefundOrdersResponse> call, Response<RefundOrdersResponse> response) {
                    hideLoader();
                    if (response.body().isError()) {
                        DialogUtils.showDialog(RefundConfirmActivity.this, "Error", response.body().getMessage(), null, null);
                    } else {
                        orderInfo = response.body().getOrder();
                        orderDetails = response.body().getOrderDetails();
//                    updateOrderInfo();
//                    updateRefundOrderDetails();

                        DialogUtils.showDialog(RefundConfirmActivity.this, "Confirm", getString(R.string.success_refund_order), okListener, okListener);
                    }
                }

                @Override
                public void onFailure(Call<RefundOrdersResponse> call, Throwable t) {
                    hideLoader();
                    if (t.getLocalizedMessage() != null) {
                        Log.d("Order Receipt", t.getLocalizedMessage());
                    } else {
                        Log.d("Order Receipt", "Unknown error");
                    }
                }
            });
        } catch (Exception e) {
            DialogUtils.showDialog(RefundConfirmActivity.this, "Error", "BrainTree Error", okListener, okListener);
        }

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.refundButton:
                if (!totalPriceStr.isEmpty() && Float.parseFloat(totalPriceStr) != 0) {
                    refundOrders();
                }
                break;

            case R.id.backButton:
                finish();
                overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                break;
        }
    }

    private void updateInfo() {

        storeTax = Float.parseFloat(orderInfo.getTax());
        rewards = 0;
        float subTotal = 0;
        float redeemCredit = 0;

        for (int i = 0; i < orderDetails.size(); i++) {
            if (orderDetails.get(i).getDrinkable().equals("1")) {
                // calculate rewards
                rewards = rewards + Integer.parseInt(orderDetails.get(i).getRewards()) * Integer.parseInt(orderDetails.get(i).getQuantity());
            }

//            if (orderDetails.get(i).getDrinkable().equals("1") && orderDetails.get(i).getRedeemed().equals("1")) {
//                // calculate redeemed free drink
//                float price = Float.parseFloat(orderDetails.get(i).getPrice());
//                if (price > Constants.REWARD_MAX) {
//                    redeemCredit = redeemCredit + Constants.REWARD_MAX * Integer.parseInt(orderDetails.get(i).getQuantity());
//                } else {
//                    redeemCredit = redeemCredit + price * Integer.parseInt(orderDetails.get(i).getQuantity());
//                }
//            }

            subTotal = subTotal + Float.parseFloat(orderDetails.get(i).getPrice()) * Integer.parseInt(orderDetails.get(i).getQuantity());
        }

        redeemCredit = Constants.REWARD_MAX * Integer.parseInt(orderInfo.getRedeem());
        if (redeemCredit > subTotal) {
            redeemCredit = subTotal;
        }

        this.subTotalStr = String.format("%.2f", subTotal);
        this.redeemCreditStr = String.format("%.2f", redeemCredit);
        this.taxAmountStr = String.format("%.2f", round((subTotal - redeemCredit) * storeTax, 2));
        this.totalPriceStr = String.format("%.2f", round(subTotal + (subTotal - redeemCredit) * storeTax - redeemCredit, 2));

        updateOrderInfo();
    }


    @Override
    public void onPlusButtonClicked(int position) {
        int refundQuantity = Integer.parseInt(orderDetails.get(position).getQuantity());
        if (refundQuantity < quantityArray.get(position)) {
            refundQuantity++;
            orderDetails.get(position).setQuantity(Integer.toString(refundQuantity));
            updateRefundOrderDetails();
            updateInfo();
        }
    }

    @Override
    public void onMinusButtonClicked(int position) {
        int refundQuantity = Integer.parseInt(orderDetails.get(position).getQuantity());
        if (refundQuantity > 0) {
            refundQuantity--;
            orderDetails.get(position).setQuantity(Integer.toString(refundQuantity));
            updateRefundOrderDetails();
            updateInfo();
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
//        Intent intent = new Intent(RefundConfirmActivity.this, MainActivity.class);
//        intent.putExtra("menuItem", 1);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
//        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
        Intent intent = new Intent(RefundConfirmActivity.this, OrderReceiptActivity.class);
        intent.putExtra("order", this.orderInfo);
        startActivity(intent);
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
        finish();
    }
}
