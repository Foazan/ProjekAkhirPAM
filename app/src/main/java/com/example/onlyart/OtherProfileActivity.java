package com.example.onlyart;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OtherProfileActivity extends AppCompatActivity {

    private ImageView otherProfileImage;
    private TextView otherProfileName, labelWorks;
    private RecyclerView otherRecyclerView;
    private WorkAdapter adapter;
    private List<Work> workList = new ArrayList<>();

    private DatabaseReference usersRef, worksRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_profile);

        String uid = getIntent().getStringExtra("uid");

        otherProfileImage = findViewById(R.id.otherProfileImage);
        otherProfileName = findViewById(R.id.otherProfileName);
        otherRecyclerView = findViewById(R.id.otherRecyclerView);
        labelWorks = findViewById(R.id.otherProfileLabelWorks);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        worksRef = FirebaseDatabase.getInstance().getReference("works");

        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("displayName").getValue(String.class);
                String photoUrl = snapshot.child("profilePicUrl").getValue(String.class);

                otherProfileName.setText(name != null ? name : "Unknown");

                TextView labelWorks = findViewById(R.id.otherProfileLabelWorks);
                labelWorks.setText((name != null ? name : "User") + "'s Work");

                if (photoUrl != null) {
                    Glide.with(OtherProfileActivity.this)
                            .load(photoUrl)
                            .circleCrop()
                            .into(otherProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        otherRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        adapter = new WorkAdapter(workList);
        otherRecyclerView.setAdapter(adapter);

        worksRef.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                workList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Work work = data.getValue(Work.class);
                    if (work != null) {
                        workList.add(work);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
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
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });
    }
}
