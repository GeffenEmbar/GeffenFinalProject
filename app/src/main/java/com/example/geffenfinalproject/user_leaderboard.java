package com.example.geffenfinalproject;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geffenfinalproject.adapters.LeaderboardAdapter;
import com.example.geffenfinalproject.models.User;
import com.example.geffenfinalproject.services.DatabaseService;

import java.util.Collections;
import java.util.List;

public class user_leaderboard extends AppCompatActivity {

    private static final String TAG = "leaderboard";

    private LeaderboardAdapter adapter;
    private DatabaseService databaseService;

    private TextView tvFirstName, tvFirstScore;
    private TextView tvSecondName, tvSecondScore;
    private TextView tvThirdName, tvThirdScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_leaderboard);

        RecyclerView recyclerView = findViewById(R.id.rv_leaderboard);

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
    }

    @Override
    protected void onResume() {
        super.onResume();

        databaseService.getUserList(new DatabaseService.DatabaseCallback<List<User>>() {

            @Override
            public void onCompleted(List<User> users) {

                Collections.sort(users, (u1, u2) ->
                        Integer.compare(u2.getCorrect_answers(), u1.getCorrect_answers())
                );

                // Top 3
                if(users.size() > 0){
                    tvFirstName.setText(users.get(0).getFname());
                    tvFirstScore.setText(String.valueOf(users.get(0).getCorrect_answers()));
                }

                if(users.size() > 1){
                    tvSecondName.setText(users.get(1).getFname());
                    tvSecondScore.setText(String.valueOf(users.get(1).getCorrect_answers()));
                }

                if(users.size() > 2){
                    tvThirdName.setText(users.get(2).getFname());
                    tvThirdScore.setText(String.valueOf(users.get(2).getCorrect_answers()));
                }

                // Remaining players
                if(users.size() > 3){
                    adapter.setUserList(users.subList(3, users.size()));
                }
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "Failed loading leaderboard", e);
            }
        });
    }
}