package com.azizah.msocial;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
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


import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class Adapterchat extends RecyclerView.Adapter<Adapterchat.MyHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT =1;
    Context context;
    List<Datachat> chatlist;
    FirebaseUser firebaseUser;
    String imgurl;

    public Adapterchat(Context context, List<Datachat> chatlist, String imgurl) {
        this.context = context;
        this.chatlist = chatlist;
        this.imgurl = imgurl;
    }



    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.sebelahkiri, parent, false);
            return new MyHolder(view);
        }
        else{
            View view = LayoutInflater.from(context).inflate(R.layout.sebelahkanan, parent, false);
            return new MyHolder(view);
        }


    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {

        String message = chatlist.get(position).getMessage();
        String type = chatlist.get(position).getType();
        String msgtime = chatlist.get(position).getTimestamp();


        if(type.equals("text")){
            holder.pesancht.setVisibility(View.VISIBLE);
            holder.gambar.setVisibility(View.GONE);
            holder.pesancht.setText(message);
        }
        else{
            holder.gambar.setVisibility(View.VISIBLE);
            holder.pesancht.setVisibility(View.GONE);
            Picasso.get().load(message).placeholder(R.drawable.ic_off).into(holder.gambar);
        }


        holder.pesancht.setText(message);

        try{
            Picasso.get().load(imgurl).into(holder.profile);
        }
        catch (Exception e){

        }

        holder.layarpesan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Hapus");
                builder.setMessage("Apakah Anda yakin ingin menghapus pesan ini?");
                builder.setPositiveButton("Hapus", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        hapuspesan1(msgtime);
                    }
                });
                builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
            }
        });

        if( position == chatlist.size()-1){
            if(chatlist.get(position).isSeen()){
                holder.seen.setText("Dibaca");
            }
            else{
                holder.seen.setText("Terkirim");
            }
        }

        else {
            holder.seen.setVisibility(View.GONE);
        }

    }


    private void hapusakun(String msgtime, String type) {

        if(type.equals("image")){
            hapusdengangambar(msgtime);

        }
        else{
            hapuspesan1(msgtime);
        }
    }

    private void hapusdengangambar(String msgtime) {

        StorageReference pict = FirebaseStorage.getInstance().getReferenceFromUrl(msgtime);
        pict.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        final String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        DatabaseReference drb = FirebaseDatabase.getInstance().getReference("Chats");
                        Query querr = drb.orderByChild("timestamp").equalTo(msgtime);

                        querr.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                    if (dataSnapshot1.child("sender").getValue().equals(myUID)) {

                                        dataSnapshot1.getRef().removeValue();


                                    } else {

                                    }
                                }
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

    private void hapuspesan1(String msgtime) {

        final String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference drb = FirebaseDatabase.getInstance().getReference("Chats");
        Query querr = drb.orderByChild("timestamp").equalTo(msgtime);
        querr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
                    if(dataSnapshot1.child("sender").getValue().equals(myUID)){

                        dataSnapshot1.getRef().removeValue();


                    }
                    else{

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public int getItemCount() {
        return chatlist.size();
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(chatlist.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TYPE_LEFT;
        }
        else{
            return MSG_TYPE_RIGHT;
        }
    }

    class MyHolder extends RecyclerView.ViewHolder{

        TextView pesancht, seen;
        CircleImageView profile;
        LinearLayout layarpesan;
        ImageView gambar;

        public MyHolder(@NonNull View item){
            super(item);

            layarpesan = item.findViewById(R.id.layarpesan);
            profile = item.findViewById(R.id.profiletv);
            pesancht = item.findViewById(R.id.pesantv);
            seen   = item.findViewById(R.id.seen);
            gambar = item.findViewById(R.id.pesangambar);

        }
    }

}
