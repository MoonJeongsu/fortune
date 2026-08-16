package com.dakbit.fortune

import android.content.Context
import org.json.JSONObject

class FortuneDataSource(context: Context) {
    private val tables: Map<String, List<JSONObject>>

    init {
        val json = context.assets.open("fortune_data.json")
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
        val rawTables = JSONObject(json).getJSONObject("tables")
        tables = rawTables.keys().asSequence().associateWith { tableName ->
            val array = rawTables.getJSONArray(tableName)
            List(array.length()) { index -> array.getJSONObject(index) }
        }
    }

    fun row(table: String, predicate: (JSONObject) -> Boolean): JSONObject =
        tables[table]?.firstOrNull(predicate)
            ?: error("운세 데이터에서 $table 조건에 맞는 행을 찾지 못했습니다.")

    fun text(row: JSONObject, key: String): String =
        row.optString(key).trim()

    fun number(row: JSONObject, key: String): Int =
        row.optDouble(key).toInt()
}
