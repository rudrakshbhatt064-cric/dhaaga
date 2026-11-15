package com.dhaaga.app

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.text.format.DateUtils
import android.widget.RemoteViews

/** Home screen widget that shows the latest message from the partner. */
class DhaagaWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        for (id in ids) update(context, manager, id)
    }

    companion object {
        fun refresh(ctx: Context) {
            val manager = AppWidgetManager.getInstance(ctx)
            val ids = manager.getAppWidgetIds(ComponentName(ctx, DhaagaWidget::class.java))
            for (id in ids) update(ctx, manager, id)
        }

        private fun update(ctx: Context, manager: AppWidgetManager, id: Int) {
            val views = RemoteViews(ctx.packageName, R.layout.widget)
            val text = Store.lastText(ctx)
            if (text.isEmpty()) {
                views.setTextViewText(R.id.w_from, "Dhaaga")
                views.setTextViewText(R.id.w_text, "Abhi koi sandesh nahi")
            } else {
                val ago = DateUtils.getRelativeTimeSpanString(
                    Store.lastAt(ctx), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS
                )
                views.setTextViewText(R.id.w_from, Store.lastFrom(ctx) + " · " + ago)
                views.setTextViewText(R.id.w_text, text)
            }
            val open = PendingIntent.getActivity(
                ctx, 0, Intent(ctx, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.w_root, open)
            manager.updateAppWidget(id, views)
        }
    }
}
