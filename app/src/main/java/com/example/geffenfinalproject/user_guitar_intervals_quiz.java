package com.example.geffenfinalproject;

import android.content.Intent;
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
import com.example.geffenfinalproject.services.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.*;

public class user_guitar_intervals_quiz extends BaseActivity {

    private Button btnPlayInterval, btnReplayInterval, btnStop;
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

    private FirebaseAuth mAuth;
    private DatabaseService databaseService;

    private final String[] notePrefixes = {"c", "csharp", "d", "dsharp", "e", "f", "fsharp", "g", "gsharp", "a", "asharp", "b"};
    private final String[] noteNames = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_guitar_intervals_quiz);

        btnPlayInterval = findViewById(R.id.btnPlayInterval);
        btnReplayInterval = findViewById(R.id.btnReplayInterval);
        btnStop = findViewById(R.id.btnStop);

        scoreText = findViewById(R.id.scoreText);
        wrongText = findViewById(R.id.wrongText);

        mAuth = FirebaseAuth.getInstance();
        databaseService = DatabaseService.getInstance();

        initIntervals();
        initAllKeysChromatic();
        setupButtons();

        btnPlayInterval.setOnClickListener(v -> playInterval());

        btnReplayInterval.setOnClickListener(v -> {
            if (questionActive) replayInterval();
        });

        btnStop.setOnClickListener(v -> stopQuiz());

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

    private void initAllKeysChromatic() {
        // Add notes chromatically from octave 2 to 5
        for (int octave = 2; octave <= 5; octave++) {
            for (int i = 0; i < 12; i++) {
                String prefix = notePrefixes[i];
                String name = noteNames[i];
                int resId = getResources().getIdentifier(prefix + octave + "guitar", "raw", getPackageName());
                if (resId != 0) {
                    allKeys.add(new Note(name, resId));
                }
            }
        }
    }

    private void playInterval() {
        if (allKeys.isEmpty()) {
            Toast.makeText(this, "No guitar sounds loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        if (questionActive) {
            Toast.makeText(this, "Answer first!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pick a random base note and a random interval, then check if the second note exists
        // Since allKeys is chromatically sorted (with possible gaps), we should be careful.
        // Actually, let's change how we store keys to ensure semitone logic.
        
        List<Integer> availableIntervals = new ArrayList<>();
        int attempts = 0;
        
        while (availableIntervals.isEmpty() && attempts < 100) {
            baseIndex = random.nextInt(allKeys.size());
            Note baseNote = allKeys.get(baseIndex);
            
            // Find which intervals are possible from this base note
            for (int interval = 0; interval <= 12; interval++) {
                if (hasNoteAtInterval(baseIndex, interval)) {
                    availableIntervals.add(interval);
                }
            }
            attempts++;
        }

        if (availableIntervals.isEmpty()) {
            Toast.makeText(this, "Could not find a valid interval. Try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        correctInterval = availableIntervals.get(random.nextInt(availableIntervals.size()));
        
        playNotes(baseIndex, getIndexAtInterval(baseIndex, correctInterval));
        questionActive = true;
    }

    private boolean hasNoteAtInterval(int baseIdx, int interval) {
        if (interval == 0) return true;
        
        // We need to find if there's a note that is exactly 'interval' semitones above baseNote
        // Since we added notes in chromatic order in initAllKeysChromatic, we can check by semitone count
        int targetSemitones = getSemitoneValue(allKeys.get(baseIdx)) + interval;
        
        for (Note note : allKeys) {
            if (getSemitoneValue(note) == targetSemitones) {
                return true;
            }
        }
        return false;
    }

    private int getIndexAtInterval(int baseIdx, int interval) {
        if (interval == 0) return baseIdx;
        int targetSemitones = getSemitoneValue(allKeys.get(baseIdx)) + interval;
        for (int i = 0; i < allKeys.size(); i++) {
            if (getSemitoneValue(allKeys.get(i)) == targetSemitones) {
                return i;
            }
        }
        return baseIdx; // Should not happen if hasNoteAtInterval was true
    }

    private int getSemitoneValue(Note note) {
        // Find which note and octave it is
        // We can parse the resource name or store it in the Note object.
        // For now, let's use a helper that matches prefix and octave.
        String resName = getResources().getResourceEntryName(note.getAudioResId());
        // resName is like "c3guitar"
        int octave = Character.getNumericValue(resName.charAt(resName.length() - 7)); // "c3guitar" -> '3' is at length-7
        if (resName.contains("asharp")) return octave * 12 + 10;
        if (resName.contains("csharp")) return octave * 12 + 1;
        if (resName.contains("dsharp")) return octave * 12 + 3;
        if (resName.contains("fsharp")) return octave * 12 + 6;
        if (resName.contains("gsharp")) return octave * 12 + 8;
        
        char noteChar = resName.charAt(0);
        int val = 0;
        switch (noteChar) {
            case 'c': val = 0; break;
            case 'd': val = 2; break;
            case 'e': val = 4; break;
            case 'f': val = 5; break;
            case 'g': val = 7; break;
            case 'a': val = 9; break;
            case 'b': val = 11; break;
        }
        return octave * 12 + val;
    }

    private void replayInterval() {
        playNotes(baseIndex, getIndexAtInterval(baseIndex, correctInterval));
    }

    private void playNotes(int first, int second) {
        if (mp != null) mp.release();

        mp = MediaPlayer.create(this, allKeys.get(first).getAudioResId());
        if (mp != null) mp.start();

        new Handler().postDelayed(() -> {
            MediaPlayer mp2 = MediaPlayer.create(this, allKeys.get(second).getAudioResId());
            if (mp2 != null) mp2.start();
        }, 700);
    }

    private void checkAnswer(int userInterval) {
        if (!questionActive) return;

        if (userInterval == correctInterval) {
            score++;
            if (mAuth.getCurrentUser() != null) {
                databaseService.userAnsweredCorrectly(mAuth.getCurrentUser().getUid(), DatabaseService.GameType.INTERVALS);
            }
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            wrong++;
            if (mAuth.getCurrentUser() != null) {
                databaseService.userAnsweredWrongly(mAuth.getCurrentUser().getUid(), DatabaseService.GameType.INTERVALS);
            }
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

    private void stopQuiz() {
        if (mp != null) {
            mp.release();
        }
        startActivity(new Intent(this, user_menu.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        if (mp != null) mp.release();
        super.onDestroy();
    }
}
