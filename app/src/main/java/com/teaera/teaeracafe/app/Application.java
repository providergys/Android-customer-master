package com.teaera.teaeracafe.app;

import com.teaera.teaeracafe.net.ServerAPI;
import com.teaera.teaeracafe.utils.FontsOverride;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by admin on 24/07/2017.
 */

public class Application extends android.app.Application {

    private static Retrofit retrofit;
    private static ServerAPI serverApi;
    private static int location = 0;


    @Override
    public void onCreate() {
        super.onCreate();
        FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/Montserrat-Light.otf");
        FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/Montserrat-Regular.otf");
        FontsOverride.setDefaultFont(this, "SERIF", "fonts/Montserrat-Bold.otf");

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        retrofit = new Retrofit.Builder()
                .baseUrl(ServerAPI.BASE_URL)
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        serverApi = retrofit.create(ServerAPI.class);
    }

    public static ServerAPI getServerApi() {
        return serverApi;
    }

    public static int getLocation() {
        return location;
    }

    public static void setLocation(int location) {
        Application.location = location;
    }
}
