package com.teaera.teaeracafe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.facebook.login.widget.LoginButton;
import com.teaera.teaeracafe.R;
import com.teaera.teaeracafe.app.Application;
import com.teaera.teaeracafe.net.Request.LoginRequest;
import com.teaera.teaeracafe.net.Response.UserResponse;
import com.teaera.teaeracafe.preference.CategoryPrefs;
import com.teaera.teaeracafe.preference.LocationPrefs;
import com.teaera.teaeracafe.preference.PromotedMenuPrefs;
import com.teaera.teaeracafe.preference.UserPrefs;
import com.teaera.teaeracafe.utils.DialogUtils;
import com.teaera.teaeracafe.utils.FacebookUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInActivity extends BaseActivity implements View.OnClickListener {

    private EditText emailEditText;
    private EditText passwordEditText;
    private LoginButton facebookButton;
    private ImageButton facebookImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookUtils.initFacebook(this);

        setContentView(R.layout.activity_sign_in);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        ImageButton signIn_button = findViewById(R.id.signIn_button);
        signIn_button.setOnClickListener(this);

        facebookImageButton = findViewById(R.id.imageButton);
        facebookImageButton.setOnClickListener(this);

        Button signUpButton = findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(this);

        facebookButton = findViewById(R.id.facebookButton);

        Button forgotButton = findViewById(R.id.forgot_button);
        forgotButton.setOnClickListener(this);

        FacebookUtils.setFacebookCallback(this, facebookButton);
        FacebookUtils.logOut();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.signIn_button:
                loginWithEmail();
                break;

            case R.id.imageButton:
                facebookButton.performClick();
                break;

            case R.id.signUpButton:
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                finish();
                break;
            case R.id.forgot_button:
                intent = new Intent(SignInActivity.this, ForgotActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                finish();
                break;
        }
    }

    private void loginWithEmail() {

        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (email.isEmpty()) {
            DialogUtils.showDialog(this, "Error", getString(R.string.enter_email), null, null);
            return;
        }

        if (password.isEmpty()) {
            DialogUtils.showDialog(this, "Error", getString(R.string.enter_password), null, null);
            return;
        }

        if (email.matches("^\\s*$")) {
            DialogUtils.showDialog(this, "Error", getString(R.string.enter_email_space), null, null);
            return;
        }

        if (password.matches("^\\s*$")) {
            DialogUtils.showDialog(this, "Error", getString(R.string.enter_pass_space), null, null);
            return;
        }

        if (!isNetworkConnected(this)) {
            DialogUtils.showDialog(this, "Error", getString(R.string.internet_error), null, null);
            return;
        }

        showLoader(R.string.empty);

        Application.getServerApi().login(new LoginRequest(email, password)).enqueue(new Callback<UserResponse>() {

            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                hideLoader();
                if (response.body().isError()) {
                    DialogUtils.showDialog(SignInActivity.this, "Error", response.body().getMessage(), null, null);
                } else {
                    UserPrefs.saveUserInfo(SignInActivity.this, response.body().getUser());
                    UserPrefs.setLoggedIn(SignInActivity.this, true);
                    UserPrefs.setFBLogged(SignInActivity.this, false);
                    LocationPrefs.setLocations(SignInActivity.this, response.body().getLocations());
                    CategoryPrefs.setCategories(SignInActivity.this, response.body().getCategories());
                    PromotedMenuPrefs.setPromotedMenu(SignInActivity.this, response.body().getPromoted());

                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                hideLoader();
                t.printStackTrace();
                if (t.getLocalizedMessage() != null) {
                    Log.d("Login", t.getLocalizedMessage());
                } else {
                    Log.d("Login", "Unknown error");
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        FacebookUtils.onActivityResult(requestCode, resultCode, data);
    }
}
