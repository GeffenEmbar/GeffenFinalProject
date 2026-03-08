package com.example.geffenfinalproject;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.geffenfinalproject.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class user_profile extends AppCompatActivity {

    private TextView wrong_answers, right_answers, name;
    private String userUid;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        userUid = getIntent().getStringExtra("USER_UID");
        if(userUid == null){
            Toast.makeText(this, "No user selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        usersRef = FirebaseDatabase.getInstance().getReference("users").child(userUid);

        name = findViewById(R.id.name);
        right_answers = findViewById(R.id.right_answers);
        wrong_answers = findViewById(R.id.wrong_answers);

        loadUserData();

    }

    private void loadUserData() {
        usersRef.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                DataSnapshot snapshot = task.getResult();
                if(snapshot.exists()){
                    User user = snapshot.getValue(User.class);
                    if(user != null){
                        name.setText(user.getFname() + " " + user.getLname());
                        right_answers.setText(String.valueOf(user.getCorrect_answers()));
                        wrong_answers.setText(String.valueOf(user.getWrong_answers()));
                    }
                } else {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to load user", Toast.LENGTH_SHORT).show();
            }
        });
    }
}