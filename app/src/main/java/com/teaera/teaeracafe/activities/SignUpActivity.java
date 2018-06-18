package com.teaera.teaeracafe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.teaera.teaeracafe.R;
import com.teaera.teaeracafe.app.Application;
import com.teaera.teaeracafe.net.Model.PromotedMenuInfo;
import com.teaera.teaeracafe.net.Request.RegisterRequest;
import com.teaera.teaeracafe.net.Response.UserResponse;
import com.teaera.teaeracafe.preference.CategoryPrefs;
import com.teaera.teaeracafe.preference.LocationPrefs;
import com.teaera.teaeracafe.preference.PromotedMenuPrefs;
import com.teaera.teaeracafe.preference.UserPrefs;
import com.teaera.teaeracafe.utils.DialogUtils;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends BaseActivity implements View.OnClickListener{

    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);


        ImageButton signUpImageButton = (ImageButton) findViewById(R.id.signUpImageButton);
        signUpImageButton.setOnClickListener(this);

        ImageButton backImageButton = (ImageButton) findViewById(R.id.backImageButton);
        backImageButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.signUpImageButton:
                register();
                break;

            case R.id.backImageButton:
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                finish();
                break;
        }
    }

    private void register() {

        String firstname = firstNameEditText.getText().toString().trim();
        String lastname = lastNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        if (firstname.isEmpty()) {
            DialogUtils.showDialog(this, "Error", getString(R.string.enter_firstname), null, null);
            return;
        }

        if (!firstname.matches("[a-zA-Z ]+")) {
            DialogUtils.showDialog(this, "Error", getString(R.string.enter_first_speical_characters), null, null);
            return;
        }

        if (lastname.isEmpty()) {
            DialogUtils.showDialog(this, "Error", getString(R.string.enter_lastname), null, null);
            return;
        }

        if (!lastname.matches("[a-zA-Z ]+")) {
            DialogUtils.showDialog(this, "Error", getString(R.string.enter_last_speical_characters), null, null);
            return;
        }

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

        Application.getServerApi().register(new RegisterRequest(firstname, lastname, email, password)).enqueue(new Callback<UserResponse>(){

            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                hideLoader();
                if (response.body().isError()) {
                    DialogUtils.showDialog(SignUpActivity.this, "Error", response.body().getMessage(), null, null);
                } else {
                    UserPrefs.saveUserInfo(SignUpActivity.this, response.body().getUser());
                    UserPrefs.setLoggedIn(SignUpActivity.this, true);
                    UserPrefs.setFBLogged(SignUpActivity.this, false);
                    LocationPrefs.setLocations(SignUpActivity.this, response.body().getLocations());
                    CategoryPrefs.setCategories(SignUpActivity.this, response.body().getCategories());
                    PromotedMenuPrefs.setPromotedMenu(SignUpActivity.this, response.body().getPromoted());

                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                hideLoader();
                if (t.getLocalizedMessage() != null) {
                    Log.d("Register", t.getLocalizedMessage());
                } else {
                    Log.d("Register", "Unknown error");
                }
            }
        });

    }

}
