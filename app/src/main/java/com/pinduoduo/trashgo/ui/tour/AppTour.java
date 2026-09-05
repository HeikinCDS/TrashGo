package com.pinduoduo.trashgo.ui.tour;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import androidx.annotation.StringRes;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.pinduoduo.trashgo.R;
import java.util.ArrayList;
import java.util.List;

/** Lightweight, lifecycle-safe walkthrough used by the Home screen. */
public final class AppTour {
    private final Activity activity;
    private final List<Step> steps = new ArrayList<>();
    private Runnable finished;
    private View highlightedView;
    private GradientDrawable highlight;

    private AppTour(Activity activity) { this.activity = activity; }
    public static AppTour with(Activity activity) { return new AppTour(activity); }
    public AppTour step(View anchor, @StringRes int title, @StringRes int body) {
        if (anchor != null) steps.add(new Step(anchor, title, body));
        return this;
    }
    public AppTour onFinished(Runnable callback) { finished = callback; return this; }
    public void start() { show(0); }
    private void show(int position) {
        if (activity.isFinishing() || activity.isDestroyed()) return;
        if (position >= steps.size()) { complete(); return; }
        Step step = steps.get(position);
        showHighlight(step.anchor);
        new MaterialAlertDialogBuilder(activity, R.style.ThemeOverlay_TrashGo_Walkthrough)
                .setTitle(step.title)
                .setMessage(activity.getString(step.body) + "\n\n" +
                        activity.getString(R.string.tour_step_counter, position + 1, steps.size()))
                .setNegativeButton(R.string.tour_skip, (d, w) -> complete())
                .setPositiveButton(position == steps.size() - 1 ? R.string.tour_done : R.string.tour_next,
                        (d, w) -> show(position + 1))
                .setOnCancelListener(d -> complete())
                .setOnDismissListener(d -> clearHighlight(step.anchor))
                .show();
    }
    private void complete() {
        Runnable callback = finished;
        finished = null;
        if (callback != null) callback.run();
    }
    private void showHighlight(View view) {
        clearHighlight();
        highlightedView = view;
        highlight = new GradientDrawable();
        highlight.setColor(Color.TRANSPARENT);
        highlight.setCornerRadius(16 * activity.getResources().getDisplayMetrics().density);
        highlight.setStroke((int) (2 * activity.getResources().getDisplayMetrics().density),
                activity.getColor(R.color.trashgo_primary));
        highlight.setBounds(0, 0, view.getWidth(), view.getHeight());
        view.getOverlay().add(highlight);
    }
    private void clearHighlight() {
        if (highlightedView != null && highlight != null) highlightedView.getOverlay().remove(highlight);
        highlightedView = null;
        highlight = null;
    }
    private void clearHighlight(View expected) {
        if (highlightedView == expected) clearHighlight();
    }
    private static final class Step {
        final View anchor;
        final int title;
        final int body;
        Step(View anchor, int title, int body) {
            this.anchor = anchor;
            this.title = title;
            this.body = body;
        }
    }
}
