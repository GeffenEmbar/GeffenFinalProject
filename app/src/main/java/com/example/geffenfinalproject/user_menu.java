package com.example.geffenfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class user_menu extends AppCompatActivity implements View.OnClickListener {

    Button piano_notes_quiz, questionnaire;

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

        piano_notes_quiz = findViewById(R.id.piano_notes_quiz);
        piano_notes_quiz.setOnClickListener(this);
        questionnaire = findViewById(R.id.questionnaire);
        questionnaire.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.piano_notes_quiz) {
            Intent intent = new Intent(this, user_notes_quiz.class);
            startActivity(intent);
        }
        else if (v.getId() == questionnaire.getId()) {
            Intent intent = new Intent(this, user_questions.class);
            startActivity(intent);
        }
    }
}