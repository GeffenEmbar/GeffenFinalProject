package com.example.geffenfinalproject;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.geffenfinalproject.adapters.ImageSourceAdapter;
import com.example.geffenfinalproject.models.Group;
import com.example.geffenfinalproject.models.ImageSourceOption;
import com.example.geffenfinalproject.models.User;
import com.example.geffenfinalproject.services.DatabaseService;
import com.example.geffenfinalproject.utils.ImageUtil;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class user_profile extends BaseActivity {

    private EditText etFirstName, etLastName, etPhone;
    private TextView right_answers, wrong_answers, tvGroupName, tvRank;
    private ImageView profileImage;
    private Button btnSave;

    private String userUid;
    private DatabaseReference usersRef;

    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_GALLERY = 101;

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
            showImageSourceDialog();
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
            if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                Bitmap bitmap = ImageUtil.convertFrom64base(user.getProfileImage());
                if (bitmap != null) {
                    profileImage.setImageBitmap(bitmap);
                }
            } else {
                // fallback icon
                profileImage.setImageResource(android.R.drawable.ic_menu_myplaces);
            }

            // GROUP
            bindGroupName(user.getGroupId());
            bindUserRank(user.getGroupId(), userUid);

        });
    }

    private void bindUserRank(String groupId, String currentUserId) {
        if (groupId == null || groupId.trim().isEmpty()) {
            tvRank.setText("N/A");
            return;
        }

        DatabaseService.getInstance().getUserList(new DatabaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                // Filter by group
                List<User> groupUsers = new ArrayList<>();
                for (User u : users) {
                    if (groupId.equals(u.getGroupId())) {
                        groupUsers.add(u);
                    }
                }

                // Sort by correct answers descending
                Collections.sort(groupUsers, (u1, u2) ->
                        Integer.compare(u2.getCorrect_answers(), u1.getCorrect_answers())
                );

                // Find rank
                int rank = -1;
                for (int i = 0; i < groupUsers.size(); i++) {
                    if (groupUsers.get(i).getId().equals(currentUserId)) {
                        rank = i + 1;
                        break;
                    }
                }

                if (rank != -1) {
                    tvRank.setText(String.valueOf(rank));
                } else {
                    tvRank.setText("N/A");
                }
            }

            @Override
            public void onFailed(Exception e) {
                tvRank.setText("Error");
            }
        });
    }

    private void bindGroupName(String groupId) {
        if (groupId == null || groupId.trim().isEmpty()) {
            tvGroupName.setText("No Group");
            return;
        }

        tvGroupName.setText("Group...");

        com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference("groups")
                .child(groupId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    Group guild = snapshot.getValue(Group.class);
                    String groupName = guild != null ? guild.getGroupName() : null;

                    if (groupName == null || groupName.trim().isEmpty()) {
                        tvGroupName.setText("Unknown Group");
                        return;
                    }

                    tvGroupName.setText(groupName.trim());
                })
                .addOnFailureListener(e -> tvGroupName.setText("Unknown Group"));
    }

    private void saveUser() {
        String fname = etFirstName.getText().toString();
        String lname = etLastName.getText().toString();
        String phone = etPhone.getText().toString();

        String base64Image = ImageUtil.convertTo64Base(profileImage);


        updateUser(fname, lname, phone, base64Image);
    }

    private void updateUser(String fname, String lname, String phone, String imageId) {
        usersRef.get().addOnSuccessListener(snapshot -> {

            User user = snapshot.getValue(User.class);
            if (user == null) return;

            user.setFname(fname);
            user.setLname(lname);
            user.setPhone(phone);
            user.setProfileImage(imageId);

            usersRef.setValue(user).addOnSuccessListener(unused ->
                    Toast.makeText(this, "Updated!", Toast.LENGTH_SHORT).show()
            );
        });
    }

    private void showImageSourceDialog() {
        List<ImageSourceOption> options = new ArrayList<>();

        options.add(new ImageSourceOption(
                "Camera",
                "Take a photo",
                R.drawable.photo_camera
        ));

        options.add(new ImageSourceOption(
                "Gallery",
                "Choose from gallery",
                R.drawable.gallery_thumbnail
        ));

        ImageSourceAdapter adapter = new ImageSourceAdapter(
                this,
                options,
                option -> {
                    if ("Camera".equals(option.getTitle())) {
                        ImageUtil.requestPermission(this);
                        openCamera();
                    } else {
                        openGallery();
                    }
                }
        );

        new AlertDialog.Builder(this)
                .setAdapter(adapter, null)
                .show();
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) return;

        try {
            if (requestCode == REQUEST_CAMERA) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                profileImage.setImageBitmap(bitmap);
            }

            if (requestCode == REQUEST_GALLERY) {
                Uri imageUri = data.getData();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                profileImage.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load image", e);
        }
    }
}
