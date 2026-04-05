package com.example.geffenfinalproject;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import com.example.geffenfinalproject.services.DatabaseService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Random;

public class user_guitar_chords_quiz extends BaseActivity {

    private Button btnPlayChord, btnCheckAnswer, btnStop, btnReplay;
    private TextView scoreText, wrongText;
    private SwitchMaterial switchType;
    private MaterialButton[] rootButtons = new MaterialButton[12];

    private MediaPlayer mpRoot, mpThird, mpFifth;

    private int score = 0;
    private int wrong = 0;

    private int correctRootIndex; // 0-11
    private boolean correctIsMinor;
    private int currentOctave;
    private int selectedRootIndex = -1;

    private boolean questionActive = false;

    private final String[] notePrefixes = {"c", "csharp", "d", "dsharp", "e", "f", "fsharp", "g", "gsharp", "a", "asharp", "b"};
    private final String[] noteNames = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    
    private FirebaseAuth mAuth;
    private DatabaseService databaseService;
    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_guitar_chords_quiz);

        mAuth = FirebaseAuth.getInstance();
        databaseService = DatabaseService.getInstance();

        btnPlayChord = findViewById(R.id.btnPlayChord);
        btnReplay = findViewById(R.id.btnReplay);
        btnCheckAnswer = findViewById(R.id.btnCheckAnswer);
        btnStop = findViewById(R.id.btnStop);
        scoreText = findViewById(R.id.scoreText);
        wrongText = findViewById(R.id.wrongText);
        switchType = findViewById(R.id.switchType);

        setupRootButtons();

        btnPlayChord.setOnClickListener(v -> generateAndPlayChord());
        btnReplay.setOnClickListener(v -> {
            if (questionActive) {
                playChord(correctRootIndex, correctIsMinor, currentOctave);
            } else {
                Toast.makeText(this, "Play a chord first!", Toast.LENGTH_SHORT).show();
            }
        });
        btnCheckAnswer.setOnClickListener(v -> checkAnswer());
        btnStop.setOnClickListener(v -> {
            stopAudio();
            startActivity(new Intent(this, user_menu.class));
            finish();
        });

        updateScoreUI();
    }

    private void setupRootButtons() {
        int[] resIds = {
            R.id.btnRootC, R.id.btnRootCSharp, R.id.btnRootD, R.id.btnRootDSharp,
            R.id.btnRootE, R.id.btnRootF, R.id.btnRootFSharp, R.id.btnRootG,
            R.id.btnRootGSharp, R.id.btnRootA, R.id.btnRootASharp, R.id.btnRootB
        };

        for (int i = 0; i < 12; i++) {
            final int index = i;
            rootButtons[i] = findViewById(resIds[i]);
            rootButtons[i].setOnClickListener(v -> {
                selectedRootIndex = index;
                highlightSelectedRoot();
            });
        }
    }

    private void highlightSelectedRoot() {
        for (int i = 0; i < 12; i++) {
            if (i == selectedRootIndex) {
                rootButtons[i].setStrokeWidth(5);
                rootButtons[i].setStrokeColor(getColorStateList(R.color.white));
                rootButtons[i].setBackgroundColor(getColor(R.color.black));
            } else {
                rootButtons[i].setStrokeWidth(1);
                rootButtons[i].setStrokeColor(getColorStateList(R.color.white));
                rootButtons[i].setBackgroundColor(getColor(android.R.color.transparent));
            }
        }
    }

    private void generateAndPlayChord() {
        correctRootIndex = random.nextInt(12);
        correctIsMinor = random.nextBoolean();
        currentOctave = 2 + random.nextInt(2); // Try octaves 2 and 3 for base
        questionActive = true;
        
        playChord(correctRootIndex, correctIsMinor, currentOctave);
    }

    private void playChord(int rootIdx, boolean isMinor, int octave) {
        stopAudio();

        int rootRes = getNoteResId(rootIdx, octave);
        
        int thirdIdx = rootIdx + (isMinor ? 3 : 4);
        int thirdOctave = octave;
        if (thirdIdx >= 12) {
            thirdIdx -= 12;
            thirdOctave++;
        }
        int thirdRes = getNoteResId(thirdIdx, thirdOctave);
        
        int fifthIdx = rootIdx + 7;
        int fifthOctave = octave;
        if (fifthIdx >= 12) {
            fifthIdx -= 12;
            fifthOctave++;
        }
        int fifthRes = getNoteResId(fifthIdx, fifthOctave);

        // Play all at once
        mpRoot = MediaPlayer.create(this, rootRes);
        mpThird = MediaPlayer.create(this, thirdRes);
        mpFifth = MediaPlayer.create(this, fifthRes);

        if (mpRoot != null) mpRoot.start();
        if (mpThird != null) mpThird.start();
        if (mpFifth != null) mpFifth.start();
    }

    private int getNoteResId(int noteIdx, int octave) {
        String prefix = notePrefixes[noteIdx];
        // Try requested octave
        int resId = getResources().getIdentifier(prefix + octave + "guitar", "raw", getPackageName());
        
        // If not found, try octaves 2, 3, 4, 5 sequentially
        if (resId == 0) {
            for (int oct : new int[]{3, 2, 4, 5}) {
                resId = getResources().getIdentifier(prefix + oct + "guitar", "raw", getPackageName());
                if (resId != 0) break;
            }
        }
        
        // Final fallback to piano if still 0 (though we expect guitar to be there)
        if (resId == 0) {
            resId = getResources().getIdentifier(prefix + "4", "raw", getPackageName());
        }
        
        return resId;
    }

    private void checkAnswer() {
        if (!questionActive) {
            Toast.makeText(this, "Play a chord first!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedRootIndex == -1) {
            Toast.makeText(this, "Select a root note!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean userIsMinor = switchType.isChecked();

        if (selectedRootIndex == correctRootIndex && userIsMinor == correctIsMinor) {
            score++;
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
            if (mAuth.getCurrentUser() != null) {
                databaseService.userAnsweredCorrectly(mAuth.getCurrentUser().getUid(), DatabaseService.GameType.CHORDS);
            }
        } else {
            wrong++;
            String correctName = noteNames[correctRootIndex] + (correctIsMinor ? " Minor" : " Major");
            Toast.makeText(this, "Wrong! It was " + correctName, Toast.LENGTH_SHORT).show();
            if (mAuth.getCurrentUser() != null) {
                databaseService.userAnsweredWrongly(mAuth.getCurrentUser().getUid(), DatabaseService.GameType.CHORDS);
            }
        }

        updateScoreUI();
        questionActive = false;
        selectedRootIndex = -1;
        highlightSelectedRoot();
    }

    private void updateScoreUI() {
        scoreText.setText("Correct: " + score);
        wrongText.setText("Wrong: " + wrong);
    }

    private void stopAudio() {
        try {
            if (mpRoot != null) {
                if (mpRoot.isPlaying()) mpRoot.stop();
                mpRoot.release();
                mpRoot = null;
            }
            if (mpThird != null) {
                if (mpThird.isPlaying()) mpThird.stop();
                mpThird.release();
                mpThird = null;
            }
            if (mpFifth != null) {
                if (mpFifth.isPlaying()) mpFifth.stop();
                mpFifth.release();
                mpFifth = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        stopAudio();
        super.onDestroy();
    }
}
