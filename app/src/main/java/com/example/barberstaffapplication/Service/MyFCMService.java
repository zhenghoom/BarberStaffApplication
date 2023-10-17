package com.example.barberstaffapplication.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;

import com.example.barberstaffapplication.Common.Common;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MyFCMService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        Common.updateToken(this,token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Common.showNotification(this,
                new Random().nextInt(),
                message.getData().get(Common.TITLE_KEY),
                message.getData().get(Common.CONTENT_KEY),
                null);
    }
}