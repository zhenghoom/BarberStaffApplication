package com.example.barberstaffapplication.Retrofit;

import com.example.barberstaffapplication.Model.FCMResponse;
import com.example.barberstaffapplication.Model.FCMSendData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAmm_5IG4:APA91bGseTkcaYWg7fC3_64z26sS5N4lqWbw-p5vpvkXV4VtROV3U3k8gXhfRjsx9DAaWEd0vAksqlVOwlpUl271H8V9Kt7alUifBeqDUttVyT_1-Bg2ga1nAyCS1xuVE_XpXIPAR2uW"
    })
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);
}
