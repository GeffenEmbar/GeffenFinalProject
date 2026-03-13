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

public class user_group extends AppCompatActivity implements View.OnClickListener {

    Button btnGroupTable, btnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_group);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnGroupTable = findViewById(R.id.btnGroupTable);
        btnGroupTable.setOnClickListener(this);
        btnCreate = findViewById(R.id.btnCreate);
        btnCreate.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == btnGroupTable.getId()) {
            Intent intent = new Intent(this, group_table.class);
            startActivity(intent);
        }
        else if (v.getId() == btnCreate.getId()) {
            Intent intent = new Intent(this, create_group.class);
            startActivity(intent);
        }
    }
}