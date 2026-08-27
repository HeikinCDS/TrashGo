package com.pinduoduo.trashgo.ui.onboarding;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.pinduoduo.trashgo.R;
import com.pinduoduo.trashgo.databinding.ActivityOnboardingBinding;
import com.pinduoduo.trashgo.ui.auth.LoginActivity;

import java.util.Arrays;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {
    private static final String PREFERENCES = "trashgo_onboarding";
    private static final String COMPLETED = "completed";

    private ActivityOnboardingBinding binding;
    private List<OnboardingAdapter.Page> pages;

    public static boolean isCompleted(Context context) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
                .getBoolean(COMPLETED, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pages = Arrays.asList(
                new OnboardingAdapter.Page(
                        R.mipmap.ic_launcher,
                        R.string.onboarding_welcome_title,
                        R.string.onboarding_welcome_body,
                        false),
                new OnboardingAdapter.Page(
                        R.drawable.ic_scan_waste,
                        R.string.onboarding_scan_title,
                        R.string.onboarding_scan_body,
                        true),
                new OnboardingAdapter.Page(
                        R.drawable.ic_map,
                        R.string.onboarding_map_title,
                        R.string.onboarding_map_body,
                        true),
                new OnboardingAdapter.Page(
                        R.drawable.ic_leaderboard,
                        R.string.onboarding_points_title,
                        R.string.onboarding_points_body,
                        true)
        );

        binding.onboardingPager.setAdapter(new OnboardingAdapter(pages));
        binding.onboardingPager.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        updateNavigation(position);
                    }
                });

        binding.skipButton.setOnClickListener(view -> completeOnboarding());
        binding.backButton.setOnClickListener(view -> {
            int position = binding.onboardingPager.getCurrentItem();
            if (position > 0) {
                binding.onboardingPager.setCurrentItem(position - 1, true);
            }
        });
        binding.nextButton.setOnClickListener(view -> {
            int position = binding.onboardingPager.getCurrentItem();
            if (position == pages.size() - 1) {
                completeOnboarding();
            } else {
                binding.onboardingPager.setCurrentItem(position + 1, true);
            }
        });

        updateNavigation(0);
    }

    private void updateNavigation(int position) {
        boolean lastPage = position == pages.size() - 1;
        binding.backButton.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
        binding.skipButton.setVisibility(lastPage ? View.INVISIBLE : View.VISIBLE);
        binding.nextButton.setText(lastPage ? R.string.get_started : R.string.next);
        updateIndicators(position);
    }

    private void updateIndicators(int selectedPosition) {
        binding.indicatorContainer.removeAllViews();
        for (int index = 0; index < pages.size(); index++) {
            TextView indicator = new TextView(this);
            indicator.setText("●");
            indicator.setTextSize(18);
            indicator.setTextColor(ContextCompat.getColor(this,
                    index == selectedPosition
                            ? R.color.trashgo_primary
                            : R.color.trashgo_outline));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMarginStart(5);
            params.setMarginEnd(5);
            indicator.setLayoutParams(params);
            binding.indicatorContainer.addView(indicator);
        }
    }

    private void completeOnboarding() {
        getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(COMPLETED, true)
                .apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
