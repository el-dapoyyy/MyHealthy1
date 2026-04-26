package com.example.myhealthy;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText etEmail, etPassword;
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;

    // Launcher untuk Google Sign-In intent
    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        Task<GoogleSignInAccount> task =
                                GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            firebaseAuthWithGoogle(account);
                        } catch (ApiException e) {
                            Log.e(TAG, "Google Sign-In gagal: " + e.getStatusCode(), e);
                            Toast.makeText(this,
                                    "Login Google gagal! Pastikan akun Google tersedia.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Setup Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Bind views
        etEmail    = findViewById(R.id.login_email);
        etPassword = findViewById(R.id.login_password);
        Button btnLogin          = findViewById(R.id.login_button);
        LinearLayout btnGoogle   = findViewById(R.id.btn_google_sign_in);
        TextView tvRegister      = findViewById(R.id.tv_go_to_register);
        ImageView btnToggle      = findViewById(R.id.btn_toggle_password);

        // Toggle password visibility
        btnToggle.setOnClickListener(v -> {
            if (etPassword.getInputType() == (android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btnToggle.setImageResource(R.drawable.ic_eye_on);
            } else {
                etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btnToggle.setImageResource(R.drawable.ic_eye_off);
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        // Email/Password login
        btnLogin.setOnClickListener(v -> attemptLogin());

        // Google Sign-In
        btnGoogle.setOnClickListener(v -> signInWithGoogle());

        // Navigate to Register
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });
    }

    // ─────────────────────────────────────────────────────────────
    // Email/Password Login
    // ─────────────────────────────────────────────────────────────

    private void attemptLogin() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

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

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        navigateToMenu();
                    } else {
                        String errorMsg = "Akun belum terdaftar, silahkan register dahulu";
                        if (task.getException() != null && task.getException().getMessage() != null) {
                            String msg = task.getException().getMessage();
                            if (msg.contains("no user record") || msg.contains("user-not-found")) {
                                errorMsg = "Akun belum terdaftar, silahkan register dahulu";
                            } else if (msg.contains("password is invalid") || msg.contains("INVALID_LOGIN_CREDENTIALS")) {
                                errorMsg = "Password salah atau Akun belum terdaftar, silahkan register dahulu";
                            } else if (msg.contains("badly formatted")) {
                                errorMsg = "Format email tidak valid!";
                            } else if (msg.contains("network")) {
                                errorMsg = "Tidak ada koneksi internet!";
                            }
                        }
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ─────────────────────────────────────────────────────────────
    // Google Sign-In
    // ─────────────────────────────────────────────────────────────

    private void signInWithGoogle() {
        // Sign out dulu agar selalu muncul dialog pilih akun
        googleSignInClient.signOut().addOnCompleteListener(this, t -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        navigateToMenu();
                    } else {
                        Log.e(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(this,
                                "Autentikasi Google gagal, silakan coba lagi.",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ─────────────────────────────────────────────────────────────
    // Navigate ke Menu
    // ─────────────────────────────────────────────────────────────

    private void navigateToMenu() {
        FirebaseUser user = mAuth.getCurrentUser();
        String displayName = (user != null && user.getDisplayName() != null)
                ? user.getDisplayName() : "User";
        Toast.makeText(this, "Welcome, " + displayName + "!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(LoginActivity.this, MainNavActivity.class));
        finish();
    }
}