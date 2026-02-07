package com.cleansoft.duvoice.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.cleansoft.duvoice.R
import com.cleansoft.duvoice.service.AudioRecordService

class QuickRecordWidget : AppWidgetProvider() {

    companion object {
        const val ACTION_TOGGLE_RECORDING = "com.cleansoft.duvoice.TOGGLE_RECORDING"
        const val ACTION_QUICK_IDEA = "com.cleansoft.duvoice.QUICK_IDEA"

        fun updateWidget(context: Context, isRecording: Boolean) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetComponent = ComponentName(context, QuickRecordWidget::class.java)
            val widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)

            widgetIds.forEach { widgetId ->
                updateAppWidget(context, appWidgetManager, widgetId, isRecording)
            }
        }

        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            isRecording: Boolean
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_quick_record)

            // Configurar botão de gravação
            val toggleIntent = Intent(context, QuickRecordWidget::class.java).apply {
                action = ACTION_TOGGLE_RECORDING
            }
            val togglePendingIntent = PendingIntent.getBroadcast(
                context, 0, toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_widget_record, togglePendingIntent)

            // Configurar botão de ideia rápida
            val quickIdeaIntent = Intent(context, QuickRecordWidget::class.java).apply {
                action = ACTION_QUICK_IDEA
            }
            val quickIdeaPendingIntent = PendingIntent.getBroadcast(
                context, 1, quickIdeaIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_widget_quick_idea, quickIdeaPendingIntent)

            // Atualizar visual baseado no estado
            if (isRecording) {
                views.setImageViewResource(R.id.btn_widget_record, R.drawable.ic_stop)
                views.setTextViewText(R.id.tv_widget_status, context.getString(R.string.recording))
                views.setInt(R.id.btn_widget_record, "setColorFilter",
                    context.getColor(R.color.recording_red))
            } else {
                views.setImageViewResource(R.id.btn_widget_record, R.drawable.ic_mic)
                views.setTextViewText(R.id.tv_widget_status, context.getString(R.string.tap_to_start))
                views.setInt(R.id.btn_widget_record, "setColorFilter",
                    context.getColor(R.color.primary))
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, appWidgetManager, appWidgetId, false)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_TOGGLE_RECORDING -> {
                toggleRecording(context, isQuickIdea = false)
            }
            ACTION_QUICK_IDEA -> {
                toggleRecording(context, isQuickIdea = true)
            }
        }
    }

    private fun toggleRecording(context: Context, isQuickIdea: Boolean) {
        val serviceIntent = Intent(context, AudioRecordService::class.java).apply {
            action = AudioRecordService.ACTION_TOGGLE_FROM_WIDGET
            putExtra(AudioRecordService.EXTRA_IS_QUICK_IDEA, isQuickIdea)
        }
        context.startForegroundService(serviceIntent)
    }

    override fun onEnabled(context: Context) {
        // Widget adicionado pela primeira vez
    }

    override fun onDisabled(context: Context) {
        // Último widget removido
    }
}

