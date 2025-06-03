package com.example.onlyart;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 101;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, worksRef;
    private StorageReference storageRef;

    private ImageView profileImage;
    private TextView profileName;
    private RecyclerView recyclerView;
    private WorkAdapter adapter;
    private List<Work> myWorks = new ArrayList<>();
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        worksRef = FirebaseDatabase.getInstance().getReference("works");
        storageRef = FirebaseStorage.getInstance().getReference("profile_pictures");

        profileImage = findViewById(R.id.profileImage);
        profileName = findViewById(R.id.profileName);
        recyclerView = findViewById(R.id.recyclerViewMyIllust);
        logoutButton = findViewById(R.id.logoutButton);

        profileImage.setOnClickListener(v -> pickImageFromGallery());

        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("displayName").getValue(String.class);
                String photoUrl = snapshot.child("profilePicUrl").getValue(String.class);
                profileName.setText(name != null ? name : "Unknown");
                if (photoUrl != null) {
                    Glide.with(ProfileActivity.this)
                            .load(photoUrl)
                            .circleCrop()
                            .into(profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                profileName.setText("Unknown");
            }
        });

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        adapter = new WorkAdapter(myWorks);
        recyclerView.setAdapter(adapter);

        worksRef.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myWorks.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Work work = data.getValue(Work.class);
                    if (work != null) {
                        myWorks.add(work);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        setupBottomNavigation();
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadProfileImage(imageUri);
        }
    }

    private void uploadProfileImage(Uri uri) {
        String uid = mAuth.getCurrentUser().getUid();
        StorageReference fileRef = storageRef.child(uid + ".jpg");

        fileRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(url -> {
                    usersRef.child(uid).child("profilePicUrl").setValue(url.toString());
                    Glide.with(this)
                            .load(url)
                            .circleCrop()
                            .into(profileImage);
                    Toast.makeText(this, "Foto profil diperbarui", Toast.LENGTH_SHORT).show();
                }))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Gagal upload foto profil", Toast.LENGTH_SHORT).show());
    }

    private void setupBottomNavigation() {
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        findViewById(R.id.nav_add).setOnClickListener(v -> {
            startActivity(new Intent(this, SubmitActivity.class));
            finish();
        });

        findViewById(R.id.nav_profile).setOnClickListener(v -> {
        });
    }
}
