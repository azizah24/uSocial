package com.azizah.msocial;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterAktivitas extends RecyclerView.Adapter<AdapterAktivitas.HolderAktivitas>{

    private Context context;
    private ArrayList<DataAktivitas> dataAktivitas;
    private FirebaseAuth firebaseAuth;

    public AdapterAktivitas(Context context, ArrayList<DataAktivitas> dataAktivitas) {
        this.context = context;
        this.dataAktivitas = dataAktivitas;
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderAktivitas onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.aktivitaslist, parent, false);



        return new HolderAktivitas(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderAktivitas holder, int position) {
        DataAktivitas dataAktivita = dataAktivitas.get(position);
        String name = dataAktivita.getsName();
        String notif = dataAktivita.getNotification();
        String image = dataAktivita.getsImage();
        String timestamp = dataAktivita.getTimestamp();
        String sender = dataAktivita.getsUid();
        String pId = dataAktivita.getpId();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(sender)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            String name = ""+ds.child("name").getValue();
                            String image = ""+ds.child("image").getValue();
                            String email = ""+ds.child("email").getValue();

                            dataAktivita.setsName(name);
                            dataAktivita.setsEmail(email);
                            dataAktivita.setsImage(image);

                            holder.namatv.setText(name);
                            try{
                                Picasso.get().load(image).placeholder(R.drawable.ic_usr_name).into(holder.avaakt);

                            }
                            catch (Exception e){
                                holder.avaakt.setImageResource(R.drawable.ic_usr_name);
                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        holder.aktiftv.setText(notif);
        holder.waktutv.setText(pTime);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, Postdetail.class);
                intent.putExtra("postId", pId);
                context.startActivity(intent);

            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Hapus");
                builder.setMessage("Apakah Anda yakin ingin menghapusnya?");
                builder.setPositiveButton("Hapus", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                        databaseReference.child(firebaseAuth.getUid()).child("Notifications").child(timestamp).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Toast.makeText(context, "Aktivitas dihapus...", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                });

                builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                      dialogInterface.dismiss();
                    }
                });
                builder.create().show();

                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return dataAktivitas.size();
    }


    class HolderAktivitas extends RecyclerView.ViewHolder{

        CircleImageView avaakt;
        TextView namatv, aktiftv, waktutv;

        public HolderAktivitas(@NonNull View itemView) {
            super(itemView);

            avaakt = itemView.findViewById(R.id.avaaktif);
            namatv = itemView.findViewById(R.id.namaaktiftv);
            aktiftv = itemView.findViewById(R.id.notiaktif);
            waktutv = itemView.findViewById(R.id.waktuaktif);

        }
    }

}
