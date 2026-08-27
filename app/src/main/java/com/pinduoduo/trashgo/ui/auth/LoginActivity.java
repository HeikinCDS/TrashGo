package com.pinduoduo.trashgo.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.pinduoduo.trashgo.MainActivity;
import com.pinduoduo.trashgo.R;
import com.pinduoduo.trashgo.databinding.ActivityLoginBinding;
import com.pinduoduo.trashgo.ui.onboarding.OnboardingActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!OnboardingActivity.isCompleted(this)) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();

        clearErrorWhenTyping(binding.emailInput, binding.emailLayout);
        clearErrorWhenTyping(binding.passwordInput, binding.passwordLayout);

        binding.loginButton.setOnClickListener(view -> login());
        binding.createAccountLink.setOnClickListener(view ->
                startActivity(new Intent(this, RegisterActivity.class)));
        binding.forgotPasswordLink.setOnClickListener(view ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (auth != null && auth.getCurrentUser() != null) {
            openMainScreen();
        }
    }

    private void login() {
        String email = textOf(binding.emailInput);
        String password = textOf(binding.passwordInput);

        binding.emailLayout.setError(null);
        binding.passwordLayout.setError(null);

        if (TextUtils.isEmpty(email)) {
            binding.emailLayout.setError(getString(R.string.error_email_required));
            binding.emailInput.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.setError(getString(R.string.error_valid_email));
            binding.emailInput.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            binding.passwordLayout.setError(getString(R.string.error_password_required));
            binding.passwordInput.requestFocus();
            return;
        }

        setLoading(true);
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        openMainScreen();
                    } else {
                        showLoginError(task.getException());
                    }
                });
    }

    private void showLoginError(Exception error) {
        if (error instanceof FirebaseAuthInvalidUserException) {
            binding.emailLayout.setError(getString(R.string.error_no_account));
            binding.emailInput.requestFocus();
        } else if (error instanceof FirebaseAuthInvalidCredentialsException) {
            binding.passwordLayout.setError(getString(R.string.error_invalid_credentials));
            binding.passwordInput.requestFocus();
        } else if (error instanceof FirebaseTooManyRequestsException) {
            Toast.makeText(this, R.string.error_too_many_attempts, Toast.LENGTH_LONG).show();
        } else if (error instanceof FirebaseNetworkException) {
            Toast.makeText(this, R.string.error_network, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.error_login_failed, Toast.LENGTH_LONG).show();
        }
    }

    private void clearErrorWhenTyping(TextInputEditText input, TextInputLayout layout) {
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                layout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private String textOf(TextInputEditText input) {
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
