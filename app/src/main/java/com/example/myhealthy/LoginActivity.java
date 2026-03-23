package com.example.myhealthy;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etEmail    = findViewById(R.id.login_email);
        etPassword = findViewById(R.id.login_password);
        Button btnLogin     = findViewById(R.id.login_button);
        TextView tvRegister = findViewById(R.id.tv_go_to_register);

        btnLogin.setOnClickListener(v -> attemptLogin());

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });
    }

    private void attemptLogin() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validasi
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email tidak boleh kosong");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password tidak boleh kosong");
            etPassword.requestFocus();
            return;
        }

        // Login via Firebase Auth
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        String displayName = (user != null && user.getDisplayName() != null)
                                ? user.getDisplayName() : "User";

                        Toast.makeText(this, "Welcome, " + displayName + "!",
                                Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MenuActivity.class));
                        finish();
                    } else {
                        String errorMsg = "Email atau password salah!";
                        if (task.getException() != null && task.getException().getMessage() != null) {
                            String msg = task.getException().getMessage();
                            if (msg.contains("no user record")) {
                                errorMsg = "Email belum terdaftar!";
                            } else if (msg.contains("password is invalid") || msg.contains("INVALID_LOGIN_CREDENTIALS")) {
                                errorMsg = "Password salah!";
                            } else if (msg.contains("badly formatted")) {
                                errorMsg = "Format email tidak valid!";
                            } else if (msg.contains("network")) {
                                errorMsg = "Tidak ada koneksi internet!";
                            } else {
                                errorMsg = "Login gagal, silakan coba lagi.";
                            }
                        }
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }
}