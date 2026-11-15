package com.dhaaga.app

import android.content.Context
import android.content.SharedPreferences
import java.security.SecureRandom
import java.util.UUID

/** Everything the app remembers: room code, own name, last message received. */
object Store {
    const val PREFS = "dhaaga"

    fun prefs(c: Context): SharedPreferences = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun room(c: Context): String = prefs(c).getString("room", "") ?: ""
    fun setRoom(c: Context, v: String) = prefs(c).edit().putString("room", v).putString("since", "").apply()

    fun name(c: Context): String = prefs(c).getString("name", "") ?: ""
    fun setName(c: Context, v: String) = prefs(c).edit().putString("name", v).apply()

    /** Used to ignore our own messages when they come back from the room. */
    fun deviceId(c: Context): String {
        val p = prefs(c)
        var id = p.getString("did", null)
        if (id == null) {
            id = UUID.randomUUID().toString().take(8)
            p.edit().putString("did", id).apply()
        }
        return id
    }

    fun sinceId(c: Context): String = prefs(c).getString("since", "") ?: ""
    fun setSinceId(c: Context, v: String) = prefs(c).edit().putString("since", v).apply()

    fun lastText(c: Context): String = prefs(c).getString("lastText", "") ?: ""
    fun lastFrom(c: Context): String = prefs(c).getString("lastFrom", "") ?: ""
    fun lastAt(c: Context): Long = prefs(c).getLong("lastAt", 0L)

    fun saveMessage(c: Context, text: String, from: String, at: Long) {
        prefs(c).edit().putString("lastText", text).putString("lastFrom", from).putLong("lastAt", at).apply()
    }

    fun clearAll(c: Context) {
        prefs(c).edit().remove("room").remove("since").remove("lastText").remove("lastFrom").remove("lastAt").apply()
    }

    fun topic(c: Context): String = "dhaaga-" + room(c)

    /** 10 random characters from an alphabet without look-alikes. Hard to guess. */
    fun newRoomCode(): String {
        val alphabet = "abcdefghjkmnpqrstuvwxyz23456789"
        val rnd = SecureRandom()
        val sb = StringBuilder()
        repeat(10) { sb.append(alphabet[rnd.nextInt(alphabet.length)]) }
        return sb.toString()
    }
}
