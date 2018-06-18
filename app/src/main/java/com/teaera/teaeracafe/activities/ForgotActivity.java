package com.teaera.teaeracafe.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.teaera.teaeracafe.R;
import com.teaera.teaeracafe.app.Application;
import com.teaera.teaeracafe.net.Request.ForgotPasswordRequest;
import com.teaera.teaeracafe.net.Response.BaseResponse;
import com.teaera.teaeracafe.utils.DialogUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotActivity extends BaseActivity implements View.OnClickListener {

    private EditText emailEditText;
    public static final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    Pattern pattern = Pattern.compile( EMAIL_PATTERN );
    private Matcher matcher;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_forgot );


        emailEditText = findViewById( R.id.emailEditText );


        ImageButton forgotImageButton = findViewById( R.id.forgotImageButton );
        forgotImageButton.setOnClickListener( this );

        ImageButton backImageButton = findViewById( R.id.backImageButton );
        backImageButton.setOnClickListener( this );
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.forgotImageButton:

                email= emailEditText.getText().toString().trim();
                if (!email.isEmpty()) {
                    if (validateEmail( emailEditText.getText().toString() )) {
                        requestForgotPassword();
                    } else {
                        DialogUtils.showDialog( this, "Error", getString( R.string.valid_email ), null, null );

                    }
                } else {
                    DialogUtils.showDialog( this, "Error", getString( R.string.enter_email ), null, null );
                }

                break;

            case R.id.backImageButton:
                Intent intent = new Intent( ForgotActivity.this, SignInActivity.class );
                startActivity( intent );
                overridePendingTransition( R.anim.pull_in_left, R.anim.push_out_right );
                finish();
                break;
        }
    }

    private void requestForgotPassword() {

        showLoader( R.string.empty );
        Application.getServerApi().forgotPassword( new ForgotPasswordRequest( email ) ).enqueue( new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                hideLoader();
                if (response.body().isError()) {
                    DialogUtils.showDialog( ForgotActivity.this, "Error", response.body().getMessage(), null, null );
                } else {
                    emailEditText.setText( "" );
                    DialogUtils.showDialog( ForgotActivity.this, "Alert", "Please check your email. You will receive the link to reset the password.", null, null );
                }
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                hideLoader();
                if (t.getLocalizedMessage() != null) {
                    Log.d( "Forgot", t.getLocalizedMessage() );
                } else {
                    Log.d( "Forgot", "Unknown error" );
                }
            }
        } );
    }

    public boolean validateEmail(String email) {
        matcher = pattern.matcher( email );
        return matcher.matches();
    }
}
