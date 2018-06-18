package com.teaera.teaeracafe.utils;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.teaera.teaeracafe.app.Application;
import com.teaera.teaeracafe.net.Request.UpdateTokenRequest;
import com.teaera.teaeracafe.net.Response.BaseResponse;
import com.teaera.teaeracafe.preference.UserPrefs;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by admin on 19/07/2017.
 */

public class FirebaseIDService extends FirebaseInstanceIdService {
    private static final String TAG = "FirebaseIDService";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // TODO: Implement this method to send any registration to your app's servers.
        sendRegistrationToServer(refreshedToken);
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
//        Application.getServerApi().updateDeviceToken(new UpdateTokenRequest(UserPrefs.getUserInfo(this).getId(), token)).enqueue(new Callback<BaseResponse>(){
//
//            @Override
//            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
//                if (response.body().isError()) {
//                    Log.d("Update Device Token", response.message());
//                } else {
//                    Log.d("Update Device Token", response.message());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<BaseResponse> call, Throwable t) {
//                if (t.getLocalizedMessage() != null) {
//                    Log.d("Update Device Token", t.getLocalizedMessage());
//                } else {
//                    Log.d("Update Device Token", "Unknown error");
//                }
//            }
//        });
    }
}