package com.example.onlyart;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WorkAdapter adapter;
    private List<Work> workList = new ArrayList<>();
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        adapter = new WorkAdapter(workList);
        recyclerView.setAdapter(adapter);

        databaseRef = FirebaseDatabase.getInstance().getReference("works");

        loadWorksFromFirebase();
        setupBottomNavigation();
    }

    private void loadWorksFromFirebase() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                workList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Work work = data.getValue(Work.class);
                    if (work != null && work.getImageUrl() != null) {
                        workList.add(work);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void setupBottomNavigation() {
        findViewById(R.id.nav_home).setOnClickListener(v -> {

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
