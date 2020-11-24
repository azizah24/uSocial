package com.azizah.msocial;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

import static com.android.volley.VolleyLog.TAG;

public class FirebaseMessaging extends FirebaseMessagingService {

    private static final String ADMIN_CHANNEL_ID = "admin_channel";
    String myUid;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        SharedPreferences shr = getSharedPreferences("SP_USER", MODE_PRIVATE);
        String savedc = shr.getString("Current_USERID", "None");//

        String notificationType = remoteMessage.getData().get("notificationType");

        if (notificationType != null) {
            if (notificationType.equals("PostNotification")) {

                String sender = remoteMessage.getData().get("sender");
                String pId = remoteMessage.getData().get("pId");
                String pTitle = remoteMessage.getData().get("uName");
                String pDesc = remoteMessage.getData().get("pDesc");

                if (!sender.equals(savedc)) {

                    shownotifpost("" + pId, "" + pTitle + " " + "add new post", "" + pDesc);
                }
            } else if (notificationType.equals("ChatNotification")) {
                String sent = remoteMessage.getData().get("sent");
                String user = "" + remoteMessage.getData().get("user");
                String title = remoteMessage.getData().get("title");
                String body = remoteMessage.getData().get("body");
                String icon = remoteMessage.getData().get("icon");
                RemoteMessage.Notification notification = remoteMessage.getNotification();

                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser != null && sent != null && sent.equals(firebaseUser.getUid())) {
                    if (!savedc.equals(user)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Log.e("data notif1", user + "-" + "-" + title + "-" + body);
                            sendnotif(remoteMessage);

                        } else {
                            Log.e("data notif2", user + "-" + "-" + title + "-" + body);
                            sendnormal(remoteMessage);
                        }
                    }
                }
            }
        }
    }

    private void shownotifpost(String pId, String pTitle, String pDesc) {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationID = new Random().nextInt(100);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            aturnotifpost(notificationManager);
        }

        Intent intent = new Intent(this, Postdetail.class);
        intent.putExtra("postId", pId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Bitmap largeicon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_stat_name);


        Uri urinotif = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notifi = new NotificationCompat.Builder(this, "" + ADMIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setLargeIcon(largeicon)
                .setContentTitle(pTitle)
                .setContentText(pDesc)
                .setSound(urinotif)
                .setContentIntent(pendingIntent);

        notificationManager.notify(notificationID, notifi.build());

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void aturnotifpost(NotificationManager notificationManager) {

        CharSequence channelName = "New Notification";
        String channeldsc = "Device to device post notification";
        NotificationChannel adminchanel = new NotificationChannel(ADMIN_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        adminchanel.setDescription(channeldsc);
        adminchanel.enableLights(true);
        adminchanel.enableVibration(true);
        adminchanel.setLightColor(Color.RED);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminchanel);
        }
    }


    private void sendnormal(RemoteMessage remoteMessage) {

        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int a = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, ChatAct.class);
        Bundle bundle = new Bundle();
        bundle.putString("hisUid", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, a, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri soundur = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentText(body)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setSound(soundur)
                .setShowWhen(true)
                .setSmallIcon(Integer.parseInt(icon))
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        int j = 0;
        if (a > 0) {
            j = a;
        }

        notificationManager.notify(j, builder.build());

    }


    private void sendnotif(RemoteMessage remoteMessage) {
        String user = "" + remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");


        //RemoteMessage.Notification notification = remoteMessage.getNotification();
        int a = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, ChatAct.class);
        Bundle bundle = new Bundle();
        bundle.putString("hisUid", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, a, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri soundur = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notif notif = new Notif(this);
        Notification.Builder builder = notif.getnotif(title, body, pendingIntent, soundur, icon);

        int j = 0;
        if (a > 0) {
            j = a;
        }

        notif.getNotificationManager().notify(j, builder.build());
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {

            updateToken(s);
        }
    }

    private void updateToken(String s) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Tokenclass token = new Tokenclass(s);
        ref.child(user.getUid()).setValue(token);


    }
}

