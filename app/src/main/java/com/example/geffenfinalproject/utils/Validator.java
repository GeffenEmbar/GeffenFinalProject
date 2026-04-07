package com.example.geffenfinalproject.utils;

import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class Validator {

    public static boolean isSpinnerValid(Spinner spinner, String defaultText) {
        if (spinner.getSelectedItem() == null || spinner.getSelectedItem().toString().equals(defaultText)) {
            TextView errorText = (TextView) spinner.getSelectedView();
            if (errorText != null) {
                errorText.setError("");
                errorText.setTextColor(android.graphics.Color.RED);
                errorText.setText("Please select a valid option");
            }
            return false;
        }
        return true;
    }

    public static boolean isEmailValid(EditText editText) {
        String email = editText.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            editText.setError("Email is required");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editText.setError("Invalid email format (e.g. example@email.com)");
            return false;
        }
        return true;
    }

    public static boolean isPasswordValid(EditText editText) {
        String password = editText.getText().toString().trim();
        if (TextUtils.isEmpty(password)) {
            editText.setError("Password is required");
            return false;
        }
        if (password.length() < 6 || password.length() > 8) {
            editText.setError("Password must be between 6 and 8 characters");
            return false;
        }
        return true;
    }

    public static boolean isNameValid(EditText editText, String fieldName) {
        String name = editText.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            editText.setError(fieldName + " is required");
            return false;
        }
        if (name.length() < 2) {
            editText.setError(fieldName + " must be at least 2 characters long");
            return false;
        }
        return true;
    }

    public static boolean isPhoneValid(EditText editText) {
        String phone = editText.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            editText.setError("Phone number is required");
            return false;
        }
        if (!phone.matches("\\d{10}")) {
            editText.setError("Phone number must be exactly 10 digits");
            return false;
        }
        return true;
    }

    public static boolean isNotEmpty(EditText editText, String fieldName) {
        String text = editText.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            editText.setError(fieldName + " cannot be empty");
            return false;
        }
        return true;
    }
}
