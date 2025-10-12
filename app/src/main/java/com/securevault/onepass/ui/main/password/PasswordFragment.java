package com.securevault.onepass.ui.main.password;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.securevault.onepass.data.DatabaseHelper;
import com.securevault.onepass.data.PasswordItem;
import com.securevault.onepass.databinding.FragmentPasswordBinding;
import com.securevault.onepass.utils.EditTextHelper;
import com.securevault.onepass.utils.SecureEncryptionHelper;

import java.time.LocalDate;
import java.util.Objects;

public class PasswordFragment extends Fragment {
    private FragmentPasswordBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        EditTextHelper editTextHelper = new EditTextHelper();
        editTextHelper.setStrokeColorByTyping(binding.passwordInputLayout.nameEditText);
        editTextHelper.setStrokeColorByTyping(binding.passwordInputLayout.urlEditText);
        editTextHelper.setStrokeColorByTyping(binding.passwordInputLayout.usernameEditText);
        editTextHelper.setStrokeColorByTyping(binding.passwordInputLayout.passwordEditText);

        editTextHelper.setDrawableEndIcon(requireContext(), binding.passwordInputLayout.passwordEditText);
        binding.passwordInputLayout.generateNewButton.setOnClickListener(v -> startActivity(new Intent(requireContext(), GeneratePasswordActivity.class)));
        binding.passwordInputLayout.addPasswordButton.setOnClickListener(v -> addPassword());
    }

    private void addPassword() {
        String name = Objects.requireNonNull(binding.passwordInputLayout.nameEditText.getText()).toString();
        String url = Objects.requireNonNull(binding.passwordInputLayout.urlEditText.getText()).toString();
        String username = Objects.requireNonNull(binding.passwordInputLayout.usernameEditText.getText()).toString();
        String password = Objects.requireNonNull(binding.passwordInputLayout.passwordEditText.getText()).toString();

        if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter the details", Toast.LENGTH_SHORT).show();
            return;
        }

        LocalDate date = LocalDate.now();
        name = name.substring(0, 1).toUpperCase() + name.substring(1);

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(requireContext());
        SecureEncryptionHelper secureEncryptionHelper = new SecureEncryptionHelper(requireContext());
        String encryptedPassword = secureEncryptionHelper.encrypt(password);

        databaseHelper.passwordDao().insertRecord(new PasswordItem(name, url, username, encryptedPassword, password.length(), date));
        Toast.makeText(requireContext(), "Password added successfully!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.passwordInputLayout.nameEditText.setText(null);
        binding.passwordInputLayout.urlEditText.setText(null);
        binding.passwordInputLayout.usernameEditText.setText(null);
        binding.passwordInputLayout.passwordEditText.setText(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}