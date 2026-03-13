package com.example.geffenfinalproject;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.geffenfinalproject.models.Note;
import com.example.geffenfinalproject.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class user_intervals_quiz extends AppCompatActivity implements View.OnClickListener {

    private Button btnPlayInterval, btnReplayInterval;
    private Button btnMinorSecond, btnMajorSecond, btnMinorThird, btnMajorThird;
    private Button btnPerfectFourth, btnPerfectFifth, btnOctave;

    private TextView scoreText, wrongText;

    private MediaPlayer mp;

    private int score = 0;
    private int wrong = 0;

    private int baseIndex;
    private int correctInterval;

    private boolean questionActive = false;

    private final List<Note> allKeys = new ArrayList<>();

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private String currentUserId;

    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_intervals_quiz);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnPlayInterval = findViewById(R.id.btnPlayInterval);
        btnReplayInterval = findViewById(R.id.btnReplayInterval);

        btnMinorSecond = findViewById(R.id.btnMinorSecond);
        btnMajorSecond = findViewById(R.id.btnMajorSecond);
        btnMinorThird = findViewById(R.id.btnMinorThird);
        btnMajorThird = findViewById(R.id.btnMajorThird);
        btnPerfectFourth = findViewById(R.id.btnPerfectFourth);
        btnPerfectFifth = findViewById(R.id.btnPerfectFifth);
        btnOctave = findViewById(R.id.btnOctave);

        scoreText = findViewById(R.id.scoreText);
        wrongText = findViewById(R.id.wrongText);

        initAllKeys();

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
            usersRef = FirebaseDatabase.getInstance().getReference("users");
        }

        updateScore();

        btnPlayInterval.setOnClickListener(v -> playInterval());

        btnReplayInterval.setOnClickListener(v -> {
            if(questionActive){
                replayInterval();
            }
        });

        btnMinorSecond.setOnClickListener(v -> checkAnswer(1));
        btnMajorSecond.setOnClickListener(v -> checkAnswer(2));
        btnMinorThird.setOnClickListener(v -> checkAnswer(3));
        btnMajorThird.setOnClickListener(v -> checkAnswer(4));
        btnPerfectFourth.setOnClickListener(v -> checkAnswer(5));
        btnPerfectFifth.setOnClickListener(v -> checkAnswer(7));
        btnOctave.setOnClickListener(v -> checkAnswer(12));
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


    private void playInterval() {

        if(questionActive){
            Toast.makeText(this,"Answer the current interval first!",Toast.LENGTH_SHORT).show();
            return;
        }

        if(allKeys.size() < 20) return;

        baseIndex = random.nextInt(allKeys.size() - 12);

        int[] intervals = {1,2,3,4,5,7,12};

        correctInterval = intervals[random.nextInt(intervals.length)];

        playNotes(baseIndex, baseIndex + correctInterval);

        questionActive = true;
    }


    private void replayInterval(){

        playNotes(baseIndex, baseIndex + correctInterval);

    }


    private void playNotes(int first, int second){

        Note note1 = allKeys.get(first);
        Note note2 = allKeys.get(second);

        if(mp != null) mp.release();

        mp = MediaPlayer.create(this, note1.getAudioResId());
        mp.start();

        new Handler().postDelayed(() -> {

            MediaPlayer mp2 = MediaPlayer.create(this, note2.getAudioResId());
            mp2.start();

        },700);
    }

    private void checkAnswer(int userInterval){

        if(!questionActive) return;

        if(userInterval == correctInterval){

            score++;
            Toast.makeText(this,"Correct!",Toast.LENGTH_SHORT).show();
            updateUserScore(true);

        }
        else{

            wrong++;
            if (correctInterval == 1)
                Toast.makeText(this,"Wrong! Minor Second",Toast.LENGTH_SHORT).show();
            else if (correctInterval == 2)
                Toast.makeText(this,"Wrong! Major Second",Toast.LENGTH_SHORT).show();
            else if (correctInterval == 3)
                Toast.makeText(this,"Wrong! Minor Third",Toast.LENGTH_SHORT).show();
            else if (correctInterval == 4)
                Toast.makeText(this,"Wrong! Major Third",Toast.LENGTH_SHORT).show();
            else if (correctInterval == 5)
                Toast.makeText(this,"Wrong! Perfect Fourth",Toast.LENGTH_SHORT).show();
            else if (correctInterval == 7)
                Toast.makeText(this,"Wrong! Perfect Fifth",Toast.LENGTH_SHORT).show();
            else if (correctInterval == 12)
                Toast.makeText(this,"Wrong! Octave",Toast.LENGTH_SHORT).show();
            updateUserScore(false);
        }

        updateScore();

        questionActive = false;
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

                User user = currentData.getValue(User.class);

                if(user != null){

                    if(isCorrect){
                        user.setCorrect_answers(user.getCorrect_answers() + 1);
                    }else{
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

    @Override
    public void onClick(View v) {

    }
}