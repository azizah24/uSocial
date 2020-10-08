package com.azizah.msocial;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.connection.ConnectionAuthTokenProvider;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterChatlist extends RecyclerView.Adapter<AdapterChatlist.Holder> {

    Context context;
    List<Datachatlist> chatlist;
    List<DataUser> userlist;
    List<Datachat> datachats;
    private HashMap<String, String> chatterakhir;

    public AdapterChatlist(Context context, List<DataUser> userlist) {
        this.context = context;
        this.userlist = userlist;
        chatterakhir = new HashMap<>();
    }



    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.listchat, parent, false);



        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        String hisUid = userlist.get(position).getUid();
        String image = userlist.get(position).getImage();
        String username = userlist.get(position).getName();
        String chatakhir = chatterakhir.get(hisUid);

        holder.nametvchat.setText(username);
        if(chatakhir==null || chatakhir.equals("default")){
            holder.typing.setVisibility(View.GONE);
            //holder.itemView.setVisibility(View.GONE);
        }
        else{
            holder.typing.setVisibility(View.VISIBLE);
            holder.typing.setText(chatakhir);
        }


        try{
            Picasso.get().load(image).placeholder(R.drawable.ic_usr_name).into(holder.dpgambar);
        }
        catch (Exception e){

        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatAct.class);
                intent.putExtra("hisUid", hisUid);
                context.startActivity(intent);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Hapus");
                builder.setMessage("Apakah Anda yakin ingin menyembunyikan pesan ini?");
                builder.setPositiveButton("Hapus", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Query query = FirebaseDatabase.getInstance().getReference("Chatlist");

                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot ds: dataSnapshot.getChildren()){
                                    holder.itemView.setVisibility(View.GONE);
                                    ds.getRef().removeValue();

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

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

                return true;
            }
        });

    }

    private void hapuspesan1() {
        Query query = FirebaseDatabase.getInstance().getReference("Chats");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ds.getRef().removeValue();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void setchatakhir(String userId, String chatakhir){
        chatterakhir.put(userId, chatakhir);
    }

    @Override
    public int getItemCount() {
        return userlist.size();
    }

    class Holder extends RecyclerView.ViewHolder{
        CircleImageView dpgambar, read;
        TextView nametvchat, typing;

       public Holder(@NonNull View itemView){

           super(itemView);

           dpgambar = itemView.findViewById(R.id.dpgamba);
           nametvchat = itemView.findViewById(R.id.nametvchat);
           typing = itemView.findViewById(R.id.isichat);


       }
    }
}
