package com.example.geffenfinalproject;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geffenfinalproject.adapters.GroupAdapter;
import com.example.geffenfinalproject.models.Group;
import com.example.geffenfinalproject.models.User;
import com.example.geffenfinalproject.services.DatabaseService;
import com.example.geffenfinalproject.utils.SharedPreferencesUtil;
import com.google.firebase.auth.FirebaseAuth;

import android.content.Intent;
import android.widget.Toast;

import java.util.List;

public class group_table extends BaseActivity {

    private static final String TAG = "group_table";

    private GroupAdapter groupAdapter;
    private TextView tvGroupCount;
    private DatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_group_table);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseService = DatabaseService.getInstance();

        RecyclerView groupList = findViewById(R.id.rv_groups_list);
        tvGroupCount = findViewById(R.id.tv_group_count);

        groupList.setLayoutManager(new LinearLayoutManager(this));

        groupAdapter = new GroupAdapter();
        groupList.setAdapter(groupAdapter);

        groupAdapter.setOnItemClickListener(group -> {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            databaseService.joinGroup(uid, group.getGroupId(), new DatabaseService.DatabaseCallback<Void>() {
                @Override
                public void onCompleted(Void object) {
                    Toast.makeText(group_table.this, "Joined " + group.getGroupName(), Toast.LENGTH_SHORT).show();
                    
                    // Fetch the updated user to get the new groupId
                    databaseService.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
                        @Override
                        public void onCompleted(User updatedUser) {
                            if (updatedUser != null) {
                                // Update local shared preferences
                                SharedPreferencesUtil.saveUser(group_table.this, updatedUser);
                                
                                // Redirect to group page
                                Intent intent = new Intent(group_table.this, group_page.class);
                                startActivity(intent);
                                finish(); // Optional: close group_table so back button doesn't return here
                            }
                        }

                        @Override
                        public void onFailed(Exception e) {
                            Log.e(TAG, "Failed to fetch updated user", e);
                            onResume(); // Fallback to refreshing the list
                        }
                    });
                }

                @Override
                public void onFailed(Exception e) {
                    Toast.makeText(group_table.this, "Failed to join group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        databaseService.getGroupList(new DatabaseService.DatabaseCallback<List<Group>>() {
            @Override
            public void onCompleted(List<Group> groups) {

                groupAdapter.setGroupList(groups);

                tvGroupCount.setText("Total groups: " + groups.size());
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "Failed to get group list", e);
            }
        });
    }
}
