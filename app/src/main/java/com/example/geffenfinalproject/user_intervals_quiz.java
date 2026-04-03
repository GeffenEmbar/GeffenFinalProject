package com.example.geffenfinalproject;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.geffenfinalproject.models.Note;

import java.util.*;

public class user_intervals_quiz extends BaseActivity {

    private Button btnPlayInterval, btnReplayInterval;
    private TextView scoreText, wrongText;

    private MediaPlayer mp;

    private int score = 0;
    private int wrong = 0;

    private int baseIndex;
    private int correctInterval;

    private boolean questionActive = false;

    private final List<Note> allKeys = new ArrayList<>();
    private final Random random = new Random();

    private final Map<Integer, String> intervalNames = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_intervals_quiz);

        btnPlayInterval = findViewById(R.id.btnPlayInterval);
        btnReplayInterval = findViewById(R.id.btnReplayInterval);

        scoreText = findViewById(R.id.scoreText);
        wrongText = findViewById(R.id.wrongText);

        initIntervals();
        initAllKeys();
        setupButtons();

        btnPlayInterval.setOnClickListener(v -> playInterval());

        btnReplayInterval.setOnClickListener(v -> {
            if (questionActive) replayInterval();
        });

        updateScore();
    }

    private void initIntervals() {
        intervalNames.put(0, "Unison");
        intervalNames.put(1, "Minor Second");
        intervalNames.put(2, "Major Second");
        intervalNames.put(3, "Minor Third");
        intervalNames.put(4, "Major Third");
        intervalNames.put(5, "Perfect Fourth");
        intervalNames.put(6, "Tritone");
        intervalNames.put(7, "Perfect Fifth");
        intervalNames.put(8, "Minor Sixth");
        intervalNames.put(9, "Major Sixth");
        intervalNames.put(10, "Minor Seventh");
        intervalNames.put(11, "Major Seventh");
        intervalNames.put(12, "Octave");
    }

    private void setupButtons() {
        for (int i = 0; i <= 12; i++) {
            int resId = getResources().getIdentifier("btn" + i, "id", getPackageName());
            Button btn = findViewById(resId);

            int finalI = i;
            btn.setOnClickListener(v -> checkAnswer(finalI));
        }
    }

    private void playInterval() {
        if (questionActive) {
            Toast.makeText(this, "Answer first!", Toast.LENGTH_SHORT).show();
            return;
        }

        baseIndex = random.nextInt(allKeys.size() - 12);
        correctInterval = random.nextInt(13); // 0–12

        playNotes(baseIndex, baseIndex + correctInterval);
        questionActive = true;
    }

    private void replayInterval() {
        playNotes(baseIndex, baseIndex + correctInterval);
    }

    private void playNotes(int first, int second) {
        if (mp != null) mp.release();

        mp = MediaPlayer.create(this, allKeys.get(first).getAudioResId());
        mp.start();

        new Handler().postDelayed(() -> {
            MediaPlayer mp2 = MediaPlayer.create(this, allKeys.get(second).getAudioResId());
            mp2.start();
        }, 700);
    }

    private void checkAnswer(int userInterval) {
        if (!questionActive) return;

        if (userInterval == correctInterval) {
            score++;
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            wrong++;
            Toast.makeText(this,
                    "Wrong! It was " + intervalNames.get(correctInterval),
                    Toast.LENGTH_SHORT).show();
        }

        updateScore();
        questionActive = false;
    }

    private void updateScore() {
        scoreText.setText("Correct: " + score);
        wrongText.setText("Wrong: " + wrong);
    }

    void initAllKeys() {
        addNotes("C", "c", 1, 7);
        addNotes("C#", "csharp", 1, 7);
        addNotes("D", "d", 1, 7);
        addNotes("D#", "dsharp", 1, 7);
        addNotes("E", "e", 1, 7);
        addNotes("F", "f", 1, 7);
        addNotes("F#", "fsharp", 1, 7);
        addNotes("G", "g", 1, 7);
        addNotes("G#", "gsharp", 1, 7);
        addNotes("A", "a", 0, 7);
        addNotes("A#", "asharp", 0, 7);
        addNotes("B", "b", 0, 7);
    }

    void addNotes(String noteName, String filePrefix, int start, int end) {
        for (int i = start; i <= end; i++) {
            int resId = getResources().getIdentifier(
                    filePrefix + i,
                    "raw",
                    getPackageName()
            );

            if (resId != 0) {
                allKeys.add(new Note(noteName, resId));
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mp != null) mp.release();
        super.onDestroy();
    }
}