package com.azizah.msocial;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Adapterpost extends RecyclerView.Adapter<Adapterpost.holder> {

    Context context;
    List<Datapost> postlist;
    String myUid, hisUid, myEmail, myName, myDp;
    boolean mproseslike = false;

    private DatabaseReference likerf;
    private DatabaseReference postref;

    public Adapterpost(Context context, List<Datapost> postlist) {
        this.context = context;
        this.postlist = postlist;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likerf = FirebaseDatabase.getInstance().getReference().child("Likes");
        postref = FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    @NonNull
    @Override
    public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_post, parent, false);

        return new holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull holder holder, int position) {

        String uid = postlist.get(position).getUid();
        String uEmail = postlist.get(position).getuEmail();
        String uName = postlist.get(position).getuName();
        String uDp = postlist.get(position).getuDp();
        String pId = postlist.get(position).getpId();
        String pDesc = postlist.get(position).getpDesc();
        String pImage = postlist.get(position).getpImage();
        String pTimestamp = postlist.get(position).getpTime();
        String pLikes = postlist.get(position).getpLikes();
        String pComments = postlist.get(position).getpComments();
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimestamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        holder.Nametv.setText(uName);
        holder.timetv.setText(pTime);
        holder.desktv.setText(pDesc);
        holder.likebtn.setText(pLikes + " Suka");
        holder.komenbtn.setText(pComments + " Komentar");
        setlike(holder, pId);
        try{
            Picasso.get().load(uDp).placeholder(R.drawable.ic_usr_name).into(holder.picttv);
        }
        catch (Exception e){

        }
        if(pImage.equals("noImage")){
            holder.imagetv.setVisibility(View.GONE);
        }
        else {
            holder.imagetv.setVisibility(View.VISIBLE);
            try {
                Picasso.get().load(pImage).into(holder.imagetv);
            } catch (Exception e) {

            }
        }

        holder.morebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showmoreopt(holder.morebtn, uid, myUid, pId, pImage);

            }
        });
        holder.likebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int pLikes = Integer.parseInt(postlist.get(position).getpLikes());
                mproseslike = true;

                String postIde = postlist.get(position).getpId();
                likerf.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(mproseslike){
                            if (dataSnapshot.child(postIde).hasChild(myUid)){

                                postref.child(postIde).child("pLikes").setValue(""+(pLikes-1));
                                likerf.child(postIde).child(myUid).removeValue();
                                mproseslike = false;
                            }
                            else{
                                postref.child(postIde).child("pLikes").setValue(""+(pLikes+1));
                                likerf.child(postIde).child(myUid).setValue("");
                                mproseslike = false;
                                adddiaktivitas(""+uid, ""+pId, "Menyukai Postingan Anda");

                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });
        holder.komenbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, Postdetail.class);
                intent.putExtra("postId", pId);
                context.startActivity(intent);

            }
        });
        holder.sharebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                BitmapDrawable bitmapDrawable = (BitmapDrawable)holder.imagetv.getDrawable();
                if(bitmapDrawable == null){
                    shareposttext(pDesc);
                }
                else{

                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    sharedenganpict(pDesc,bitmap);

                }

            }
        });

        holder.profiltamp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, PostProfil.class);
                intent.putExtra("uid", uid);
                context.startActivity(intent);
            }
        });


    }

    private void sharedenganpict(String pDesc, Bitmap bitmap) {
        String share = pDesc;

        Uri uri = saveshareimag(bitmap);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_TEXT, share);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        intent.setType("image/png");
        context.startActivity(Intent.createChooser(intent, "Bagikan Melalui"));
    }

    private Uri saveshareimag(Bitmap bitmap) {
        File imageFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        try{

            imageFolder.mkdirs();
            File file = new File(imageFolder, "shared_image.png");
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
            outputStream.flush();
            outputStream.close();
            uri = FileProvider.getUriForFile(context, "com.azizah.msocial", file);


        }
        catch (Exception e){
            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return uri;
    }

    private void shareposttext(String pDesc) {
        String share = pDesc;

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        intent.putExtra(Intent.EXTRA_TEXT, share);
        context.startActivity(Intent.createChooser(intent, "Bagikan Melalui"));
    }

    private void adddiaktivitas(String hisUid, String pId, String notification){

        String timestamp = ""+System.currentTimeMillis();

        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("pId", pId);
        hashMap.put("timestamp", timestamp);
        hashMap.put("pUid", hisUid);
        hashMap.put("notification", notification);
        hashMap.put("sUid", myUid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUid).child("Notifications").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });


    }

    private void setlike(holder holder, String pkey) {
        likerf.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.child(pkey).hasChild(myUid)){

                    holder.likebtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like, 0, 0, 0);

                }
                else{
                    holder.likebtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_name, 0, 0, 0);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showmoreopt(ImageButton morebtn, String uid, String myUid, String pId, String pImage) {

        PopupMenu popupMenu = new PopupMenu(context, morebtn, Gravity.END);

        if(uid.equals(myUid)) {
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Hapus");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id==0){

                    hapusakun(pId, pImage);
                }

                else if (id==1){

                    Intent intent = new Intent(context, Addpost.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", pId);
                    context.startActivity(intent);
                }

                return false;
            }
        });

        popupMenu.show();

    }



    private void hapusakun(String pId, String pImage) {

        if(pImage.equals("noImage")){
            hapustanpagambar(pId);
        }
        else{
            hapusdengangambar(pId, pImage);
        }
    }

    private void hapusdengangambar(String pId, String pImage) {

        ProgressDialog progd = new ProgressDialog(context);
        progd.setMessage("Menghapus...");

        StorageReference pict = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        pict.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);

                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot ds: dataSnapshot.getChildren()){
                                    ds.getRef().removeValue();
                                }

                                Toast.makeText(context, "Sukses Terhapus", Toast.LENGTH_SHORT).show();
                                progd.dismiss();
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

    private void hapustanpagambar(String pId) {

        ProgressDialog progd = new ProgressDialog(context);
        progd.setMessage("Menghapus...");

        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ds.getRef().removeValue();
                }

                Toast.makeText(context, "Sukses Terhapus", Toast.LENGTH_SHORT).show();
                progd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return postlist.size();
    }

    class holder extends RecyclerView.ViewHolder{
        ImageView picttv, imagetv;
        TextView Nametv, timetv, desktv, liketv;
        ImageButton morebtn;
        Button likebtn, komenbtn, sharebtn;
        LinearLayout profiltamp;

        public holder(@NonNull View view){
            super(view);

            picttv = view.findViewById(R.id.upicpost);
            imagetv = view.findViewById(R.id.fotok);
            Nametv = view.findViewById(R.id.Nametv);
            timetv = view.findViewById(R.id.timetv);
            //titletv = view.findViewById(R.id.titletv);
            desktv = view.findViewById(R.id.desktv);
           // liketv = view.findViewById(R.id.liketv);
            morebtn = view.findViewById(R.id.morebtn);
            likebtn = view.findViewById(R.id.btnlike);
            komenbtn = view.findViewById(R.id.btnkomen);
            sharebtn = view.findViewById(R.id.btnshare);
            profiltamp = view.findViewById(R.id.profiltamp);

        }
    }
}
