package com.teaera.teaeracafe.utils;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.teaera.teaeracafe.R;
import com.teaera.teaeracafe.activities.BaseActivity;
import com.teaera.teaeracafe.activities.MainActivity;
import com.teaera.teaeracafe.app.Application;
import com.teaera.teaeracafe.net.Request.SocialLoginRequest;
import com.teaera.teaeracafe.net.Response.UserResponse;
import com.teaera.teaeracafe.preference.CategoryPrefs;
import com.teaera.teaeracafe.preference.LocationPrefs;
import com.teaera.teaeracafe.preference.PromotedMenuPrefs;
import com.teaera.teaeracafe.preference.UserPrefs;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by admin on 01/10/2017.
 */

public class FacebookUtils {

    private static final String TAG = FacebookUtils.class.getSimpleName();
    public static CallbackManager callbackManager;
    public static String email, firstname, lastname, name;

    public static void initFacebook(Context context) {

        FacebookSdk.sdkInitialize(context.getApplicationContext());
        AppEventsLogger.activateApp(context);

    }

    public static void logKeyHash(Context context) {
        PackageInfo info;
        try {
            info = context.getPackageManager().getPackageInfo(context.getApplicationContext().getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hash = new String(Base64.encode(md.digest(), 0));
                Log.d("for facebook hash key", hash);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("name not found", e.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("no such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("exception", e.toString());
        }
    }

    public static void setFacebookCallback(final BaseActivity activity, LoginButton loginButton) {
        callbackManager = CallbackManager.Factory.create();
        loginButton.setReadPermissions(Arrays.asList("user_friends", "email")); // "publish_actions", "email"
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                getFacbookProfileData(activity, loginResult);
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "onError");
            }
        });
    }

    private static void getFacbookProfileData(final BaseActivity activity, final LoginResult loginResult) {
        GraphRequest request = GraphRequest.newMeRequest(
                loginResult.getAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {


                        try {

                            name = object.getString("name");
                            String[] separated = name.split(" ");
                            firstname = separated[0];
                            lastname = separated[1];
                            try {
                                email = object.getString("email");

                            } catch (Exception e) {
                                email = " ";
                            }

                            if (email.isEmpty() || email.matches(" ")) {

                                Toast.makeText(activity, "No email id is associated with your facebook account", Toast.LENGTH_LONG).show();
                                final Dialog dialog = new Dialog(activity);
                                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                dialog.setCancelable(false);
                                dialog.setContentView(R.layout.dialog_facebook_email);
                                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                                final EditText edit_txt_facebook = dialog.findViewById(R.id.edit_txt_facebook);
                                Button confirm = dialog.findViewById(R.id.buttonConfirm);
                                Button buttonCancel = dialog.findViewById(R.id.buttonCancel);

                                buttonCancel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                    }
                                });

                                confirm.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (edit_txt_facebook.getText().toString().isEmpty()) {
                                            DialogUtils.showDialog(activity, "Oops", "Please add email id", null, null);
                                        } else {
                                            activity.showLoader(R.string.empty);

                                            Application.getServerApi().socialLogin(new SocialLoginRequest(firstname, lastname, edit_txt_facebook.getText().toString(), loginResult.getAccessToken().getToken())).enqueue(new Callback<UserResponse>() {

                                                @Override
                                                public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                                                    activity.hideLoader();
                                                    if (response.body().isError()) {
                                                        DialogUtils.showDialog(activity, "Error", response.body().getMessage(), null, null);
                                                    } else {
                                                        UserPrefs.saveUserInfo(activity, response.body().getUser());
                                                        UserPrefs.setLoggedIn(activity, true);
                                                        UserPrefs.setFBLogged(activity, true);
                                                        LocationPrefs.setLocations(activity, response.body().getLocations());
                                                        CategoryPrefs.setCategories(activity, response.body().getCategories());
                                                        PromotedMenuPrefs.setPromotedMenu(activity, response.body().getPromoted());

                                                        Intent intent = new Intent(activity, MainActivity.class);
                                                        activity.startActivity(intent);
                                                        activity.overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                                                        activity.finish();
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<UserResponse> call, Throwable t) {
                                                    activity.hideLoader();
                                                    if (t.getLocalizedMessage() != null) {
                                                        Log.d("Social Login", t.getLocalizedMessage());
                                                    } else {
                                                        Log.d("Social Login", "Unknown error");
                                                    }
                                                }
                                            });
                                            dialog.dismiss();
                                        }

                                    }
                                });
                                dialog.show();
                            } else {
                                Application.getServerApi().socialLogin(new SocialLoginRequest(firstname, lastname, email, loginResult.getAccessToken().getToken())).enqueue(new Callback<UserResponse>() {

                                    @Override
                                    public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                                        activity.hideLoader();
                                        if (response.body().isError()) {
                                            DialogUtils.showDialog(activity, "Error", response.body().getMessage(), null, null);
                                        } else {
                                            UserPrefs.saveUserInfo(activity, response.body().getUser());
                                            UserPrefs.setLoggedIn(activity, true);
                                            UserPrefs.setFBLogged(activity, true);
                                            LocationPrefs.setLocations(activity, response.body().getLocations());
                                            CategoryPrefs.setCategories(activity, response.body().getCategories());
                                            PromotedMenuPrefs.setPromotedMenu(activity, response.body().getPromoted());

                                            Intent intent = new Intent(activity, MainActivity.class);
                                            activity.startActivity(intent);
                                            activity.overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                                            activity.finish();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<UserResponse> call, Throwable t) {
                                        activity.hideLoader();
                                        if (t.getLocalizedMessage() != null) {
                                            Log.d("Social Login", t.getLocalizedMessage());
                                        } else {
                                            Log.d("Social Login", "Unknown error");
                                        }
                                    }
                                });

                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,gender,birthday");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public static void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (FacebookUtils.callbackManager != null) {
            FacebookUtils.callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    public static void logOut() {
        LoginManager.getInstance().logOut();
    }


}
