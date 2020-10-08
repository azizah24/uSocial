package com.azizah.msocial;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class AktivitasFragment extends Fragment {

    RecyclerView aktifrv;
    private FirebaseAuth firebaseAuth;
    private ArrayList<DataAktivitas> dataAktivitas;
    private AdapterAktivitas adapterAktivitas;

    public AktivitasFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_aktivitas, container, false);

        aktifrv = view.findViewById(R.id.rvaktifitas);

        firebaseAuth = FirebaseAuth.getInstance();

        getAllnotif();

        return view;
    }

    private void getAllnotif() {

        dataAktivitas = new ArrayList<>();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid()).child("Notifications")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        dataAktivitas.clear();
                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            DataAktivitas dataAktivitas1 = ds.getValue(DataAktivitas.class);

                            dataAktivitas.add(dataAktivitas1);
                        }

                        adapterAktivitas = new AdapterAktivitas(getActivity(), dataAktivitas);
                        aktifrv.setAdapter(adapterAktivitas);
                        adapterAktivitas.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

}
