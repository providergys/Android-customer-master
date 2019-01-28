package com.teaera.teaeracafe.activities;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.PaymentData;
import com.teaera.teaeracafe.R;
import com.teaera.teaeracafe.app.Application;
import com.teaera.teaeracafe.net.Model.CartInfo;
import com.teaera.teaeracafe.net.Model.UserInfo;
import com.teaera.teaeracafe.net.Request.GenerateTokenRequest;
import com.teaera.teaeracafe.net.Request.LoadBalanceRequest;
import com.teaera.teaeracafe.net.Request.UserProfileRequest;
import com.teaera.teaeracafe.net.Response.GenerateTokenResponse;
import com.teaera.teaeracafe.net.Response.UserProfileResponse;
import com.teaera.teaeracafe.preference.CartPrefs;
import com.teaera.teaeracafe.preference.UserPrefs;
import com.teaera.teaeracafe.utils.DialogUtils;

import java.util.ArrayList;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class RewardsFragment extends Fragment implements View.OnClickListener {

    final int REQUEST_CODE = 125;
    final int LOAD_PAYMENT_DATA_REQUEST_CODE = 125;
    private String clientToken = "";
    private Spinner drinkSpinner;
    private TextView drinkTextView;
    private TextView balanceTextView;
    private MaterialRatingBar ratingBar1;
    private MaterialRatingBar ratingBar2;
    private ProgressDialog dialog;

    private ArrayList<String> drinkOptions = new ArrayList<String>();
    private UserInfo userInfo;
    private int redeemCount = 0;
    private int balance = 0;

    public RewardsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rewards, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        init();
    }

    private void init() {

        ratingBar1 = getActivity().findViewById(R.id.ratingBar1);
        ratingBar2 = getActivity().findViewById(R.id.ratingBar2);

        drinkSpinner = getActivity().findViewById(R.id.drinkSpinner);
        drinkTextView = getActivity().findViewById(R.id.drinkTextView);
        balanceTextView = getActivity().findViewById(R.id.balanceTextView);

        Button redeemButton = getActivity().findViewById(R.id.redeemButton);
        redeemButton.setOnClickListener(this);

        Button orderButton = getActivity().findViewById(R.id.orderButton);
        orderButton.setOnClickListener(this);

        Button firstButton = getActivity().findViewById(R.id.firstButton);
        firstButton.setOnClickListener(this);

        Button secondButton = getActivity().findViewById(R.id.secondButton);
        secondButton.setOnClickListener(this);

        Button thirdButton = getActivity().findViewById(R.id.thirdButton);
        thirdButton.setOnClickListener(this);

    }

    @Override
    public void onResume() {
        super.onResume();

        loadProfile();
    }

    private void updateProfile() {
        int rewards = userInfo.getRewardStar();
        ArrayList<CartInfo> carts = CartPrefs.getCarts(getActivity());

        try {
            for (CartInfo cart : carts) {
                if (cart.getRedeemed().equals("1")) {
                    rewards = rewards - 10 * Integer.parseInt(cart.getQuantity());
                }
            }

            redeemCount = (rewards - rewards % 10) / 10;
            if (redeemCount > 0) {
                for (int i = 1; i <= redeemCount; i++) {
                    drinkOptions.add(String.format("%d Free Drink", i));
                }
            } else {
                drinkOptions.add("No Free Drink");
            }

            int star = rewards - redeemCount * 10;

            if (star <= 5) {
                ratingBar1.setRating(star);
                ratingBar2.setRating(0);
            } else {
                ratingBar1.setRating(5);
                ratingBar2.setRating(star - 5);
            }
        } catch (Exception e) {

        }


        balanceTextView.setText("$" + userInfo.getBalance());

        drinkSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                drinkTextView.setText(drinkSpinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, drinkOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        drinkSpinner.setAdapter(adapter);
        if (redeemCount > 0) {
            drinkSpinner.setSelection(redeemCount - 1);
        } else {
            drinkSpinner.setSelection(0);
        }

        try {
            drinkTextView.setText(drinkSpinner.getSelectedItem().toString());
        } catch (Exception e) {

        }


    }


    private void loadProfile() {
        showLoader(R.string.empty);

        Application.getServerApi().getUserProfile(new UserProfileRequest(UserPrefs.getUserInfo(getActivity()).getId())).enqueue(new Callback<UserProfileResponse>() {

            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                hideLoader();
                if (response.body().isError()) {
                    DialogUtils.showDialog(getActivity(), "Error", response.body().getMessage(), null, null);
                } else {
                    userInfo = response.body().getUser();
                    UserPrefs.saveUserInfo(getActivity(), userInfo);
                    updateProfile();
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                hideLoader();
                if (t.getLocalizedMessage() != null) {
                    Log.d("Rewards", t.getLocalizedMessage());
                } else {
                    Log.d("Rewards", "Unknown error");
                }
            }
        });
    }

    private void getToken() {
        Application.getServerApi().generateClientToken(new GenerateTokenRequest(UserPrefs.getUserInfo(getActivity()).getId())).enqueue(new Callback<GenerateTokenResponse>() {

            @Override
            public void onResponse(Call<GenerateTokenResponse> call, Response<GenerateTokenResponse> response) {
                hideLoader();
                if (response.body().isError()) {
                    DialogUtils.showDialog(getActivity(), "Error", response.body().getMessage(), null, null);
                } else {
                    //isReadyToPay();
                    clientToken = response.body().getToken();
                    DropInRequest dropInRequest = new DropInRequest()
                            .clientToken(clientToken);
                    startActivityForResult(dropInRequest.getIntent(getActivity()), REQUEST_CODE);
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

    private void loadBalance(String nonce) {

        showLoader(R.string.empty);
        Application.getServerApi().loadBalance(new LoadBalanceRequest(UserPrefs.getUserInfo(getActivity()).getId(), Integer.toString(balance), nonce)).enqueue(new Callback<UserProfileResponse>() {

            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                hideLoader();
                if (response.body().isError()) {
                    DialogUtils.showDialog(getActivity(), "Error", response.body().getMessage(), null, null);
                } else {
                    userInfo = response.body().getUser();
                    UserPrefs.saveUserInfo(getActivity(), userInfo);
                    updateProfile();
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                hideLoader();
                if (t.getLocalizedMessage() != null) {
                    Log.d("Cart", t.getLocalizedMessage());
                } else {
                    Log.d("Cart", "Unknown error");
                }
            }
        });

    }

    public void showLoader(int resId) {
        dialog = ProgressDialog.show(getActivity(), "",
                getString(resId), true);
    }

    public void hideLoader() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.redeemButton:
                if (redeemCount > 0) {
                    ((MainActivity) getActivity()).rewardMenu();
                }
                break;

            case R.id.orderButton:
                break;
            case R.id.firstButton:
                balance = 25;
                getToken();
                break;

            case R.id.secondButton:
                balance = 50;
                getToken();
                break;

            case R.id.thirdButton:
                balance = 100;
                getToken();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {

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
                Log.d("mylog", "Result: " + stringNonce);

                loadBalance(stringNonce);

            } else if (resultCode == Activity.RESULT_CANCELED) {
                // the user canceled
                Log.d("mylog", "user canceled");
            } else {
                // handle errors here, an exception may be available in
                Exception error = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
                Log.d("mylog", "Error : " + error.toString());
            }
        } else if (requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentData paymentData = PaymentData.getFromIntent(data);
                String token = paymentData.getPaymentMethodToken().getToken();

                loadBalance(token);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.d("mylog", "android pay canceled");
            } else if (resultCode == AutoResolveHelper.RESULT_ERROR) {
                Status status = AutoResolveHelper.getStatusFromIntent(data);
                // Log the status for debugging.
                // Generally, there is no need to show an error to
                // the user as the Google Payment API will do that.
            }
        }
    }


}
