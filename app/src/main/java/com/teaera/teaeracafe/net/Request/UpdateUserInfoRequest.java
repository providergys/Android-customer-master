package com.teaera.teaeracafe.net.Request;

/**
 * Created by admin on 26/10/2017.
 */

public class UpdateUserInfoRequest {
    final String userId;
    final String firstname;
    final String lastname;
    final String email;

    public UpdateUserInfoRequest(String userId, String firstname, String lastname, String email) {
        this.userId     = userId;
        this.firstname  = firstname;
        this.lastname   = lastname;
        this.email      = email;
    }
}
