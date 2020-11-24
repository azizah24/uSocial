package com.azizah.msocial;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class UserFragment extends Fragment {

    RecyclerView recyclerView;
    AdapterData adapterData;
    List<DataUser> dataUsers;
    FirebaseAuth firebaseauth;

    public UserFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View view = inflater.inflate(R.layout.fragment_user, container, false);

       recyclerView = view.findViewById(R.id.recyclervw);
       recyclerView.setHasFixedSize(true);
       recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        firebaseauth = FirebaseAuth.getInstance();
       dataUsers = new ArrayList<>();

       getAlluser();

        return view;
    }

    private void getAlluser() {
        final FirebaseUser fuuser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                dataUsers.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    DataUser dataUser = ds.getValue(DataUser.class);
                    if (!dataUser.getUid().equals(fuuser.getUid())){
                        dataUsers.add(dataUser);
                    }

                    adapterData = new AdapterData(getActivity(), dataUsers);
                    recyclerView.setAdapter(adapterData);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



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
           MenuItem item = menu.findItem(R.id.searchin);
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {

                    if(!TextUtils.isEmpty(s.trim())){
                        searchus(s);
                    }
                    else{
                        getAlluser();
                    }
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {

                    if(!TextUtils.isEmpty(s.trim())){
                        searchus(s);
                    }
                    else{
                        getAlluser();
                    }

                    return false;
                }
            });

            super.onCreateOptionsMenu(menu, inflater);

        }

    private void searchus(final String query) {
        final FirebaseUser fuuser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                dataUsers.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    DataUser dataUser = ds.getValue(DataUser.class);
                    if (!dataUser.getUid().equals(fuuser.getUid())){

                        if(dataUser.getName().toLowerCase().contains(query.toLowerCase())){
                            dataUsers.add(dataUser);
                        }


                    }

                    adapterData = new AdapterData(getActivity(), dataUsers);
                    adapterData.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterData);
                }
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
        else if(id==R.id.pengaturan){
            startActivity(new Intent(getActivity(), PengaturanAct.class));
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

}
