package com.example.onlyart;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.*;

public class DetailActivity extends AppCompatActivity {

    private ImageView detailImage;
    private TextView detailUploader, detailAI, detailTags, detailDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        detailImage    = findViewById(R.id.detailImage);
        detailUploader = findViewById(R.id.detailUploader);
        detailAI       = findViewById(R.id.detailAI);
        detailTags     = findViewById(R.id.detailTags);
        detailDesc     = findViewById(R.id.detailDesc);

        String imageUrl    = getIntent().getStringExtra("imageUrl");
        String tags        = getIntent().getStringExtra("tags");
        String desc        = getIntent().getStringExtra("desc");
        String uploaderUid = getIntent().getStringExtra("uploaderUid");
        boolean aiGenerated = getIntent().getBooleanExtra("ai_generated", false);

        Glide.with(this).load(imageUrl).into(detailImage);

        if (tags != null)  detailTags.setText(tags);
        if (desc != null)  detailDesc.setText(desc);

        detailAI.setVisibility(aiGenerated ? View.VISIBLE : View.GONE);

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.child(uploaderUid).child("displayName")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String displayName = snapshot.getValue(String.class);
                        detailUploader.setText(displayName != null ? displayName : "Unknown");
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        detailUploader.setText("Unknown");
                    }
                });
    }
}
