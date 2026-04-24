package com.example.spendsence.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricHelper {
    private const val AUTHENTICATORS = BIOMETRIC_STRONG or DEVICE_CREDENTIAL

    enum class Status {
        Available,
        NoneEnrolled,
        NoHardware,
        HardwareUnavailable,
        Unsupported
    }

    /** Returns true if the device supports biometric or device-credential auth. */
    fun isAvailable(context: Context): Boolean {
        return getStatus(context) == Status.Available
    }

    fun getStatus(context: Context): Status {
        return when (BiometricManager.from(context).canAuthenticate(AUTHENTICATORS)) {
            BiometricManager.BIOMETRIC_SUCCESS -> Status.Available
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> Status.NoneEnrolled
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> Status.NoHardware
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> Status.HardwareUnavailable
            else -> Status.Unsupported
        }
    }

    fun openEnrollmentSettings(context: Context) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_BIOMETRIC_ENROLL).putExtra(
                Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                AUTHENTICATORS
            )
        } else {
            Intent(Settings.ACTION_SECURITY_SETTINGS)
        }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            context.startActivity(
                Intent(Settings.ACTION_SECURITY_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    fun findFragmentActivity(context: Context): FragmentActivity? {
        var current = context
        while (current is ContextWrapper) {
            if (current is FragmentActivity) return current
            current = current.baseContext
        }
        return null
    }

    /**
     * Shows the system biometric / PIN prompt.
     * @param onSuccess called when authentication succeeds.
     * @param onError   called with a human-readable message when auth fails permanently.
     */
    fun showPrompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                // errorCode 10 = user cancelled / pressed back – treat as soft error
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                    errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON
                ) {
                    onError(errString.toString())
                }
            }

            override fun onAuthenticationFailed() {
                // Single failed attempt – the prompt stays open, no action needed here
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("SpendSense")
            .setSubtitle("Verify your identity to continue")
            .setAllowedAuthenticators(AUTHENTICATORS)
            .build()

        prompt.authenticate(promptInfo)
    }
}
