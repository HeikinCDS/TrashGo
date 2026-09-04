package com.pinduoduo.trashgo.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pinduoduo.trashgo.MainActivity;
import com.pinduoduo.trashgo.R;
import com.pinduoduo.trashgo.databinding.ActivityRegisterBinding;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        binding.registerButton.setOnClickListener(view -> register());
        binding.loginLink.setOnClickListener(view -> finish());
    }

    private void register() {
        String displayName = textOf(binding.nameInput);
        String email = textOf(binding.emailInput);
        String password = textOf(binding.passwordInput);
        String confirmation = textOf(binding.confirmPasswordInput);

        clearErrors();
        if (TextUtils.isEmpty(displayName)) {
            binding.nameLayout.setError(getString(R.string.error_name_required));
            return;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.setError(getString(R.string.error_valid_email));
            return;
        }
        if (password.length() < 6) {
            binding.passwordLayout.setError(getString(R.string.error_password_length));
            return;
        }
        if (!password.equals(confirmation)) {
            binding.confirmPasswordLayout.setError(getString(R.string.error_password_mismatch));
            return;
        }

        setLoading(true);
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful() || auth.getCurrentUser() == null) {
                        setLoading(false);
                        showError(task.getException());
                        return;
                    }

                    UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                            .setDisplayName(displayName)
                            .build();
                    auth.getCurrentUser().updateProfile(profile);

                    String uid = auth.getCurrentUser().getUid();
                    Map<String, Object> userProfile = new HashMap<>();
                    userProfile.put("uid", uid);
                    userProfile.put("displayName", displayName);
                    userProfile.put("email", email);
                    userProfile.put("totalPoints", 0L);
                    userProfile.put("lifetimePoints", 0L);
                    userProfile.put("itemsRecycled", 0L);
                    userProfile.put("currentStreak", 0L);
                    userProfile.put("lastScanDate", null);

                    firestore.collection("users").document(uid).set(userProfile)
                            .addOnSuccessListener(unused -> openMainScreen())
                            .addOnFailureListener(error -> {
                                setLoading(false);
                                Toast.makeText(this,
                                        getString(R.string.error_profile_creation, error.getLocalizedMessage()),
                                        Toast.LENGTH_LONG).show();
                            });
                });
    }

    private void clearErrors() {
        binding.nameLayout.setError(null);
        binding.emailLayout.setError(null);
        binding.passwordLayout.setError(null);
        binding.confirmPasswordLayout.setError(null);
    }

    private String textOf(com.google.android.material.textfield.TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    private void showError(Exception error) {
        String message = error == null ? getString(R.string.error_registration_failed)
                : error.getLocalizedMessage();
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void setLoading(boolean loading) {
        binding.registerProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.registerButton.setEnabled(!loading);
    }

    private void openMainScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
