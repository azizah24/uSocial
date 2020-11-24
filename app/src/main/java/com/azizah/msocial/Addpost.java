package com.azizah.msocial;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
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
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class Addpost extends AppCompatActivity {

    ActionBar actionBar;
    FirebaseAuth firebaseauth;
    EditText titleet, desket;
    Button postingbtn;
    ImageView imageViewtv;
    Uri image_uri = null;
    FirebaseUser fuser;
    DatabaseReference databaseReference;
    String name, uid, dp, email, myUid, hisUid, myEmail, myName, myDp;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    String[] campermiss;
    String[] storagepermis;
    ProgressDialog progressDialog;
    String editTitle, editDesc, editImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addpost);


        actionBar = getSupportActionBar();
        actionBar.setTitle("Add New Post");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);


        desket = findViewById(R.id.descet);
        postingbtn = findViewById(R.id.btnpost);
        imageViewtv = findViewById(R.id.imagepost);
        progressDialog = new ProgressDialog(this);

        firebaseauth = FirebaseAuth.getInstance();
        checkuser();
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent = getIntent();
        String updatekey = "" + intent.getStringExtra("key");
        String editpostid = "" + intent.getStringExtra("editPostId");

        if(updatekey.equals("editPost")){

            actionBar.setTitle("Update Post");
            postingbtn.setText("Update");
            loadpostdat(editpostid);
        }
        else{
            actionBar.setTitle("Add New Post");
            postingbtn.setText("Posting");
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        Query query = databaseReference.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    uid = "" + ds.child("uid").getValue();
                    name = ""+ ds.child("name").getValue();
                    dp = ""+ ds.child("image").getValue();
                    email = "" + ds.child("email").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        campermiss = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagepermis = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        checkuser();

        imageViewtv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showimagepick();
            }
        });

        postingbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String deskt = desket.getText().toString().trim();

                if (TextUtils.isEmpty(deskt)){
                    Toast.makeText(Addpost.this, "Ketikan deskripsi", Toast.LENGTH_SHORT).show();
                }

                if(updatekey.equals("editPost")){
                    editpost(deskt, editpostid);
                }
                else{
                    uploaddata(deskt);
                }

            }
        });


    }

    private void editpost(String deskt, String editpostid) {
        progressDialog.setMessage("Mengubah Postingan...");
        progressDialog.show();

        if(!editImage.equals("noImage")){

            ubahdengangambar(deskt, editpostid);
        }
        else if(imageViewtv.getDrawable() != null){

            ubahdangambar(deskt, editpostid);
        }

        else{
            ubahtanpagambar(deskt, editpostid);
        }
    }

    private void ubahtanpagambar(String deskt ,String editpostid) {

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("uid", uid);
        hashMap.put("uName", name);
        hashMap.put("uEmail", email);
        hashMap.put("uDp", dp);
        hashMap.put("pDesc", deskt);
        hashMap.put("pImage", "noImage");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.child(editpostid)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        progressDialog.dismiss();
                        Toast.makeText(Addpost.this, "Updated...", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(Addpost.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void ubahdangambar(String deskt, String editpostid) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String filepathandname = "Posts/" + "post_" + timestamp;

        Bitmap bitmap = ((BitmapDrawable)imageViewtv.getDrawable()).getBitmap();
        ByteArrayOutputStream outs = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outs);
        byte[] data = outs.toByteArray();

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filepathandname);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;

                        String downuri = uriTask.getResult().toString();
                        if (uriTask.isSuccessful()) {

                            HashMap<String, Object> hashMap = new HashMap<>();

                            hashMap.put("uid", uid);
                            hashMap.put("uName", name);
                            hashMap.put("uEmail", email);
                            hashMap.put("uDp", dp);
                            hashMap.put("pDesc", deskt);
                            hashMap.put("pLikes", "0");
                            hashMap.put("pComments", "0");
                            hashMap.put("pImage", downuri);

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                            ref.child(editpostid)
                                    .updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            progressDialog.dismiss();
                                            Toast.makeText(Addpost.this, "Updated...", Toast.LENGTH_SHORT).show();

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            Toast.makeText(Addpost.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        progressDialog.dismiss();
                        Toast.makeText(Addpost.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void ubahdengangambar(String deskt, String editpostid) {

        StorageReference mpict = FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
        mpict.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        String timestamp = String.valueOf(System.currentTimeMillis());
                        String filepathandname = "Posts/" + "post_" + timestamp;

                        Bitmap bitmap = ((BitmapDrawable)imageViewtv.getDrawable()).getBitmap();
                        ByteArrayOutputStream outs = new ByteArrayOutputStream();

                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outs);
                        byte[] data = outs.toByteArray();

                        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filepathandname);
                        ref.putBytes(data)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                        while (!uriTask.isSuccessful()) ;

                                        String downuri = uriTask.getResult().toString();
                                        if (uriTask.isSuccessful()) {

                                            HashMap<String, Object> hashMap = new HashMap<>();

                                            hashMap.put("uid", uid);
                                            hashMap.put("uName", name);
                                            hashMap.put("uEmail", email);
                                            hashMap.put("uDp", dp);
                                            hashMap.put("pDesc", deskt);
                                            hashMap.put("pLikes", "0");
                                            hashMap.put("pComments", "0");
                                            hashMap.put("pImage", downuri);

                                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                            ref.child(editpostid)
                                                    .updateChildren(hashMap)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            progressDialog.dismiss();
                                                            Toast.makeText(Addpost.this, "Updated...", Toast.LENGTH_SHORT).show();

                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {

                                                            Toast.makeText(Addpost.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });

                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        progressDialog.dismiss();
                                        Toast.makeText(Addpost.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        progressDialog.dismiss();
                        Toast.makeText(Addpost.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void loadpostdat(String editpostid) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");

        Query query = databaseReference.orderByChild("pId").equalTo(editpostid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot ds: dataSnapshot.getChildren()){

                    editDesc = "" + ds.child("pDesc").getValue();
                    editImage = "" + ds.child("pImage").getValue();

                    desket.setText(editDesc);

                    if(!editImage.equals("noImage")){
                        try{

                            Picasso.get().load(editImage).into(imageViewtv);
                        }
                        catch (Exception e){

                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void uploaddata(final String deskt) {

        progressDialog.setMessage("Membuat Post...");
        progressDialog.show();

        final String timestamp = String.valueOf(System.currentTimeMillis());

        String filepath = "Posts/" + "post_" + timestamp;
        if(imageViewtv.getDrawable() != null){

            Bitmap bitmap = ((BitmapDrawable)imageViewtv.getDrawable()).getBitmap();
            ByteArrayOutputStream outs = new ByteArrayOutputStream();

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outs);
            byte[] data = outs.toByteArray();

            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filepath);
            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while(!uriTask.isSuccessful());
                            String downuri = uriTask.getResult().toString();

                            if(uriTask.isSuccessful()){
                                HashMap<Object, String> hashMap = new HashMap<>();

                                hashMap.put("uid",uid);
                                hashMap.put("uName", name);
                                hashMap.put("uDp", dp);
                                hashMap.put("pId", timestamp);
                                hashMap.put("uEmail",email);
                                hashMap.put("pDesc", deskt);
                                hashMap.put("pImage", downuri);
                                hashMap.put("pLikes", "0");
                                hashMap.put("pComments", "0");
                                hashMap.put("pTime", timestamp);

                                DatabaseReference  databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
                                databaseReference.child(timestamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressDialog.dismiss();
                                                Toast.makeText(Addpost.this, "Post dibuat", Toast.LENGTH_SHORT).show();

                                                desket.setText("");
                                                imageViewtv.setImageURI(null);
                                                image_uri = null;
                                                persiapannotif(""+timestamp,
                                                        ""+name+ "add new post",
                                                        ""+deskt,
                                                        "PostNotification",
                                                        "POST");
                                            }

                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                progressDialog.dismiss();
                                                Toast.makeText(Addpost.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(Addpost.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        else {
            HashMap<Object, String> hashMap = new HashMap<>();

            hashMap.put("uid",uid);
            hashMap.put("uName",name);
            hashMap.put("uDp", dp);
            hashMap.put("pId", timestamp);
            hashMap.put("uEmail",email);
            hashMap.put("pDesc", deskt);
            hashMap.put("pLikes", "0");
            hashMap.put("pComments", "0");
            hashMap.put("pImage", "noImage");
            hashMap.put("pTime", timestamp);

            DatabaseReference  databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
            databaseReference.child(timestamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            Toast.makeText(Addpost.this, "Post dibuat", Toast.LENGTH_SHORT).show();

                            desket.setText("");
                            imageViewtv.setImageURI(null);
                            image_uri = null;
                            persiapannotif(""+timestamp,
                                    ""+name+ "add new post",
                                    ""+deskt,
                                    "PostNotification",
                                    "POST");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            progressDialog.dismiss();
                            Toast.makeText(Addpost.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }


    }

    private void adddiaktivitas(String hisUid, String pId, String notification){

        String timestamp = ""+System.currentTimeMillis();

        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("pId", pId);
        hashMap.put("timestamp", timestamp);
        hashMap.put("pUid", hisUid);
        hashMap.put("notification", notification);
        hashMap.put("sUid", myUid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUid).child("Notifications").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });


    }

    private void persiapannotif(String pId, String title, String deskt, String notificationType, String notificationTopic){

        String NOTIFICATION_TOPIC = "/topics/" + notificationTopic;
        String NOTIFICATION_TITLE = title;
        String NOTIFICATION_MESSAGE = deskt;
        String NOTIFICATION_TYPE = notificationType;

        JSONObject notifjson = new JSONObject();
        JSONObject notifbody = new JSONObject();

        try{
            notifbody.put("notificationType", NOTIFICATION_TYPE);
            notifbody.put("sender", uid);
            notifbody.put("pId", pId);
            notifbody.put("pDesc", NOTIFICATION_TITLE);
            notifbody.put("pDesc", NOTIFICATION_MESSAGE);
            notifbody.put("to", NOTIFICATION_TOPIC);
            notifjson.put("data", notifjson);

        } catch (JSONException e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        sendnotifjo(notifjson);

    }

    private void sendnotifjo(JSONObject notifjson) {


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https;/fcm.googleapis.com/fcm/send", notifjson,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d("FCM_RESPONSE", "onResponse: "+response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Toast.makeText(Addpost.this, ""+error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type","application/json");
                headers.put("Authorization", "key=AAAAImjs9vw:APA91bFnhPsORo2XbYqFyRbisa6SMOakNMjpnR6GFfeeaAxA_qn1o4J3BjggWnL5SnOsEDTPCaNuZXdHI4ZI70GMzo7HB_pHxvEkr0iWX4w_QefCpIz2O1J71X5oyhxEw5Ak8YSRTNFx");

                return headers;
            }
        };

        Volley.newRequestQueue(this).add(jsonObjectRequest);

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

    @Override
    protected void onStart() {
        super.onStart();
        checkuser();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout, menu);

        menu.findItem(R.id.create).setVisible(false);
        menu.findItem(R.id.searchin).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if(id==R.id.logout){
            firebaseauth.signOut();
            checkuser();
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkuser(){

        FirebaseUser user = firebaseauth.getCurrentUser();
        if (user != null){

            email = user.getEmail();
            uid = user.getUid();

        }

        else{
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
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

                imageViewtv.setImageURI(image_uri);
            }

            if (requestCode == IMAGE_PICK_CAMERA_CODE){
                imageViewtv.setImageURI(image_uri);
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
