package com.example.geffenfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.geffenfinalproject.models.Question;
import com.example.geffenfinalproject.services.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class user_questions extends BaseActivity implements View.OnClickListener {

    private TextView tvQuestion, tvScore;
    private Button answer1, answer2, answer3, answer4, btnOut;
    private Spinner difficultySelector;
    DatabaseService databaseService;
    private FirebaseAuth mAuth;
    private ArrayList<Question> masterList = new ArrayList<>();
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
        tvScore.setText("Correct: " + 0 + " | Wrong: " + 0);
        answer1 = findViewById(R.id.answer1);
        answer2 = findViewById(R.id.answer2);
        answer3 = findViewById(R.id.answer3);
        answer4 = findViewById(R.id.answer4);
        btnOut = findViewById(R.id.btnOut);
        btnOut.setOnClickListener(this);

        difficultySelector = findViewById(R.id.difficultySelector);
        difficultySelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterQuestions();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        databaseService=DatabaseService.getInstance();
        mAuth = FirebaseAuth.getInstance();

        loadQuestions();
    }

    private void filterQuestions() {
        String selectedDifficulty = difficultySelector.getSelectedItem().toString();
        questions.clear();

        if (selectedDifficulty.equals("Enter difficulty")) {
            questions.addAll(masterList);
        } else {
            for (Question q : masterList) {
                if (q.getDifficulty().equalsIgnoreCase(selectedDifficulty)) {
                    questions.add(q);
                }
            }
        }
        
        resetQuiz();
    }

    private void resetQuiz() {
        currentQuestionIndex = 0;
        correctCount = 0;
        wrongCount = 0;
        updateScore();
        btnOut.setVisibility(View.GONE);
        answer1.setEnabled(true);
        answer2.setEnabled(true);
        answer3.setEnabled(true);
        answer4.setEnabled(true);
        
        if (questions.isEmpty()) {
            tvQuestion.setText("No questions found for this difficulty.");
            answer1.setVisibility(View.INVISIBLE);
            answer2.setVisibility(View.INVISIBLE);
            answer3.setVisibility(View.INVISIBLE);
            answer4.setVisibility(View.INVISIBLE);
        } else {
            answer1.setVisibility(View.VISIBLE);
            answer2.setVisibility(View.VISIBLE);
            answer3.setVisibility(View.VISIBLE);
            answer4.setVisibility(View.VISIBLE);
            showNextQuestion();
        }
    }

    private void loadQuestions() {
        DatabaseService.getInstance().getQuestionList(new DatabaseService.DatabaseCallback<List<Question>>() {
            @Override
            public void onCompleted(List<Question> challenges) {
                masterList.clear();
                masterList.addAll(challenges);
                filterQuestions();
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
            btnOut.setVisibility(View.VISIBLE);
            btnOut.setText("Out of Questions - Back to menu");
            answer1.setEnabled(false);
            answer2.setEnabled(false);
            answer3.setEnabled(false);
            answer4.setEnabled(false);
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


    }

    private void checkAnswer(String userAnswer) {

        if (currentQuestion.checkAnswer(userAnswer)) {

            correctCount++;

            // ⭐ NEW – update user + group score
            databaseService.userAnsweredCorrectly(mAuth.getCurrentUser().getUid(), DatabaseService.GameType.QUIZ);

            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();

        } else {

            wrongCount++;
            // ⭐ NEW – update user wrong score
            if (mAuth.getCurrentUser() != null) {
                databaseService.userAnsweredWrongly(mAuth.getCurrentUser().getUid(), DatabaseService.GameType.QUIZ);
            }
            Toast.makeText(this, "Wrong!", Toast.LENGTH_SHORT).show();
        }

        updateScore();

        currentQuestionIndex++;

        showNextQuestion();
    }

    private void updateScore() {
        tvScore.setText("Correct: " + correctCount + " | Wrong: " + wrongCount);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == btnOut.getId())
        {
            Intent intent = new Intent(this, user_menu.class);
            startActivity(intent);
        }
    }
}
