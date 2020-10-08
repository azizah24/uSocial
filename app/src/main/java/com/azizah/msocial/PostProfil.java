package com.azizah.msocial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.drm.DrmStore;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostProfil extends AppCompatActivity {
    RecyclerView postrv;
    List<Datapost> datapostList;
    Adapterpost adapterpost;
    String uid, uName, email;
    FirebaseAuth firebaseAuth;
    ImageView cover2;
    TextView nametv,biotv;
    CircleImageView ava;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_profil);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Profil");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        ava = findViewById(R.id.avaim);
        nametv = findViewById(R.id.namatv);
        biotv = findViewById(R.id.biotv);
        cover2 = findViewById(R.id.rela);
        postrv = findViewById(R.id.recyclerv_post);
        datapostList = new ArrayList<>();

        firebaseAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        uName = intent.getStringExtra("uName");

        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    String name = ""+ ds.child("name").getValue();
                    String email = ""+ ds.child("email").getValue();
                    String bio = ""+ ds.child("bio").getValue();
                    String image = ""+ ds.child("image").getValue();
                    String cover = ""+ds.child("cover").getValue();

                    nametv.setText(name);
                    biotv.setText(bio);
                    try {
                        Picasso.get().load(image).into(ava);
                    }
                    catch (Exception e){
                        Picasso.get().load(R.drawable.ic_cam_name).into(ava);
                    }

                    try {
                        Picasso.get().load(cover).into(cover2);
                    }
                    catch (Exception e){

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        checkuser();
        loadpostlain();

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void loadpostlain() {

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(PostProfil.this);

        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        postrv.setLayoutManager(linearLayoutManager);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = databaseReference.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                datapostList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Datapost datapost = ds.getValue(Datapost.class);

                    datapostList.add(datapost);

                    adapterpost = new Adapterpost(PostProfil.this, datapostList);
                    postrv.setAdapter(adapterpost);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(PostProfil.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void searchpostlain(String searchqueku) {

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(PostProfil.this);

        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        postrv.setLayoutManager(linearLayoutManager);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = databaseReference.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                datapostList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Datapost datapost = ds.getValue(Datapost.class);

                    if (datapost.getpTitle().toLowerCase().contains(searchqueku.toLowerCase()) ||
                            datapost.getpDesc().toLowerCase().contains(searchqueku.toLowerCase())){

                        datapostList.add(datapost);

                    }


                    adapterpost = new Adapterpost(PostProfil.this, datapostList);
                    postrv.setAdapter(adapterpost);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(PostProfil.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void checkuser(){

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){

        }

        else{
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout, menu);
        menu.findItem(R.id.create).setVisible(false);

        MenuItem item = menu.findItem(R.id.searchin);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if(!TextUtils.isEmpty(s)){
                    searchpostlain(s);
                }
                else{
                    loadpostlain();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                if(!TextUtils.isEmpty(s)){
                    searchpostlain(s);
                }
                else{
                    loadpostlain();
                }

                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.logout){
            firebaseAuth.signOut();
            checkuser();
        }


        return super.onOptionsItemSelected(item);
    }
}
