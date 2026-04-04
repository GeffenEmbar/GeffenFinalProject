package com.example.geffenfinalproject.models;

public class User {
    private String id, fname, lname, email, phone, password;
    private boolean admin;
    private int correct_answers, wrong_answers;
    
    // Game-specific stats
    private int notesCorrect, notesWrong;
    private int intervalsCorrect, intervalsWrong;
    private int chordsCorrect, chordsWrong;
    private int complexChordsCorrect, complexChordsWrong;
    private int quizCorrect, quizWrong;

    // Streaks
    private int notesStreak, maxNotesStreak;
    private int intervalsStreak, maxIntervalsStreak;
    private int chordsStreak, maxChordsStreak;
    private int complexChordsStreak, maxComplexChordsStreak;
    private int quizStreak, maxQuizStreak;
    
    private String groupId;
    private String profileImage;


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

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public int getNotesCorrect() { return notesCorrect; }
    public void setNotesCorrect(int notesCorrect) { this.notesCorrect = notesCorrect; }

    public int getNotesWrong() { return notesWrong; }
    public void setNotesWrong(int notesWrong) { this.notesWrong = notesWrong; }

    public int getIntervalsCorrect() { return intervalsCorrect; }
    public void setIntervalsCorrect(int intervalsCorrect) { this.intervalsCorrect = intervalsCorrect; }

    public int getIntervalsWrong() { return intervalsWrong; }
    public void setIntervalsWrong(int intervalsWrong) { this.intervalsWrong = intervalsWrong; }

    public int getQuizCorrect() { return quizCorrect; }
    public void setQuizCorrect(int quizCorrect) { this.quizCorrect = quizCorrect; }

    public int getQuizWrong() { return quizWrong; }
    public void setQuizWrong(int quizWrong) { this.quizWrong = quizWrong; }

    public int getChordsCorrect() { return chordsCorrect; }
    public void setChordsCorrect(int chordsCorrect) { this.chordsCorrect = chordsCorrect; }

    public int getChordsWrong() { return chordsWrong; }
    public void setChordsWrong(int chordsWrong) { this.chordsWrong = chordsWrong; }

    public int getComplexChordsCorrect() { return complexChordsCorrect; }
    public void setComplexChordsCorrect(int complexChordsCorrect) { this.complexChordsCorrect = complexChordsCorrect; }

    public int getComplexChordsWrong() { return complexChordsWrong; }
    public void setComplexChordsWrong(int complexChordsWrong) { this.complexChordsWrong = complexChordsWrong; }

    public int getNotesStreak() { return notesStreak; }
    public void setNotesStreak(int notesStreak) { this.notesStreak = notesStreak; }

    public int getMaxNotesStreak() { return maxNotesStreak; }
    public void setMaxNotesStreak(int maxNotesStreak) { this.maxNotesStreak = maxNotesStreak; }

    public int getIntervalsStreak() { return intervalsStreak; }
    public void setIntervalsStreak(int intervalsStreak) { this.intervalsStreak = intervalsStreak; }

    public int getMaxIntervalsStreak() { return maxIntervalsStreak; }
    public void setMaxIntervalsStreak(int maxIntervalsStreak) { this.maxIntervalsStreak = maxIntervalsStreak; }

    public int getChordsStreak() { return chordsStreak; }
    public void setChordsStreak(int chordsStreak) { this.chordsStreak = chordsStreak; }

    public int getMaxChordsStreak() { return maxChordsStreak; }
    public void setMaxChordsStreak(int maxChordsStreak) { this.maxChordsStreak = maxChordsStreak; }

    public int getComplexChordsStreak() { return complexChordsStreak; }
    public void setComplexChordsStreak(int complexChordsStreak) { this.complexChordsStreak = complexChordsStreak; }

    public int getMaxComplexChordsStreak() { return maxComplexChordsStreak; }
    public void setMaxComplexChordsStreak(int maxComplexChordsStreak) { this.maxComplexChordsStreak = maxComplexChordsStreak; }

    public int getQuizStreak() { return quizStreak; }
    public void setQuizStreak(int quizStreak) { this.quizStreak = quizStreak; }

    public int getMaxQuizStreak() { return maxQuizStreak; }
    public void setMaxQuizStreak(int maxQuizStreak) { this.maxQuizStreak = maxQuizStreak; }

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
