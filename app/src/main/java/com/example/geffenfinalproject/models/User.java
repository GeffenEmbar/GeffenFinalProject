package com.example.geffenfinalproject.models;

public class User {
    private String id, fname, lname, email, phone, password;
    private boolean admin;
    private int correct_answers, wrong_answers;
    private String imageUrl;
    private String groupId;
    private String profileImageUrl;


    public User(String id, String fname, String lname, String email, String phone, String password, boolean admin, int correct_answers, int wrong_answers) {
        this.id = id;
        this.fname = fname;
        this.lname = lname;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.admin = admin;
        this.correct_answers = correct_answers;
        this.wrong_answers = wrong_answers;
    }
    public User ()
    {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public int getCorrect_answers() {
        return correct_answers;
    }

    public void setCorrect_answers(int correct_answers) {
        this.correct_answers = correct_answers;
    }

    public int getWrong_answers() {
        return wrong_answers;
    }

    public void setWrong_answers(int wrong_answers) {
        this.wrong_answers = wrong_answers;
    }

    public String getGroupId() { return groupId; }

    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getImageUrl() { return imageUrl; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }


    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", fname='" + fname + '\'' +
                ", lname='" + lname + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", password='" + password + '\'' +
                ", admin=" + admin +
                ", correct_answers=" + correct_answers +
                ", wrong_answers=" + wrong_answers +
                ", groupId='" + groupId + '\'' +
                '}';
    }
}
