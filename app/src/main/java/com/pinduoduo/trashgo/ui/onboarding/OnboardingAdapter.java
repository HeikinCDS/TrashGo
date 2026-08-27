package com.pinduoduo.trashgo.ui.onboarding;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.pinduoduo.trashgo.R;
import com.pinduoduo.trashgo.databinding.ItemOnboardingPageBinding;

import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.PageViewHolder> {
    private final List<Page> pages;

    public OnboardingAdapter(List<Page> pages) {
        this.pages = pages;
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOnboardingPageBinding binding = ItemOnboardingPageBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        holder.bind(pages.get(position));
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }

    static class PageViewHolder extends RecyclerView.ViewHolder {
        private final ItemOnboardingPageBinding binding;

        PageViewHolder(ItemOnboardingPageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Page page) {
            binding.onboardingImage.setImageResource(page.imageResource);
            binding.onboardingImage.setContentDescription(
                    binding.getRoot().getContext().getString(page.titleResource));
            binding.onboardingTitle.setText(page.titleResource);
            binding.onboardingBody.setText(page.bodyResource);

            ColorStateList tint = page.tintIcon
                    ? ColorStateList.valueOf(ContextCompat.getColor(
                            binding.getRoot().getContext(), R.color.trashgo_primary))
                    : null;
            ImageViewCompat.setImageTintList(binding.onboardingImage, tint);
        }
    }

    public static class Page {
        final int imageResource;
        final int titleResource;
        final int bodyResource;
        final boolean tintIcon;

        public Page(int imageResource, int titleResource, int bodyResource, boolean tintIcon) {
            this.imageResource = imageResource;
            this.titleResource = titleResource;
            this.bodyResource = bodyResource;
            this.tintIcon = tintIcon;
        }
    }
}
