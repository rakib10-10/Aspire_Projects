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
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import de.hdodenhof.circleimageview.CircleImageView;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

public class ProfileFragment extends Fragment {

    private CircleImageView profileImage;
    private TextInputEditText etFullName, etEmail, etPhone, etBio;
    private MaterialButton btnSaveProfile, btnShareTasks, btnLogout;
    private FloatingActionButton btnEditPhoto;

    // This is the variable for the name at the top of the Profile Page
    private TextView tvUserNameHeader, tvUserEmailDisplay;

    private DatabaseHelper dbHelper;
    private TaskManager taskManager;
    private SessionManager sessionManager;
    private String userId = "local_user";

    private static final int PICK_IMAGE_REQUEST = 1001;
    private static final int MAX_IMAGE_SIZE = 500;

    private Uri selectedImageUri;
    private String currentBase64Image = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile, container, false); // Make sure this matches your XML filename

        dbHelper = new DatabaseHelper(requireContext());
        taskManager = TaskManager.getInstance(requireContext());
        sessionManager = new SessionManager(requireContext());

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
        btnShareTasks = view.findViewById(R.id.btn_share_tasks);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnEditPhoto = view.findViewById(R.id.btn_edit_photo);

        // Correctly linking the header TextView
        tvUserNameHeader = view.findViewById(R.id.tv_user_name);
        tvUserEmailDisplay = view.findViewById(R.id.tv_user_email_display);
    }

    private void loadUserData() {
        String sessionEmail = sessionManager.getUserEmail();
        UserProfile userProfile;

        if (!sessionEmail.isEmpty()) {
            // Logged in: Fetch from AUTH table (which now has the image column)
            userProfile = dbHelper.getUserDetails(sessionEmail);
        } else {
            // Not logged in: Fetch from local PROFILE table
            userProfile = dbHelper.getUserProfile();
        }

        if (userProfile != null) {
            updateUI(userProfile);
        }
    }

    private void updateUI(UserProfile userProfile) {
        etFullName.setText(userProfile.getName());
        etEmail.setText(userProfile.getEmail());
        etPhone.setText(userProfile.getPhone());
        etBio.setText(userProfile.getBio());

        // Update the header text with the correct variable
        if (tvUserNameHeader != null) {
            tvUserNameHeader.setText(userProfile.getName());
        }
        if (tvUserEmailDisplay != null) {
            tvUserEmailDisplay.setText(userProfile.getEmail());
        }

        // Load Image
        if (userProfile.getProfileImageBase64() != null && !userProfile.getProfileImageBase64().isEmpty()) {
            currentBase64Image = userProfile.getProfileImageBase64();
            loadBase64Image(currentBase64Image);
        } else {
            profileImage.setImageResource(R.drawable.outline_account_circle_24);
        }
    }

    private void setupClickListeners() {
        if (btnEditPhoto != null) btnEditPhoto.setOnClickListener(v -> openImagePicker());
        if (btnSaveProfile != null) btnSaveProfile.setOnClickListener(v -> saveProfile());
        if (btnShareTasks != null) btnShareTasks.setOnClickListener(v -> exportAndShareTasks());
        if (btnLogout != null) btnLogout.setOnClickListener(v -> logoutUser());
    }

    private void logoutUser() {

        TaskManager.clearInstance();
        sessionManager.logoutUser();
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void exportAndShareTasks() {
        List<Task> tasks = taskManager.getAllTasks();
        String shareContent = generateTaskSummary(tasks);

        if (shareContent.isEmpty()) {
            Toast.makeText(requireContext(), "No tasks to export.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My ToDo App Task List Export");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent);

        if (shareIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(Intent.createChooser(shareIntent, "Share Tasks Via"));
        } else {
            Toast.makeText(requireContext(), "No apps installed to handle sharing.", Toast.LENGTH_SHORT).show();
        }
    }

    private String generateTaskSummary(List<Task> tasks) {
        StringBuilder sb = new StringBuilder();
        String userName = sessionManager.getUserName();
        sb.append("--- Task List Export (").append(userName).append(") ---\n\n");

        if (tasks == null || tasks.isEmpty()) return "";

        for (Task task : tasks) {
            sb.append("Title: ").append(task.getTitle()).append("\n");
            sb.append("Status: ").append(task.getStatus()).append("\n");
            sb.append("Due: ").append(task.getDate()).append("\n");
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
                saveImageAsBase64(selectedImageUri);
            }
        }
    }

    private void saveImageAsBase64(Uri imageUri) {
        Toast.makeText(getActivity(), "Processing image...", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();

                // Compression
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
                byte[] imageBytes = baos.toByteArray();
                String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                getActivity().runOnUiThread(() -> {
                    currentBase64Image = base64Image;
                    loadBase64Image(currentBase64Image); // Update UI

                    // Trigger save immediately so user doesn't have to hit "Save Profile"
                    saveProfile();
                });

            } catch (Exception e) {
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Error processing image", Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void loadBase64Image(String base64Image) {
        try {
            byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            if (getActivity() != null) {
                Glide.with(this).load(bitmap).into(profileImage);
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

        // Call the database update
        updateDatabase(name, email, phone, bio, currentBase64Image);

        // Update session
        sessionManager.createLoginSession(name, email);

        // FIXED LINE: Use tvUserNameHeader instead of tvUserNameGreeting
        if (tvUserNameHeader != null) tvUserNameHeader.setText(name);

        Toast.makeText(getActivity(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
    }

    private void updateDatabase(String name, String email, String phone, String bio, String image) {
        UserProfile updatedProfile = new UserProfile(userId, name, email, phone, bio, image);
        String sessionEmail = sessionManager.getUserEmail();

        if (!sessionEmail.isEmpty()) {
            // Logged in: Update Auth Table
            dbHelper.updateUserInfo(updatedProfile);
        } else {
            // Not logged in: Update Local Profile Table
            dbHelper.saveUserProfile(updatedProfile);
        }
    }
}