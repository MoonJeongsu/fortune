package com.dakbit.fortune

import android.content.Context
import java.time.LocalDate

class ProfileStore(context: Context) {
    private val preferences = context.getSharedPreferences("dakbit_profile", Context.MODE_PRIVATE)

    fun load(): UserProfile? {
        val birthDate = preferences.getString("birth_date", null) ?: return null
        return runCatching {
            UserProfile(
                nickname = preferences.getString("nickname", "나")?.ifBlank { "나" } ?: "나",
                birthDate = LocalDate.parse(birthDate),
                gender = Gender.fromCode(preferences.getString("gender", "M") ?: "M"),
            )
        }.getOrNull()
    }

    fun save(profile: UserProfile) {
        preferences.edit()
            .putString("nickname", profile.nickname)
            .putString("birth_date", profile.birthDate.toString())
            .putString("gender", profile.gender.code)
            .apply()
    }
}
