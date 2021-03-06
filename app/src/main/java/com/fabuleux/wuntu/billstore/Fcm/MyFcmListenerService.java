package com.fabuleux.wuntu.billstore.Fcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.util.Log;

import com.fabuleux.wuntu.billstore.Activity.MainActivity;
import com.fabuleux.wuntu.billstore.Manager.SessionManager;
import com.fabuleux.wuntu.billstore.Network.CommonRequest;
import com.fabuleux.wuntu.billstore.Pojos.NotificationPojo;
import com.fabuleux.wuntu.billstore.R;
import com.freshchat.consumer.sdk.Freshchat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Created by Dell on 6/8/16.
 */

public class MyFcmListenerService extends FirebaseMessagingService {


    private FirebaseFirestore db;
    FirebaseUser firebaseUser;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("FCM TAG","MESSAGE RECIEVED");
//        if (Freshchat.isFreshchatNotification(remoteMessage)) {
//
//            Freshchat.getInstance(this).handleFcmMessage(remoteMessage);
//        }
//        else
//        {
            Map data = remoteMessage.getData();
            String message = data.containsKey("message") ? data.get("message").toString() : "" ;
            String title = data.containsKey("title") ? data.get("title").toString() : "" ;
            String time = data.containsKey("time") ? data.get("time").toString() : "" ;

            db = FirebaseFirestore.getInstance();
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            firebaseUser = firebaseAuth.getCurrentUser();

            CollectionReference collectionReference = db.collection("Users").document(firebaseUser.getUid()).collection("Notifications");
            final DocumentReference documentReference = collectionReference.document(time);

            NotificationPojo notificationPojo = new NotificationPojo(title,message,time);
            documentReference.set(notificationPojo);

            sendNotification(message,title);
//        }

    }

    public void sendNotification(String message, String title) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        //Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;

        NotificationCompat.Builder builder = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel("ID", "Name", importance);
            notificationManager.createNotificationChannel(notificationChannel);
            builder = new NotificationCompat.Builder(this, notificationChannel.getId());
        } else {
            builder = new NotificationCompat.Builder(this);
        }
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setColor(Color.parseColor("#0f9595"))
                .setContentIntent(pendingIntent);

//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0 /* ID of notification */, builder.build());

    }

    @Override
    public void onNewToken(@NonNull String refreshedToken) {
        assert refreshedToken != null;
        Freshchat.getInstance(this).setPushRegistrationToken(refreshedToken);
        registerDevice(refreshedToken);
        super.onNewToken(refreshedToken);
    }

    private void registerDevice(String token)
    {
        FirebaseUser firebaseUser;
        SessionManager sessionManager = new SessionManager(MyFcmListenerService.this);
        sessionManager.setDeviceToken(token);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        Freshchat.getInstance(this).setPushRegistrationToken(token);
        if (firebaseUser != null)
        {
            CommonRequest.getInstance(this).sendDeviceToken(firebaseUser.getUid());
        }
    }
}