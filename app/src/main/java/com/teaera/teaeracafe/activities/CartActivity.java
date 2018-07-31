package com.teaera.teaeracafe.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.CardRequirements;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.teaera.teaeracafe.R;
import com.teaera.teaeracafe.adapter.CartListAdapter;
import com.teaera.teaeracafe.app.Application;
import com.teaera.teaeracafe.net.Model.CartInfo;
import com.teaera.teaeracafe.net.Model.LocationInfo;
import com.teaera.teaeracafe.net.Model.OrderInfo;
import com.teaera.teaeracafe.net.Model.UserInfo;
import com.teaera.teaeracafe.net.Request.GenerateTokenRequest;
import com.teaera.teaeracafe.net.Request.OrderRequest;
import com.teaera.teaeracafe.net.Response.GenerateTokenResponse;
import com.teaera.teaeracafe.net.Response.GetCategoryResponse;
import com.teaera.teaeracafe.net.Response.PlaceOrderResponse;
import com.teaera.teaeracafe.preference.CartPrefs;
import com.teaera.teaeracafe.preference.CategoryPrefs;
import com.teaera.teaeracafe.preference.LocationPrefs;
import com.teaera.teaeracafe.preference.PromotedMenuPrefs;
import com.teaera.teaeracafe.preference.UserPrefs;
import com.teaera.teaeracafe.utils.Constants;
import com.teaera.teaeracafe.utils.DialogUtils;
import com.teaera.teaeracafe.utils.TinyDB;

import java.util.ArrayList;
import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends BaseActivity
        implements View.OnClickListener, CartListAdapter.OnQuantityChangeListener, DialogInterface.OnClickListener {

    private String clientToken = "";
    final int REQUEST_CODE = 124;
    final int LOAD_PAYMENT_DATA_REQUEST_CODE = 124;

    private Spinner locationSpinner;
    private TextView locationTextView;
    private TextView redeemTextView;
    private TextView rewardTextView;
    private ListView cartListView;
    private TextView subtotalTextView;
    private TextView creditTextView;
    private TextView taxTextView;
    private TextView totalTextView;
    private TextView estimateTextView;
    private TextView noItemTextView;
    private static final String TAG = "CartActivity";
    private CartListAdapter cartListAdapter;

    private ArrayList<LocationInfo> locations;
    private ArrayList<String> locationNames = new ArrayList<String>();
    private ArrayList<String> waitingTimes = new ArrayList<String>();
    private ArrayList<CartInfo> carts = new ArrayList<CartInfo>();
    private ArrayList<CartInfo> sortedCarts = new ArrayList<CartInfo>();
    private String selectedLocationID;
    private String selectedWaitingTime;

    private float storeTax;
    private int availableRedeem = 0;
    private int redeems = 0;
    private int rewards = 0;

    private String totalPriceStr;
    private String subTotalStr;
    private String redeemCreditStr;
    private String taxAmountStr;
    TinyDB         tinydb;
    Boolean        aBooleanFreeDrinks;
    private String strFreeDrinks="";
    private DialogInterface.OnClickListener onConfirmListner;
    private OrderInfo orderInfo;

    private PaymentsClient mPaymentsClient;

    TextView textView4;
    RelativeLayout relativeLayout11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_cart);

        tinydb=new TinyDB( CartActivity.this );

//        loadData();

        locations = LocationPrefs.getLocations(this);
        for (int i = 0; i < locations.size(); i++) {
            locationNames.add(locations.get(i).getLocationName());
            waitingTimes.add(locations.get(i).getWaitingTime());
        }
        carts = CartPrefs.getCarts(this);
        onConfirmListner = this;

        mPaymentsClient =
                Wallet.getPaymentsClient(
                        this,
                        new Wallet.WalletOptions.Builder()
                                .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                                .build());

        init();

    }

    void init() {

        redeemTextView      = findViewById( R.id.redeemTextView);
        rewardTextView      = findViewById( R.id.rewardTextView);
        subtotalTextView    = findViewById( R.id.subtotalTextView);
        creditTextView      = findViewById( R.id.creditTextView);
        taxTextView         = findViewById( R.id.taxTextView);
        totalTextView       = findViewById( R.id.totalTextView);
        estimateTextView    = findViewById( R.id.estimateTextView);
        noItemTextView      = findViewById( R.id.noItemTextView);

        textView4           = findViewById(R.id.tv_redeems);
        relativeLayout11    =findViewById( R.id.relativeLayout11 );

        relativeLayout11.setVisibility( View.GONE );

        locationSpinner     = findViewById( R.id.locationSpinner);
        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                locationTextView.setText(locationSpinner.getSelectedItem().toString());
                Application.setLocation(position);
                noItemTextView.setVisibility(View.GONE);
                updateCartInfo(position);
                cartListAdapter.updateCategories(sortedCarts);
                cartListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item,locationNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(adapter);
        locationSpinner.setSelection( Application.getLocation());
        updateCartInfo( Application.getLocation());

        locationTextView = findViewById( R.id.locationTextView1);
        locationTextView.setText(locationSpinner.getSelectedItem().toString());

        cartListView = findViewById( R.id.cartListView);
        cartListAdapter = new CartListAdapter(this, sortedCarts);
        cartListView.setAdapter(cartListAdapter);

        Button orderButton =  findViewById( R.id.orderButton);
        orderButton.setOnClickListener(this);
        Button addButton = findViewById( R.id.addButton);
        addButton.setOnClickListener(this);
        Button redeemPlusButton = findViewById( R.id.redeemPlusButton);
        redeemPlusButton.setOnClickListener(this);

//        TextView textView4 = findViewById( R.id.textView4);
//        textView4.setOnClickListener(this);
        Button redeemMinusButton = findViewById( R.id.redeemMinusButton);
        redeemMinusButton.setOnClickListener(this);
        Button backButton = findViewById( R.id.backButton);
        backButton.setOnClickListener(this);
    }

    private void updateCartInfo(int index) {

        carts = CartPrefs.getCarts(this);
        sortedCarts.clear();
        selectedLocationID = locations.get(index).getId();
        storeTax = Float.parseFloat(locations.get(index).getTax());
        selectedWaitingTime = waitingTimes.get(index);
        estimateTextView.setText("Current wait time: " + selectedWaitingTime + "mins");

        int rewards = UserPrefs.getUserInfo(this).getRewardStar();
        if (carts != null) {
            for (int i = 0; i<carts.size(); i++) {
                if (carts.get(i).getRedeemed().equals("1")) {
                    rewards = rewards - Integer.parseInt(carts.get(i).getQuantity()) * 10;
                    rewardTextView.setText(String.valueOf(rewards));
                }

                if (carts.get(i).getLocationId().equals(selectedLocationID)) {
                    sortedCarts.add(carts.get(i));
                }
            }
        }

//        if (carts != null) {
//            for (int i = 0; i<carts.size(); i++) {
//                if (Integer.parseInt(carts.get(i).getRewards())>0) {
//                    System.out.println("ayaaaaaaaaaa");
//                    rewards = rewards * Integer.parseInt(carts.get(i).getQuantity());
//                    rewardTextView.setText(String.valueOf(rewards));
//                }
//
//                if (carts.get(i).getLocationId().equals(selectedLocationID)) {
//                    sortedCarts.add(carts.get(i));
//                }
//            }
//        }



        System.out.println(  tinydb.getString( "reddem_count" )+"fgggggggggggggggh"+
                tinydb.getBoolean( "reddem_check" ));

        aBooleanFreeDrinks  = tinydb.getBoolean( "reddem_check" );
        strFreeDrinks       = tinydb.getString( "reddem_count" );

        if (aBooleanFreeDrinks){
            availableRedeem = Integer.valueOf(strFreeDrinks);
        }
        else {
            availableRedeem = (rewards - rewards % 10) / 10;
        }


//        availableRedeem=0;

        Log.d( TAG, "updateCartInfo: "+availableRedeem );

        if (availableRedeem==0){
            textView4.setVisibility( View.VISIBLE );
            relativeLayout11.setVisibility( View.GONE );
            textView4.setText( "Currently no rewards available to redeem on the account" );
        }
        else {
            relativeLayout11.setVisibility( View.VISIBLE );
//            textView4.setVisibility( View.GONE );
            textView4.setText( "Redeem Free Drink" );
            redeemTextView.setText(Integer.toString(availableRedeem));
        }

        calculateOrder();
    }

    private void calculateOrder() {


        redeems = 0;
        rewards = 0;

        float totalPrice    = 0;
        float subTotal      = 0;
        float redeemCredit  = 0;
        float tax           = 0;

        if (sortedCarts.size() == 0) {
            noItemTextView.setVisibility(View.VISIBLE);

        } else {
            for (int i=0; i<sortedCarts.size(); i++) {
                System.out.println(sortedCarts.get(i).getDrinkable()+"   ayaaaaaaaaaa     "+sortedCarts.get(i).getRedeemed());
//                if (sortedCarts.get(i).getDrinkable().equals("1") && sortedCarts.get(i).getRedeemed().equals("0")) {
                if (sortedCarts.get(i).getDrinkable().equals("1") && sortedCarts.get(i).getRedeemed().equals("0")) {
                    // calculate rewards
                   rewards = rewards + Integer.parseInt(sortedCarts.get(i).getRewards()) * Integer.parseInt(sortedCarts.get(i).getQuantity());
                    UserInfo info = new UserInfo();
                    info.setRewardStar(rewards);
                    info.setEmail( UserPrefs.getUserInfo(this).getEmail());
                    info.setId( UserPrefs.getUserInfo(this).getId());
                    info.setBalance( UserPrefs.getUserInfo(this).getBalance());
                    info.setImage( UserPrefs.getUserInfo(this).getImage());
                    info.setFirstname( UserPrefs.getUserInfo(this).getFirstname());
                    info.setLastname( UserPrefs.getUserInfo(this).getLastname());
                    UserPrefs.saveUserInfo(this, info);

                }

//                if (sortedCarts.get(i).getDrinkable().equals("1") && sortedCarts.get(i).getRedeemed().equals("1")) {
                if (sortedCarts.get(i).getDrinkable().equals("1")) {
                    // calculate redeemed free drink
                    redeems += 1;
                    float price = Float.parseFloat(sortedCarts.get(i).getPrice());
                    Log.d( TAG, "Price: "+price +" "+redeemCredit+"Menu Id"+sortedCarts.get( i).getMenuId());
                    if (price > Constants.REWARD_MAX) {
//                        redeemCredit = redeemCredit + Constants.REWARD_MAX * Integer.parseInt(sortedCarts.get(i).getQuantity());

                        redeemCredit =  Constants.REWARD_MAX * availableRedeem;
                    } else {
//                        redeemCredit = redeemCredit + price * Integer.parseInt(sortedCarts.get(i).getQuantity());
                        redeemCredit = price * Integer.parseInt(sortedCarts.get(i).getQuantity());
                    }

                }



                subTotal = subTotal + Float.parseFloat(sortedCarts.get(i).getPrice()) * Integer.parseInt(sortedCarts.get(i).getQuantity());
                Log.d( TAG, "calculateOrder: "+" redeem"+redeemCredit+" Total" +subTotal);
            }
        }

        this.subTotalStr         = String.format("%.2f", subTotal);
        this.redeemCreditStr     = String.format("%.2f", redeemCredit);
        this.taxAmountStr        = String.format("%.2f", round((subTotal - redeemCredit) * storeTax, 2));
        this.totalPriceStr       = String.format("%.2f", round(subTotal + (subTotal-redeemCredit) * storeTax - redeemCredit, 2));

        subtotalTextView.setText("$"+this.subTotalStr);
        creditTextView.setText("-$"+this.redeemCreditStr);
        taxTextView.setText("$"+this.taxAmountStr);
        totalTextView.setText("$"+this.totalPriceStr);

        int nw_reward=rewards/10;

        int nwAvailableReedeem=availableRedeem+nw_reward;

        redeemTextView.setText(Integer.toString(nwAvailableReedeem));
        rewardTextView.setText(String.format("+%d", rewards));

        Log.d( TAG, "calculateOrder: Rewards"+rewards +nw_reward);

    }

    private void removeOrderCarts() {

        for (int i = 0; i<sortedCarts.size(); i++) {
            for (int j=0; j<carts.size(); j++) {
                if (carts.get(j).getCartIndex() == sortedCarts.get(i).getCartIndex()) {
                    carts.remove(j);
                    break;
                }
            }
        }

        for (int i = 0; i < carts.size(); i++) {
            carts.get(i).setCartIndex(i);
        }

        redeems = 0;
        CartPrefs.clearCarts(CartActivity.this);
        CartPrefs.setCarts(CartActivity.this, carts);
        carts = CartPrefs.getCarts(CartActivity.this);

        updateCartInfo(locationSpinner.getSelectedItemPosition());
        cartListAdapter.updateCategories(sortedCarts);
        cartListAdapter.notifyDataSetChanged();
    }

    private void removeCart(int cartIndex) {

        for (int i=0; i<carts.size(); i++) {
            if (carts.get(i).getCartIndex() == cartIndex) {
                carts.remove(i);
                break;
            }
        }

        for (int i = 0; i < carts.size(); i++) {
            carts.get(i).setCartIndex(i);
        }

        CartPrefs.clearCarts(CartActivity.this);
        CartPrefs.setCarts(CartActivity.this, carts);
        carts = CartPrefs.getCarts(CartActivity.this);
    }

    private void decreaseRedeem() {
        int maxPrice = 0;
        int index = 0;
        for (int i=0; i<sortedCarts.size(); i++) {
            int price = Integer.parseInt(sortedCarts.get(i).getPrice());
            if (sortedCarts.get(i).getRedeemed().equals("1") &&  price > maxPrice) {
                maxPrice = price;
                index = i;
            }
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.orderButton:
                if (sortedCarts.size() == 0) {
                    DialogUtils.showDialog(CartActivity.this, "Error", getString( R.string.empty_order), null, null);
                    return;
                }

                Log.d( TAG, "onClick: "+availableRedeem );

                onBraintreeSubmit();
                break;

            case R.id.addButton:
                // Move to first page
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("menuItem", 0);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition( R.anim.pull_in_left, R.anim.push_out_right);
                break;



            case R.id.redeemMinusButton:
                if (availableRedeem == 0)
                    return;
                availableRedeem-=1;
                redeemTextView.setText(Integer.toString(availableRedeem));

                System.out.println("REWARDSSSS   "+ UserPrefs.getUserInfo(this).getRewardStar());
                if (redeems == 0)
                    return;
                redeems -= 1;
                Log.d( TAG, "onClick: "+ Integer.toString(redeems));
                calculateOrder();
                break;

            case R.id.redeemPlusButton:

                int n = (UserPrefs.getUserInfo(this).getRewardStar() - UserPrefs.getUserInfo(this).getRewardStar() % 10) / 10;
                Log.d( TAG, "onClick: rewww"+n );

                if (aBooleanFreeDrinks){

                    if (availableRedeem < Integer.valueOf( strFreeDrinks )) {
                        availableRedeem += 1;
                        redeemTextView.setText( Integer.toString( availableRedeem ) );
                    }

                    redeems += 1;
                    Log.d( TAG, "onClick: " + Integer.toString( redeems ) );
                    calculateOrder();
                }
                else {

                    if (availableRedeem<n){
                        availableRedeem+=1;
                        redeemTextView.setText(Integer.toString(availableRedeem));
                    }

                    System.out.println(10 * redeems+1+"   REWARDSSSS   "+ UserPrefs.getUserInfo(this).getRewardStar());
                    if (UserPrefs.getUserInfo(this).getRewardStar() >= 10 * redeems+1) {
                        redeems += 1;
                        Log.d( TAG, "onClick: "+ Integer.toString(redeems));
                        calculateOrder();
                    }
                }

                break;

            case R.id.backButton:

                loadData();
                finish();
                overridePendingTransition( R.anim.pull_in_left, R.anim.push_out_right);


                break;
        }

    }

    public void onBraintreeSubmit() {

        Application.getServerApi().generateClientToken(new GenerateTokenRequest( UserPrefs.getUserInfo(this).getId())).enqueue( new Callback<GenerateTokenResponse>(){
            @Override
            public void onResponse(Call<GenerateTokenResponse> call, Response<GenerateTokenResponse> response) {
                hideLoader();
                if (response.body().isError()) {
                    DialogUtils.showDialog(CartActivity.this, "Error", response.body().getMessage(), null, null);
                } else {
                    //isReadyToPay();
                    clientToken = response.body().getToken();
                    DropInRequest dropInRequest = new DropInRequest()
                            .clientToken(clientToken);
                    startActivityForResult(dropInRequest.getIntent(CartActivity.this), REQUEST_CODE);
                }
            }

            @Override
            public void onFailure(Call<GenerateTokenResponse> call, Throwable t) {
                hideLoader();
                if (t.getLocalizedMessage() != null) {
                    Log.d("New Order", t.getLocalizedMessage());
                } else {
                    Log.d("New Order", "Unknown error");
                }
            }
        });
    }

    private void sendOrderToServer(String nonce) {

        if (!isNetworkConnected(this)) {
            DialogUtils.showDialog(this, "Error", getString( R.string.internet_error), null, null);
            return;
        }

        showLoader( R.string.empty);
        Application.getServerApi().placeOrderToServer(new OrderRequest( UserPrefs.getUserInfo(this).getId(), selectedLocationID, subTotalStr, redeemCreditStr, Float.toString(storeTax), taxAmountStr, totalPriceStr, Integer.toString(rewards), Integer.toString(availableRedeem), selectedWaitingTime, sortedCarts, nonce)).enqueue( new Callback<PlaceOrderResponse>(){
            @Override
            public void onResponse(Call<PlaceOrderResponse> call, Response<PlaceOrderResponse> response) {
                try {
                    hideLoader();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (response.body().isError()) {
                    DialogUtils.showDialog(CartActivity.this, "Error", response.body().getMessage(), null, null);
                } else {
                    removeOrderCarts();
                    tinydb.clear();
                    orderInfo = response.body().getOrderInfo();
                    DialogUtils.showDialog(CartActivity.this, "Confirm", getString( R.string.success_order), onConfirmListner, onConfirmListner);
                }
            }

            @Override
            public void onFailure(Call<PlaceOrderResponse> call, Throwable t) {
                hideLoader();
                if (t.getLocalizedMessage() != null) {
                    Log.d("Cart", t.getLocalizedMessage());
                } else {
                    Log.d("Cart", "Unknown error");
                }
            }
        });
    }

    @Override
    public void onPlusButtonClicked(int cartIndex) {

        for (int i = 0; i<carts.size(); i ++) {
            if (carts.get(i).getCartIndex() == cartIndex) {
                int num = Integer.parseInt(carts.get(i).getQuantity());
                carts.get(i).setQuantity(Integer.toString(num+1));
            }
        }

        CartPrefs.setCarts(this, carts);
        updateCartInfo(locationSpinner.getSelectedItemPosition());
        cartListAdapter.updateCategories(sortedCarts);
        cartListAdapter.notifyDataSetChanged();

    }

    @Override
    public void onMinusButtonClicked(int cartIndex) {
        for (int i = 0; i<carts.size(); i ++) {
            if (carts.get(i).getCartIndex() == cartIndex) {
                int num = Integer.parseInt(carts.get(i).getQuantity());
                carts.get(i).setQuantity(Integer.toString(num-1));
                if (num == 1) {
                    removeCart(carts.get(i).getCartIndex());
                }
            }
        }

        CartPrefs.setCarts(this, carts);
        updateCartInfo(locationSpinner.getSelectedItemPosition());
        cartListAdapter.updateCategories(sortedCarts);
        cartListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {

              /********************* Here we recieve the Nonec from the Brain Tree *********************/
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                // use the result to update your UI and send the payment method nonce to your server

//                if (result.getPaymentMethodType() == ANDROID_PAY) {
//                    isReadyToPay();
//                } else {
//                    PaymentMethodNonce nonce = result.getPaymentMethodNonce();
//                    String stringNonce = nonce.getNonce();
//                    Log.d("mylog", "Result: " + stringNonce);
//
//                    sendOrderToServer(stringNonce);
//                }

                PaymentMethodNonce nonce = result.getPaymentMethodNonce();
                String stringNonce = nonce.getNonce();
                Log.d("mylog1", "Result:1 " + stringNonce);

                sendOrderToServer(stringNonce);

            } else if (resultCode == Activity.RESULT_CANCELED) {
                // the user canceled
                Log.d("mylog1", "user canceled1");
            } else {

                if (!isNetworkConnected(this)) {
                    DialogUtils.showDialog(this, "Error", getString( R.string.internet_error), null, null);
                    return;
                }

                Exception error = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
                DialogUtils.showDialog(this, "Error", error.toString(), null, null);
                // handle errors here, an exception may be available in

                Log.d("mylog1", "Error1 : " + error.toString());
            }

        }

        else if (requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentData paymentData = PaymentData.getFromIntent(data);
                String token = paymentData.getPaymentMethodToken().getToken();

                sendOrderToServer(token);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.d("mylog", "android pay canceled");
            } else if (resultCode == AutoResolveHelper.RESULT_ERROR){
                Status status = AutoResolveHelper.getStatusFromIntent(data);

                if (!isNetworkConnected(this)) {
                    DialogUtils.showDialog(this, "Error", getString( R.string.internet_error), null, null);
                    return;
                }

                Exception error = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
                DialogUtils.showDialog(this, "Error", error.toString(), null, null);
                // handle errors here, an exception may be available in
                // Log the status for debugging.
                // Generally, there is no need to show an error to
                // the user as the Google Payment API will do that.
            }
        }
    }

    private void isReadyToPay() {
        IsReadyToPayRequest request =
                IsReadyToPayRequest.newBuilder()
                        .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
                        .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
                        .build();
        Task<Boolean> task = mPaymentsClient.isReadyToPay(request);
        task.addOnCompleteListener(
                new OnCompleteListener<Boolean>() {
                    public void onComplete(Task<Boolean> task) {
                        try {
                            boolean result = task.getResult(ApiException.class);
                            if (result == true) {
                                PaymentDataRequest request = createPaymentDataRequest();
                                if (request != null) {
                                    AutoResolveHelper.resolveTask(
                                            mPaymentsClient.loadPaymentData(request), CartActivity.this,
                                            LOAD_PAYMENT_DATA_REQUEST_CODE);
                                }

                            } else {
                                // Hide Google as payment option.
                            }
                        } catch (ApiException exception) {
                        }
                    }
                });
    }

    /////////////////////////////////////Success Payment/////////////////////////////////////

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        Intent intent = new Intent(this, OrderReceiptActivity.class);
        intent.putExtra("order", this.orderInfo);
        startActivity(intent);
        overridePendingTransition( R.anim.pull_in_right, R.anim.push_out_left);
    }

    private PaymentDataRequest createPaymentDataRequest() {
        PaymentDataRequest.Builder request =
                PaymentDataRequest.newBuilder()
                        .setTransactionInfo(
                                TransactionInfo.newBuilder()
                                        .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                                        .setTotalPrice("10.00")
                                        .setCurrencyCode("USD")
                                        .build())
                        .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
                        .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
                        .setCardRequirements(
                                CardRequirements.newBuilder()
                                        .addAllowedCardNetworks(
                                                Arrays.asList(
                                                        WalletConstants.CARD_NETWORK_AMEX,
                                                        WalletConstants.CARD_NETWORK_DISCOVER,
                                                        WalletConstants.CARD_NETWORK_VISA,
                                                        WalletConstants.CARD_NETWORK_MASTERCARD))
                                        .build());

        PaymentMethodTokenizationParameters params =
                PaymentMethodTokenizationParameters.newBuilder()
                        .setPaymentMethodTokenizationType(
                                WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY)
                        .addParameter("gateway", "yourGateway")
                        .addParameter("gatewayMerchantId", "yourMerchantIdGivenFromYourGateway")
                        .build();

        request.setPaymentMethodTokenizationParameters(params);
        return request.build();
    }

    private void loadData() {

//        showLoader(R.string.empty);
        Application.getServerApi().getCategories().enqueue(new Callback<GetCategoryResponse>(){

            @Override
            public void onResponse(Call<GetCategoryResponse> call, Response<GetCategoryResponse> response) {
//                hideLoader();
                if (response.body().isError()) {
                    DialogUtils.showDialog(CartActivity.this, "Error", response.body().getMessage(), null, null);
                } else {
                    LocationPrefs.setLocations(CartActivity.this, response.body().getLocations());
                }
            }

            @Override
            public void onFailure(Call<GetCategoryResponse> call, Throwable t) {
//                hideLoader();
                if (t.getLocalizedMessage() != null) {
                    Log.d("Splash", t.getLocalizedMessage());
                } else {
                    Log.d("Splash", "Unknown error");
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        loadData();
    }
}
