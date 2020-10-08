package com.azizah.msocial;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.ConditionVariable;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfFragment extends Fragment {

FirebaseAuth firebaseAuth;
FirebaseUser user;
CircleImageView ava;
ImageView cover2;
TextView nametv,biotv;
FirebaseDatabase firebaseDatabase;
DatabaseReference databaseReference;
FloatingActionButton floatingActionButton;
ProgressDialog progressDialog;
RecyclerView postrv;


private static final int CAMERA_REQUEST_CODE = 100;
private static final int STORAGE_REQUEST_CODE = 200;
private static final int IMAGE_PICK_CAMERA_CODE = 300;
private static final int IMAGE_PICK_GALLERY_CODE = 400;

    String[] cameraPermissions;
    String[] storagePermissions;
    Uri image_uri;
    String profilorcover;

    StorageReference storageReference;
    String storagePath = "Users_Profile_Cover_Imgs/";
    List<Datapost> datapostList;
    Adapterpost adapterpost;
    String uid;

    public ProfFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_prof, container, false);

       firebaseAuth = FirebaseAuth.getInstance();
       user = firebaseAuth.getCurrentUser();
       firebaseDatabase = FirebaseDatabase.getInstance();
       databaseReference = firebaseDatabase.getReference("Users");
        progressDialog = new ProgressDialog(getActivity());
       ava = view.findViewById(R.id.avaim);
       nametv = view.findViewById(R.id.namatv);
       biotv = view.findViewById(R.id.biotv);
       cover2 = view.findViewById(R.id.rela);
       floatingActionButton = view.findViewById(R.id.editbtn);
       storageReference = getInstance().getReference();
       postrv = view.findViewById(R.id.recyclerv_post);


       cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
       storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
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

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editprofil();
            }
        });

        datapostList = new ArrayList<>();
        checkuser();
        loadingpostku();

        return view;
    }

    private void loadingpostku() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

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

                    adapterpost = new Adapterpost(getActivity(), datapostList);
                    postrv.setAdapter(adapterpost);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {


            }
        });
    }

    private void searchpostku(String searchqueku) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

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

                    if (datapost.getpDesc().toLowerCase().contains(searchqueku.toLowerCase())){

                        datapostList.add(datapost);

                    }


                    adapterpost = new Adapterpost(getActivity(), datapostList);
                    postrv.setAdapter(adapterpost);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private boolean checkstorage(){
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;


    }

    private  void reqstorage(){
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);

    }

    private boolean checkcam(){
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);

        return result && result1;


    }

    private  void reqcam(){
       requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);

    }



    private void editprofil() {

        String[] pengaturan = {"Edit Foto Profil", "Edit Foto Sampul", "Edit Nama", "Edit Bio", "Edit Kata Sandi"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Edit Profil");
        builder.setItems(pengaturan, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (i == 0){

                    progressDialog.setMessage("Mengubah Profil");
                    profilorcover = "image";
                    showimage();
                }

                else if (i == 1){
                    progressDialog.setMessage("Mengubah Profil");
                    profilorcover = "cover";
                    showimage();
                }

                else if (i == 2){
                    progressDialog.setMessage("Mengubah Profil");
                    shownamebio("name");
                }

                else if (i == 3){
                    progressDialog.setMessage("Mengubah Profil");
                    shownamebio("bio");
                }
                else if(i==4){
                    progressDialog.setMessage("Mengubah Kata Sandi");
                    showubahpass();
                }

            }
        });

        builder.create().show();
    }

    private  void showubahpass(){
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialogubah_pass, null);

        EditText passed = view.findViewById(R.id.passed);
        EditText newpass =view.findViewById(R.id.upassed);
        Button updatepass =view.findViewById(R.id.btnubahpas);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        updatepass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String passlama = passed.getText().toString().trim();
                String passbaru = newpass.getText().toString().trim();
                if(TextUtils.isEmpty(passlama)){
                    Toast.makeText(getActivity(), "Ketikan Kata Sandi Lama Anda", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(passbaru.length()<6){
                    Toast.makeText(getActivity(), "Kata Sandi Harus 6 Karakter", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                updatepassw(passlama, passbaru);
            }
        });

    }

    private void updatepassw(String passlama, String passbaru) {

        progressDialog.show();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), passlama);
        firebaseUser.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        firebaseUser.updatePassword(passbaru)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        progressDialog.dismiss();
                                        Toast.makeText(getActivity(), "Kata Sandi diubah...", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        progressDialog.dismiss();
                                        Toast.makeText(getActivity(), "Gagal: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void shownamebio(final String ubah) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Mengubah "+ubah);

        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);

        final EditText edt = new EditText(getActivity());
        edt.setHint("Ketikan "+ubah);
        linearLayout.addView(edt);

        builder.setView(linearLayout);

        builder.setPositiveButton("Mengubah", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String value = edt.getText().toString().trim();

                if(!TextUtils.isEmpty(value)){
                    progressDialog.dismiss();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(ubah, value);

                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(), "Tersimpan...", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    if(ubah.equals("name")){
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

                        Query query = ref.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds: dataSnapshot.getChildren()){
                                    String child = ds.getKey();
                                    dataSnapshot.getRef().child(child).child("uName").setValue(value);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot ds: dataSnapshot.getChildren()){
                                    String child = ds.getKey();
                                    if(dataSnapshot.child(child).hasChild("Comments")){
                                        String child1 = "" + dataSnapshot.child(child).getKey();
                                        Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                        child2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for(DataSnapshot ds: dataSnapshot.getChildren()){
                                                    String child = ds.getKey();
                                                    dataSnapshot.getRef().child(child).child("uName").setValue(value);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                    }

                }
                else{
                    Toast.makeText(getActivity(), "Silahkan Ketikan "+ ubah, Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Kembali", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.create().show();

    }

    private void showimage() {
        String[] pengaturan = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Edit Profil");
        builder.setItems(pengaturan, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (i == 0){

                    if (!checkcam()){
                        reqcam();
                    }
                    else{
                        memilih();
                    }

                }

                else if (i == 1){

                    if (!checkstorage()){
                        reqstorage();
                    }
                    else{
                        memilihgal();
                    }

                }



            }
        });

        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{

                if (grantResults.length >0){
                    boolean cameraacc = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writestor = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(cameraacc && writestor){
                        memilih();
                    }
                    else {
                        Toast.makeText(getActivity(), "Setujui Kamera dan File Manager", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
            case STORAGE_REQUEST_CODE:{

                if (grantResults.length >0){
                     boolean writestor = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if(writestor){
                        memilihgal();
                    }
                    else {
                        Toast.makeText(getActivity(), "Setujui File Manager", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == RESULT_OK){

            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                image_uri = data.getData();

                uploadprofile(image_uri);
            }

            if (requestCode == IMAGE_PICK_CAMERA_CODE){
                uploadprofile(image_uri);
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadprofile(Uri uri) {

        progressDialog.show();
        String file = storagePath+ ""+ profilorcover +"_"+ user.getUid();

        StorageReference storageReference2 = storageReference.child(file);
        storageReference2.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                Uri downloaduri = uriTask.getResult();

                if (uriTask.isSuccessful()){
                    HashMap<String, Object> results = new HashMap<>();

                    results.put(profilorcover, downloaduri.toString());

                    databaseReference.child(user.getUid()).updateChildren(results)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(), "Mengubah Gambar...", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Tidak dapat mengubah gambar...", Toast.LENGTH_SHORT).show();
                        }
                    });

                    if(profilorcover.equals("image")){
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

                        Query query = ref.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds: dataSnapshot.getChildren()){
                                    String child = ds.getKey();
                                    dataSnapshot.getRef().child(child).child("uDp").setValue(downloaduri.toString());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot ds: dataSnapshot.getChildren()){
                                    String child = ds.getKey();
                                    if(dataSnapshot.child(child).hasChild("Comments")){
                                        String child1 = "" + dataSnapshot.child(child).getKey();
                                        Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                        child2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for(DataSnapshot ds: dataSnapshot.getChildren()){
                                                    String child = ds.getKey();
                                                    dataSnapshot.getRef().child(child).child("uDp").setValue(downloaduri.toString());
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                }
                else{
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void memilih() {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");

        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent camerai = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camerai.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(camerai, IMAGE_PICK_CAMERA_CODE);

    }

    private void memilihgal() {

        Intent gall = new Intent(Intent.ACTION_PICK);
        gall.setType("image/*");
        startActivityForResult(gall, IMAGE_PICK_GALLERY_CODE);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.logout, menu);

        MenuItem item = menu.findItem(R.id.searchin);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if(!TextUtils.isEmpty(s)){
                    searchpostku(s);
                }
                else{
                    loadingpostku();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                if(!TextUtils.isEmpty(s)){
                    searchpostku(s);
                }
                else{
                    loadingpostku();
                }

                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public  boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id==R.id.logout){
            firebaseAuth.signOut();
            checkuser();
        }

        if(id==R.id.create){
            startActivity(new Intent(getActivity(), Addpost.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkuser(){

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){

            uid = user.getUid();
        }

        else{
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }
}
