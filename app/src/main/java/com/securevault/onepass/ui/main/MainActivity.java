package com.securevault.onepass.ui.main;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.securevault.onepass.R;
import com.securevault.onepass.databinding.ActivityMainBinding;
import com.securevault.onepass.utils.BiometricHelper;
import com.securevault.onepass.utils.PreferenceHelper;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        setUpNavigation();
        unlockWithBiometric();
    }

    private void setUpNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNavigationView, navController);
        }
    }

    private void unlockWithBiometric() {
        PreferenceHelper preferenceHelper = PreferenceHelper.getInstance(this);
        boolean check = preferenceHelper.getBiometricMode(PreferenceHelper.KEY_BIOMETRIC);
        if (check) {
            BiometricHelper biometricHelper = new BiometricHelper(this);
            biometricHelper.setBiometricCallback(new BiometricHelper.BiometricCallback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailed() {

                }
            });
            biometricHelper.checkAndShowBiometricPrompt();
        }
    }
}