package com.securevault.onepass.utils;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.securevault.onepass.R;

import java.util.concurrent.Executor;

public class BiometricHelper {
    private final Context context;
    private final BiometricManager biometricManager;
    private BiometricPrompt biometricPrompt;
    private BiometricCallback biometricCallback;

    public BiometricHelper(Context context) {
        this.context = context;
        this.biometricManager = BiometricManager.from(context);
        initializeBiometricPrompt();
    }

    public void setBiometricCallback(BiometricCallback callback) {
        this.biometricCallback = callback;
    }

    private void initializeBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(context);
        biometricPrompt = new BiometricPrompt((FragmentActivity) context, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                showBiometricUnlockDialog();
                if (biometricCallback != null) {
                    biometricCallback.onFailed();
                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                if (biometricCallback != null) {
                    biometricCallback.onSuccess();
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                if (biometricCallback != null) {
                    biometricCallback.onFailed();
                }
            }
        });
    }

    public void checkAndShowBiometricPrompt() {
        BiometricManager biometricManager = BiometricManager.from(context);
        int canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.DEVICE_CREDENTIAL);

        switch (canAuthenticate) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                showBiometricPrompt();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                showDeviceCredentialPrompt();
                break;
            default:
                showBiometricUnlockDialog();
                if (biometricCallback != null) {
                    biometricCallback.onFailed();
                }
                break;
        }
    }

    private void showBiometricPrompt() {
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock to use OnePass")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .setConfirmationRequired(false)
                .build();
        biometricPrompt.authenticate(promptInfo);
    }

    private void showDeviceCredentialPrompt() {
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock to use OnePass")
                .setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();
        biometricPrompt.authenticate(promptInfo);
    }

    public boolean isBiometricAvailable() {
        int canAuthenticate = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_WEAK |
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL);
        return canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS;
    }

    private void showBiometricUnlockDialog() {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.biometric_dialog);
        dialog.setCancelable(false);
        dialog.findViewById(R.id.unlock).setOnClickListener(v -> {
            dialog.dismiss();
            checkAndShowBiometricPrompt();
        });
        dialog.show();
    }

    public interface BiometricCallback {
        void onSuccess();

        void onFailed();
    }
}