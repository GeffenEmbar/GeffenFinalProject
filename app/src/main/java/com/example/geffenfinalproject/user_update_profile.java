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

public class user_update_profile extends BaseActivity {

    private EditText etFirstName, etLastName, etPhone;
    private ImageView profileImage;
    private Button btnSave;
    private com.google.android.material.button.MaterialButton btnRemovePhoto;

    private String userUid;
    private DatabaseReference usersRef;
    private boolean isImageRemoved = false;

    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_GALLERY = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_update_profile);

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

        profileImage = findViewById(R.id.profileImage);
        btnSave = findViewById(R.id.btnSave);
        btnRemovePhoto = findViewById(R.id.btnRemovePhoto);

        loadUserData();

        // PICK IMAGE
        profileImage.setOnClickListener(v -> {
            showImageSourceDialog();
        });

        // REMOVE IMAGE
        btnRemovePhoto.setOnClickListener(v -> {
            isImageRemoved = true;
            profileImage.setImageResource(android.R.drawable.ic_menu_camera);
            Toast.makeText(this, "Photo removed. Save to apply.", Toast.LENGTH_SHORT).show();
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

            // LOAD IMAGE
            if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                Bitmap bitmap = ImageUtil.convertFrom64base(user.getProfileImage());
                if (bitmap != null) {
                    profileImage.setImageBitmap(bitmap);
                }
            }

        });
    }

    private void saveUser() {
        String fname = etFirstName.getText().toString();
        String lname = etLastName.getText().toString();
        String phone = etPhone.getText().toString();

        String base64Image = isImageRemoved ? null : ImageUtil.convertTo64Base(profileImage);


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

            usersRef.setValue(user).addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Updated!", Toast.LENGTH_SHORT).show();
                    finish();
            });
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
                isImageRemoved = false;
            }

            if (requestCode == REQUEST_GALLERY) {
                Uri imageUri = data.getData();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                profileImage.setImageBitmap(bitmap);
                isImageRemoved = false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load image", e);
        }
    }
}
