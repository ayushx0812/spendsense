package com.example.spendsence.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.spendsence.util.FirestoreHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class NotificationCaptureService : NotificationListenerService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        serviceScope.launch {
            val packageName = sbn.packageName.lowercase()
            if (!isPaymentApp(packageName)) return@launch

            val extras = sbn.notification.extras
            val text = extras.getCharSequence("android.text")?.toString().orEmpty()
            val title = extras.getCharSequence("android.title")?.toString().orEmpty()
            val message = "$title $text".trim()
            if (message.isBlank()) return@launch

            // Skip non-payment/status notifications to reduce noisy writes.
            if (!looksLikePayment(message)) return@launch

            handleTransaction(message, packageName)
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun handleTransaction(message: String, packageName: String) {
        val amountRegex = Regex("(?:₹|rs\\.?|inr)\\s?(\\d+(?:,\\d{3})*(?:\\.\\d{1,2})?)", RegexOption.IGNORE_CASE)
        val amount = amountRegex.find(message)
            ?.groups?.get(1)?.value
            ?.replace(",", "")
            ?.toDoubleOrNull()
            ?: return

        val merchantRegex = Regex("(?:to|at)\\s+([A-Za-z0-9& ._-]{2,40})", RegexOption.IGNORE_CASE)
        val detectedMerchant = merchantRegex.find(message)?.groups?.get(1)?.value?.trim().orEmpty()
            .ifBlank { "AutoDetected" }

        FirestoreHelper.storeParsedTransaction(
            detectedAmount = amount,
            detectedMerchant = detectedMerchant,
            source = detectSource(packageName, message),
            category = "Uncategorized",
            type = "debit"
        )
    }

    private fun isPaymentApp(packageName: String): Boolean {
        return packageName.contains("gpay") ||
            packageName.contains("paytm") ||
            packageName.contains("tez") ||
            packageName.contains("phonepe")
    }

    private fun looksLikePayment(message: String): Boolean {
        val lower = message.lowercase()
        return lower.contains("paid") ||
            lower.contains("debited") ||
            lower.contains("sent") ||
            lower.contains("spent") ||
            lower.contains("payment")
    }

    private fun detectSource(packageName: String, message: String): String {
        val lower = "$packageName $message".lowercase()
        return when {
            lower.contains("paytm") -> "paytm"
            lower.contains("gpay") || lower.contains("tez") -> "gpay"
            lower.contains("phonepe") -> "phonepe"
            else -> "autodetected"
        }
    }
}
