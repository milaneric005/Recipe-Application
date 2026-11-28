package com.team10.recipeapp.data.model;

import com.google.firebase.firestore.DocumentId;

public class User {

    @DocumentId
    private String id;

    private String username;
    private String email;
    private String profileImage;
    private String region;
    private String bio;
    private boolean activated;
    private Role role;
    private int publishedRecipesCount;

    public User() {
    }

    public User(String id, String username, String email, String region) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.region = region;
        this.activated = false;
        this.role = Role.USER;
        this.publishedRecipesCount = 0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public boolean isActivated() { return activated; }
    public void setActivated(boolean activated) { this.activated = activated; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public int getPublishedRecipesCount() { return publishedRecipesCount; }
    public void setPublishedRecipesCount(int publishedRecipesCount) { this.publishedRecipesCount = publishedRecipesCount; }
}