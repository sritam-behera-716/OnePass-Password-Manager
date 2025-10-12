package com.securevault.onepass.ui.main.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.securevault.onepass.R;
import com.securevault.onepass.data.DatabaseHelper;
import com.securevault.onepass.data.PasswordItem;
import com.securevault.onepass.databinding.ActivityUpdateBinding;
import com.securevault.onepass.ui.main.password.GeneratePasswordActivity;
import com.securevault.onepass.utils.EditTextHelper;
import com.securevault.onepass.utils.SecureEncryptionHelper;

import java.time.LocalDate;
import java.util.Objects;

public class UpdateActivity extends AppCompatActivity {
    private ActivityUpdateBinding binding;
    private int id;
    private String title;
    private String date;
    private String link;
    private String username;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityUpdateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        setUpUserInterface();

        EditTextHelper editTextHelper = new EditTextHelper();
        editTextHelper.setDrawableEndIcon(this, binding.passwordInputLayout.passwordEditText);

        binding.passwordInputLayout.screenTitle.setText(R.string.update);
        binding.passwordInputLayout.addPasswordButton.setText(R.string.save_changes);

        binding.passwordInputLayout.generateNewButton.setOnClickListener(v -> startActivity(new Intent(this, GeneratePasswordActivity.class)));
        binding.passwordInputLayout.addPasswordButton.setOnClickListener(v -> updatePassword());
    }

    private void setUpUserInterface() {
        Intent intent = getIntent();
        id = intent.getIntExtra("id", -1);
        title = intent.getStringExtra("password_name");
        date = intent.getStringExtra("created_date");
        link = intent.getStringExtra("url");
        username = intent.getStringExtra("username");
        password = intent.getStringExtra("encrypted_password");

        binding.passwordInputLayout.nameEditText.setText(title);
        binding.passwordInputLayout.urlEditText.setText(link);
        binding.passwordInputLayout.usernameEditText.setText(username);
        binding.passwordInputLayout.passwordEditText.setText(password);
    }

    private void updatePassword() {
        String updatedTitle = Objects.requireNonNull(binding.passwordInputLayout.nameEditText.getText()).toString();
        String updatedLink = Objects.requireNonNull(binding.passwordInputLayout.urlEditText.getText()).toString();
        String updatedUsername = Objects.requireNonNull(binding.passwordInputLayout.usernameEditText.getText()).toString();
        String updatedPassword = Objects.requireNonNull(binding.passwordInputLayout.passwordEditText.getText()).toString();

        if (updatedTitle.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter the details", Toast.LENGTH_SHORT).show();
            return;
        }

        updatedTitle = updatedTitle.substring(0, 1).toUpperCase() + updatedTitle.substring(1);

        if (!(updatedTitle.equalsIgnoreCase(title) && updatedLink.equalsIgnoreCase(link) && updatedUsername.equalsIgnoreCase(username) && updatedPassword.equalsIgnoreCase(password))) {
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);

            SecureEncryptionHelper secureEncryptionHelper = new SecureEncryptionHelper(this);
            String encryptedPassword = secureEncryptionHelper.encrypt(updatedPassword);

            databaseHelper.passwordDao().updateRecord(new PasswordItem(id, updatedTitle, updatedLink, updatedUsername, encryptedPassword, updatedPassword.length(), LocalDate.parse(date)));
            setResult(Activity.RESULT_OK);
        }

        Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}