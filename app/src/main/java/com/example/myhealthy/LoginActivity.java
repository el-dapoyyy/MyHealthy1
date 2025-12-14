package com.example.myhealthy; // 1. Tambahkan baris package ini

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 2. Sesuaikan ID dengan yang ada di activity_login.xml
        Button loginButton = findViewById(R.id.login_button);
        final EditText usernameEditText = findViewById(R.id.username);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();

                if (!username.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Welcome " + username, Toast.LENGTH_SHORT).show();

                    // Pastikan MenuActivity sudah dibuat/ada
                    Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(LoginActivity.this, "Please enter a username", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}