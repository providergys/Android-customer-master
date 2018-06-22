package com.teaera.teaeracafe.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import java.math.BigDecimal;

/**
 * Created by admin on 01/08/2017.
 */
public class BaseActivity extends Activity {

    private SharedPreferences preferences;
    private ProgressDialog dialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    public void showLoader(int resId) {
        dialog = ProgressDialog.show(this, "",
                getString(resId), true);
    }

    public void hideLoader() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

}
