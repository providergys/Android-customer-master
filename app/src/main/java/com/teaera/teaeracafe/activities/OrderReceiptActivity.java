package com.teaera.teaeracafe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.teaera.teaeracafe.R;
import com.teaera.teaeracafe.adapter.ItemListAdapter;
import com.teaera.teaeracafe.app.Application;
import com.teaera.teaeracafe.net.Model.LocationInfo;
import com.teaera.teaeracafe.net.Model.OrderDetailsInfo;
import com.teaera.teaeracafe.net.Model.OrderInfo;
import com.teaera.teaeracafe.net.Request.OrderDetailsRequest;
import com.teaera.teaeracafe.net.Response.OrderDetailsResponse;
import com.teaera.teaeracafe.preference.LocationPrefs;
import com.teaera.teaeracafe.utils.DialogUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderReceiptActivity extends BaseActivity implements View.OnClickListener {

    private TextView orderNumberTextView;
    private TextView locationTextView;
    private TextView estimateTextView;
    private TextView receivedDateTextView;
    private TextView rewardTextView;
    private TextView subtotalTextView;
    private TextView creditTextView;
    private TextView taxTextView;
    private TextView totalTextView;
    private ListView itemListView;
    private ImageView stateImageView;

    private Button refundButton;

    private ItemListAdapter itemListAdapter;

    private OrderInfo orderInfo;
    private ArrayList<OrderDetailsInfo> orderDetails = new ArrayList<OrderDetailsInfo>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_receipt);

        orderInfo = (OrderInfo) getIntent().getSerializableExtra("order");
        init();
    }

    void init() {

        orderNumberTextView = findViewById(R.id.orderNumberTextView);
        locationTextView = findViewById(R.id.locationTextView);
        estimateTextView = findViewById(R.id.estimateTextView);
        receivedDateTextView = findViewById(R.id.receivedDateTextView);
        rewardTextView = findViewById(R.id.rewardTextView);
        subtotalTextView = findViewById(R.id.subtotalTextView);
        creditTextView = findViewById(R.id.creditTextView);
        taxTextView = findViewById(R.id.taxTextView);
        totalTextView = findViewById(R.id.totalTextView);
        stateImageView = findViewById(R.id.stateImageView);

        itemListView = findViewById(R.id.itemListView);
        itemListAdapter = new ItemListAdapter(this, orderDetails);
        itemListView.setAdapter(itemListAdapter);


        refundButton = findViewById(R.id.refundButton);
        refundButton.setOnClickListener(this);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(this);

        updateOrderInfo();
        loadOrderDetails();
    }

    private void updateOrderInfo() {

        int orderId = Integer.parseInt(orderInfo.getId());

        orderNumberTextView.setText(String.format("Order #%05d", orderId));
        locationTextView.setText(getLocationName(orderInfo.getLocationId()));
        estimateTextView.setText("Estimate Pickup Time: " + getEstimatedTime(orderInfo.getTimeStamp(), orderInfo.getWaitingTime()));
        receivedDateTextView.setText(orderInfo.getTimeStamp());
        receivedDateTextView.setText(getReceivedDate(orderInfo.getTimeStamp()));
        totalTextView.setText("$" + orderInfo.getTotalPrice());
        subtotalTextView.setText("$" + orderInfo.getSubTotal());
        creditTextView.setText("-$" + orderInfo.getRewardsCredit());
        taxTextView.setText("-$" + orderInfo.getTaxAmount());
        rewardTextView.setText("+" + orderInfo.getRewards());

        if (orderInfo.getStatus().equals("0") && Float.valueOf(orderInfo.getTotalPrice()) != 0) {
            refundButton.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams params = refundButton.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            refundButton.setLayoutParams(params);
        } else {
            refundButton.setVisibility(View.INVISIBLE);
            ViewGroup.LayoutParams params = refundButton.getLayoutParams();
            params.height = 0;
            refundButton.setLayoutParams(params);
        }

        updateOrderState();
    }

    private void updateOrderState() {
        switch (orderInfo.getStatus()) {
            case "0":
                stateImageView.setImageResource(R.drawable.line_received_state);
                break;
            case "1":
                stateImageView.setImageResource(R.drawable.line_progress_state);
                break;
            case "2":
                stateImageView.setImageResource(R.drawable.line_completed_state);
                break;
            default:
                stateImageView.setImageResource(R.drawable.line_received_state);
                break;
        }
    }

    private void loadOrderDetails() {
        showLoader(R.string.empty);

        Application.getServerApi().getOrderDetails(new OrderDetailsRequest(orderInfo.getId())).enqueue(new Callback<OrderDetailsResponse>() {

            @Override
            public void onResponse(Call<OrderDetailsResponse> call, Response<OrderDetailsResponse> response) {
                hideLoader();
                if (response.body().isError()) {
                    DialogUtils.showDialog(OrderReceiptActivity.this, "Error", response.body().getMessage(), null, null);
                } else {
                    orderDetails = response.body().getOrders();
                    if (orderDetails.size() > 0) {
                        updateOrderDetails();
                    }
                }
            }

            @Override
            public void onFailure(Call<OrderDetailsResponse> call, Throwable t) {
                hideLoader();
                if (t.getLocalizedMessage() != null) {
                    Log.d("Order Receipt", t.getLocalizedMessage());
                } else {
                    Log.d("Order Receipt", "Unknown error");
                }
            }
        });
    }

    private String getLocationName(String locationId) {
        ArrayList<LocationInfo> info = LocationPrefs.getLocations(this);
        for (int i = 0; i < info.size(); i++) {
            if (info.get(i).getId().equals(locationId)) {
                return info.get(i).getLocationName();
            }
        }

        return "";
    }

    private String getEstimatedTime(String dateString, String waitingTime) {

        int diff = Integer.parseInt(waitingTime);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        SimpleDateFormat timeSdf = new SimpleDateFormat("hh:mm a");

        try {
            Date date = sdf.parse(dateString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.MINUTE, diff);
            return timeSdf.format(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return "";
    }

    private String getReceivedDate(String dateString) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        SimpleDateFormat newSdf = new SimpleDateFormat("MM/dd/yy, hh:mm a");

        try {
            Date date = sdf.parse(dateString);
            return newSdf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return "";
    }

    private void updateOrderDetails() {
        itemListAdapter.updateCategories(orderDetails);
        itemListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.refundButton:
                Intent intent = new Intent(this, RefundConfirmActivity.class);
                intent.putExtra("order", orderInfo);
                intent.putExtra("orderDetails", orderDetails);
                startActivity(intent);
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                finish();
                break;

            case R.id.backButton:
                finish();
                overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                break;
        }
    }

}
