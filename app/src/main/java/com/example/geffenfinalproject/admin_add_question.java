package com.example.geffenfinalproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.geffenfinalproject.models.Question;
import com.example.geffenfinalproject.services.DatabaseService;
import com.example.geffenfinalproject.utils.Validator;
import com.google.firebase.auth.FirebaseAuth;

public class admin_add_question extends BaseActivity implements View.OnClickListener {

    EditText question, correct, wrong1, wrong2, wrong3;
    Spinner difficulty;
    Button add;
    SharedPreferences sharedPreferences;
    DatabaseService databaseService;
    private FirebaseAuth mAuth;
    public static final String MyPREFERENCES = "MyPrefs";

    String questionString, correctString, wrong1String, wrong2String, wrong3String, difficultyString;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_add_question);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        question = findViewById(R.id.question);
        correct = findViewById(R.id.correct);
        wrong1 = findViewById(R.id.wrong1);
        wrong2 = findViewById(R.id.wrong2);
        wrong3 = findViewById(R.id.wrong3);
        difficulty = findViewById(R.id.difficulty);
        add = findViewById(R.id.add);
        add.setOnClickListener(this);

        databaseService=DatabaseService.getInstance();
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == add.getId())
        {
            if (!validateForm()) {
                return;
            }

            questionString = question.getText().toString();
            correctString = correct.getText().toString();
            wrong1String = wrong1.getText().toString();
            wrong2String = wrong2.getText().toString();
            wrong3String = wrong3.getText().toString();
            difficultyString = difficulty.getSelectedItem().toString();

            Question q = new Question(questionString, correctString, wrong1String, wrong2String, wrong3String, difficultyString, databaseService.generateQuestionId());

            databaseService.createNewQuestion(q, new DatabaseService.DatabaseCallback<Void>() {
                @Override
                public void onFailed(Exception e) {
                    Toast.makeText(admin_add_question.this, "Didn't work, try again", Toast.LENGTH_LONG).show();
                    return;
                }

                @Override
                public void onCompleted(Void object) {
                    Toast.makeText(admin_add_question.this, "Question added successfully", Toast.LENGTH_LONG).show();
                }
            });

        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        if (!Validator.isNotEmpty(question, "Question")) {
            isValid = false;
        }
        if (!Validator.isNotEmpty(correct, "Correct Answer")) {
            isValid = false;
        }
        if (!Validator.isNotEmpty(wrong1, "Wrong Answer 1")) {
            isValid = false;
        }
        if (!Validator.isNotEmpty(wrong2, "Wrong Answer 2")) {
            isValid = false;
        }
        if (!Validator.isNotEmpty(wrong3, "Wrong Answer 3")) {
            isValid = false;
        }
        if (!Validator.isSpinnerValid(difficulty, "Enter difficulty")) {
            isValid = false;
        }

        return isValid;
    }
}
