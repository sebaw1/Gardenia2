package com.gardenia.domain.gardenia2;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import androidx.core.app.NotificationCompat;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d("NEW_TOKEN",s);
        sendRegistrationToServer(s);
    }
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        sendNotification(remoteMessage.getData().get("title"),remoteMessage.getData().get("body"));

    }
    private void sendRegistrationToServer(String token) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("Settings");
        // then store your token ID
        ref.child("tokenMessaging").setValue(token);
    }


    private void sendNotification(String messageTitle,String messageBody) {
    Intent _intent = new Intent(this, MainActivity.class);


    NotificationManager mNotificationManager =(NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);

    PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
            _intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "M_CH_ID")
            .setSmallIcon(R.drawable.ic_stat_ic_notification)
                      .setContentTitle(messageTitle)
                      .setContentText(messageBody)
                .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_MAX)
        .setFullScreenIntent(contentIntent, true);

        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);


mNotificationManager.notify(0, builder.build());
    }



}
