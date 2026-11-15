package com.dhaaga.app

import android.content.Context
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/** Talks to ntfy.sh, a free public publish/subscribe server. The room code is the topic name. */
object Net {
    const val SERVER = "https://ntfy.sh"

    /** No read timeout: the listener keeps one connection open and waits for messages. */
    val client: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val sender: OkHttpClient = client.newBuilder()
        .readTimeout(15, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    /** Sends [text] to the room on a background thread, then calls [onDone] with true or false. */
    fun send(ctx: Context, text: String, onDone: (Boolean) -> Unit) {
        val app = ctx.applicationContext
        Thread {
            var ok = false
            try {
                val json = JSONObject()
                json.put("topic", Store.topic(app))
                json.put("message", text)
                json.put("title", Store.name(app).ifBlank { "Partner" })
                json.put("tags", JSONArray().put(Store.deviceId(app)))
                val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val req = Request.Builder().url(SERVER).post(body).build()
                sender.newCall(req).execute().use { ok = it.isSuccessful }
            } catch (e: Exception) {
                ok = false
            }
            onDone(ok)
        }.start()
    }
}
