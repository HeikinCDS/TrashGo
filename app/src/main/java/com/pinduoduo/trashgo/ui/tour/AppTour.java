package com.pinduoduo.trashgo.ui.tour;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.widget.NestedScrollView;

import com.pinduoduo.trashgo.R;

import java.util.ArrayList;
import java.util.List;

public final class AppTour {
    private static final float HOLE_PADDING_DP = 8f;

    public interface OnFinished {
        void onFinished();
    }

    private static final class Step {
        final View target;
        @StringRes final int title;
        @StringRes final int body;

        Step(View target, int title, int body) {
            this.target = target;
            this.title = title;
            this.body = body;
        }
    }

    private final Activity activity;
    private final List<Step> steps = new ArrayList<>();
    @Nullable private OnFinished onFinished;

    private FrameLayout overlayRoot;
    private TourOverlayView scrim;
    private View card;
    private int index = 0;
    private boolean running = false;

    private AppTour(@NonNull Activity activity) {
        this.activity = activity;
    }

    @NonNull
    public static AppTour with(@NonNull Activity activity) {
        return new AppTour(activity);
    }

    @NonNull
    public AppTour step(@Nullable View target, @StringRes int title, @StringRes int body) {
        if (target != null) {
            steps.add(new Step(target, title, body));
        }
        return this;
    }

    @NonNull
    public AppTour onFinished(@Nullable OnFinished listener) {
        this.onFinished = listener;
        return this;
    }

    public void start() {
        if (running || steps.isEmpty() || activity.isFinishing()) {
            return;
        }
        running = true;

        ViewGroup content = activity.findViewById(android.R.id.content);
        if (content == null) {
            running = false;
            return;
        }

        overlayRoot = new FrameLayout(activity);
        overlayRoot.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        scrim = new TourOverlayView(activity);
        scrim.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        scrim.setOnClickListener(v -> { });
        overlayRoot.addView(scrim);

        card = LayoutInflater.from(activity)
                .inflate(R.layout.view_tour_card, overlayRoot, false);
        FrameLayout.LayoutParams cardParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = dp(20);
        cardParams.leftMargin = margin;
        cardParams.rightMargin = margin;
        card.setLayoutParams(cardParams);
        card.setAlpha(0f);
        overlayRoot.addView(card);

        card.findViewById(R.id.tour_next).setOnClickListener(v -> next());
        card.findViewById(R.id.tour_skip).setOnClickListener(v -> finish());

        overlayRoot.setFocusableInTouchMode(true);
        overlayRoot.requestFocus();
        overlayRoot.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                finish();
                return true;
            }
            return false;
        });

        content.addView(overlayRoot);
        showStep(0);
    }

    private void next() {
        if (index + 1 >= steps.size()) {
            finish();
        } else {
            showStep(index + 1);
        }
    }

    private void showStep(int position) {
        index = position;
        Step step = steps.get(position);

        ((TextView) card.findViewById(R.id.tour_counter)).setText(
                activity.getString(R.string.tour_step_counter, position + 1, steps.size()));
        ((TextView) card.findViewById(R.id.tour_title)).setText(step.title);
        ((TextView) card.findViewById(R.id.tour_body)).setText(step.body);
        ((TextView) card.findViewById(R.id.tour_next)).setText(
                position + 1 >= steps.size() ? R.string.tour_done : R.string.tour_next);

        scrollIntoView(step.target);

        step.target.postDelayed(() -> place(step.target), 220L);
    }

    private void place(@NonNull View target) {
        if (overlayRoot == null || target.getWidth() == 0 || target.getHeight() == 0) {
            return;
        }

        int[] targetXy = new int[2];
        int[] overlayXy = new int[2];
        target.getLocationOnScreen(targetXy);
        overlayRoot.getLocationOnScreen(overlayXy);

        float pad = dp(HOLE_PADDING_DP);
        float left = targetXy[0] - overlayXy[0] - pad;
        float top = targetXy[1] - overlayXy[1] - pad;
        float right = left + target.getWidth() + pad * 2;
        float bottom = top + target.getHeight() + pad * 2;

        scrim.setHole(left, top, right, bottom, dp(14));

        card.measure(
                View.MeasureSpec.makeMeasureSpec(
                        overlayRoot.getWidth() - dp(40), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        int cardHeight = card.getMeasuredHeight();

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) card.getLayoutParams();
        int gap = dp(16);

        if (bottom + gap + cardHeight < overlayRoot.getHeight() - dp(16)) {
            params.topMargin = (int) bottom + gap;
        } else {
            params.topMargin = Math.max(dp(16), (int) top - gap - cardHeight);
        }
        card.setLayoutParams(params);

        card.setAlpha(0f);
        card.setTranslationY(dp(10));
        card.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(260)
                .setInterpolator(new DecelerateInterpolator(1.6f))
                .start();
    }

    private void scrollIntoView(@NonNull View target) {
        View scroller = findScrollParent(target);
        if (scroller == null) {
            return;
        }

        int[] targetXy = new int[2];
        int[] scrollXy = new int[2];
        target.getLocationOnScreen(targetXy);
        scroller.getLocationOnScreen(scrollXy);

        int desired = scroller.getScrollY() + (targetXy[1] - scrollXy[1]) - scroller.getHeight() / 3;
        int clamped = Math.max(0, desired);

        if (scroller instanceof ScrollView) {
            ((ScrollView) scroller).smoothScrollTo(0, clamped);
        } else if (scroller instanceof NestedScrollView) {
            ((NestedScrollView) scroller).smoothScrollTo(0, clamped);
        }
    }

    @Nullable
    private static View findScrollParent(@NonNull View view) {
        ViewParent parent = view.getParent();
        while (parent instanceof View) {
            if (parent instanceof ScrollView || parent instanceof NestedScrollView) {
                return (View) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    private void finish() {
        running = false;
        if (overlayRoot != null && overlayRoot.getParent() instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) overlayRoot.getParent();
            overlayRoot.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> parent.removeView(overlayRoot))
                    .start();
        }
        if (onFinished != null) {
            onFinished.onFinished();
        }
    }

    private int dp(float value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }
}
