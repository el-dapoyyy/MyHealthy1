package com.example.myhealthy;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
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
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;

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
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        // Setup Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Bind views
        etName            = findViewById(R.id.register_name);
        etEmail           = findViewById(R.id.register_email);
        etPassword        = findViewById(R.id.register_password);
        etConfirmPassword = findViewById(R.id.register_confirm_password);
        Button btnRegister    = findViewById(R.id.register_button);
        TextView tvLogin      = findViewById(R.id.tv_go_to_login);
        ImageView btnBack     = findViewById(R.id.btn_back);
        LinearLayout btnGoogle = findViewById(R.id.btn_google_sign_up);

        btnRegister.setOnClickListener(v -> attemptRegister());

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        btnGoogle.setOnClickListener(v -> signInWithGoogle());
    }

    private void attemptRegister() {
        String name     = etName.getText().toString().trim();
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm  = etConfirmPassword.getText().toString().trim();

        // Validasi
        if (TextUtils.isEmpty(name)) {
            etName.setError("Nama tidak boleh kosong");
            etName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email tidak boleh kosong");
            etEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Format email tidak valid");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password tidak boleh kosong");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password minimal 6 karakter");
            etPassword.requestFocus();
            return;
        }
        if (!password.equals(confirm)) {
            etConfirmPassword.setError("Password tidak cocok");
            etConfirmPassword.requestFocus();
            return;
        }

        // Register via Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        UserProfileChangeRequest profileUpdate =
                                new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();

                        mAuth.getCurrentUser().updateProfile(profileUpdate)
                                .addOnCompleteListener(profileTask -> {
                                    mAuth.signOut();
                                    Toast.makeText(RegisterActivity.this,
                                            "Registrasi berhasil! Silakan login.",
                                            Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                    finish();
                                });
                    } else {
                        String errorMsg = "Registrasi gagal!";
                        if (task.getException() != null) {
                            String msg = task.getException().getMessage();
                            if (msg != null && msg.contains("email address is already in use")) {
                                errorMsg = "Email sudah terdaftar!";
                            } else if (msg != null && msg.contains("badly formatted")) {
                                errorMsg = "Format email tidak valid!";
                            } else if (msg != null) {
                                errorMsg = msg;
                            }
                        }
                        Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ─────────────────────────────────────────────────────────────
    // Google Sign-In (langsung daftar + login)
    // ─────────────────────────────────────────────────────────────

    private void signInWithGoogle() {
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
                        Toast.makeText(this, "Berhasil masuk dengan Google!",
                                Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, MenuActivity.class));
                        finish();
                    } else {
                        Log.e(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(this,
                                "Autentikasi Google gagal, silakan coba lagi.",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
