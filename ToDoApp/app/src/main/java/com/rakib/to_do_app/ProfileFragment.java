package com.rakib.to_do_app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import de.hdodenhof.circleimageview.CircleImageView;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ProfileFragment extends Fragment {

    private CircleImageView profileImage;
    private TextInputEditText etFullName, etEmail, etPhone, etBio;
    private Button btnSaveProfile, btnCancel, btnChangePassword, btnPrivacySettings, btnLogout;
    private ImageButton btnEditPhoto;

    private DatabaseHelper dbHelper;
    private String userId = "local_user";

    private static final String TAG = "ProfileFragment";
    private static final int PICK_IMAGE_REQUEST = 1001;
    private static final int MAX_IMAGE_SIZE = 500;

    private Uri selectedImageUri;
    private String currentBase64Image;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile, container, false);

        Log.d(TAG, "onCreateView: Fragment created");

        // Initialize SQLite Database
        dbHelper = new DatabaseHelper(requireContext());

        // Initialize views
        initializeViews(view);

        // Load user data
        loadUserData();

        // Set click listeners
        setupClickListeners();

        return view;
    }

    private void initializeViews(View view) {
        profileImage = view.findViewById(R.id.iv_profile_picture);
        etFullName = view.findViewById(R.id.et_full_name);
        etEmail = view.findViewById(R.id.et_email);
        etPhone = view.findViewById(R.id.et_phone);
        etBio = view.findViewById(R.id.et_bio);
        btnSaveProfile = view.findViewById(R.id.btn_save_profile);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnChangePassword = view.findViewById(R.id.btn_change_password);
        btnPrivacySettings = view.findViewById(R.id.btn_privacy_settings);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnEditPhoto = view.findViewById(R.id.btn_edit_photo);

        Log.d(TAG, "All views initialized successfully");
    }

    private void loadUserData() {
        Log.d(TAG, "loadUserData: Loading user data from SQLite");

        UserProfile userProfile = dbHelper.getUserProfile();
        if (userProfile != null) {
            updateUI(userProfile);
        }
    }

    private void updateUI(UserProfile userProfile) {
        etFullName.setText(userProfile.getName());
        etEmail.setText(userProfile.getEmail());
        etPhone.setText(userProfile.getPhone());
        etBio.setText(userProfile.getBio());

        // Load profile image from Base64 if exists
        if (userProfile.getProfileImageBase64() != null && !userProfile.getProfileImageBase64().isEmpty()) {
            currentBase64Image = userProfile.getProfileImageBase64();
            loadBase64Image(userProfile.getProfileImageBase64());
            Log.d(TAG, "Profile image loaded from Base64");
        } else {
            profileImage.setImageResource(R.drawable.outline_account_circle_24);
            Log.d(TAG, "No profile image, using placeholder");
        }
    }

    private void setupClickListeners() {
        // Edit Photo button click listener
        btnEditPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Edit Photo button clicked");
                openImagePicker();
            }
        });

        btnSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfile();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUserData();
            }
        });

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Change Password feature coming soon!", Toast.LENGTH_SHORT).show();
            }
        });

        btnPrivacySettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Privacy Settings feature coming soon!", Toast.LENGTH_SHORT).show();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Logout feature would require authentication", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImagePicker() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE_REQUEST);
        } catch (Exception e) {
            Log.e(TAG, "Error opening image picker: " + e.getMessage());
            Toast.makeText(getActivity(), "Error opening image picker", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                Log.d(TAG, "Image selected: " + selectedImageUri.toString());

                // Set the selected image to CircleImageView immediately
                try {
                    Glide.with(this)
                            .load(selectedImageUri)
                            .into(profileImage);

                    // Convert to Base64 and save
                    saveImageAsBase64(selectedImageUri);
                } catch (Exception e) {
                    Log.e(TAG, "Error loading selected image: " + e.getMessage());
                    Toast.makeText(getActivity(), "Error loading image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void saveImageAsBase64(Uri imageUri) {
        Toast.makeText(getActivity(), "Processing image...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);

                // First, decode the bitmap to check size
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(inputStream, null, options);
                inputStream.close();

                // Calculate sampling to reduce image size
                int scale = 1;
                while ((options.outWidth / scale / 2) >= 400 && (options.outHeight / scale / 2) >= 400) {
                    scale *= 2;
                }

                // Decode with sampling
                BitmapFactory.Options sampledOptions = new BitmapFactory.Options();
                sampledOptions.inSampleSize = scale;
                inputStream = getActivity().getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, sampledOptions);
                inputStream.close();

                if (bitmap != null) {
                    // Compress to JPEG with quality adjustment
                    int quality = 80;
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);

                    // Check size and reduce quality if needed
                    byte[] imageBytes = baos.toByteArray();
                    int sizeInKB = imageBytes.length / 1024;

                    while (sizeInKB > MAX_IMAGE_SIZE && quality > 40) {
                        quality -= 10;
                        baos.reset();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                        imageBytes = baos.toByteArray();
                        sizeInKB = imageBytes.length / 1024;
                    }

                    // Convert to Base64
                    String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                    // Update on UI thread
                    getActivity().runOnUiThread(() -> {
                        currentBase64Image = base64Image;
                        saveBase64ImageToDatabase(base64Image);
                        Log.d(TAG, "Image converted to Base64");
                        Toast.makeText(getActivity(), "Profile picture saved!", Toast.LENGTH_SHORT).show();
                    });

                    bitmap.recycle();
                }

            } catch (Exception e) {
                Log.e(TAG, "Error converting image to Base64: " + e.getMessage());
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "Error processing image", Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void saveBase64ImageToDatabase(String base64Image) {
        // Get current profile and update image
        UserProfile currentProfile = dbHelper.getUserProfile();
        if (currentProfile != null) {
            currentProfile.setProfileImageBase64(base64Image);
            dbHelper.saveUserProfile(currentProfile);
            Log.d(TAG, "Profile image saved to database");
        }
    }

    private void loadBase64Image(String base64Image) {
        try {
            byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            profileImage.setImageBitmap(bitmap);
            Log.d(TAG, "Base64 image loaded successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error loading Base64 image: " + e.getMessage());
            profileImage.setImageResource(R.drawable.outline_account_circle_24);
        }
    }

    private void saveProfile() {
        String name = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String bio = etBio.getText().toString().trim();

        if (name.isEmpty()) {
            etFullName.setError("Name is required");
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            return;
        }

        UserProfile updatedProfile = new UserProfile(
                userId,
                name,
                email,
                phone,
                bio,
                currentBase64Image != null ? currentBase64Image : ""
        );

        long result = dbHelper.saveUserProfile(updatedProfile);
        if (result > 0) {
            Toast.makeText(getActivity(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "Failed to update profile", Toast.LENGTH_SHORT).show();
        }
    }
}