package com.teaera.teaeracafe.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;

import com.teaera.teaeracafe.R;
import com.teaera.teaeracafe.app.Application;
import com.teaera.teaeracafe.net.Model.PromotedMenuInfo;
import com.teaera.teaeracafe.net.Response.GetCategoryResponse;
import com.teaera.teaeracafe.preference.CategoryPrefs;
import com.teaera.teaeracafe.preference.LocationPrefs;
import com.teaera.teaeracafe.preference.PromotedMenuPrefs;
import com.teaera.teaeracafe.preference.UserPrefs;
import com.teaera.teaeracafe.utils.DialogUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends BaseActivity {

    private final int SPLASH_DISPLAY_LENGTH = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashKey = new String(Base64.encode(md.digest(), 0));

                //Log.i(TAG, "printHashKey() Hash Key: " + hashKey);
//                System.out.println("sdgggggggggggggggggggg"+hashKey);
            }
        } catch (NoSuchAlgorithmException e) {
        } catch (Exception e) {
        }
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                if (UserPrefs.isLoggedIn(SplashActivity.this)) {
                    loadData();
                } else {
                    Intent intent = new Intent(SplashActivity.this, SignInActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                    finish();
                }
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    private void loadData() {
        showLoader(R.string.empty);

        Application.getServerApi().getCategories().enqueue(new Callback<GetCategoryResponse>(){

            @Override
            public void onResponse(Call<GetCategoryResponse> call, Response<GetCategoryResponse> response) {
                hideLoader();
                if (response.body().isError()) {
                    DialogUtils.showDialog(SplashActivity.this, "Error", response.body().getMessage(), null, null);
                } else {
                    LocationPrefs.setLocations(SplashActivity.this, response.body().getLocations());
                    CategoryPrefs.setCategories(SplashActivity.this, response.body().getCategories());
                    PromotedMenuPrefs.setPromotedMenu(SplashActivity.this, response.body().getPromoted());

                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<GetCategoryResponse> call, Throwable t) {
                hideLoader();
                if (t.getLocalizedMessage() != null) {
                    Log.d("Splash", t.getLocalizedMessage());
                } else {
                    Log.d("Splash", "Unknown error");
                }
            }
        });
    }
}
