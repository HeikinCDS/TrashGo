package com.pinduoduo.trashgo.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.pinduoduo.trashgo.R;
import com.pinduoduo.trashgo.databinding.ActivityForgotPasswordBinding;

public class ForgotPasswordActivity extends AppCompatActivity {
    private ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.resetPasswordButton.setOnClickListener(view -> sendResetEmail());
        binding.backToLoginLink.setOnClickListener(view -> finish());
    }

    private void sendResetEmail() {
        String email = binding.emailInput.getText() == null
                ? "" : binding.emailInput.getText().toString().trim();
        binding.emailLayout.setError(null);

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.setError(getString(R.string.error_valid_email));
            return;
        }

        setLoading(true);
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, R.string.reset_email_sent, Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        String message = task.getException() == null
                                ? getString(R.string.error_reset_failed)
                                : task.getException().getLocalizedMessage();
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setLoading(boolean loading) {
        binding.resetProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.resetPasswordButton.setEnabled(!loading);
    }
}
