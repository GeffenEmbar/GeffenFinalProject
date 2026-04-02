package com.example.geffenfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geffenfinalproject.adapters.GroupChatAdapter;
import com.example.geffenfinalproject.adapters.UserAdapter;
import com.example.geffenfinalproject.models.Group;
import com.example.geffenfinalproject.models.GroupMessage;
import com.example.geffenfinalproject.models.User;
import com.example.geffenfinalproject.services.DatabaseService;
import com.example.geffenfinalproject.utils.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

public class group_page extends BaseActivity {

    private static final String TAG = "group_page";
    private TextView tvGroupName;
    private RecyclerView rvMembers, rvChat;
    private UserAdapter userAdapter;
    private GroupChatAdapter chatAdapter;
    private DatabaseService databaseService;
    private EditText etChatMessage;
    private Button btnSendMessage;
    private String currentGroupId;
    private User currentUser;

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

        currentUser = SharedPreferencesUtil.getUser(this);
        
        userAdapter = new UserAdapter(null);
        rvMembers.setAdapter(userAdapter);

        // Chat initialization
        rvChat = findViewById(R.id.rv_chat);
        etChatMessage = findViewById(R.id.et_chat_message);
        btnSendMessage = findViewById(R.id.btn_send_message);

        rvChat.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new GroupChatAdapter(currentUser != null ? currentUser.getId() : "");
        rvChat.setAdapter(chatAdapter);

        btnSendMessage.setOnClickListener(v -> sendMessage());

        databaseService = DatabaseService.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGroupData();
    }

    private void loadGroupData() {
        currentUser = SharedPreferencesUtil.getUser(this);
        if (currentUser == null || currentUser.getGroupId() == null) {
            Log.w(TAG, "Current user or group ID is null");
            return;
        }

        currentGroupId = currentUser.getGroupId();

        databaseService.getGroup(currentGroupId, new DatabaseService.DatabaseCallback<Group>() {
            @Override
            public void onCompleted(Group group) {
                if (group != null) {
                    tvGroupName.setText(group.getGroupName());
                    loadMembers(currentGroupId);
                    startChatListener(currentGroupId);
                } else {
                    Log.w(TAG, "Group not found for ID: " + currentGroupId);
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

    private void startChatListener(String groupId) {
        databaseService.listenForGroupMessages(groupId, new DatabaseService.DatabaseCallback<List<GroupMessage>>() {
            @Override
            public void onCompleted(List<GroupMessage> messages) {
                chatAdapter.setMessageList(messages);
                if (messages.size() > 0) {
                    rvChat.scrollToPosition(messages.size() - 1);
                }
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "Failed to listen for messages", e);
            }
        });
    }

    private void sendMessage() {
        String messageText = etChatMessage.getText().toString().trim();
        if (messageText.isEmpty()) return;

        if (currentUser == null || currentGroupId == null) {
            Toast.makeText(this, "Error sending message", Toast.LENGTH_SHORT).show();
            return;
        }

        String messageId = databaseService.generateMessageId(currentGroupId);
        String senderName = currentUser.getFname() + " " + currentUser.getLname();
        GroupMessage message = new GroupMessage(
                messageId,
                currentUser.getId(),
                senderName,
                messageText,
                System.currentTimeMillis()
        );

        databaseService.sendGroupMessage(currentGroupId, message, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                etChatMessage.setText("");
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(group_page.this, "Failed to send message", Toast.LENGTH_SHORT).show();
            }
        });
    }
}