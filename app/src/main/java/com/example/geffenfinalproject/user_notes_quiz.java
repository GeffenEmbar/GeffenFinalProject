package com.example.geffenfinalproject;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.geffenfinalproject.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.example.geffenfinalproject.models.Note;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class user_notes_quiz extends AppCompatActivity {

    private Button btnPlay;
    private Button[] noteButtons = new Button[12];
    private TextView scoreText, wrongText;

    private MediaPlayer mp;
    private String correctNoteName;
    private boolean quizRunning = true;
    private Button btnStop;
    private int score = 0; // session correct answers
    private int wrong = 0; // session wrong answers

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private String currentUserId;


    private final String[] noteNames = {"C","C#","D","D#","E","F","F#","G","G#","A","A#","B"};
    private final List<Note> allKeys = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_notes_quiz);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        btnPlay = findViewById(R.id.btnPlay);
        btnStop = findViewById(R.id.btnStop);
        scoreText = findViewById(R.id.scoreText);
        scoreText = findViewById(R.id.scoreText);
        wrongText = findViewById(R.id.wrongText);



        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
        }


        int[] buttonIds = {R.id.btnC,R.id.btnCSharp,R.id.btnD,R.id.btnDSharp,R.id.btnE,
                R.id.btnF,R.id.btnFSharp,R.id.btnG,R.id.btnGSharp,R.id.btnA,R.id.btnASharp,R.id.btnB};

        for(int i=0;i<12;i++){
            int index = i;
            noteButtons[i] = findViewById(buttonIds[i]);
            noteButtons[i].setOnClickListener(v -> checkAnswer(noteNames[index]));
        }


        initAllKeys();


        // Firebase
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null){
            currentUserId = mAuth.getCurrentUser().getUid();
            usersRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
        }


        updateScore();

        // Button listeners
        btnPlay.setOnClickListener(v -> playRandomKey());
        btnStop.setOnClickListener(v -> stopQuiz());
    }

    void initAllKeys() {
        // --- A notes ---
        allKeys.add(new Note("A", R.raw.a0));
        allKeys.add(new Note("A", R.raw.a1));
        allKeys.add(new Note("A", R.raw.a2));
        allKeys.add(new Note("A", R.raw.a3));
        allKeys.add(new Note("A", R.raw.a4));
        allKeys.add(new Note("A", R.raw.a5));
        allKeys.add(new Note("A", R.raw.a6));
        allKeys.add(new Note("A", R.raw.a7));

        // --- A# notes ---
        allKeys.add(new Note("A#", R.raw.asharp1));
        allKeys.add(new Note("A#", R.raw.asharp2));
        allKeys.add(new Note("A#", R.raw.asharp3));
        allKeys.add(new Note("A#", R.raw.asharp4));
        allKeys.add(new Note("A#", R.raw.asharp5));
        allKeys.add(new Note("A#", R.raw.asharp6));
        allKeys.add(new Note("A#", R.raw.asharp7));

        // --- B notes ---
        allKeys.add(new Note("B", R.raw.b0));
        allKeys.add(new Note("B", R.raw.b1));
        allKeys.add(new Note("B", R.raw.b2));
        allKeys.add(new Note("B", R.raw.b3));
        allKeys.add(new Note("B", R.raw.b4));
        allKeys.add(new Note("B", R.raw.b5));
        allKeys.add(new Note("B", R.raw.b6));
        allKeys.add(new Note("B", R.raw.b7));

        // --- C notes ---
        allKeys.add(new Note("C", R.raw.c1));
        allKeys.add(new Note("C", R.raw.c2));
        allKeys.add(new Note("C", R.raw.c3));
        allKeys.add(new Note("C", R.raw.c4));
        allKeys.add(new Note("C", R.raw.c5));
        allKeys.add(new Note("C", R.raw.c6));
        allKeys.add(new Note("C", R.raw.c7));

        // --- C# notes ---
        allKeys.add(new Note("C#", R.raw.csharp1));
        allKeys.add(new Note("C#", R.raw.csharp2));
        allKeys.add(new Note("C#", R.raw.csharp3));
        allKeys.add(new Note("C#", R.raw.csharp4));
        allKeys.add(new Note("C#", R.raw.csharp5));
        allKeys.add(new Note("C#", R.raw.csharp6));
        allKeys.add(new Note("C#", R.raw.csharp7));

        // --- D notes ---
        allKeys.add(new Note("D", R.raw.d1));
        allKeys.add(new Note("D", R.raw.d2));
        allKeys.add(new Note("D", R.raw.d3));
        allKeys.add(new Note("D", R.raw.d4));
        allKeys.add(new Note("D", R.raw.d5));
        allKeys.add(new Note("D", R.raw.d6));
        allKeys.add(new Note("D", R.raw.d7));

        // --- D# notes ---
        allKeys.add(new Note("D#", R.raw.dsharp1));
        allKeys.add(new Note("D#", R.raw.dsharp2));
        allKeys.add(new Note("D#", R.raw.dsharp3));
        allKeys.add(new Note("D#", R.raw.dsharp4));
        allKeys.add(new Note("D#", R.raw.dsharp5));
        allKeys.add(new Note("D#", R.raw.dsharp6));
        allKeys.add(new Note("D#", R.raw.dsharp7));

        // --- E notes ---
        allKeys.add(new Note("E", R.raw.e1));
        allKeys.add(new Note("E", R.raw.e2));
        allKeys.add(new Note("E", R.raw.e3));
        allKeys.add(new Note("E", R.raw.e4));
        allKeys.add(new Note("E", R.raw.e5));
        allKeys.add(new Note("E", R.raw.e6));
        allKeys.add(new Note("E", R.raw.e7));

        // --- F notes ---
        allKeys.add(new Note("F", R.raw.f1));
        allKeys.add(new Note("F", R.raw.f2));
        allKeys.add(new Note("F", R.raw.f3));
        allKeys.add(new Note("F", R.raw.f4));
        allKeys.add(new Note("F", R.raw.f5));
        allKeys.add(new Note("F", R.raw.f6));
        allKeys.add(new Note("F", R.raw.f7));

        // --- F# notes ---
        allKeys.add(new Note("F#", R.raw.fsharp1));
        allKeys.add(new Note("F#", R.raw.fsharp2));
        allKeys.add(new Note("F#", R.raw.fsharp3));
        allKeys.add(new Note("F#", R.raw.fsharp4));
        allKeys.add(new Note("F#", R.raw.fsharp5));
        allKeys.add(new Note("F#", R.raw.fsharp6));
        allKeys.add(new Note("F#", R.raw.fsharp7));

        // --- G notes ---
        allKeys.add(new Note("G", R.raw.g1));
        allKeys.add(new Note("G", R.raw.g2));
        allKeys.add(new Note("G", R.raw.g3));
        allKeys.add(new Note("G", R.raw.g4));
        allKeys.add(new Note("G", R.raw.g5));
        allKeys.add(new Note("G", R.raw.g6));
        allKeys.add(new Note("G", R.raw.g7));

        // --- G# notes ---
        allKeys.add(new Note("G#", R.raw.gsharp1));
        allKeys.add(new Note("G#", R.raw.gsharp2));
        allKeys.add(new Note("G#", R.raw.gsharp3));
        allKeys.add(new Note("G#", R.raw.gsharp4));
        allKeys.add(new Note("G#", R.raw.gsharp5));
        allKeys.add(new Note("G#", R.raw.gsharp6));
        allKeys.add(new Note("G#", R.raw.gsharp7));
    }

    private void playRandomKey(){
        if(allKeys.isEmpty()) return;

        int index = new Random().nextInt(allKeys.size());
        Note key = allKeys.get(index);
        correctNoteName = key.getNoteName();

        if(mp != null) mp.release();
        mp = MediaPlayer.create(this, key.getAudioResId());
        mp.start();
    }

    private void checkAnswer(String userAnswer){
        if(userAnswer.equalsIgnoreCase(correctNoteName)){
            score++;
            Toast.makeText(this,"Correct!", Toast.LENGTH_SHORT).show();
            updateUserScore(true);
        } else {
            Toast.makeText(this,"Wrong! Correct: "+correctNoteName,Toast.LENGTH_SHORT).show();
            updateUserScore(false);
        }
        updateScore();
    }

    private void updateScore(){
        scoreText.setText("Correct: " + score);
        wrongText.setText("Wrong: " + wrong);
    }

    private void updateUserScore(boolean isCorrect){
        if(currentUserId == null) return;

        DatabaseReference userRef = usersRef.child(currentUserId);

        userRef.runTransaction(new com.google.firebase.database.Transaction.Handler() {
            @Override
            public com.google.firebase.database.Transaction.Result doTransaction(com.google.firebase.database.MutableData currentData) {
                if (currentData.getValue(User.class) != null) {
                    User user = currentData.getValue(User.class);
                    if(isCorrect){
                        user.setCorrect_answers(user.getCorrect_answers() + 1);
                    } else {
                        user.setWrong_answers(user.getWrong_answers() + 1);
                    }
                    currentData.setValue(user);
                }
                return com.google.firebase.database.Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, com.google.firebase.database.DataSnapshot currentData) {
            }
        });
    }


    @Override
    protected void onDestroy(){
        if(mp != null) mp.release();
        super.onDestroy();
    }


    private void stopQuiz() {
        quizRunning = false;
        if(mp != null && mp.isPlaying()){
            mp.stop();
            mp.release();
            mp = null;
        }

        btnPlay.setEnabled(false);
        for(Button b : noteButtons){
            b.setEnabled(false);
        }
        btnStop.setEnabled(false);

        Toast.makeText(this,"Quiz stopped",Toast.LENGTH_SHORT).show();


        saveScoreToFirebase();

        Intent intent = new Intent(this, user_menu.class);
        startActivity(intent);
    }


    private void saveScoreToFirebase() {
        if(currentUserId == null || usersRef == null) return;

        usersRef.runTransaction(new com.google.firebase.database.Transaction.Handler() {
            @Override
            public com.google.firebase.database.Transaction.Result doTransaction(com.google.firebase.database.MutableData currentData) {
                User user = currentData.getValue(User.class);
                if(user != null){
                    user.setCorrect_answers(user.getCorrect_answers() + score);
                    user.setWrong_answers(user.getWrong_answers() + wrong);
                    currentData.setValue(user);
                }
                return com.google.firebase.database.Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, com.google.firebase.database.DataSnapshot currentData) {
                if(committed){
                    Toast.makeText(user_notes_quiz.this, "Score saved!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(user_notes_quiz.this, "Failed to save score", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}