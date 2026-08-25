package com.pinduoduo.trashgo.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.pinduoduo.trashgo.MainActivity;
import com.pinduoduo.trashgo.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();

        binding.loginButton.setOnClickListener(view -> login());
        binding.createAccountLink.setOnClickListener(view ->
                startActivity(new Intent(this, RegisterActivity.class)));
        binding.forgotPasswordLink.setOnClickListener(view ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null) {
            openMainScreen();
        }
    }

    private void login() {
        String email = textOf(binding.emailInput);
        String password = textOf(binding.passwordInput);

        binding.emailLayout.setError(null);
        binding.passwordLayout.setError(null);

        if (TextUtils.isEmpty(email)) {
            binding.emailLayout.setError(getString(com.pinduoduo.trashgo.R.string.error_email_required));
            return;
        }
        if (TextUtils.isEmpty(password)) {
            binding.passwordLayout.setError(getString(com.pinduoduo.trashgo.R.string.error_password_required));
            return;
        }

        setLoading(true);
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        openMainScreen();
                    } else {
                        String message = task.getException() == null
                                ? getString(com.pinduoduo.trashgo.R.string.error_login_failed)
                                : task.getException().getLocalizedMessage();
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String textOf(com.google.android.material.textfield.TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    private void setLoading(boolean loading) {
        binding.loginProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.loginButton.setEnabled(!loading);
    }

    private void openMainScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
