package com.azizah.msocial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class Profil extends AppCompatActivity {
FirebaseAuth firebaseauth;
TextView tv;
ActionBar actionBar;
String mUID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);
        actionBar = getSupportActionBar();

        firebaseauth = FirebaseAuth.getInstance();

        BottomNavigationView navigationView = findViewById(R.id.navbt);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        DashFragment fg1 = new DashFragment();
        FragmentTransaction fr1 = getSupportFragmentManager().beginTransaction();
        fr1.replace(R.id.layar, fg1, "");
        fr1.commit();

        checkuser();


    }

    @Override
    protected void onResume() {
        checkuser();
        super.onResume();
    }

    public void updateToken(String token){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Tokenclass mtoken = new Tokenclass(token);
        ref.child(mUID).setValue(mtoken);
    }


    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                    switch (menuItem.getItemId()){
                        case R.id.navhome:
                            actionBar.setTitle("Home");
                            DashFragment fg1 = new DashFragment();
                            FragmentTransaction fr1 = getSupportFragmentManager().beginTransaction();
                            fr1.replace(R.id.layar, fg1, "");
                            fr1.commit();

                            return true;

                        case R.id.navprof:
                            actionBar.setTitle("Akun Saya");
                            ProfFragment fg2 = new ProfFragment();
                            FragmentTransaction fr2 = getSupportFragmentManager().beginTransaction();
                            fr2.replace(R.id.layar, fg2, "");
                            fr2.commit();
                            return true;

                        case R.id.navpengg:
                            actionBar.setTitle("Pengguna");
                            UserFragment fg3 = new UserFragment();
                            FragmentTransaction fr3 = getSupportFragmentManager().beginTransaction();
                            fr3.replace(R.id.layar, fg3, "");
                            fr3.commit();
                            return true;

                        case R.id.navcht:
                            actionBar.setTitle("Pesan");
                            Chatlist fg4 = new Chatlist();
                            FragmentTransaction fr4 = getSupportFragmentManager().beginTransaction();
                            fr4.replace(R.id.layar, fg4, "");
                            fr4.commit();
                            return true;

                        case R.id.navaktif:
                            actionBar.setTitle("Aktivitas");
                            AktivitasFragment fg5 = new AktivitasFragment();
                            FragmentTransaction fr5 = getSupportFragmentManager().beginTransaction();
                            fr5.replace(R.id.layar, fg5, "");
                            fr5.commit();
                            return true;
                    }

                    return false;
                }
            };

    private void checkuser(){

        FirebaseUser user = firebaseauth.getCurrentUser();
        if (user != null){
            mUID = user.getUid();

            SharedPreferences shr = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = shr.edit();
            editor.putString("Current_USERID", mUID);
            editor.apply();

            updateToken(FirebaseInstanceId.getInstance().getToken());

        }

        else{
            startActivity(new Intent(Profil.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected  void onStart(){
        checkuser();
        super.onStart();
    }



}
