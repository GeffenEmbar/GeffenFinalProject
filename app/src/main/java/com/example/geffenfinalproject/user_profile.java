package com.example.geffenfinalproject;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.geffenfinalproject.adapters.ImageSourceAdapter;
import com.example.geffenfinalproject.models.Group;
import com.example.geffenfinalproject.models.ImageSourceOption;
import com.example.geffenfinalproject.models.User;
import com.example.geffenfinalproject.services.DatabaseService;
import com.example.geffenfinalproject.utils.ImageUtil;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.firebase.auth.FirebaseAuth;

public class user_profile extends BaseActivity {

    private TextView etFirstName, etLastName, etPhone;
    private TextView tvNotesStats, tvIntervalsStats, tvChordsStats, tvQuizStats, tvComplexChordsStats, tvTotalStats;
    private TextView tvGroupName, tvNotesRank, tvIntervalsRank, tvChordsRank, tvQuizRank, tvComplexChordsRank;
    private ImageView profileImage;
    private Button btnUpdate, btnAdminMenu;

    private String userUid;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);

        userUid = getIntent().getStringExtra("USER_UID");

        if (userUid == null) {
            Toast.makeText(this, "No user selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        usersRef = FirebaseDatabase.getInstance().getReference("users").child(userUid);

        // INIT
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etPhone = findViewById(R.id.etPhoneNum);

        tvNotesStats = findViewById(R.id.tvNotesStats);
        tvIntervalsStats = findViewById(R.id.tvIntervalsStats);
        tvChordsStats = findViewById(R.id.tvChordsStats);
        tvQuizStats = findViewById(R.id.tvQuizStats);
        tvComplexChordsStats = findViewById(R.id.tvComplexChordsStats);
        tvTotalStats = findViewById(R.id.tvTotalStats);

        tvGroupName = findViewById(R.id.tvGroupName);
        tvNotesRank = findViewById(R.id.tvNotesRank);
        tvIntervalsRank = findViewById(R.id.tvIntervalsRank);
        tvChordsRank = findViewById(R.id.tvChordsRank);
        tvQuizRank = findViewById(R.id.tvQuizRank);
        tvComplexChordsRank = findViewById(R.id.tvComplexChordsRank);

        profileImage = findViewById(R.id.profileImage);
        btnUpdate = findViewById(R.id.btnGoToUpdate);
        btnAdminMenu = findViewById(R.id.btnAdminMenu);

        // Only show the update button if the viewing user is the profile owner
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (currentUid.equals(userUid)) {
            btnUpdate.setVisibility(View.VISIBLE);
        } else {
            btnUpdate.setVisibility(View.GONE);
        }

        // Show Admin Menu button if current user is admin
        DatabaseService.getInstance().getUser(currentUid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                if (user != null && user.isAdmin()) {
                    btnAdminMenu.setVisibility(View.VISIBLE);
                } else {
                    btnAdminMenu.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailed(Exception e) {
                btnAdminMenu.setVisibility(View.GONE);
            }
        });

        loadUserData();

        // UPDATE BUTTON
        btnUpdate.setOnClickListener(v -> {
            Intent intent = new Intent(this, user_update_profile.class);
            intent.putExtra("USER_UID", userUid);
            startActivity(intent);
        });

        // ADMIN MENU BUTTON
        btnAdminMenu.setOnClickListener(v -> {
            Intent intent = new Intent(this, admin_menu.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }

    private void loadUserData() {
        usersRef.get().addOnSuccessListener(snapshot -> {

            User user = snapshot.getValue(User.class);

            if (user == null) return;

            // FILL FIELDS
            etFirstName.setText(user.getFname());
            etLastName.setText(user.getLname());
            etPhone.setText(user.getPhone());

            tvNotesStats.setText(user.getNotesCorrect() + "/" + user.getNotesWrong() + " | " + user.getNotesStreak() + " | " + user.getMaxNotesStreak());
            tvIntervalsStats.setText(user.getIntervalsCorrect() + "/" + user.getIntervalsWrong() + " | " + user.getIntervalsStreak() + " | " + user.getMaxIntervalsStreak());
            tvChordsStats.setText(user.getChordsCorrect() + "/" + user.getChordsWrong() + " | " + user.getChordsStreak() + " | " + user.getMaxChordsStreak());
            tvComplexChordsStats.setText(user.getComplexChordsCorrect() + "/" + user.getComplexChordsWrong() + " | " + user.getComplexChordsStreak() + " | " + user.getMaxComplexChordsStreak());
            tvQuizStats.setText(user.getQuizCorrect() + "/" + user.getQuizWrong() + " | " + user.getQuizStreak() + " | " + user.getMaxQuizStreak());
            tvTotalStats.setText(user.getCorrect_answers() + " / " + user.getWrong_answers());

            // LOAD IMAGE
            if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                Bitmap bitmap = ImageUtil.convertFrom64base(user.getProfileImage());
                if (bitmap != null) {
                    profileImage.setImageBitmap(bitmap);
                }
            }

            // GROUP
            bindGroupName(user.getGroupId());
            bindUserRanks(user.getGroupId(), userUid);

        });
    }

    private void bindUserRanks(String groupId, String currentUserId) {
        if (groupId == null || groupId.trim().isEmpty()) {
            tvNotesRank.setText("Notes Rank: N/A");
            tvIntervalsRank.setText("Intervals Rank: N/A");
            tvQuizRank.setText("General Quiz Rank: N/A");
            return;
        }

        DatabaseService.getInstance().getUserList(new DatabaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                // Filter by group
                List<User> groupUsers = new ArrayList<>();
                for (User u : users) {
                    if (groupId.equals(u.getGroupId())) {
                        groupUsers.add(u);
                    }
                }

                // Rank for Notes
                tvNotesRank.setText("Notes Rank: " + calculateRank(groupUsers, currentUserId, "NOTES"));
                // Rank for Intervals
                tvIntervalsRank.setText("Intervals Rank: " + calculateRank(groupUsers, currentUserId, "INTERVALS"));
                // Rank for Chords
                tvChordsRank.setText("Chords Rank: " + calculateRank(groupUsers, currentUserId, "CHORDS"));
                // Rank for Complex Chords
                tvComplexChordsRank.setText("Complex Chords Rank: " + calculateRank(groupUsers, currentUserId, "COMPLEX_CHORDS"));
                // Rank for Quiz
                tvQuizRank.setText("General Quiz Rank: " + calculateRank(groupUsers, currentUserId, "QUIZ"));
            }

            @Override
            public void onFailed(Exception e) {
                tvNotesRank.setText("Error");
                tvIntervalsRank.setText("Error");
                tvChordsRank.setText("Error");
                tvComplexChordsRank.setText("Error");
                tvQuizRank.setText("Error");
            }
        });
    }

    private String calculateRank(List<User> groupUsers, String currentUserId, String type) {
        // Sort by correct answers descending
        Collections.sort(groupUsers, (u1, u2) -> {
            int s1 = getScoreByType(u1, type);
            int s2 = getScoreByType(u2, type);
            return Integer.compare(s2, s1);
        });

        int rank = -1;
        int currentRank = 1;
        for (int i = 0; i < groupUsers.size(); i++) {
            if (i > 0 && getScoreByType(groupUsers.get(i), type) < getScoreByType(groupUsers.get(i - 1), type)) {
                currentRank = i + 1;
            }
            
            if (groupUsers.get(i).getId().equals(currentUserId)) {
                rank = currentRank;
                break;
            }
        }
        return rank != -1 ? String.valueOf(rank) : "N/A";
    }

    private int getScoreByType(User user, String type) {
        switch (type) {
            case "NOTES": return user.getMaxNotesStreak();
            case "INTERVALS": return user.getMaxIntervalsStreak();
            case "CHORDS": return user.getMaxChordsStreak();
            case "COMPLEX_CHORDS": return user.getMaxComplexChordsStreak();
            case "QUIZ": return user.getMaxQuizStreak();
            default: return user.getCorrect_answers();
        }
    }

    private void bindGroupName(String groupId) {
        if (groupId == null || groupId.trim().isEmpty()) {
            tvGroupName.setText("No Group");
            return;
        }

        tvGroupName.setText("Group...");

        DatabaseService.getInstance().getGroup(groupId, new DatabaseService.DatabaseCallback<Group>() {
            @Override
            public void onCompleted(Group guild) {
                String groupName = guild != null ? guild.getGroupName() : null;

                if (groupName == null || groupName.trim().isEmpty()) {
                    tvGroupName.setText("Unknown Group");
                    return;
                }

                tvGroupName.setText(groupName.trim());
            }

            @Override
            public void onFailed(Exception e) {
                tvGroupName.setText("Unknown Group");
            }
        });
    }
}