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

    private Button btnPlay, replay;
    private Button[] noteButtons = new Button[12];
    private TextView scoreText, wrongText;

    private MediaPlayer mp;
    private String correctNoteName;
    private boolean quizRunning = true;
    private Button btnStop;
    private int score = 0; // session correct answers
    private int wrong = 0; // session wrong answers
    private int index;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private String currentUserId;


    private final String[] noteNames = {"C","C#","D","D#","E","F","F#","G","G#","A","A#","B"};
    private final List<Note> allKeys = new ArrayList<>();



    private Button btnC, btnCSharp, btnD, btnDSharp, btnE, btnF;
    private Button btnFSharp, btnG, btnGSharp, btnA, btnASharp, btnB;

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
        replay = findViewById(R.id.replay);
        btnStop = findViewById(R.id.btnStop);
        scoreText = findViewById(R.id.scoreText);
        scoreText = findViewById(R.id.scoreText);
        wrongText = findViewById(R.id.wrongText);


        btnC = findViewById(R.id.btnC);
        btnCSharp = findViewById(R.id.btnCSharp);
        btnD = findViewById(R.id.btnD);
        btnDSharp = findViewById(R.id.btnDSharp);
        btnE = findViewById(R.id.btnE);
        btnF = findViewById(R.id.btnF);
        btnFSharp = findViewById(R.id.btnFSharp);
        btnG = findViewById(R.id.btnG);
        btnGSharp = findViewById(R.id.btnGSharp);
        btnA = findViewById(R.id.btnA);
        btnASharp = findViewById(R.id.btnASharp);
        btnB = findViewById(R.id.btnB);





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
        addNotes("A", "a", 0, 7);
        addNotes("A#", "asharp", 0, 7);
        addNotes("B", "b", 0, 7);
        addNotes("C", "c", 1, 7);
        addNotes("C#", "csharp", 1, 7);
        addNotes("D", "d", 1, 7);
        addNotes("D#", "dsharp", 1, 7);
        addNotes("E", "e", 1, 7);
        addNotes("F", "f", 1, 7);
        addNotes("F#", "fsharp", 1, 7);
        addNotes("G", "g", 1, 7);
        addNotes("G#", "gsharp", 1, 7);

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

    private void playRandomKey(){
        if(allKeys.isEmpty()) return;

        for (Button b : noteButtons) {
            b.setEnabled(true);
        }

        index = new Random().nextInt(allKeys.size());
        playNote();
    }

    private void playNote() {
        Note key = allKeys.get(index);
        correctNoteName = key.getNoteName();

        if(mp != null) mp.release();
        mp = MediaPlayer.create(this, key.getAudioResId());
        mp.start();
    }

    private void checkAnswer(String userAnswer){
        for (Button b : noteButtons) {
            b.setEnabled(false);
        }
        if(userAnswer.equalsIgnoreCase(correctNoteName)){
            score++;
            Toast.makeText(this,"Correct!", Toast.LENGTH_SHORT).show();
            updateUserScore(true);
        } else {
            wrong++;
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