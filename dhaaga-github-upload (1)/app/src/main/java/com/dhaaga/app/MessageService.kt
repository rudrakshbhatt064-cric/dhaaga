package com.dhaaga.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import okhttp3.Request
import org.json.JSONObject

/**
 * Keeps one connection open to the room and shows every message from the partner
 * as a notification and on the home screen widget.
 */
class MessageService : Service() {

    @Volatile
    private var running = false
    private var worker: Thread? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ensureChannels(this)
        val ongoing = Notification.Builder(this, CH_SERVICE)
            .setSmallIcon(R.drawable.ic_heart)
            .setContentTitle("Dhaaga chalu hai")
            .setContentText("Partner ke sandesh yahin se aayenge")
            .setOngoing(true)
            .build()
        startForeground(1, ongoing)

        if (Store.room(this).isEmpty()) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (worker?.isAlive != true) {
            running = true
            worker = Thread { listen() }.apply {
                isDaemon = true
                start()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        running = false
        Net.client.dispatcher.cancelAll()
        worker?.interrupt()
        super.onDestroy()
    }

    private fun listen() {
        while (running) {
            try {
                val since = Store.sinceId(this)
                val url = Net.SERVER + "/" + Store.topic(this) + "/json" +
                    (if (since.isNotEmpty()) "?since=$since" else "")
                val req = Request.Builder().url(url).build()
                Net.client.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) return@use
                    val source = resp.body?.source() ?: return@use
                    while (running && !source.exhausted()) {
                        val line = source.readUtf8Line() ?: break
                        if (line.isNotBlank()) handle(line)
                    }
                }
            } catch (e: Exception) {
                // connection dropped, retry below
            }
            if (running) {
                try {
                    Thread.sleep(5000)
                } catch (e: InterruptedException) {
                    return
                }
            }
        }
    }

    private fun handle(line: String) {
        val j = try {
            JSONObject(line)
        } catch (e: Exception) {
            return
        }
        if (j.optString("event") != "message") return

        val id = j.optString("id")
        if (id.isNotEmpty()) Store.setSinceId(this, id)

        // Skip what this phone sent itself.
        val tags = j.optJSONArray("tags")
        val mine = Store.deviceId(this)
        if (tags != null) {
            for (i in 0 until tags.length()) {
                if (tags.optString(i) == mine) return
            }
        }

        val text = j.optString("message")
        if (text.isEmpty()) return
        val from = j.optString("title").ifEmpty { "Partner" }
        val seconds = j.optLong("time")
        val at = if (seconds > 0) seconds * 1000 else System.currentTimeMillis()

        Store.saveMessage(this, text, from, at)
        DhaagaWidget.refresh(this)
        showNotification(from, text)
    }

    private fun showNotification(from: String, text: String) {
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val n = Notification.Builder(this, CH_MESSAGES)
            .setSmallIcon(R.drawable.ic_heart)
            .setContentTitle(from)
            .setContentText(text)
            .setStyle(Notification.BigTextStyle().bigText(text))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        getSystemService(NotificationManager::class.java)
            .notify(System.currentTimeMillis().toInt(), n)
    }

    companion object {
        const val CH_SERVICE = "dhaaga_service"
        const val CH_MESSAGES = "dhaaga_messages"

        fun ensureChannels(ctx: Context) {
            val nm = ctx.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(
                NotificationChannel(CH_SERVICE, "Dhaaga chalu hai", NotificationManager.IMPORTANCE_MIN)
            )
            nm.createNotificationChannel(
                NotificationChannel(CH_MESSAGES, "Partner ke sandesh", NotificationManager.IMPORTANCE_HIGH)
            )
        }

        fun start(ctx: Context) {
            ctx.startForegroundService(Intent(ctx, MessageService::class.java))
        }

        fun stop(ctx: Context) {
            ctx.stopService(Intent(ctx, MessageService::class.java))
        }
    }
}
