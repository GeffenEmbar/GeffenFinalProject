package com.example.geffenfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class admin_menu extends AppCompatActivity implements View.OnClickListener {
    Button user_table, admin_add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        user_table = findViewById(R.id.user_table);
        user_table.setOnClickListener(this);
        admin_add = findViewById(R.id.admin_add);
        admin_add.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.user_table) {
            Intent intent = new Intent(this, admin_user_table.class);
            startActivity(intent);
        } else if (v.getId() == admin_add.getId()) {
            Intent intent = new Intent(this, admin_add_question.class);
            startActivity(intent);
        }
    }
}