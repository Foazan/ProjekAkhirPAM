package com.example.onlyart;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SubmitActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;

    private ImageView uploadIcon;
    private EditText inputTitle, inputDesc, inputTags;
    private CheckBox checkboxAi;
    private MaterialButton submitButton;

    private FirebaseStorage storage;
    private StorageReference storageRef;
    private DatabaseReference databaseRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit);

        uploadIcon = findViewById(R.id.uploadIcon);
        inputTitle = findViewById(R.id.inputTitle);
        inputDesc = findViewById(R.id.inputDesc);
        inputTags = findViewById(R.id.inputTags);
        checkboxAi = findViewById(R.id.checkboxAi);
        submitButton = findViewById(R.id.submitButton);
        FrameLayout uploadContainer = findViewById(R.id.uploadContainer);

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference("uploads");
        databaseRef = FirebaseDatabase.getInstance().getReference("works");
        mAuth = FirebaseAuth.getInstance();

        uploadContainer.setOnClickListener(view -> openImageChooser());

        submitButton.setOnClickListener(view -> {
            if (selectedImageUri != null) {
                uploadData(selectedImageUri);
            } else {
                Toast.makeText(this, "Pilih gambar terlebih dahulu", Toast.LENGTH_SHORT).show();
            }
        });

        setupBottomNavigation();
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Pilih gambar"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            uploadIcon.setImageURI(selectedImageUri);
        }
    }

    private void uploadData(Uri imageUri) {
        String fileName = UUID.randomUUID().toString();
        StorageReference imageRef = storageRef.child(fileName);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> saveToDatabase(uri.toString(), fileName)))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Gagal upload gambar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveToDatabase(String imageUrl, String fileName) {
        String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "anonymous";
        String title = inputTitle.getText().toString();
        String desc = inputDesc.getText().toString();
        String rawTags = inputTags.getText().toString();
        boolean isAI = checkboxAi.isChecked();

        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Title dan Description wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] splitTags = rawTags.split(",");
        StringBuilder formattedTags = new StringBuilder();
        for (String tag : splitTags) {
            tag = tag.trim();
            if (!tag.isEmpty()) {
                formattedTags.append("#").append(tag).append(" ");
            }
        }
        String finalTags = formattedTags.toString().trim();

        String key = databaseRef.push().getKey();
        Map<String, Object> data = new HashMap<>();
        data.put("key", key);
        data.put("uid", uid);
        data.put("title", title);
        data.put("desc", desc);
        data.put("tags", finalTags);
        data.put("ai_generated", isAI);
        data.put("imageUrl", imageUrl);
        data.put("imagePath", fileName);

        databaseRef.child(key).setValue(data)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Berhasil disubmit!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SubmitActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Gagal menyimpan data", Toast.LENGTH_SHORT).show());
    }

    private void setupBottomNavigation() {
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        findViewById(R.id.nav_add).setOnClickListener(v -> {

        });

        findViewById(R.id.nav_profile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });
    }
}
