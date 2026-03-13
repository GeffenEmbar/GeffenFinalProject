package com.example.geffenfinalproject;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.geffenfinalproject.models.Group;
import com.example.geffenfinalproject.models.User;
import com.example.geffenfinalproject.services.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;

public class create_group extends AppCompatActivity {

    private EditText editGroupName;
    private Button btnCreateGroup;

    private DatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_group);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editGroupName = findViewById(R.id.etGroupName);
        btnCreateGroup = findViewById(R.id.btnCreateGroup);

        databaseService = DatabaseService.getInstance();

        btnCreateGroup.setOnClickListener(v -> createGroup());
    }

    private void createGroup() {
        String groupName = editGroupName.getText().toString().trim();

        if (groupName.isEmpty()) {
            Toast.makeText(this, "Enter group name", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "No logged-in user found", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        databaseService.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                if (user == null) {
                    Toast.makeText(create_group.this, "User data not found in database", Toast.LENGTH_SHORT).show();
                    return;
                }

                String groupId = databaseService.generateGroupId();
                if (groupId == null || groupId.isEmpty()) {
                    Toast.makeText(create_group.this, "Failed to generate group ID", Toast.LENGTH_SHORT).show();
                    return;
                }

                Group group = new Group();
                group.setGroupId(groupId);
                group.setGroupName(groupName);
                group.setOwnerUid(uid);

                Map<String, Boolean> members = new HashMap<>();
                members.put(uid, true);
                group.setMembers(members);

                group.setTotalQuestions(user.getCorrect_answers());

                databaseService.createNewGroup(group, new DatabaseService.DatabaseCallback<Void>() {
                    @Override
                    public void onCompleted(Void object) {
                        Toast.makeText(create_group.this, "Group Created!", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onFailed(Exception e) {
                        String message = (e != null && e.getMessage() != null)
                                ? e.getMessage()
                                : "Unknown error";
                        Toast.makeText(create_group.this, "Failed: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailed(Exception e) {
                String message = (e != null && e.getMessage() != null)
                        ? e.getMessage()
                        : "Unknown error";
                Toast.makeText(create_group.this, "User load failed: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}