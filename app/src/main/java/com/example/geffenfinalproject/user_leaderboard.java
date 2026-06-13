package com.example.geffenfinalproject;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geffenfinalproject.adapters.LeaderboardAdapter;
import com.example.geffenfinalproject.models.User;
import com.example.geffenfinalproject.services.DatabaseService;
import com.example.geffenfinalproject.utils.SharedPreferencesUtil;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class user_leaderboard extends BaseActivity {

    private static final String TAG = "leaderboard";

    private LeaderboardAdapter adapter;
    private DatabaseService databaseService;

    private TextView tvFirstName, tvFirstScore;
    private TextView tvSecondName, tvSecondScore;
    private TextView tvThirdName, tvThirdScore;
    private TabLayout tabLayout;

    private List<User> allUsers = new ArrayList<>();
    private LeaderboardAdapter.Mode currentMode = LeaderboardAdapter.Mode.TOTAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_leaderboard);

        RecyclerView recyclerView = findViewById(R.id.rv_leaderboard);
        tabLayout = findViewById(R.id.tl_leaderboard);

        tvFirstName = findViewById(R.id.tv_first_name);
        tvFirstScore = findViewById(R.id.tv_first_score);

        tvSecondName = findViewById(R.id.tv_second_name);
        tvSecondScore = findViewById(R.id.tv_second_score);

        tvThirdName = findViewById(R.id.tv_third_name);
        tvThirdScore = findViewById(R.id.tv_third_score);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new LeaderboardAdapter();
        recyclerView.setAdapter(adapter);

        databaseService = DatabaseService.getInstance();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: currentMode = LeaderboardAdapter.Mode.TOTAL; break;
                    case 1: currentMode = LeaderboardAdapter.Mode.NOTES; break;
                    case 2: currentMode = LeaderboardAdapter.Mode.INTERVALS; break;
                    case 3: currentMode = LeaderboardAdapter.Mode.QUIZ; break;
                    case 4: currentMode = LeaderboardAdapter.Mode.CHORDS; break;
                    case 5: currentMode = LeaderboardAdapter.Mode.COMPLEX_CHORDS; break;
                }
                updateLeaderboardUI();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        databaseService.getUserList(new DatabaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                allUsers.clear();
                if (users != null) {
                    allUsers.addAll(users);
                }
                updateLeaderboardUI();
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "Failed loading leaderboard", e);
            }
        });
    }

    private void updateLeaderboardUI() {
        if (allUsers == null || allUsers.isEmpty()) return;

        // Sort based on current mode
        // פעולת הסידור של המערכת זקוקה רק לקבוצה של שני משתמשים ומסדרת את כל הרשימה מהגבוה ביותר במקום הראשון לנמוך ביותר למקום האחרון
        Collections.sort(allUsers, (u1, u2) -> {
            int s1 = getScoreForMode(u1);
            int s2 = getScoreForMode(u2);
            return Integer.compare(s2, s1);
        });

        // Update Podium
        updatePodium();

        // Remaining players
        adapter.setMode(currentMode);
        if (allUsers.size() > 3) {
            // יש פה פילטר בגלל שלוקחים רק משתתפים באפליקציה שהראנק שלהם ארבע ומעלה
            adapter.setStartRank(4);
            adapter.setUserList(new ArrayList<>(allUsers.subList(3, allUsers.size())));
        } else {
            adapter.setUserList(new ArrayList<>());
        }
    }

    private void updatePodium() {
        tvFirstName.setText("---"); tvFirstScore.setText("0");
        tvSecondName.setText("---"); tvSecondScore.setText("0");
        tvThirdName.setText("---"); tvThirdScore.setText("0");

        if (allUsers.size() > 0) {
            User u = allUsers.get(0);
            tvFirstName.setText(u.getFname());
            tvFirstScore.setText(String.valueOf(getScoreForMode(u)));
        }
        if (allUsers.size() > 1) {
            User u = allUsers.get(1);
            tvSecondName.setText(u.getFname());
            tvSecondScore.setText(String.valueOf(getScoreForMode(u)));
        }
        if (allUsers.size() > 2) {
            User u = allUsers.get(2);
            tvThirdName.setText(u.getFname());
            tvThirdScore.setText(String.valueOf(getScoreForMode(u)));
        }
    }

    private int getScoreForMode(User u) {
        switch (currentMode) {
            case TOTAL: return u.getCorrect_answers();
            case NOTES: return u.getNotesCorrect();
            case INTERVALS: return u.getIntervalsCorrect();
            case QUIZ: return u.getQuizCorrect();
            case CHORDS: return u.getChordsCorrect();
            case COMPLEX_CHORDS: return u.getComplexChordsCorrect();
            default: return 0;
        }
    }
}