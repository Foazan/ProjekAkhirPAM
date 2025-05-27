package com.example.onlyart;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class DetailActivity extends AppCompatActivity {

    private ImageView detailImage, downloadButton, uploaderProfilePic;
    private TextView detailUploader, detailAI, detailTags, detailDesc;
    private Button deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        detailImage = findViewById(R.id.detailImage);
        uploaderProfilePic = findViewById(R.id.detailProfileImage);
        detailUploader = findViewById(R.id.detailUploader);
        detailAI = findViewById(R.id.detailAI);
        detailTags = findViewById(R.id.detailTags);
        detailDesc = findViewById(R.id.detailDesc);
        deleteButton = findViewById(R.id.deleteButton);
        downloadButton = findViewById(R.id.downloadIcon);

        String imageUrl = getIntent().getStringExtra("imageUrl");
        String tags = getIntent().getStringExtra("tags");
        String desc = getIntent().getStringExtra("desc");
        String uploaderUid = getIntent().getStringExtra("uploaderUid");
        String imagePath = getIntent().getStringExtra("imagePath");
        String key = getIntent().getStringExtra("key");
        boolean aiGenerated = getIntent().getBooleanExtra("ai_generated", false);

        Glide.with(this).load(imageUrl).into(detailImage);
        detailTags.setText(tags != null ? tags : "-");
        detailDesc.setText(desc != null ? desc : "-");
        detailAI.setVisibility(aiGenerated ? View.VISIBLE : View.GONE);

        if (uploaderUid != null) {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
            usersRef.child(uploaderUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String displayName = snapshot.child("displayName").getValue(String.class);
                    String profilePicUrl = snapshot.child("profilePicUrl").getValue(String.class);

                    detailUploader.setText(displayName != null ? displayName : "Unknown");

                    if (profilePicUrl != null) {
                        Glide.with(DetailActivity.this)
                                .load(profilePicUrl)
                                .apply(RequestOptions.circleCropTransform())
                                .into(uploaderProfilePic);
                    }
                }

                @Override public void onCancelled(@NonNull DatabaseError error) {
                    detailUploader.setText("Unknown");
                }
            });
        }

        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (uploaderUid != null && uploaderUid.equals(currentUid)) {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(v -> {
                if (key != null && imagePath != null) {
                    FirebaseDatabase.getInstance().getReference("works").child(key).removeValue();
                    FirebaseStorage.getInstance().getReference("uploads").child(imagePath).delete();
                    Toast.makeText(this, "Gambar berhasil dihapus", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Gagal menghapus: data tidak lengkap", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            deleteButton.setVisibility(View.GONE);
        }

        downloadButton.setOnClickListener(v -> downloadImage(imageUrl));
    }

    private void downloadImage(String imageUrl) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            return;
        }

        Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                        String filename = "OnlyArt_" + System.currentTimeMillis() + ".jpg";
                        OutputStream fos;

                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
                                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/OnlyArt");

                                Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                                if (imageUri == null) {
                                    Toast.makeText(DetailActivity.this, "Gagal membuat file", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                fos = getContentResolver().openOutputStream(imageUri);
                            } else {
                                File imagesDir = new File(Environment.getExternalStoragePublicDirectory(
                                        Environment.DIRECTORY_PICTURES), "OnlyArt");
                                if (!imagesDir.exists()) {
                                    imagesDir.mkdirs();
                                }

                                File image = new File(imagesDir, filename);
                                fos = new FileOutputStream(image);
                            }

                            resource.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                            fos.flush();
                            fos.close();

                            Toast.makeText(DetailActivity.this, "Gambar berhasil didownload", Toast.LENGTH_SHORT).show();

                        } catch (Exception e) {
                            Toast.makeText(DetailActivity.this, "Gagal download: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onLoadCleared(Drawable placeholder) {}
                });
    }
}
