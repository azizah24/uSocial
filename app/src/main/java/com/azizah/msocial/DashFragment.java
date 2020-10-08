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
import android.widget.Toast;

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
public class DashFragment extends Fragment {
    FirebaseAuth firebaseauth;

    RecyclerView recyclerView;
    List<Datapost> postlist;
    Adapterpost adapterpost;

    public DashFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dash, container, false);

        firebaseauth = FirebaseAuth.getInstance();

        recyclerView = view.findViewById(R.id.homerec);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        recyclerView.setLayoutManager(linearLayoutManager);
        postlist = new ArrayList<>();
        loadpost();


        return  view;
    }

    private void loadpost() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postlist.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Datapost datapost = ds.getValue(Datapost.class);

                    postlist.add(datapost);

                    adapterpost = new Adapterpost(getActivity(), postlist);
                    recyclerView.setAdapter(adapterpost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {


            }
        });
    }

    private void  searchpost(final String searchque){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postlist.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Datapost datapost = ds.getValue(Datapost.class);

                    if(datapost.getpDesc().toLowerCase().contains(searchque.toLowerCase())) {
                        postlist.add(datapost);
                    }

                    adapterpost = new Adapterpost(getActivity(), postlist);
                    recyclerView.setAdapter(adapterpost);
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

        MenuItem item = menu.findItem(R.id.searchin);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if(!TextUtils.isEmpty(s)){
                    searchpost(s);
                }
                else {
                    loadpost();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(!TextUtils.isEmpty(s)){
                    searchpost(s);
                }
                else {
                    loadpost();
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
            firebaseauth.signOut();
            checkuser();
        }
        if(id==R.id.create){
          startActivity(new Intent(getActivity(), Addpost.class));
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
