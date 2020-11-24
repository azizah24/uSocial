package com.azizah.msocial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class Postdetail extends AppCompatActivity {

    ImageView picttv, imagetv;
    TextView uNametv, timetv, desktv, comenttv;
    ImageButton morebtn;
    Button likebtn, komenbtn, sharebtn;
    LinearLayout profiltamp;
    boolean mproseskomen = false;
    boolean mproseslike = false;
    ProgressDialog progressDialog;

    RecyclerView recyclerView;
    EditText comenedit;
    ImageButton sendbuton;
    CircleImageView avatarcom;
    List<DataKomen> komenlist;
    AdapterKomen adapterKomen;

    String myUid, myEmail, myDp, myName, postId, pImage, pLikes, hisDp, hisName, hisUid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postdetail);

        picttv = findViewById(R.id.upicpost);
        recyclerView = findViewById(R.id.recyclerkomen);
        imagetv = findViewById(R.id.fotok);
        uNametv = findViewById(R.id.Nametv);
        timetv = findViewById(R.id.timetv);
        //titletv = view.findViewById(R.id.titletv);
        desktv = findViewById(R.id.desktv);
        comenttv = findViewById(R.id.pCommentstv);
        morebtn = findViewById(R.id.morebtn);
        likebtn = findViewById(R.id.btnlike);
        komenbtn = findViewById(R.id.btnkomen);
        sharebtn = findViewById(R.id.btnshare);
        profiltamp = findViewById(R.id.profiltamp);
        comenedit = findViewById(R.id.comenet);
        sendbuton = findViewById(R.id.sendbtncomen);
        avatarcom = findViewById(R.id.avacoment);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Detail Postingan");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        komenlist = new ArrayList<>();
        loadpostinfo();
        checkuser();
        loaduserinfo();
        loadkomen();
        setlike();

        sendbuton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postcoment();
            }
        });

        morebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showmoreopt();

            }
        });

        likebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 mproseslike = true;

                DatabaseReference likerf = FirebaseDatabase.getInstance().getReference().child("Likes");
                DatabaseReference postref = FirebaseDatabase.getInstance().getReference().child("Posts");

                likerf.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(mproseslike){
                            if (dataSnapshot.child(postId).hasChild(myUid)){

                                postref.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)-1));
                                likerf.child(postId).child(myUid).removeValue();
                                mproseslike = false;

                            }
                            else{
                                postref.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)+1));
                                likerf.child(postId).child(myUid).setValue("");
                                mproseslike = false;

                                adddiaktivitas(""+hisUid, ""+postId, "Menyukai Postingan Anda");

                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }
        });

        sharebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pDesc = desktv.getText().toString().trim();
                BitmapDrawable bitmapDrawable = (BitmapDrawable)imagetv.getDrawable();
                if(bitmapDrawable == null){
                    shareposttext(pDesc);
                }
                else{

                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    sharedenganpict(pDesc,bitmap);

                }
            }
        });

    }

    private void sharedenganpict(String pDesc, Bitmap bitmap) {
        String share = pDesc;

        Uri uri = saveshareimag(bitmap);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_TEXT, share);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        intent.setType("image/png");
        startActivity(Intent.createChooser(intent, "Bagikan Melalui"));
    }

    private Uri saveshareimag(Bitmap bitmap) {
        File imageFolder = new File(getCacheDir(), "images");
        Uri uri = null;
        try{

            imageFolder.mkdirs();
            File file = new File(imageFolder, "shared_image.png");
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
            outputStream.flush();
            outputStream.close();
            uri = FileProvider.getUriForFile(this, "com.azizah.msocial", file);


        }
        catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return uri;
    }

    private void shareposttext(String pDesc) {
        String share = pDesc;

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        intent.putExtra(Intent.EXTRA_TEXT, share);
        startActivity(Intent.createChooser(intent, "Bagikan Melalui"));
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


    private void loadkomen() {

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());

        recyclerView.setLayoutManager(linearLayoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        //Query query = ref.orderByChild("Comments").equalTo(postId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                komenlist.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){

                    DataKomen dataKomen = ds.getValue(DataKomen.class);
                    komenlist.add(dataKomen);

                    adapterKomen = new AdapterKomen(getApplicationContext(), komenlist, myUid, postId);

                    recyclerView.setAdapter(adapterKomen);
                    recyclerView.setLayoutManager(linearLayoutManager);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showmoreopt() {
        PopupMenu popupMenu = new PopupMenu(this, morebtn, Gravity.END);

        if(hisUid.equals(myUid)) {
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Hapus");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id==0){

                    hapusakun();
                }

                else if (id==1){

                    Intent intent = new Intent(Postdetail.this, Addpost.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", postId);
                    startActivity(intent);
                }

                return false;
            }
        });

        popupMenu.show();
    }

    private void hapusakun() {
        if(pImage.equals("noImage")){
            hapustanpagambar();
        }
        else{
            hapusdengangambar();
        }
    }

    private void hapusdengangambar() {
        ProgressDialog progd = new ProgressDialog(this);
        progd.setMessage("Menghapus...");

        StorageReference pict = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        pict.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);

                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot ds: dataSnapshot.getChildren()){
                                    ds.getRef().removeValue();
                                }

                                Toast.makeText(Postdetail.this, "Sukses Terhapus", Toast.LENGTH_SHORT).show();
                                progd.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void hapustanpagambar() {
        ProgressDialog progd = new ProgressDialog(this);
        progd.setMessage("Menghapus...");

        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ds.getRef().removeValue();
                }

                Toast.makeText(Postdetail.this, "Sukses Terhapus", Toast.LENGTH_SHORT).show();
                progd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setlike() {

        DatabaseReference likerf = FirebaseDatabase.getInstance().getReference().child("Likes");

        likerf.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.child(postId).hasChild(myUid)){

                    likebtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like, 0, 0, 0);

                }
                else{
                    likebtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_name, 0, 0, 0);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void postcoment() {

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Menambahakan Komentar");

        String comment = comenedit.getText().toString().trim();
        if(TextUtils.isEmpty(comment)){
            Toast.makeText(this, "Komentar Kosong...", Toast.LENGTH_SHORT).show();

            return;

        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        String times = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("cId", times);
        hashMap.put("comment", comment);
        hashMap.put("timestamp", times);
        hashMap.put("uid", myUid);
        hashMap.put("uEmail", myEmail);
        hashMap.put("uDp", myDp);
        hashMap.put("uName", myName);

        ref.child(times).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        Toast.makeText(Postdetail.this, "Komentar Ditambahkan", Toast.LENGTH_SHORT).show();
                        comenedit.setText("");
                        updatecomentjumlah();
                        loadkomen();
                        adddiaktivitas(""+hisUid, ""+postId, "Mengomentari Postingan Anda" );
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Postdetail.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }


    private void updatecomentjumlah() {

        mproseskomen = true;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(mproseskomen){
                    String comen = ""+ dataSnapshot.child("pComments").getValue();
                    int newcomentval = Integer.parseInt(comen)+1;
                    ref.child("pComments").setValue(""+newcomentval);
                    mproseskomen = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loaduserinfo() {

        Query myrf = FirebaseDatabase.getInstance().getReference("Users");
        myrf.orderByChild("uid").equalTo(myUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){

                    myName = ""+ ds.child("name").getValue();
                    myDp = ""+ ds.child("image").getValue();

                    try{

                        Picasso.get().load(myDp).placeholder(R.drawable.ic_usr_name).into(avatarcom);

                    }catch (Exception e){

                        Picasso.get().load(R.drawable.ic_usr_name).into(avatarcom);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void loadpostinfo() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){

                    String pDesc = "" + ds.child("pDesc").getValue();
                    pLikes = "" + ds.child("pLikes").getValue();
                    String pTime = "" + ds.child("pTime").getValue();
                    pImage = "" + ds.child("pImage").getValue();
                    hisDp = "" + ds.child("uDp").getValue();
                    hisUid = "" + ds.child("uid").getValue();
                    String uEmail = "" + ds.child("uEmail").getValue();
                    hisName = "" + ds.child("uName").getValue();
                    String komenjumlah = "" + ds.child("pComments").getValue();

                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTime));
                    String pTime1 = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                    desktv.setText(pDesc);
                    timetv.setText(pTime1);
                    likebtn.setText(pLikes + " Suka");
                    uNametv.setText(hisName);
                    comenttv.setText(komenjumlah + " Komentar");

                    if(pImage.equals("noImage")){
                        imagetv.setVisibility(View.GONE);
                    }
                    else {
                        imagetv.setVisibility(View.VISIBLE);
                        try {
                            Picasso.get().load(pImage).into(imagetv);
                        } catch (Exception e) {

                        }
                    }

                    try {
                        Picasso.get().load(hisDp).placeholder(R.drawable.ic_usr_name).into(picttv);
                    } catch (Exception e) {

                        Picasso.get().load(R.drawable.ic_usr_name).into(picttv);

                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void checkuser(){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){

            myEmail = user.getEmail();
            myUid = user.getUid();

        }

        else{
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed();

        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if(id==R.id.logout){
            FirebaseAuth.getInstance().signOut();
            checkuser();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.logout, menu);

        menu.findItem(R.id.create).setVisible(false);
        menu.findItem(R.id.searchin).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }
}
