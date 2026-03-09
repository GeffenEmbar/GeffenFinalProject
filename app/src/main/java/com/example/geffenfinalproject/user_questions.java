package com.example.geffenfinalproject;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.geffenfinalproject.models.Question;
import com.example.geffenfinalproject.services.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class user_questions extends AppCompatActivity implements View.OnClickListener {

    private TextView tvQuestion, tvScore;
    private Button answer1, answer2, answer3, answer4, next;
    DatabaseService databaseService;
    private FirebaseAuth mAuth;
    private ArrayList<Question> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private Question currentQuestion;


    private int correctCount = 0;
    private int wrongCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_questions);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvQuestion = findViewById(R.id.tvQuestion);
        tvScore = findViewById(R.id.tvScore);
        answer1 = findViewById(R.id.answer1);
        answer1.setOnClickListener(this);
        answer2 = findViewById(R.id.answer2);
        answer2.setOnClickListener(this);
        answer3 = findViewById(R.id.answer3);
        answer3.setOnClickListener(this);
        answer4 = findViewById(R.id.answer4);
        answer4.setOnClickListener(this);

        next = findViewById(R.id.next);
        next.setOnClickListener(this);

        databaseService=DatabaseService.getInstance();
        mAuth = FirebaseAuth.getInstance();

        loadQuestions();



    }

    private void loadQuestions() {
        DatabaseService.getInstance().getQuestionList(new DatabaseService.DatabaseCallback<List<Question>>() {
            @Override
            public void onCompleted(List<Question> challenges) {
                questions.addAll(challenges);
                showNextQuestion();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(user_questions.this, "Failed to load challenges", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNextQuestion() {

        if (currentQuestionIndex >= questions.size()) {
            Toast.makeText(this, "Finished all challenges!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        currentQuestion = questions.get(currentQuestionIndex);

        tvQuestion.setText(currentQuestion.getQuestion());


        ArrayList<String> answers = new ArrayList<>();
        answers.add(currentQuestion.getCorrect());
        answers.add(currentQuestion.getWrong1());
        answers.add(currentQuestion.getWrong2());
        answers.add(currentQuestion.getWrong3());

        Collections.shuffle(answers);

        answer1.setText(answers.get(0));
        answer2.setText(answers.get(1));
        answer3.setText(answers.get(2));
        answer4.setText(answers.get(3));

        answer1.setOnClickListener(v -> checkAnswer(answer1.getText().toString()));
        answer2.setOnClickListener(v -> checkAnswer(answer2.getText().toString()));
        answer3.setOnClickListener(v -> checkAnswer(answer3.getText().toString()));
        answer4.setOnClickListener(v -> checkAnswer(answer4.getText().toString()));

        next.setEnabled(true);
    }

    private void checkAnswer(String userAnswer) {

        if (currentQuestion.checkAnswer(userAnswer)) {
            correctCount++;
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            wrongCount++;
            Toast.makeText(this, "Wrong!", Toast.LENGTH_SHORT).show();
        }

        updateScore();

        currentQuestionIndex++;
    }

    private void updateScore() {
        tvScore.setText("Correct: " + correctCount + " | Wrong: " + wrongCount);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == next.getId())
        {
            showNextQuestion();
            next.setEnabled(false);
        }
    }
}