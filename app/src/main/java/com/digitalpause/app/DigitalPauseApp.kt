package com.digitalpause.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.digitalpause.app.data.PasswordManager
import com.digitalpause.app.data.PreferencesManager
import com.digitalpause.app.data.UsageStatsRepository

class DigitalPauseApp : Application() {

    lateinit var preferencesManager: PreferencesManager
        private set

    lateinit var passwordManager: PasswordManager
        private set

    lateinit var usageStatsRepository: UsageStatsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        preferencesManager = PreferencesManager(this)
        passwordManager = PasswordManager(this)
        usageStatsRepository = UsageStatsRepository(this)

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                getString(R.string.notification_channel_reminders),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_reminders_desc)
                enableVibration(true)
            }

            val protectionChannel = NotificationChannel(
                CHANNEL_PROTECTION,
                getString(R.string.notification_channel_protection),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.notification_channel_protection_desc)
            }

            notificationManager?.createNotificationChannel(reminderChannel)
            notificationManager?.createNotificationChannel(protectionChannel)
        }
    }

    companion object {
        const val CHANNEL_REMINDERS = "channel_digitalpause_reminders"
        const val CHANNEL_PROTECTION = "channel_digitalpause_protection"

        lateinit var instance: DigitalPauseApp
            private set
    }
}
