package com.example.geffenfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geffenfinalproject.adapters.UserAdapter;
import com.example.geffenfinalproject.models.User;
import com.example.geffenfinalproject.services.DatabaseService;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class admin_user_table extends AppCompatActivity {

    private static final String TAG = "admin_user_table";
    private UserAdapter userAdapter;
    private TextView tvUserCount;
    DatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_user_table);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseService = DatabaseService.getInstance();
        RecyclerView usersList = findViewById(R.id.rv_users_list);
        tvUserCount = findViewById(R.id.tv_user_count);
        usersList.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(new UserAdapter.OnUserClickListener() {

            @Override
            public void onUserClick(User user) {
                // Handle user click
                Log.d(TAG, "User clicked: " + user);
                Intent intent = new Intent(admin_user_table.this, user_profile.class);
                intent.putExtra("USER_UID", user.getId());
                startActivity(intent);
            }
            @Override
            public void onLongUserClick(User user) {
                // Handle long user click
                Intent intent = new Intent(admin_user_table.this, user_profile.class);
                intent.putExtra("USER_UID", user.getId());
                startActivity(intent);
            }
        });
        usersList.setAdapter(userAdapter);
    }
    @Override
    protected void onResume() {
        super.onResume();
        databaseService.getUserList(new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(List<User> users) {
                userAdapter.setUserList(users);
                tvUserCount.setText("Total users: " + users.size());
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "Failed to get users list", e);
            }
        });
    }
}