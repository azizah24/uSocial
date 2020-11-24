package com.azizah.msocial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class PengaturanAct extends AppCompatActivity {

    SwitchCompat notifsw;
    SharedPreferences sha;
    SharedPreferences.Editor edit;
    private static final String TOPIC_POST_NOTIFICATION = "POST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pengaturan);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Settings");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        notifsw = findViewById(R.id.notifikasitom);
        sha = getSharedPreferences("Notification_SP", MODE_PRIVATE);
        boolean disetujui = sha.getBoolean(""+TOPIC_POST_NOTIFICATION, false);

        if(disetujui){
            notifsw.setChecked(true);
        }
        else{
            notifsw.setChecked(false);
        }

        notifsw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                edit = sha.edit();
                edit.putBoolean(""+TOPIC_POST_NOTIFICATION, b);
                edit.apply();

                if(b){
                    subscribenotif();
                }
                else{
                    unsubscribepostnotif();
                }
                
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void unsubscribepostnotif() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(""+TOPIC_POST_NOTIFICATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String pesan = "Kamu Akan tidak akan menerima notifikasi post";
                        if(!task.isSuccessful()){
                            pesan = "Gagal Mematikan Notifikasi";

                        }
                        Toast.makeText(PengaturanAct.this, pesan, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void subscribenotif() {

        FirebaseMessaging.getInstance().subscribeToTopic(""+TOPIC_POST_NOTIFICATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String pesan = "Kamu Akan menerima notifikasi post";
                        if(!task.isSuccessful()){
                            pesan = "Gagal Menyalakan Notifikasi";

                        }
                        Toast.makeText(PengaturanAct.this, pesan, Toast.LENGTH_SHORT).show();
                    }
                });

    }
}
