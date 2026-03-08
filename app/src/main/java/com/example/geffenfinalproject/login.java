package com.example.geffenfinalproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.geffenfinalproject.models.User;
import com.example.geffenfinalproject.services.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;

public class login extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";
    private EditText etEmail, etPassword;
    private Button login;
    private FirebaseAuth mAuth;
    private DatabaseService databaseService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etEmail = findViewById(R.id.etEmail_login);
        etPassword = findViewById(R.id.etPassword_login);
        login = findViewById(R.id.login);
        databaseService=DatabaseService.getInstance();

        login.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick: Login button clicked");

        /// get the email and password entered by the user
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();


        /// log the email and password
        Log.d(TAG, "onClick: Email: " + email);
        Log.d(TAG, "onClick: Password: " + password);

        Log.d(TAG, "onClick: Validating input...");

            /*

            /// Validate input
            if (!checkInput(email, password)) {
                /// stop if input is invalid
                return;
            }

            */
        Log.d(TAG, "onClick: Logging in user...");

        /// Login user
        loginUser(email, password);
    }


    private void loginUser(String email, String password) {
        databaseService.LoginUser(email, password, new DatabaseService.DatabaseCallback<String>() {
            /// Callback method called when the operation is completed

            @Override
            public void onCompleted(String  uid) {
                Log.d(TAG, "onCompleted: User logged in: " + uid.toString());

                databaseService.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
                    @Override
                    public void onCompleted(User user) {

                        Intent intent;

                        if (user != null && user.isAdmin()) {
                            intent = new Intent(login.this, admin_menu.class);
                        } else {
                            intent = new Intent(login.this, user_menu.class);
                        }

                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Log.e(TAG, "Failed to get user data", e);
                    }
                });


            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "onFailed: Failed to retrieve user data", e);
                /// Show error message to user
                etPassword.setError("Invalid email or password");
                etPassword.requestFocus();
                /// Sign out the user if failed to retrieve user data
                /// This is to prevent the user from being logged in again

            }
        });
    }
}