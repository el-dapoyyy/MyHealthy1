package com.example.myhealthy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashActivity extends Activity {

    private static final int SPLASH_TIME_OUT = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Reference to the TextView and ImageView
        TextView greetingText = findViewById(R.id.greeting_text);
        ImageView flagIcon = findViewById(R.id.flag_icon);

        // After the 3-second delay, start the LoginActivity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Finish the SplashActivity to remove it from the back stack
            }
        }, SPLASH_TIME_OUT);
    }
}
