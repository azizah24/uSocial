package com.azizah.msocial;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterData extends RecyclerView.Adapter<AdapterData.MyHolder> {

    Context context;
    List<DataUser> dataUsers;
    FirebaseAuth firebaseAuth;
    String myUid;

    public AdapterData(Context context, List<DataUser> dataUsers) {
        this.context = context;
        this.dataUsers = dataUsers;
        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();
    }


    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.list_user, parent, false);


        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        final String hishuid = dataUsers.get(position).getUid();
        String uimg = dataUsers.get(position).getImage();
        final String uname = dataUsers.get(position).getName();
        String ubio = dataUsers.get(position).getBio();

        holder.namatvv.setText(uname);
        holder.biotvv.setText(ubio);
        try{

            Picasso.get().load(uimg).placeholder(R.drawable.ic_usr_name).into(holder.imgv);

        }
        catch (Exception e){

        }

        holder.blocked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dataUsers.get(position).isBlocked()){
                    unBlockuser(hishuid);
                }
                else{
                    blockuser(hishuid);
                }
            }
        });
        checisblocked(hishuid, holder, position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Detail Akun", "Chat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (i == 0){
                            Intent intent = new Intent(context, PostProfil.class);
                            intent.putExtra("uid", hishuid);
                            context.startActivity(intent);
                        }

                        if(i == 1){
                            blockornot(hishuid);
                        }


                    }
                });
                builder.create().show();
            }
        });



    }
    
    private void blockornot(String hishuid){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(hishuid).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            if(ds.exists()){
                                Toast.makeText(context, "Anda telah diblokir oleh pengguna ini, tidak dapat mengirim pesan", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        Intent cht = new Intent(context, ChatAct.class);
                        cht.putExtra("hisUid", hishuid);
                        context.startActivity(cht);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void checisblocked(String hishuid, MyHolder holder, int position) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hishuid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            if(ds.exists()){
                                holder.blocked.setImageResource(R.drawable.ic_block);
                                dataUsers.get(position).setBlocked(true);
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void unBlockuser(String hishuid) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hishuid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            if(ds.exists()){
                                ds.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                Toast.makeText(context, "Blokir dibuka...", Toast.LENGTH_SHORT).show();

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                Toast.makeText(context, "Gagal: "+e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void blockuser(String hishuid) {

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", hishuid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").child(hishuid).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(context, "Pengguna Diblokir...", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(context, "Gagal: "+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    @Override
    public int getItemCount() {
        return dataUsers.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        CircleImageView imgv;
        ImageView blocked;
        TextView namatvv, biotvv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            imgv = itemView.findViewById(R.id.imgv);
            namatvv = itemView.findViewById(R.id.namatvv);
            biotvv = itemView.findViewById(R.id.biotvv);
            blocked = itemView.findViewById(R.id.blockedgamb);
        }
    }
}
