package com.example.geffenfinalproject;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class user_piano extends BaseActivity {

    private MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_piano);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupPianoKeys();
    }

    private void setupPianoKeys() {
        // White Keys Octave 4
        setPianoKeyListener(R.id.btnC4, R.raw.c4);
        setPianoKeyListener(R.id.btnD4, R.raw.d4);
        setPianoKeyListener(R.id.btnE4, R.raw.e4);
        setPianoKeyListener(R.id.btnF4, R.raw.f4);
        setPianoKeyListener(R.id.btnG4, R.raw.g4);
        setPianoKeyListener(R.id.btnA4, R.raw.a4);
        setPianoKeyListener(R.id.btnB4, R.raw.b4);

        // White Keys Octave 5
        setPianoKeyListener(R.id.btnC5, R.raw.c5);
        setPianoKeyListener(R.id.btnD5, R.raw.d5);
        setPianoKeyListener(R.id.btnE5, R.raw.e5);
        setPianoKeyListener(R.id.btnF5, R.raw.f5);
        setPianoKeyListener(R.id.btnG5, R.raw.g5);
        setPianoKeyListener(R.id.btnA5, R.raw.a5);
        setPianoKeyListener(R.id.btnB5, R.raw.b5);

        // Black Keys Octave 4
        setPianoKeyListener(R.id.btnCSharp4, R.raw.csharp4);
        setPianoKeyListener(R.id.btnDSharp4, R.raw.dsharp4);
        setPianoKeyListener(R.id.btnFSharp4, R.raw.fsharp4);
        setPianoKeyListener(R.id.btnGSharp4, R.raw.gsharp4);
        setPianoKeyListener(R.id.btnASharp4, R.raw.asharp4);

        // Black Keys Octave 5
        setPianoKeyListener(R.id.btnCSharp5, R.raw.csharp5);
        setPianoKeyListener(R.id.btnDSharp5, R.raw.dsharp5);
        setPianoKeyListener(R.id.btnFSharp5, R.raw.fsharp5);
        setPianoKeyListener(R.id.btnGSharp5, R.raw.gsharp5);
        setPianoKeyListener(R.id.btnASharp5, R.raw.asharp5);
    }

    private void setPianoKeyListener(int buttonId, int resId) {
        Button btn = findViewById(buttonId);
        if (btn != null) {
            btn.setOnClickListener(v -> playSound(resId));
        }
    }

    private void playSound(int resId) {
        if (mp != null) {
            mp.release();
        }
        mp = MediaPlayer.create(this, resId);
        if (mp != null) {
            mp.start();
            mp.setOnCompletionListener(MediaPlayer::release);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mp != null) {
            mp.release();
            mp = null;
        }
    }
}
