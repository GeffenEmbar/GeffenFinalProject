package com.example.geffenfinalproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.geffenfinalproject.utils.SharedPreferencesUtil;
import com.google.firebase.auth.FirebaseAuth;

import android.content.Intent;

public class BaseActivity extends AppCompatActivity {

    protected Button btn3, btn4, btn5, btnSignOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        // Load the base layout
        View baseView = getLayoutInflater().inflate(R.layout.activity_base, null);
        FrameLayout contentFrame = baseView.findViewById(R.id.base_content_frame);

        // Inflate the child activity's layout into the frame
        getLayoutInflater().inflate(layoutResID, contentFrame, true);

        // Set the combined view as the content
        super.setContentView(baseView);

        // Apply window insets to keep the layout above the navigation bar and below the status bar
        ViewCompat.setOnApplyWindowInsetsListener(baseView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize toolbar buttons
        btn3 = findViewById(R.id.btn3);
        btn4 = findViewById(R.id.btn4);
        btn5 = findViewById(R.id.btn5);
        btnSignOut = findViewById(R.id.btnSignOut);

        // Basic listeners for now
        View.OnClickListener listener = v -> {
            int id = v.getId();
            if (id == R.id.btn3) Toast.makeText(this, "Button 3", Toast.LENGTH_SHORT).show();
            else if (id == R.id.btn4) Toast.makeText(this, "Button 4", Toast.LENGTH_SHORT).show();
            else if (id == R.id.btn5) {
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Intent intent = new Intent(this, user_profile.class);
                intent.putExtra("USER_UID", uid);
                startActivity(intent);
            }
            else if (id == R.id.btnSignOut) {
                FirebaseAuth.getInstance().signOut();
                SharedPreferencesUtil.signOutUser(this);
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        };

        btn3.setOnClickListener(listener);
        btn4.setOnClickListener(listener);
        btn5.setOnClickListener(listener);
        btnSignOut.setOnClickListener(listener);
    }
}
