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
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import de.hdodenhof.circleimageview.CircleImageView;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

public class ProfileFragment extends Fragment {

    private CircleImageView profileImage;
    private TextInputEditText etFullName, etEmail, etPhone, etBio;
    private Button btnSaveProfile, btnCancel;
    private ImageButton btnEditPhoto;
    private TextView tvUserNameHeader; // For updating header name

    // NEW BUTTON
    private Button btnShareTasks;

    private DatabaseHelper dbHelper;
    private TaskManager taskManager; // Need TaskManager to fetch tasks
    private String userId = "local_user";

    private static final String TAG = "ProfileFragment";
    private static final int PICK_IMAGE_REQUEST = 1001;
    private static final int MAX_IMAGE_SIZE = 500;

    private Uri selectedImageUri;
    private String currentBase64Image;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // ASSUMED LAYOUT NAME: fragment_profile.xml
        View view = inflater.inflate(R.layout.activity_profile, container, false);

        Log.d(TAG, "onCreateView: Fragment created");

        dbHelper = new DatabaseHelper(requireContext());
        taskManager = TaskManager.getInstance(requireContext());

        initializeViews(view);
        loadUserData();
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
        btnEditPhoto = view.findViewById(R.id.btn_edit_photo);

        // Header Text
        tvUserNameHeader = view.findViewById(R.id.tv_user_name);

        // NEW Button
        btnShareTasks = view.findViewById(R.id.btn_share_tasks);

        // REMOVED old buttons: btnChangePassword, btnPrivacySettings, btnLogout
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

        // FIX: Update the header TextView with the loaded name
        if (tvUserNameHeader != null) {
            tvUserNameHeader.setText(userProfile.getName());
        }

        if (userProfile.getProfileImageBase64() != null && !userProfile.getProfileImageBase64().isEmpty()) {
            currentBase64Image = userProfile.getProfileImageBase64();
            loadBase64Image(userProfile.getProfileImageBase64());
        } else {
            profileImage.setImageResource(R.drawable.outline_account_circle_24);
        }
    }

    private void setupClickListeners() {
        btnEditPhoto.setOnClickListener(v -> openImagePicker());

        btnSaveProfile.setOnClickListener(v -> saveProfile());

        btnCancel.setOnClickListener(v -> loadUserData());

        // NEW: Share/Export Button Listener
        btnShareTasks.setOnClickListener(v -> exportAndShareTasks());

        // REMOVED listeners for btnChangePassword, btnPrivacySettings, btnLogout
    }

    private void exportAndShareTasks() {
        Toast.makeText(requireContext(), "Generating tasks data...", Toast.LENGTH_SHORT).show();

        // 1. Fetch all tasks
        List<Task> tasks = taskManager.getAllTasks();

        // 2. Generate a readable String (or ideally, a PDF file path)
        String shareContent = generateTaskSummary(tasks);

        if (shareContent.isEmpty()) {
            Toast.makeText(requireContext(), "No tasks to export.", Toast.LENGTH_SHORT).show();
            return;
        }

        // NOTE: For full PDF support, you would generate a PDF file here (e.g., using iText)
        // and get the file URI to share. We are using simple text share for safety and completeness.

        // 3. Launch simple sharing intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My ToDo App Task List Export");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent);

        // Ensure there is an app to handle the intent
        if (shareIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(Intent.createChooser(shareIntent, "Share Tasks Via"));
        } else {
            Toast.makeText(requireContext(), "No apps installed to handle sharing.", Toast.LENGTH_SHORT).show();
        }
    }

    private String generateTaskSummary(List<Task> tasks) {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Task List Export (").append(dbHelper.getUserProfile().getName()).append(") ---\n\n");

        if (tasks == null || tasks.isEmpty()) {
            return "";
        }

        for (Task task : tasks) {
            sb.append("Title: ").append(task.getTitle()).append("\n");
            sb.append("Priority: ").append(task.getPriority()).append("\n");
            sb.append("Status: ").append(task.getStatus()).append("\n");
            sb.append("Category: ").append(task.getCategory()).append("\n");
            sb.append("Due: ").append(task.getDate()).append(" (").append(task.getStartTime()).append(" - ").append(task.getEndTime()).append(")\n");
            sb.append("----------------------------\n");
        }

        return sb.toString();
    }

    private void openImagePicker() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE_REQUEST);
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Error opening image picker", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    Glide.with(this)
                            .load(selectedImageUri)
                            .into(profileImage);

                    saveImageAsBase64(selectedImageUri);
                } catch (Exception e) {
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
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(inputStream, null, options);
                inputStream.close();

                int scale = 1;
                while ((options.outWidth / scale / 2) >= 400 && (options.outHeight / scale / 2) >= 400) {
                    scale *= 2;
                }

                BitmapFactory.Options sampledOptions = new BitmapFactory.Options();
                sampledOptions.inSampleSize = scale;
                inputStream = getActivity().getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, sampledOptions);
                inputStream.close();

                if (bitmap != null) {
                    int quality = 80;
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);

                    byte[] imageBytes = baos.toByteArray();
                    int sizeInKB = imageBytes.length / 1024;

                    while (sizeInKB > MAX_IMAGE_SIZE && quality > 40) {
                        quality -= 10;
                        baos.reset();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                        imageBytes = baos.toByteArray();
                        sizeInKB = imageBytes.length / 1024;
                    }

                    String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                    getActivity().runOnUiThread(() -> {
                        currentBase64Image = base64Image;
                        saveBase64ImageToDatabase(base64Image);
                        Toast.makeText(getActivity(), "Profile picture saved!", Toast.LENGTH_SHORT).show();
                    });

                    bitmap.recycle();
                }

            } catch (Exception e) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "Error processing image", Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void saveBase64ImageToDatabase(String base64Image) {
        UserProfile currentProfile = dbHelper.getUserProfile();
        if (currentProfile != null) {
            currentProfile.setProfileImageBase64(base64Image);
            dbHelper.saveUserProfile(currentProfile);
        }
    }

    private void loadBase64Image(String base64Image) {
        try {
            byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

            if (getActivity() != null) {
                Glide.with(this).load(bitmap).into(profileImage);
            } else {
                profileImage.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
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
            // FIX: Immediately update the header TextView after a successful save
            if (tvUserNameHeader != null) {
                tvUserNameHeader.setText(name);
            }
        } else {
            Toast.makeText(getActivity(), "Failed to update profile", Toast.LENGTH_SHORT).show();
        }
    }
}