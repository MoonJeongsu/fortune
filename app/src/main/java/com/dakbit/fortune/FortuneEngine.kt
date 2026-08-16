package com.dakbit.fortune

import org.json.JSONObject
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class FortuneEngine(private val data: FortuneDataSource) {
    private val stems = listOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")
    private val branches = listOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")
    private val elements = listOf("木", "火", "土", "金", "水")
    private val relations = listOf("yosin", "hisin", "gisin", "gusin", "hansin")
    private val baseDate = LocalDate.of(1900, 2, 20)

    fun calculate(profile: UserProfile, date: LocalDate = LocalDate.now()): FortuneResult {
        val birthGanjiIndex = ganjiIndex(profile.birthDate)
        val todayGanjiIndex = ganjiIndex(date)
        val birthGanji = ganji(birthGanjiIndex)
        val todayGanji = ganji(todayGanjiIndex)
        val birthElement = stemElement(birthGanji.first())
        val todayElement = stemElement(todayGanji.first())
        val relation = relation(birthElement, todayElement)
        val isGood = relation in setOf("yosin", "hisin", "hansin")
        val index = resultIndex(birthGanjiIndex, todayGanjiIndex)

        val totalRow = indexedRow("skt_eve_unse1_00", index)
        val loveRow = indexedRow("skt_eve_unse1_01", index)
        val moneyRow = indexedRow("skt_eve_unse1_02", index)
        val studyRow = indexedRow("skt_eve_unse1_03", index)
        val travelRow = indexedRow("skt_eve_unse1_04", index)
        val contentKey = if (isGood) "contents1" else "contents2"
        val scoreKey = if (isGood) "jumsu1" else "jumsu2"

        val categories = listOf(
            scored("애정운", "♡", loveRow, contentKey, scoreKey),
            scored("금전운", "₩", moneyRow, contentKey, scoreKey),
            scored("학업·성장운", "✎", studyRow, contentKey, scoreKey),
            ScoredFortune("이동·여행운", "➜", null, data.text(travelRow, contentKey)),
        )
        val totalScore = categories.mapNotNull { it.score }.average().toInt()

        val styleRow = data.row("skt_eve_unse1_05") { row ->
            data.text(row, "sex") == profile.gender.code &&
                normalizeElement(data.text(row, "five")) == normalizeElement(birthElement) &&
                data.text(row, "sky") == todayGanji.first().toString()
        }

        val direction = data.text(travelRow, if (isGood) "g_direction" else "b_direction")
        val number = data.text(travelRow, if (isGood) "g_number" else "b_number")
        val zodiac = goodZodiac(birthGanji[1], todayGanji[1])
        val starSign = luckyStarSign(birthGanji.first(), todayGanji[1])

        return FortuneResult(
            date = date,
            score = totalScore,
            ganji = todayGanji,
            summary = data.text(totalRow, relation),
            categories = categories,
            styleAdvice = data.text(styleRow, "contents"),
            luckyItems = LuckyItems(
                direction = direction,
                color = data.text(styleRow, "colors"),
                number = number,
                zodiacAnimal = zodiac,
                fashionItem = data.text(styleRow, "codi"),
                starSign = starSign,
            ),
        )
    }

    private fun ganjiIndex(date: LocalDate): Int {
        val days = ChronoUnit.DAYS.between(baseDate, date) + 1
        return Math.floorMod(days, 60L).toInt()
    }

    private fun ganji(index: Int): String =
        stems[index % stems.size] + branches[index % branches.size]

    private fun stemElement(stem: Char): String {
        val index = stems.indexOf(stem.toString())
        return listOf("木", "木", "火", "火", "土", "土", "金", "金", "水", "水")[index]
    }

    private fun relation(birthElement: String, todayElement: String): String {
        val birth = elements.indexOf(normalizeElement(birthElement))
        val today = elements.indexOf(normalizeElement(todayElement))
        return relations[Math.floorMod(birth - today, elements.size)]
    }

    private fun normalizeElement(value: String) = value.replace("金", "金")

    private fun resultIndex(birthIndex: Int, todayIndex: Int): Int {
        val birth = birthIndex + 1
        val today = todayIndex + 1
        val result = if (birth == today) birth else birth + today - 1
        return if (result > 60) result - 60 else result
    }

    private fun indexedRow(table: String, index: Int): JSONObject =
        data.row(table) { row -> data.number(row, "idx") == index }

    private fun scored(
        title: String,
        emoji: String,
        row: JSONObject,
        contentKey: String,
        scoreKey: String,
    ) = ScoredFortune(
        title = title,
        emoji = emoji,
        score = data.number(row, scoreKey),
        content = data.text(row, contentKey),
    )

    private fun goodZodiac(birthBranch: Char, todayBranch: Char): String {
        val index = (branches.indexOf(birthBranch.toString()) + branches.indexOf(todayBranch.toString())) % 12
        return listOf(
            "원숭이띠 · 쥐띠 · 용띠",
            "뱀띠 · 닭띠 · 소띠",
            "호랑이띠 · 말띠 · 개띠",
            "돼지띠 · 토끼띠 · 양띠",
            "원숭이띠 · 쥐띠",
            "뱀띠 · 닭띠",
            "호랑이띠 · 말띠 · 개띠",
            "돼지띠 · 토끼띠 · 양띠",
            "원숭이띠 · 쥐띠 · 용띠",
            "뱀띠 · 닭띠 · 소띠",
            "호랑이띠 · 말띠 · 개띠",
            "토끼띠 · 양띠",
        )[index]
    }

    private fun luckyStarSign(birthStem: Char, todayBranch: Char): String {
        val nextStem = stems[
            (stems.indexOf(birthStem.toString()) + branches.indexOf(todayBranch.toString())) % stems.size
        ]
        val tongbenRow = data.row("skt_eve_unse1_Tongben") { row ->
            data.text(row, "daygan") == nextStem &&
                data.text(row, "ground") == todayBranch.toString()
        }
        return when (data.text(tongbenRow, "tongben")) {
            "비견" -> "전갈자리 · 사자자리"
            "겁재" -> "천칭자리 · 게자리"
            "식신" -> "처녀자리 · 천칭자리"
            "상관" -> "사수자리 · 물고기자리"
            "정재" -> "양자리 · 사수자리"
            "편재" -> "황소자리 · 물병자리"
            "정관" -> "물고기자리 · 양자리"
            "편관" -> "물병자리 · 전갈자리"
            "정인" -> "염소자리 · 쌍둥이자리"
            "편인" -> "사수자리 · 황소자리"
            else -> "오늘 마음이 가는 별자리"
        }
    }
}
