package com.dakbit.fortune

import java.time.LocalDate

enum class Gender(val code: String, val label: String) {
    MALE("M", "남성"),
    FEMALE("F", "여성");

    companion object {
        fun fromCode(code: String) = entries.firstOrNull { it.code == code } ?: MALE
    }
}

data class UserProfile(
    val nickname: String,
    val birthDate: LocalDate,
    val gender: Gender,
)

data class ScoredFortune(
    val title: String,
    val emoji: String,
    val score: Int?,
    val content: String,
)

data class LuckyItems(
    val direction: String,
    val color: String,
    val number: String,
    val zodiacAnimal: String,
    val fashionItem: String,
    val starSign: String,
)

data class FortuneResult(
    val date: LocalDate,
    val score: Int,
    val ganji: String,
    val summary: String,
    val categories: List<ScoredFortune>,
    val styleAdvice: String,
    val luckyItems: LuckyItems,
)

enum class AppTab(val label: String, val emoji: String) {
    TODAY("오늘", "☾"),
    LUCKY("행운", "✦"),
    SETTINGS("설정", "⚙"),
}
