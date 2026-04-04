package com.example.geffenfinalproject;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.geffenfinalproject.models.User;
import com.example.geffenfinalproject.services.DatabaseService;
import com.example.geffenfinalproject.services.ReminderReceiver;
import com.example.geffenfinalproject.utils.SharedPreferencesUtil;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

public class user_menu extends BaseActivity implements View.OnClickListener {

    Button btnChordsQuiz, btnComplexChordsQuiz, piano_notes_quiz, questionnaire, btnIntervalsQuiz, btnLeaderboard, btnGroups, btnProfile;

    private static final int PERMISSION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        checkPermissionsAndScheduleAlarm();

        btnChordsQuiz = findViewById(R.id.btnChordsQuiz);
        btnChordsQuiz.setOnClickListener(this);
        btnComplexChordsQuiz = findViewById(R.id.btnComplexChordsQuiz);
        btnComplexChordsQuiz.setOnClickListener(this);
        piano_notes_quiz = findViewById(R.id.piano_notes_quiz);
        piano_notes_quiz.setOnClickListener(this);
        questionnaire = findViewById(R.id.questionnaire);
        questionnaire.setOnClickListener(this);
        btnIntervalsQuiz = findViewById(R.id.btnIntervalsQuiz);
        btnIntervalsQuiz.setOnClickListener(this);
        btnLeaderboard = findViewById(R.id.btnLeaderboard);
        btnLeaderboard.setOnClickListener(this);
        btnGroups = findViewById(R.id.btnGroups);
        btnGroups.setOnClickListener(this);
        btnProfile = findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnChordsQuiz) {
            Intent intent = new Intent(this, user_chords_quiz.class);
            startActivity(intent);
        }
        else if (v.getId() == R.id.btnComplexChordsQuiz) {
            Intent intent = new Intent(this, user_complex_chords_quiz.class);
            startActivity(intent);
        }
        else if (v.getId() == R.id.piano_notes_quiz) {
            Intent intent = new Intent(this, user_notes_quiz.class);
            startActivity(intent);
        }
        else if (v.getId() == questionnaire.getId()) {
            Intent intent = new Intent(this, user_questions.class);
            startActivity(intent);
        }
        else if (v.getId() == btnIntervalsQuiz.getId()) {
            Intent intent = new Intent(this, user_intervals_quiz.class);
            startActivity(intent);
        }
        else if (v.getId() == btnLeaderboard.getId()) {
            Intent intent = new Intent(this, user_leaderboard.class);
            startActivity(intent);
        }
        else if (v.getId() == btnGroups.getId()) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseService.getInstance().getUser(uid, new DatabaseService.DatabaseCallback<User>() {
                @Override
                public void onCompleted(User user) {
                    if (user != null) {
                        SharedPreferencesUtil.saveUser(user_menu.this, user);
                        Intent intent;
                        if (user.getGroupId() == null || user.getGroupId().isEmpty()) {
                            intent = new Intent(user_menu.this, user_group.class);
                        } else {
                            intent = new Intent(user_menu.this, group_page.class);
                        }
                        startActivity(intent);
                    }
                }

                @Override
                public void onFailed(Exception e) {
                    // Fallback to shared preferences if database fetch fails
                    User user = SharedPreferencesUtil.getUser(user_menu.this);
                    Intent intent;
                    if (user == null || user.getGroupId() == null) {
                        intent = new Intent(user_menu.this, user_group.class);
                    } else {
                        intent = new Intent(user_menu.this, group_page.class);
                    }
                    startActivity(intent);
                }
            });
        }
        else if (v.getId() == btnProfile.getId()) {
            User user = SharedPreferencesUtil.getUser(this);
            Intent intent = new Intent(this, user_profile.class);
            intent.putExtra("USER_UID", user.getId());
            startActivity(intent);
        }
    }

    private void checkPermissionsAndScheduleAlarm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE
                );
            } else {
                scheduleDailyReminder(this);
            }
        } else {
            scheduleDailyReminder(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scheduleDailyReminder(this);
            }
        }
    }

    public static void scheduleDailyReminder(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );
    }
}
