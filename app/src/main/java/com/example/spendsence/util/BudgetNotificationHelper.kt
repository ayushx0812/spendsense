package com.example.spendsence.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import java.util.concurrent.TimeUnit

private const val CHANNEL_ID = "budget_alerts"
private const val CHANNEL_NAME = "Budget Alerts"
private const val WORK_TAG = "budget_check_work"

/**
 * Schedules a daily WorkManager task to check budget usage and fire
 * local notifications when any category exceeds 80% of its limit.
 */
fun scheduleBudgetChecks(context: Context) {
    createNotificationChannel(context)

    val request = PeriodicWorkRequestBuilder<BudgetCheckWorker>(1, TimeUnit.DAYS)
        .addTag(WORK_TAG)
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        WORK_TAG,
        ExistingPeriodicWorkPolicy.KEEP,
        request
    )
}

fun sendBudgetAlertNotification(context: Context, category: String, usedPct: Int) {
    createNotificationChannel(context)
    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_alert)
        .setContentTitle("Budget Alert — $category")
        .setContentText("You've used $usedPct% of your $category budget this month.")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()

    try {
        NotificationManagerCompat.from(context).notify(category.hashCode(), notification)
    } catch (_: SecurityException) {
        // Permission not granted — silently skip
    }
}

private fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Alerts when spending exceeds 80% of a budget category." }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}

class BudgetCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        // Actual budget checking is done in the ViewModel via Firestore real-time listeners.
        // This worker is a hook for future server-side checks or scheduled reminders.
        return Result.success()
    }
}
