package com.azizah.msocial;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAct extends AppCompatActivity {


    RecyclerView recyclerView;
    CircleImageView imgprof;
    ImageView blocked;
    TextView namacht, statuson;
    EditText pesanet;
    ImageButton sendbtn, attachbtn;
    FirebaseAuth firebaseAuth;
    String hisUid;
    String myUid, hisImage;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReferenceuser;

    ValueEventListener seenlist;
    DatabaseReference databaseReferenceseen;
    FirebaseUser fuser;

    List<Datachat> chatlist;
    Adapterchat adapterchat;

    private RequestQueue requestQueue;
    private boolean notify = false;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    String[] campermiss;
    String[] storagepermis;
    Uri image_uri = null;
    boolean isBlocked = false;
    String notificationType;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbr);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        recyclerView = findViewById(R.id.cht_recycler);
        imgprof = findViewById(R.id.profimg);
        namacht = findViewById(R.id.namacht);
        statuson = findViewById(R.id.statuson);
        pesanet = findViewById(R.id.pesanet);
        sendbtn = findViewById(R.id.sendbtn);
        attachbtn = findViewById(R.id.btnattach);

        campermiss = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagepermis = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        requestQueue = Volley.newRequestQueue(getApplicationContext());

        firebaseAuth = FirebaseAuth.getInstance();
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        Intent chatt = getIntent();
        hisUid = chatt.getStringExtra("hisUid");
        hisImage = chatt.getStringExtra("image");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReferenceuser = firebaseDatabase.getReference("Users");

        Query queryu = databaseReferenceuser.orderByChild("uid").equalTo(hisUid);
        queryu.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot ds: dataSnapshot.getChildren()){

                    String name = ""+ds.child("name").getValue();
                    hisImage = ""+ds.child("image").getValue();
                    String onstatus = ""+ ds.child("onlineST").getValue();
                    String ketikan = ""+ ds.child("mengetik").getValue();

                    if(ketikan.equals(myUid)){
                        statuson.setText("Sedang Mengetik...");
                    }
                    else{
                        if(onstatus.equals("online")){
                            statuson.setText(onstatus);
                        }

                        else{
                            statuson.setText("");

                        }
                    }



                    namacht.setText(name);
                    try{
                        Picasso.get().load(hisImage).placeholder(R.drawable.ic_usr_name).into(imgprof);
                    }
                    catch (Exception e){


                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        //blocked.setOnClickListener(new View.OnClickListener() {
           // @Override
            //public void onClick(View view) {
              //  if(isBlocked){
                //    unBlockuser();
                //}
                //else{
                  //  blockuser();
                //}
            //}
        //});

        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                notify = true;

                String pesan = pesanet.getText().toString().trim();
                if(TextUtils.isEmpty(pesan)){

                    Toast.makeText(ChatAct.this, "Tidak dapat mengirim pesan kosong", Toast.LENGTH_SHORT).show();

                }
                else{
                    kirimpesan(pesan);
                }
                pesanet.setText("");
            }
        });

        attachbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showimagepick();
            }
        });

        bacapesan();

        dilihat();
        //checisblocked();

        pesanet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(charSequence.toString().trim().length() ==0){
                    cekketik("tidakada");
                }
                else{
                    cekketik(hisUid);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void blockornot(){
        notify = true;

       String pesan = pesanet.getText().toString().trim();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
       databaseReference.child(hisUid).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
             .addValueEventListener(new ValueEventListener() {
                @Override
              public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               for(DataSnapshot ds: dataSnapshot.getChildren()){
                           if(ds.exists()){
                                Toast.makeText(ChatAct.this, "Anda telah diblokir oleh pengguna ini, tidak dapat mengirim pesan", Toast.LENGTH_SHORT).show();
                                return;
                          }
                      }

                      kirimpesan(pesan);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


   // private void checisblocked() {
       // DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //ref.child(firebaseAuth.getUid()).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
          //      .addValueEventListener(new ValueEventListener() {
            //        @Override
              //      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //        for(DataSnapshot ds: dataSnapshot.getChildren()){
                  //          if(ds.exists()){
                    //            blocked.setImageResource(R.drawable.ic_block);
                      //          isBlocked = true;
                        //    }
                        //}

                    //}

                    //@Override
                    //public void onCancelled(@NonNull DatabaseError databaseError) {

                    //}
                //});
    //}

    //private void unBlockuser() {

      //  DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
          //      .addListenerForSingleValueEvent(new ValueEventListener() {
            //        @Override
              //      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //        for(DataSnapshot ds: dataSnapshot.getChildren()){
                  //          if(ds.exists()){
                    //            ds.getRef().removeValue()
                      //                  .addOnSuccessListener(new OnSuccessListener<Void>() {
                        //                    @Override
                          //                  public void onSuccess(Void aVoid) {

                            //                    Toast.makeText(ChatAct.this, "Blokir dibuka...", Toast.LENGTH_SHORT).show();
                              //                  blocked.setImageResource(R.drawable.ic_noblock);
                                //            }
                                  //      })
                                    //    .addOnFailureListener(new OnFailureListener() {
                                      //      @Override
                                        //    public void onFailure(@NonNull Exception e) {

                                                //Toast.makeText(ChatAct.this, "Gagal: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            //}
                                        //});
                            //}
                        //}
                    //}

                    //@Override
                    //public void onCancelled(@NonNull DatabaseError databaseError) {

                    //}
                //});

    //}

    private void blockuser() {

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", hisUid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").child(hisUid).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(ChatAct.this, "Pengguna Diblokir...", Toast.LENGTH_SHORT).show();
                        blocked.setImageResource(R.drawable.ic_block);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(ChatAct.this, "Gagal: "+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void showimagepick() {
        String[] options = {"Kamera", "Galeri"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih gambar dari");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if(i==0){

                    if(!checkcamper()){
                        requestcamper();
                    }
                    else{
                        Pilihcam();
                    }
                }
                if(i==1){
                    if(!checkstorageper()){
                        requeststorper();
                    }
                    else {
                        pilihgal();
                    }
                }
            }
        });

        builder.create().show();
    }

    private void pilihgal() {
        Intent gall = new Intent(Intent.ACTION_PICK);
        gall.setType("image/*");
        startActivityForResult(gall, IMAGE_PICK_GALLERY_CODE);

    }

    private void Pilihcam() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent camerai = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camerai.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(camerai, IMAGE_PICK_CAMERA_CODE);

    }

    private boolean checkstorageper(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requeststorper(){
        ActivityCompat.requestPermissions(this, storagepermis, STORAGE_REQUEST_CODE);
    }

    private boolean checkcamper(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void requestcamper(){
        ActivityCompat.requestPermissions(this, campermiss, CAMERA_REQUEST_CODE);
    }

    private void dilihat() {
        databaseReferenceseen = FirebaseDatabase.getInstance().getReference("Chats");
        seenlist = databaseReferenceseen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    Datachat chat = ds.getValue(Datachat.class);
                    if(chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)){
                        HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                        hasSeenHashMap.put("seen", true);
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void bacapesan() {
        chatlist = new ArrayList<>();
        DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("Chats");
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatlist.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    Datachat chat = ds.getValue(Datachat.class);
                    if(chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid) ||
                    chat.getReceiver().equals(hisUid) && chat.getSender().equals(myUid)){
                        chatlist.add(chat);
                    }

                    adapterchat = new Adapterchat(ChatAct.this, chatlist, hisImage);
                    adapterchat.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterchat);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void kirimpesan(final String pesan) {
        DatabaseReference databaseReference =FirebaseDatabase.getInstance().getReference();

        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("receiver", hisUid);
        hashMap.put("message", pesan);
        hashMap.put("seen", false);
        hashMap.put("timestamp", timestamp);
        hashMap.put("type", "text");
        hashMap.put("notificationType", "ChatNotification");
        databaseReference.child("Chats").push().setValue(hashMap);



        final DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                DataUser user = dataSnapshot.getValue(DataUser.class);


                if(notify){
                    sendnotif(hisUid, user.getName(), pesan, notificationType);
                }
                notify = false;

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference chatref = FirebaseDatabase.getInstance().getReference("Chatlist").child(myUid).child(hisUid);

        chatref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!dataSnapshot.exists()){
                    chatref.child("id").setValue(hisUid);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference chatref2 = FirebaseDatabase.getInstance().getReference("Chatlist").child(hisUid).child(myUid);
        chatref2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!dataSnapshot.exists()){
                    chatref2.child("id").setValue(myUid);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void Sendimagepesan(Uri image_uri) throws IOException {

        notify = true;

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Mengirimkan gambar ...");
        pd.show();

        String timestamp = ""+System.currentTimeMillis();

        String filenamatmpt = "ChatImages/"+"post_"+timestamp;

        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_uri);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] data = bos.toByteArray();
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filenamatmpt);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        pd.dismiss();

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());
                        String downuri = uriTask.getResult().toString();

                        if(uriTask.isSuccessful()){

                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender", myUid);
                            hashMap.put("receiver", hisUid);
                            hashMap.put("message", downuri);
                            hashMap.put("seen", false);
                            hashMap.put("timestamp", timestamp);
                            hashMap.put("type", "image");
                            hashMap.put("notificationType", "ChatNotification");


                            databaseReference.child("Chats").push().setValue(hashMap);

                            DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
                            databaseReference1.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    DataUser dataUser = dataSnapshot.getValue(DataUser.class);
                                    if(notify){
                                        sendnotif(hisUid, dataUser.getName(), "Mengirimkan Gambar...", "ChatNotification");
                                    }
                                    notify = false;

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            DatabaseReference chatref = FirebaseDatabase.getInstance().getReference("Chatlist").child(myUid).child(hisUid);

                            chatref.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if(!dataSnapshot.exists()){
                                        chatref.child("id").setValue(hisUid);
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            DatabaseReference chatref2 = FirebaseDatabase.getInstance().getReference("Chatlist").child(hisUid).child(myUid);
                            chatref2.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if(!dataSnapshot.exists()){
                                        chatref2.child("id").setValue(myUid);
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });


    }

    private void sendnotif(final String hisUid, final String name, final String pesan, final String notificationType) {

        DatabaseReference alltoken = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = alltoken.orderByKey().equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    Tokenclass tokenclass = ds.getValue(Tokenclass.class);
                    Datanotif data = new Datanotif(""+myUid, "Pesan Baru", ""+name+": "+pesan, R.drawable.ic_stat_name, ""+hisUid, "ChatNotification");
                    Notifikasi notification = new Notifikasi(""+myUid,
                            "Pesan Baru",
                            ""+name +": "+ pesan,
                             R.drawable.ic_stat_name,
                            ""+hisUid,
                            "ChatNotification");
                    Pengirim pengirim = new Pengirim(data, tokenclass.getToken(), notification);

                    try{
                        String Json = new Gson().toJson(pengirim);
                        Log.e("JSON_Body", "json"+ Json);
                        JSONObject senderobj = new JSONObject(Json);
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderobj,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {

                                        Log.e("JSON_RESPONSE", "onResponse: "+ response.toString());
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("JSON_RESPONSE", "onResponse: "+ error.toString());

                            }
                        }){
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {

                                Map<String, String> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", "key=AAx");
                                return headers;
                            }
                        };

                        requestQueue.add(jsonObjectRequest);

                    } catch (JSONException e){
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



    private void checkuser(){

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){

            myUid = user.getUid();

        }

        else{
            startActivity(new Intent(this, MainActivity.class));
           finish();
        }
    }



    private void  checkOnline(String status){
        DatabaseReference dbrf = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineST", status);

        dbrf.updateChildren(hashMap);
    }

    private void  cekketik(String mengetik){
        DatabaseReference dbrf = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("mengetik", mengetik);

        dbrf.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        checkuser();
        checkOnline("online");
        super.onStart();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    @Override
    protected void onPause() {

        super.onPause();
        checkOnline("");
        cekketik("tidakada");
        databaseReferenceseen.removeEventListener(seenlist);

    }

    @Override
    protected void onResume() {
        checkOnline("online");
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout, menu);
        menu.findItem(R.id.searchin).setVisible(false);
        menu.findItem(R.id.create).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public  boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id==R.id.logout){
            firebaseAuth.signOut();
            checkuser();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean camacc = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storacc = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(camacc && storacc){
                        Pilihcam();
                    }
                    else{
                        Toast.makeText(this, "Kamera & Galeri membutuhkan Izin", Toast.LENGTH_SHORT).show();
                    }
                }
                else{

                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean storacc = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if(storacc){
                        pilihgal();
                    }
                    else{
                        Toast.makeText(this, "Galeri membutuhkan Izin", Toast.LENGTH_SHORT).show();
                    }
                }
                else{

                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == RESULT_OK){

            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                image_uri = data.getData();

                try {
                    Sendimagepesan(image_uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            if (requestCode == IMAGE_PICK_CAMERA_CODE){
                try {
                    Sendimagepesan(image_uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }



}
