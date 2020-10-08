package com.azizah.msocial;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class Notif extends ContextWrapper {

    private static final String ID = "some_id";
    private static final String NAME = "uSocial";
    private NotificationManager notificationManager;

    public Notif(Context base) {
        super(base);
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.O){
            createChannel();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel notificationChannel = new NotificationChannel(ID, NAME, NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getNotificationManager().createNotificationChannel(notificationChannel);
    }

    public NotificationManager getNotificationManager(){
        if(notificationManager == null){
            notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public Notification.Builder getnotif (String title, String body,
                                          PendingIntent pendingIntent,
                                          Uri sounduri, String icon){
        return new Notification.Builder(getApplicationContext(), ID)
                .setContentText(body)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setSound(sounduri)
                .setShowWhen(true)
                .setSmallIcon(Integer.parseInt(icon))
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);
    }


}
