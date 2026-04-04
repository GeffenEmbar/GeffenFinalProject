package com.example.geffenfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.geffenfinalproject.utils.SharedPreferencesUtil;

public class Splash extends AppCompatActivity {
    private static final String TAG = "Splash";
    private static final int SPLASH_DISPLAY_TIME = 3500; // Increased to allow animation to play

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        
        // Start Animations
        ImageView logo = findViewById(R.id.logo);
        TextView appName = findViewById(R.id.appName);
        Animation bounceAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);
        logo.startAnimation(bounceAnim);
        appName.startAnimation(bounceAnim);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Thread splashThread = new Thread(() -> {
            try {
                Thread.sleep(SPLASH_DISPLAY_TIME); // SPLASH_DISPLAY_TIME delay
            } catch (InterruptedException ignored) {
            } finally {
                // go to the correct activity after the delay
                Intent intent;
                if (SharedPreferencesUtil.isUserLoggedIn(this)) {
                    Log.d(TAG, "User signed in, redirecting to User Menu Screen");
                    intent = new Intent(Splash.this, user_menu.class);
                } else {
                    Log.d(TAG, "User not signed in, redirecting to MainActivity");
                    intent = new Intent(Splash.this, MainActivity.class);
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        splashThread.start();
    }
}