package com.example.geffenfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.geffenfinalproject.models.User;
import com.example.geffenfinalproject.services.DatabaseService;
import com.example.geffenfinalproject.utils.SharedPreferencesUtil;
import com.google.firebase.auth.FirebaseAuth;

public class user_group extends BaseActivity implements View.OnClickListener {

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
    protected void onResume() {
        super.onResume();
        checkUserGroup();
    }

    private void checkUserGroup() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseService.getInstance().getUser(uid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                if (user != null) {
                    SharedPreferencesUtil.saveUser(user_group.this, user);
                    if (user.getGroupId() != null && !user.getGroupId().isEmpty()) {
                        Intent intent = new Intent(user_group.this, group_page.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onFailed(Exception e) {
                // Ignore failure here, user can still use the page
            }
        });
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
