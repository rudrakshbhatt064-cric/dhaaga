package com.dhaaga.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Restarts the listener after the phone restarts, if a room is already set. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED && Store.room(context).isNotEmpty()) {
            MessageService.start(context)
        }
    }
}
