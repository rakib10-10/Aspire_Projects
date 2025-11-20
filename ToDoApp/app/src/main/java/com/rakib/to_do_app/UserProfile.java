package com.rakib.to_do_app;

public class UserProfile {
    private String userId;
    private String name;
    private String email;
    private String phone;
    private String bio;
    private String profileImageBase64; // Changed from profileImageUrl

    public UserProfile() {
        // Default constructor required for Firebase
    }

    public UserProfile(String userId, String name, String email, String phone, String bio, String profileImageBase64) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.bio = bio;
        this.profileImageBase64 = profileImageBase64;
    }

    // Getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getProfileImageBase64() { return profileImageBase64; }
    public void setProfileImageBase64(String profileImageBase64) { this.profileImageBase64 = profileImageBase64; }
}