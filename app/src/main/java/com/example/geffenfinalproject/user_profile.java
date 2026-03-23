package com.example.geffenfinalproject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.geffenfinalproject.models.User;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class user_profile extends AppCompatActivity {

    private EditText etFirstName, etLastName, etPhone;
    private TextView right_answers, wrong_answers, tvGroupName, tvRank;
    private ImageView profileImage;
    private Button btnSave;

    private String userUid;
    private DatabaseReference usersRef;

    private Uri imageUri;

    private static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);

        userUid = getIntent().getStringExtra("USER_UID");

        if (userUid == null) {
            Toast.makeText(this, "No user selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        usersRef = FirebaseDatabase.getInstance().getReference("users").child(userUid);

        // INIT
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etPhone = findViewById(R.id.etPhoneNum);

        right_answers = findViewById(R.id.right_answers);
        wrong_answers = findViewById(R.id.wrong_answers);
        tvGroupName = findViewById(R.id.tvGroupName);
        tvRank = findViewById(R.id.tvRank);

        profileImage = findViewById(R.id.profileImage);
        btnSave = findViewById(R.id.btnSave);

        loadUserData();

        // PICK IMAGE
        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE);
        });

        // SAVE BUTTON
        btnSave.setOnClickListener(v -> saveUser());
    }

    private void loadUserData() {
        usersRef.get().addOnSuccessListener(snapshot -> {

            User user = snapshot.getValue(User.class);

            if (user == null) return;

            // FILL FIELDS
            etFirstName.setText(user.getFname());
            etLastName.setText(user.getLname());
            etPhone.setText(user.getPhone());

            right_answers.setText(String.valueOf(user.getCorrect_answers()));
            wrong_answers.setText(String.valueOf(user.getWrong_answers()));

            // LOAD IMAGE
            if (user.getProfileImageUrl() != null) {
                Glide.with(this)
                        .load(user.getProfileImageUrl())
                        .into(profileImage);
            }

            // GROUP
            if (user.getGroupId() != null) {
                tvGroupName.setText(user.getGroupId());
            }

        });
    }

    private void saveUser() {

        String fname = etFirstName.getText().toString();
        String lname = etLastName.getText().toString();
        String phone = etPhone.getText().toString();

        if (imageUri != null) {

            StorageReference storageRef = FirebaseStorage.getInstance()
                    .getReference("profile_images/" + userUid);

            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot ->
                            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {

                                updateUser(fname, lname, phone, uri.toString());

                            }))
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show());

        } else {
            updateUser(fname, lname, phone, null);
        }
    }

    private void updateUser(String fname, String lname, String phone, String imageUrl) {

        usersRef.get().addOnSuccessListener(snapshot -> {

            User user = snapshot.getValue(User.class);
            if (user == null) return;

            user.setFname(fname);
            user.setLname(lname);
            user.setPhone(phone);

            if (imageUrl != null) {
                user.setProfileImageUrl(imageUrl);
            }

            usersRef.setValue(user).addOnSuccessListener(unused ->
                    Toast.makeText(this, "Updated!", Toast.LENGTH_SHORT).show()
            );
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }
}