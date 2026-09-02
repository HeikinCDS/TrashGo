package com.pinduoduo.trashgo.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pinduoduo.trashgo.R;
import com.pinduoduo.trashgo.databinding.FragmentProfileBinding;
import com.pinduoduo.trashgo.ui.auth.LoginActivity;
import com.pinduoduo.trashgo.ui.settings.SettingsSheet;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        binding.signOutButton.setOnClickListener(view -> signOut());
        binding.profileSettings.setOnClickListener(v ->
                SettingsSheet.show(getParentFragmentManager()));
        binding.vouchersButton.setOnClickListener(v ->
                com.pinduoduo.trashgo.ui.vouchers.VouchersSheet.show(getParentFragmentManager()));
        loadProfile();
        return binding.getRoot();
    }

    private void loadProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            signOut();
            return;
        }

        binding.profileName.setText(user.getDisplayName() == null
                ? getString(R.string.trashgo_user) : user.getDisplayName());
        binding.profileEmail.setText(user.getEmail());
        binding.profileProgress.setVisibility(View.VISIBLE);

        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (binding == null) {
                        return;
                    }
                    binding.profileProgress.setVisibility(View.GONE);
                    if (!task.isSuccessful() || task.getResult() == null) {
                        binding.profileStatus.setText(R.string.profile_load_error);
                        binding.profileStatus.setVisibility(View.VISIBLE);
                        return;
                    }

                    DocumentSnapshot document = task.getResult();
                    String displayName = document.getString("displayName");
                    if (displayName != null && !displayName.isEmpty()) {
                        binding.profileName.setText(displayName);
                    }
                    binding.profilePoints.setText(number(document.getLong("totalPoints")));
                    binding.profileItems.setText(number(document.getLong("itemsRecycled")));
                    binding.profileStreak.setText(number(document.getLong("currentStreak")));
                });
    }

    private String number(Long value) {
        return String.valueOf(value == null ? 0 : value);
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        if (getActivity() == null) {
            return;
        }
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
