package com.azizah.msocial;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class Chatlist extends Fragment {

FirebaseAuth firebaseauth;
RecyclerView chatrv;
    List<Datachatlist> chatlist;
    List<DataUser> userlist;
    DatabaseReference refr;
    FirebaseUser currentuser;
    AdapterChatlist adapterchat;

    public Chatlist() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chatlist, container, false);
        firebaseauth = FirebaseAuth.getInstance();
        chatrv = view.findViewById(R.id.chatrv);

        currentuser = FirebaseAuth.getInstance().getCurrentUser();
        chatlist = new ArrayList<>();
        refr = FirebaseDatabase.getInstance().getReference("Chatlist").child(currentuser.getUid());
        refr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatlist.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    Datachatlist chatlist1 = ds.getValue(Datachatlist.class);
                    chatlist.add(chatlist1);
                }

                loadchat();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

    private void loadchat() {
        userlist = new ArrayList<>();
        refr = FirebaseDatabase.getInstance().getReference("Users");
        refr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userlist.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    DataUser user = ds.getValue(DataUser.class);
                    for(Datachatlist datachatlist: chatlist){
                            if ( user.getUid() != null && user.getUid().equals(datachatlist.getId())) {
                                userlist.add(user);
                                break;

                        }
                    }


                    adapterchat = new  AdapterChatlist(getContext(), userlist);

                    chatrv.setAdapter(adapterchat);

                    for(int i =0; i<userlist.size(); i++){
                        chatakhirr(userlist.get(i).getUid());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void chatakhirr(String userId) {
        DatabaseReference refren = FirebaseDatabase.getInstance().getReference("Chats");
        refren.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String thechatakhir = "default";
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    Datachat datachat = ds.getValue(Datachat.class);
                    if(datachat==null){

                        continue;
                    }
                    String pengirim = datachat.getSender();
                    String penerima = datachat.getReceiver();
                    if(pengirim==null || penerima == null){
                        continue;
                    }
                    if(datachat.getReceiver().equals(currentuser.getUid()) && datachat.getSender().equals(userId)
                        || datachat.getReceiver().equals(userId) && datachat.getSender().equals(currentuser.getUid())){

                        if(datachat.getType().equals("image")){
                            thechatakhir = "Mengirimkan Gambar";
                        }
                        else {
                            thechatakhir = datachat.getMessage();
                        }
                    }
                }

                adapterchat.setchatakhir(userId, thechatakhir);
                adapterchat.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public  boolean onOptionsItemSelected(MenuItem item){
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

        }

        else{
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.logout, menu);

        menu.findItem(R.id.create).setVisible(false);

        super.onCreateOptionsMenu(menu, inflater);

    }

}
