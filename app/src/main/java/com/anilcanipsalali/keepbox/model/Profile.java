package com.anilcanipsalali.keepbox.model;

public class Profile {
    private String name, email, image, language;

    public Profile(String email, String name, String image, String language) {
        this.email = email;
        this.name = name;
        this.image = image;
        this.language = language;
    }

    public Profile() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
