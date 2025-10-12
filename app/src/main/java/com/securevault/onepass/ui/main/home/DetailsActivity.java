package com.securevault.onepass.ui.main.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.securevault.onepass.R;
import com.securevault.onepass.data.DatabaseHelper;
import com.securevault.onepass.data.PasswordItem;
import com.securevault.onepass.databinding.ActivityDetailsBinding;
import com.securevault.onepass.utils.BiometricHelper;
import com.securevault.onepass.utils.ClipboardHelper;
import com.securevault.onepass.utils.SecureEncryptionHelper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DetailsActivity extends AppCompatActivity {
    private ActivityDetailsBinding binding;
    private DatabaseHelper databaseHelper;
    private BiometricHelper biometricHelper;
    private SecureEncryptionHelper secureEncryptionHelper;
    private boolean isPasswordHide = true;
    private int id;
    private String title;
    private String date;
    private String link;
    private String username;
    private int length;

    private final ActivityResultLauncher<Intent> detailsLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    setResult(Activity.RESULT_OK);
                    refreshDetails();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        setUpUserInterface();
        databaseHelper = DatabaseHelper.getInstance(this);
        biometricHelper = new BiometricHelper(this);
        secureEncryptionHelper = new SecureEncryptionHelper(this);

        binding.passwordToggle.setOnClickListener(v -> showOrHidePassword());
        binding.copyIcon.setOnClickListener(v -> copyPassword());
        binding.deleteButton.setOnClickListener(v -> deletePassword());
        binding.updateButton.setOnClickListener(v -> updatePassword());
    }

    private void showOrHidePassword() {
        if (isPasswordHide) {
            biometricHelper.setBiometricCallback(new BiometricHelper.BiometricCallback() {
                @Override
                public void onSuccess() {
                    String encryptedPassword = databaseHelper.passwordDao().retrievePasswordById(id);
                    String decryptedPassword = secureEncryptionHelper.decrypt(encryptedPassword);

                    binding.passwordToggle.setImageResource(R.drawable.ic_eye_open);
                    binding.passwordText.setText(decryptedPassword);
                    binding.passwordText.setTransformationMethod(null);
                    isPasswordHide = false;
                }

                @Override
                public void onFailed() {

                }
            });
            biometricHelper.checkAndShowBiometricPrompt();
        } else {
            binding.passwordToggle.setImageResource(R.drawable.ic_eye_close);
            binding.passwordText.setText(emptyPassword(length));
            binding.passwordText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            isPasswordHide = true;
        }
    }

    private void copyPassword() {
        biometricHelper.setBiometricCallback(new BiometricHelper.BiometricCallback() {
            @Override
            public void onSuccess() {
                String encryptedPassword = databaseHelper.passwordDao().retrievePasswordById(id);
                String decryptedPassword = secureEncryptionHelper.decrypt(encryptedPassword);
                ClipboardHelper.copyToClipboard(getApplicationContext(), decryptedPassword);
                Toast.makeText(getApplicationContext(), "Copied", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed() {

            }
        });
        biometricHelper.checkAndShowBiometricPrompt();
    }

    private void updatePassword() {
        Intent intent = new Intent(this, UpdateActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("password_name", title);
        intent.putExtra("created_date", getOriginalDate(date));
        intent.putExtra("url", link);
        intent.putExtra("username", username);

        String encryptedPassword = databaseHelper.passwordDao().retrievePasswordById(id);
        String decryptedPassword = secureEncryptionHelper.decrypt(encryptedPassword);

        intent.putExtra("encrypted_password", decryptedPassword);
        detailsLauncher.launch(intent);
    }

    private void deletePassword() {
        Intent intent = getIntent();
        int id = intent.getIntExtra("id", -1);
        if (id == -1) {
            Toast.makeText(this, "Password can't be delete!", Toast.LENGTH_SHORT).show();
            return;
        }

        biometricHelper.setBiometricCallback(new BiometricHelper.BiometricCallback() {
            @Override
            public void onSuccess() {
                databaseHelper.passwordDao().deleteRecord(id);
                Toast.makeText(getApplicationContext(), "Password successfully deleted!", Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK);
                finish();
            }

            @Override
            public void onFailed() {

            }
        });
        biometricHelper.checkAndShowBiometricPrompt();
    }

    private void refreshDetails() {
        PasswordItem passwordItem = databaseHelper.passwordDao().retrieveRecordById(id);

        title = passwordItem.getPasswordName();
        link = passwordItem.getUrl();
        username = passwordItem.getUsername();
        length = passwordItem.getPasswordLength();
        date = getFormattedDate(passwordItem.getCreatedDate().toString());

        binding.screenTitle.setText(title);
        binding.linkText.setText(link.isEmpty() ? "null" : link);
        binding.userText.setText(username);
        binding.passwordText.setText(emptyPassword(length));
        binding.dateText.setText(date);

        binding.passwordText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        isPasswordHide = true;
    }

    private void setUpUserInterface() {
        Intent intent = getIntent();
        id = intent.getIntExtra("id", -1);
        title = intent.getStringExtra("password_name");
        date = getFormattedDate(intent.getStringExtra("created_date"));
        link = intent.getStringExtra("url");
        username = intent.getStringExtra("username");
        length = intent.getIntExtra("password_length", -1);

        binding.screenTitle.setText(title);
        binding.dateText.setText(date);
        binding.linkText.setText(link.isEmpty() ? "null" : link);
        binding.userText.setText(username);
        binding.passwordText.setText(emptyPassword(length));
        binding.passwordText.setTransformationMethod(PasswordTransformationMethod.getInstance());
    }

    private String emptyPassword(int length) {
        StringBuilder password = new StringBuilder();
        for (int i = 1; i <= length; i++) {
            password.append("*");
        }
        return password.toString();
    }

    private String getFormattedDate(String date) {
        LocalDate localDate = LocalDate.parse(date);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);
        return localDate.format(formatter);
    }

    private String getOriginalDate(String formattedDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);
        LocalDate localDate = LocalDate.parse(formattedDate, formatter);
        return localDate.toString();
    }
}