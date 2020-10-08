package com.azizah.msocial;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.provider.ContactsContract;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterKomen extends RecyclerView.Adapter<AdapterKomen.Holder> {

    Context context;
    List<DataKomen> komenlist;
    String myUid,postId;


    public AdapterKomen(Context context, List<DataKomen> komenlist, String myUid, String postId) {
        this.context = context;
        this.komenlist = komenlist;
        this.myUid = myUid;
        this.postId = postId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.listkomen, parent, false);


        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        String uid = komenlist.get(position).getUid();
        String name = komenlist.get(position).getuName();
        String email = komenlist.get(position).getuEmail();
        String image = komenlist.get(position).getuDp();
        String cid = komenlist.get(position).getcId();
        String comment = komenlist.get(position).getComment();
        String timestamp = komenlist.get(position).getTimestamp();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));

        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        holder.nametv.setText(name);
        holder.komentv.setText(comment);
        holder.timetv.setText(pTime);
        try{
            Picasso.get().load(image).placeholder(R.drawable.ic_usr_name).into(holder.avatargam);
        }
        catch (Exception e){

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(myUid.equals(uid)){

                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                    builder.setTitle("Hapus");
                    builder.setMessage("Apakah Anda yakin ingin menghapusnya?");
                    builder.setPositiveButton("Hapus", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            hapuskomen(cid);
                        }
                    });
                    builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });

                    builder.create().show();
                }
                else{

                    Toast.makeText(context, "Tidak dapat menghapus komentar orang lain ...", Toast.LENGTH_SHORT).show();
                }
            }
        });



    }


        private void hapuskomen(String cid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.child("Comments").child(cid).removeValue();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String comen = ""+ dataSnapshot.child("pComments").getValue();
                int newcomentval = Integer.parseInt(comen) - 1;
                ref.child("pComments").setValue(""+newcomentval);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return komenlist.size();
    }

    class Holder extends RecyclerView.ViewHolder

    {

        CircleImageView avatargam;
        TextView nametv, komentv, timetv;

        public Holder(@NonNull View itemView){
            super(itemView);

            avatargam = itemView.findViewById(R.id.avatargam);
            nametv = itemView.findViewById(R.id.nametv);
            komentv = itemView.findViewById(R.id.komentv);
            timetv = itemView.findViewById(R.id.timetv);

        }
    }


}
