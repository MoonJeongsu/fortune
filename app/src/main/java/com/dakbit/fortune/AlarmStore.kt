package com.dakbit.fortune

import android.content.Context

data class AlarmSettings(
    val enabled: Boolean,
    val hour: Int,
    val minute: Int,
) {
    companion object {
        const val DEFAULT_HOUR = 7
        const val DEFAULT_MINUTE = 30
    }
}

class AlarmStore(context: Context) {
    private val preferences = context.getSharedPreferences("dakbit_alarm", Context.MODE_PRIVATE)

    fun load(): AlarmSettings = AlarmSettings(
        enabled = preferences.getBoolean("enabled", false),
        hour = preferences.getInt("hour", AlarmSettings.DEFAULT_HOUR),
        minute = preferences.getInt("minute", AlarmSettings.DEFAULT_MINUTE),
    )

    fun save(settings: AlarmSettings) {
        preferences.edit()
            .putBoolean("enabled", settings.enabled)
            .putInt("hour", settings.hour)
            .putInt("minute", settings.minute)
            .apply()
    }
}
