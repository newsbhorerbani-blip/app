package com.example.data.repository

import java.text.Normalizer
import java.util.Locale

object PhoneticSearchHelper {

    private val BN_MAP = mapOf(
        'ক' to "k", 'খ' to "k", 'গ' to "g", 'ঘ' to "g", 'ঙ' to "ng",
        'চ' to "c", 'ছ' to "c", 'জ' to "j", 'ঝ' to "j", 'ঞ' to "n",
        'ট' to "t", 'ঠ' to "t", 'ড' to "d", 'ঢ' to "d", 'ণ' to "n",
        'ত' to "t", 'থ' to "t", 'দ' to "d", 'ধ' to "d", 'ন' to "n",
        'প' to "p", 'ফ' to "p", 'ব' to "b", 'ভ' to "b", 'ম' to "m",
        'য' to "j", 'র' to "r", 'ল' to "l", 'শ' to "s", 'ষ' to "s",
        'স' to "s", 'হ' to "h", 'ড়' to "r", 'ঢ়' to "r", 'য়' to "j",
        'ৎ' to "t", 'ং' to "ng", 'ঃ' to "h"
    )

    private val SYNONYMS = mapOf(
        "rent" to listOf("ভাড়া", "বাসা"),
        "ভাড়া" to listOf("rent", "cost", "price"),
        "house" to listOf("বাসা", "বাড়ি"),
        "home" to listOf("বাসা", "বাড়ি"),
        "বাসা" to listOf("house", "home", "flat", "room", "বাড়ি"),
        "বাড়ি" to listOf("house", "home", "বাসা"),
        "mess" to listOf("মেস"),
        "মেস" to listOf("mess", "seat"),
        "seat" to listOf("সিট"),
        "সিট" to listOf("seat", "mess"),
        "flat" to listOf("ফ্ল্যাট"),
        "ফ্ল্যাট" to listOf("flat", "apartment"),
        "room" to listOf("রুম"),
        "রুম" to listOf("room"),
        "bachelor" to listOf("ব্যাচেলর"),
        "ব্যাচেলর" to listOf("bachelor"),
        "family" to listOf("ফ্যামিলি"),
        "ফ্যামিলি" to listOf("family"),
        "sublet" to listOf("সাবলেট"),
        "সাবলেট" to listOf("sublet"),
        "price" to listOf("দাম", "ভাড়া", "টাকা"),
        "cost" to listOf("টাকা", "ভাড়া"),
        "দাম" to listOf("price", "cost"),
        "টাকা" to listOf("taka", "price", "cost"),
        "sell" to listOf("বিক্রি"),
        "বিক্রি" to listOf("sell", "sale"),
        "buy" to listOf("কেনা", "কিনব"),
        "কেনা" to listOf("buy"),
        "কিনব" to listOf("buy"),
        "used" to listOf("ব্যবহৃত", "পুরনো", "পুরাতন"),
        "old" to listOf("পুরনো", "পুরাতন"),
        "পুরনো" to listOf("old", "used"),
        "পুরাতন" to listOf("old", "used"),
        "ব্যবহৃত" to listOf("used", "secondhand")
    )

    fun phoneticKey(input: String?): String {
        if (input.isNullOrBlank()) return ""
        var s = Normalizer.normalize(input, Normalizer.Form.NFC).lowercase(Locale.ROOT)

        // Normalize Bengali numerals
        val bnDigits = "০১২৩৪৫৬৭৮৯"
        for (i in bnDigits.indices) {
            s = s.replace(bnDigits[i], ('0' + i))
        }

        // Normalize Bengali nukta compounds (ড/ঢ + nukta -> র)
        s = s.replace("[ডঢ]\\u09BC".toRegex(), "র")
            .replace("য\\u09BC".toRegex(), "জ")
            .replace("\\u09CD\\u09AF".toRegex(), "")

        // Normalize Latin digraphs common in Banglish
        s = s.replace("gh", "g")
            .replace("kh", "k")
            .replace("th", "t")
            .replace("dh", "d")
            .replace("bh", "b")
            .replace("ph", "p")
            .replace("ch", "c")
            .replace("sh", "s")
            .replace("x", "ks")
            .replace("q", "k")
            .replace("c", "k")
            .replace("f", "p")
            .replace("v", "b")
            .replace("z", "j")

        val sb = StringBuilder()
        for (ch in s) {
            if (BN_MAP.containsKey(ch)) {
                sb.append(BN_MAP[ch])
            } else if (ch in 'a'..'z' || ch in '0'..'9') {
                sb.append(ch)
            }
        }

        var res = sb.toString()
            .replace("[aeiouyhw]".toRegex(), "")
            .replace("[^a-z0-9]".toRegex(), "")

        // Collapse duplicate letters: "kk" -> "k"
        res = res.replace("(.)\\1+".toRegex(), "$1")
        return res
    }

    fun matches(query: String?, vararg fields: String?): Boolean {
        if (query.isNullOrBlank()) return true
        val tokens = query.trim().lowercase(Locale.ROOT).split("\\s+".toRegex()).filter { it.isNotBlank() }
        if (tokens.isEmpty()) return true

        val combinedHay = fields.filterNotNull().joinToString(" ").lowercase(Locale.ROOT)
        val combinedKey = phoneticKey(combinedHay)

        return tokens.all { token ->
            if (combinedHay.contains(token)) return@all true
            val tokenKey = phoneticKey(token)
            if (tokenKey.length >= 2 && combinedKey.contains(tokenKey)) return@all true
            val syns = SYNONYMS[token] ?: emptyList()
            syns.any { combinedHay.contains(it) }
        }
    }

    fun toBengaliNumber(number: Long): String {
        val bnDigits = "০১২৩৪৫৬৭৮৯"
        return number.toString().map { ch ->
            if (ch in '0'..'9') bnDigits[ch - '0'] else ch
        }.joinToString("")
    }
}
