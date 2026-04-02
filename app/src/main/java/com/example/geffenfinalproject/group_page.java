package com.example.geffenfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geffenfinalproject.adapters.UserAdapter;
import com.example.geffenfinalproject.models.Group;
import com.example.geffenfinalproject.models.User;
import com.example.geffenfinalproject.services.DatabaseService;
import com.example.geffenfinalproject.utils.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

public class group_page extends BaseActivity {

    private static final String TAG = "group_page";
    private TextView tvGroupName;
    private RecyclerView rvMembers;
    private UserAdapter userAdapter;
    private DatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_group_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvGroupName = findViewById(R.id.tvGroupName);
        rvMembers = findViewById(R.id.rv_users_list);
        rvMembers.setLayoutManager(new LinearLayoutManager(this));
        
        userAdapter = new UserAdapter(null);
        rvMembers.setAdapter(userAdapter);

        databaseService = DatabaseService.getInstance();
        }

        @Override
        protected void onResume() {
        super.onResume();
        loadGroupData();
        }

        private void loadGroupData() {
        User currentUser = SharedPreferencesUtil.getUser(this);
        if (currentUser == null || currentUser.getGroupId() == null) {
            Log.w(TAG, "Current user or group ID is null");
            // If the user has no group, they shouldn't be here. 
            // We could potentially finish the activity.
            return;
        }

        String groupId = currentUser.getGroupId();

        databaseService.getGroup(groupId, new DatabaseService.DatabaseCallback<Group>() {
            @Override
            public void onCompleted(Group group) {
                if (group != null) {
                    tvGroupName.setText(group.getGroupName());
                    loadMembers(groupId);
                } else {
                    Log.w(TAG, "Group not found for ID: " + groupId);
                }
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "Failed to load group", e);
            }
        });
        }

        private void loadMembers(String groupId) {
        databaseService.getUserList(new DatabaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                List<User> groupMembers = new ArrayList<>();
                for (User user : users) {
                    if (groupId.equals(user.getGroupId())) {
                        groupMembers.add(user);
                    }
                }
                userAdapter.setUserList(groupMembers);
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "Failed to load users", e);
            }
        });
        }
        }