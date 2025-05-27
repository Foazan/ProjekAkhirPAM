package com.example.onlyart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private TextView profileName;
    private RecyclerView recyclerView;
    private WorkAdapter adapter;
    private List<Work> myWorks = new ArrayList<>();

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, worksRef;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();

        profileName = findViewById(R.id.profileName);
        recyclerView = findViewById(R.id.recyclerViewMyIllust);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        worksRef = FirebaseDatabase.getInstance().getReference("works");

        usersRef.child(uid).child("displayName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.getValue(String.class);
                profileName.setText(name != null ? name : "Unknown");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                profileName.setText("Unknown");
            }
        });

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
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

        logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        setupBottomNavigation();
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
