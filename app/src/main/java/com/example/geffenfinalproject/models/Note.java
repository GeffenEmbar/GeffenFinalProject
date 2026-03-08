package com.example.geffenfinalproject.models;

public class Note {
    private String noteName;
    private int audioResId;

    public Note() {

    }

    public Note(String noteName, int audioResId)
    {
        this.noteName = noteName;
        this.audioResId = audioResId;
    }

    public String getNoteName() { return noteName; }
    public int getAudioResId() { return audioResId; }
    public void setNoteName(String noteName) { this.noteName = noteName; }
    public void setAudioResId(int audioResId) { this.audioResId = audioResId; }
}
