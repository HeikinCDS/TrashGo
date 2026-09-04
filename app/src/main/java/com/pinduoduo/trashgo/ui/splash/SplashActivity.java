package com.pinduoduo.trashgo.ui.splash;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.pinduoduo.trashgo.R;
import com.pinduoduo.trashgo.databinding.ActivitySplashBinding;
import com.pinduoduo.trashgo.ui.auth.LoginActivity;
import com.pinduoduo.trashgo.ui.onboarding.OnboardingActivity;
import com.pinduoduo.trashgo.util.Prefs;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";

    private static final long HOLD_MS = 1900L;
    private static final long HOLD_REDUCED_MS = 900L;

    private ActivitySplashBinding binding;
    private MediaPlayer chime;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean advanced = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Prefs.apply(this);

        boolean animate = animationsEnabled();

        playChime();

        if (animate) {
            runIntro();
        } else {
            binding.splashLogo.setAlpha(1f);
            binding.splashWordmark.setAlpha(1f);
            binding.splashSlogan.setAlpha(1f);
        }

        handler.postDelayed(this::advance, animate ? HOLD_MS : HOLD_REDUCED_MS);
    }

    private void runIntro() {
        binding.splashLogo.setScaleX(0.88f);
        binding.splashLogo.setScaleY(0.88f);
        binding.splashLogo.animate()
                .alpha(1f).scaleX(1f).scaleY(1f)
                .setDuration(620)
                .setInterpolator(new DecelerateInterpolator(1.6f))
                .start();

        rise(binding.splashWordmark, 340);
        rise(binding.splashSlogan, 520);
    }

    private void rise(View view, long delayMs) {
        float offset = 12f * getResources().getDisplayMetrics().density;
        view.setTranslationY(offset);
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(delayMs)
                .setDuration(520)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private boolean animationsEnabled() {
        float scale = Settings.Global.getFloat(
                getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 1f);
        return scale > 0f;
    }

    private void playChime() {
        if (!Prefs.splashSound(this)) {
            Log.i(TAG, "Chime skipped: turned off in Settings");
            return;
        }

        AudioManager audio = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audio != null && audio.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            Log.i(TAG, "Chime skipped: ringer mode is "
                    + (audio.getRingerMode() == AudioManager.RINGER_MODE_SILENT
                    ? "SILENT" : "VIBRATE"));
            return;
        }

        try {
            chime = MediaPlayer.create(this, R.raw.chime);
            if (chime == null) {
                Log.w(TAG, "Chime skipped: MediaPlayer.create returned null");
                return;
            }
            chime.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build());
            chime.setVolume(0.7f, 0.7f);
            chime.setOnCompletionListener(mp -> release());
            chime.start();
            Log.i(TAG, "Chime playing");
        } catch (Exception e) {
            Log.w(TAG, "Chime failed", e);
            release();
        }
    }

    private void release() {
        if (chime != null) {
            chime.release();
            chime = null;
        }
    }

    private void advance() {
        if (advanced || isFinishing()) {
            return;
        }
        advanced = true;

        Intent next = OnboardingActivity.isCompleted(this)
                ? new Intent(this, LoginActivity.class)
                : new Intent(this, OnboardingActivity.class);

        startActivity(next);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        release();
        binding = null;
        super.onDestroy();
    }
}
