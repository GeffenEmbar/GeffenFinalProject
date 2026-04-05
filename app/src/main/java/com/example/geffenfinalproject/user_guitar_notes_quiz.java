package com.example.geffenfinalproject;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.geffenfinalproject.models.Note;
import com.example.geffenfinalproject.services.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class user_guitar_notes_quiz extends BaseActivity {

    private Button btnPlay, replay, btnStop;
    private Button[] noteButtons = new Button[12];
    private TextView scoreText, wrongText;

    private MediaPlayer mp;
    private String correctNoteName;

    private int score = 0;
    private int wrong = 0;
    private int index;

    private final String[] noteNames = {"C","C#","D","D#","E","F","F#","G","G#","A","A#","B"};
    private final List<Note> allKeys = new ArrayList<>();

    private FirebaseAuth mAuth;
    private DatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_guitar_notes_quiz);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnPlay = findViewById(R.id.btnPlay);
        replay = findViewById(R.id.replay);
        btnStop = findViewById(R.id.btnStop);
        scoreText = findViewById(R.id.scoreText);
        wrongText = findViewById(R.id.wrongText);

        mAuth = FirebaseAuth.getInstance();
        databaseService = DatabaseService.getInstance();

        int[] buttonIds = {
                R.id.btnC,R.id.btnCSharp,R.id.btnD,R.id.btnDSharp,R.id.btnE,
                R.id.btnF,R.id.btnFSharp,R.id.btnG,R.id.btnGSharp,
                R.id.btnA,R.id.btnASharp,R.id.btnB
        };

        for(int i=0;i<12;i++){
            int idx = i;
            noteButtons[i] = findViewById(buttonIds[i]);
            noteButtons[i].setOnClickListener(v -> checkAnswer(noteNames[idx]));
        }

        initAllKeys();
        updateScore();

        btnPlay.setOnClickListener(v -> playRandomKey());
        btnStop.setOnClickListener(v -> stopQuiz());
        replay.setOnClickListener(v -> playNote());
    }

    void initAllKeys() {
        // Guitar notes range roughly from octave 2 to 5 based on provided files
        addNotes("A","a",2,5);
        addNotes("A#","asharp",2,5);
        addNotes("B","b",2,5);
        addNotes("C","c",2,5);
        addNotes("C#","csharp",2,5);
        addNotes("D","d",2,5);
        addNotes("D#","dsharp",2,5);
        addNotes("E","e",2,5);
        addNotes("F","f",2,5);
        addNotes("F#","fsharp",2,5);
        addNotes("G","g",2,5);
        addNotes("G#","gsharp",2,5);
    }

    void addNotes(String noteName,String filePrefix,int start,int end){
        for(int i=start;i<=end;i++){
            int resId = getResources().getIdentifier(
                    filePrefix + i + "guitar",
                    "raw",
                    getPackageName()
            );

            if(resId != 0){
                allKeys.add(new Note(noteName,resId));
            }
        }
    }

    private void playRandomKey(){
        if(allKeys.isEmpty()) {
            Toast.makeText(this, "No sounds available", Toast.LENGTH_SHORT).show();
            return;
        }

        for(Button b : noteButtons){
            b.setEnabled(true);
        }

        index = new Random().nextInt(allKeys.size());
        playNote();
    }

    private void playNote(){
        if(allKeys.isEmpty()) return;
        Note key = allKeys.get(index);
        correctNoteName = key.getNoteName();

        if(mp != null) mp.release();

        mp = MediaPlayer.create(this,key.getAudioResId());
        mp.start();
    }

    private void checkAnswer(String userAnswer){
        for(Button b : noteButtons){
            b.setEnabled(false);
        }

        if(userAnswer.equalsIgnoreCase(correctNoteName)){
            score++;
            databaseService.userAnsweredCorrectly(mAuth.getCurrentUser().getUid(), DatabaseService.GameType.NOTES);
            Toast.makeText(this,"Correct!",Toast.LENGTH_SHORT).show();
        }
        else{
            wrong++;
            if (mAuth.getCurrentUser() != null) {
                databaseService.userAnsweredWrongly(mAuth.getCurrentUser().getUid(), DatabaseService.GameType.NOTES);
            }
            Toast.makeText(this,"Wrong! Correct: "+correctNoteName,Toast.LENGTH_SHORT).show();
        }

        updateScore();
    }

    private void updateScore(){
        scoreText.setText("Correct: " + score);
        wrongText.setText("Wrong: " + wrong);
    }

    @Override
    protected void onDestroy(){
        if(mp != null) mp.release();
        super.onDestroy();
    }

    private void stopQuiz(){
        if(mp != null && mp.isPlaying()){
            mp.stop();
            mp.release();
        }
        btnPlay.setEnabled(false);
        for(Button b : noteButtons){
            b.setEnabled(false);
        }
        btnStop.setEnabled(false);
        Toast.makeText(this,"Quiz stopped",Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this,user_menu.class));
        finish();
    }
}
