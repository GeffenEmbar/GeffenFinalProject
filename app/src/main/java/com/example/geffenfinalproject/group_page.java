package com.example.geffenfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
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
import java.util.Collections;
import java.util.List;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class group_page extends BaseActivity implements UserAdapter.OnUserClickListener {

    private static final String TAG = "group_page";
    private TextView tvGroupName, tvTotalCorrect, tvTotalWrong;
    private TextView tvTotalNotes, tvTotalIntervals, tvTotalChords, tvTotalQuiz, tvTotalComplexChords;
    private RecyclerView rvChat;
    private UserAdapter userAdapter;
    private GroupChatAdapter chatAdapter;
    private DatabaseService databaseService;
    private EditText etChatMessage;
    private Button btnSendMessage, btnLeaveGroup, btnDeleteGroup, btnShowMembers;
    private ImageButton btnEditGroupName;
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
        tvTotalCorrect = findViewById(R.id.tv_total_correct);
        tvTotalWrong = findViewById(R.id.tv_total_wrong);
        tvTotalNotes = findViewById(R.id.tv_total_notes);
        tvTotalIntervals = findViewById(R.id.tv_total_intervals);
        tvTotalChords = findViewById(R.id.tv_total_chords);
        tvTotalQuiz = findViewById(R.id.tv_total_quiz);
        tvTotalComplexChords = findViewById(R.id.tv_total_complex_chords);
        
        currentUser = SharedPreferencesUtil.getUser(this);
        userAdapter = new UserAdapter(this);

        // Chat initialization
        rvChat = findViewById(R.id.rv_chat);
        etChatMessage = findViewById(R.id.et_chat_message);
        btnSendMessage = findViewById(R.id.btn_send_message);
        btnLeaveGroup = findViewById(R.id.btn_leave_group);
        btnDeleteGroup = findViewById(R.id.btn_delete_group);
        btnEditGroupName = findViewById(R.id.btn_edit_group_name);
        btnShowMembers = findViewById(R.id.btn_show_members);

        rvChat.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new GroupChatAdapter(currentUser != null ? currentUser.getId() : "");
        rvChat.setAdapter(chatAdapter);

        btnSendMessage.setOnClickListener(v -> sendMessage());
        btnLeaveGroup.setOnClickListener(v -> leaveGroup());
        btnDeleteGroup.setOnClickListener(v -> deleteGroup());
        btnEditGroupName.setOnClickListener(v -> showEditGroupNameDialog());
        btnShowMembers.setOnClickListener(v -> showMembersDialog());

        databaseService = DatabaseService.getInstance();
    }

    private void showMembersDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_group_members, null);
        dialog.setContentView(dialogView);

        RecyclerView rvMembers = dialogView.findViewById(R.id.rv_members_dialog);
        rvMembers.setLayoutManager(new LinearLayoutManager(this));
        rvMembers.setAdapter(userAdapter);

        dialogView.findViewById(R.id.btn_close_members).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showEditGroupNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Group Name");

        final EditText input = new EditText(this);
        input.setHint("Enter new group name");
        input.setText(tvGroupName.getText().toString());
        input.setPadding(50, 20, 50, 20);
        builder.setView(input);

        builder.setPositiveButton("Change", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                confirmGroupNameChange(newName);
            } else {
                Toast.makeText(this, "Group name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void confirmGroupNameChange(String newName) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Name Change")
                .setMessage("Are you sure you want to change the group name to \"" + newName + "\"?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    databaseService.updateGroupName(currentGroupId, newName, new DatabaseService.DatabaseCallback<Void>() {
                        @Override
                        public void onCompleted(Void object) {
                            tvGroupName.setText(newName);
                            Toast.makeText(group_page.this, "Group name updated successfully", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailed(Exception e) {
                            Toast.makeText(group_page.this, "Failed to update group name: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onUserClick(User user) {
        // Option to view profile or something
    }

    @Override
    public void onLongUserClick(User user) {
        // Maybe another way to kick
    }

    @Override
    public void onKickClick(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Kick Member")
                .setMessage("Are you sure you want to kick " + user.getFname() + " " + user.getLname() + " from the group?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    databaseService.kickUser(user.getId(), currentGroupId, new DatabaseService.DatabaseCallback<Void>() {
                        @Override
                        public void onCompleted(Void object) {
                            Toast.makeText(group_page.this, user.getFname() + " has been kicked", Toast.LENGTH_SHORT).show();
                            loadMembers(currentGroupId); // Refresh the list
                        }

                        @Override
                        public void onFailed(Exception e) {
                            Toast.makeText(group_page.this, "Failed to kick user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void leaveGroup() {
        if (currentUser == null || currentGroupId == null) return;

        databaseService.leaveGroup(currentUser.getId(), currentGroupId, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                currentUser.setGroupId(null);
                SharedPreferencesUtil.saveUser(group_page.this, currentUser);
                Toast.makeText(group_page.this, "Left group successfully", Toast.LENGTH_SHORT).show();
                
                Intent intent = new Intent(group_page.this, user_menu.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(group_page.this, "Failed to leave group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteGroup() {
        if (currentGroupId == null) return;

        databaseService.deleteGroup(currentGroupId, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                currentUser.setGroupId(null);
                SharedPreferencesUtil.saveUser(group_page.this, currentUser);
                Toast.makeText(group_page.this, "Group deleted successfully", Toast.LENGTH_SHORT).show();
                
                Intent intent = new Intent(group_page.this, user_menu.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(group_page.this, "Failed to delete group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
                    
                    // Show delete and edit buttons only for owner
                    if (currentUser.getId().equals(group.getOwnerUid())) {
                        btnDeleteGroup.setVisibility(android.view.View.VISIBLE);
                        btnEditGroupName.setVisibility(android.view.View.VISIBLE);
                    } else {
                        btnDeleteGroup.setVisibility(android.view.View.GONE);
                        btnEditGroupName.setVisibility(android.view.View.GONE);
                    }

                    userAdapter.setIds(currentUser.getId(), group.getOwnerUid());
                    loadMembers(currentGroupId);
                    startChatListener(currentGroupId);
                } else {
                    Log.w(TAG, "Group not found for ID: " + currentGroupId);
                    // If group doesn't exist anymore, clear local user data and finish
                    currentUser.setGroupId(null);
                    SharedPreferencesUtil.saveUser(group_page.this, currentUser);
                    finish();
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
                int totalCorrect = 0;
                int totalWrong = 0;
                int totalNotes = 0;
                int totalIntervals = 0;
                int totalChords = 0;
                int totalQuiz = 0;
                int totalComplexChords = 0;
                
                for (User user : users) {
                    if (groupId.equals(user.getGroupId())) {
                        groupMembers.add(user);
                        totalCorrect += user.getCorrect_answers();
                        totalWrong += user.getWrong_answers();
                        totalNotes += user.getNotesCorrect();
                        totalIntervals += user.getIntervalsCorrect();
                        totalChords += user.getChordsCorrect();
                        totalQuiz += user.getQuizCorrect();
                        totalComplexChords += user.getComplexChordsCorrect();
                    }
                }
                
                // Sort by total correct descending
                Collections.sort(groupMembers, (u1, u2) -> Integer.compare(u2.getCorrect_answers(), u1.getCorrect_answers()));
                
                userAdapter.setUserList(groupMembers);
                tvTotalCorrect.setText("Total Correct: " + totalCorrect);
                tvTotalWrong.setText("Total Wrong: " + totalWrong);
                tvTotalNotes.setText("Notes Correct: " + totalNotes);
                tvTotalIntervals.setText("Intervals Correct: " + totalIntervals);
                tvTotalChords.setText("Chords Correct: " + totalChords);
                tvTotalQuiz.setText("General Quiz Correct: " + totalQuiz);
                tvTotalComplexChords.setText("Complex Chords Correct: " + totalComplexChords);
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