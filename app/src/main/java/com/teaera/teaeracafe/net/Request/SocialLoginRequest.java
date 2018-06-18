package com.teaera.teaeracafe.net.Request;

import com.google.firebase.iid.FirebaseInstanceId;

/**
 * Created by admin on 29/08/2017.
 */

public class SocialLoginRequest {

    final String firstname;
    final String lastname;
    final String email;
    final String fb_token;
    final String device;
    final String token;

    public SocialLoginRequest(String firstname, String lastname, String email, String fbToken) {

        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.fb_token = fbToken;
        this.device = "android";
        this.token = FirebaseInstanceId.getInstance().getToken();
    }

}
