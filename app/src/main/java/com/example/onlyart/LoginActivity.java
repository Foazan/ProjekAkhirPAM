package com.example.onlyart;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameField, passwordField, displayNameField;
    private MaterialButton loginButton;
    private RadioGroup authToggle;
    private RadioButton loginTab, signinTab;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);
        displayNameField = findViewById(R.id.displayNameField);
        loginButton = findViewById(R.id.loginButton);
        authToggle = findViewById(R.id.authToggle);
        loginTab = findViewById(R.id.loginTab);
        signinTab = findViewById(R.id.signinTab);

        // Set tampilan awal
        displayNameField.setVisibility(View.GONE);

        authToggle.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.loginTab) {
                loginButton.setText("Log in");
                displayNameField.setVisibility(View.GONE);
            } else {
                loginButton.setText("Sign in");
                displayNameField.setVisibility(View.VISIBLE);
            }
        });

        loginButton.setOnClickListener(view -> {
            String email = usernameField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Email dan Password harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            if (authToggle.getCheckedRadioButtonId() == R.id.loginTab) {
                loginWithFirebase(email, password);
            } else {
                String displayName = displayNameField.getText().toString().trim();
                if (TextUtils.isEmpty(displayName)) {
                    Toast.makeText(this, "Profile Name harus diisi", Toast.LENGTH_SHORT).show();
                    return;
                }
                signUpWithFirebase(email, password, displayName);
            }
        });
    }

    private void loginWithFirebase(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Login berhasil!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Login gagal: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signUpWithFirebase(String email, String password, String displayName) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();

                        usersRef.child(uid).child("displayName").setValue(displayName)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Akun dibuat!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Gagal menyimpan profile name", Toast.LENGTH_SHORT).show();
                                });

                    } else {
                        Toast.makeText(this, "Sign up gagal: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
