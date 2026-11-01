package com.example.minimalmusicwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews

class MusicWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_PLAY = "com.example.minimalmusicwidget.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.minimalmusicwidget.ACTION_PAUSE"
        const val ACTION_PREV = "com.example.minimalmusicwidget.ACTION_PREV"
        const val ACTION_NEXT = "com.example.minimalmusicwidget.ACTION_NEXT"

        const val PREFS_NAME = "music_widget_prefs"
        const val PREF_HEADER_TEXT = "header_text"
        const val DEFAULT_HEADER_TEXT = "MUSIC IS LIFE"

        const val PREF_USE_SONG_TITLE = "use_song_title"
        const val DEFAULT_USE_SONG_TITLE = true

        const val PREF_CURRENT_SONG_TITLE = "current_song_title"
        const val DEFAULT_NO_MEDIA_TEXT = "NO TRACK PLAYING"

        fun getDisplayText(context: Context): String {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val useSongTitle = prefs.getBoolean(PREF_USE_SONG_TITLE, DEFAULT_USE_SONG_TITLE)
            return if (useSongTitle) {
                val currentTitle = prefs.getString(PREF_CURRENT_SONG_TITLE, null)
                if (!currentTitle.isNullOrBlank()) {
                    currentTitle
                } else {
                    DEFAULT_NO_MEDIA_TEXT
                }
            } else {
                prefs.getString(PREF_HEADER_TEXT, DEFAULT_HEADER_TEXT) ?: DEFAULT_HEADER_TEXT
            }
        }

        fun saveSongTitle(context: Context, title: String?) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val cleanTitle = title?.trim()
            prefs.edit().putString(PREF_CURRENT_SONG_TITLE, cleanTitle).apply()
            updateAllWidgets(context)
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, MusicWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_music_control)

            // Dynamic header text (Song Title or Custom Text based on toggle)
            views.setTextViewText(R.id.widget_title, getDisplayText(context))

            // PendingIntent flags
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            // Click listener for Title to open Companion App / Settings
            val appIntent = Intent(context, MainActivity::class.java).apply {
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            val titlePendingIntent = PendingIntent.getActivity(
                context,
                0,
                appIntent,
                flags
            )
            views.setOnClickPendingIntent(R.id.widget_title, titlePendingIntent)

            // Play button
            val playIntent = Intent(context, MusicWidgetProvider::class.java).apply {
                action = ACTION_PLAY
            }
            val playPendingIntent = PendingIntent.getBroadcast(
                context,
                101,
                playIntent,
                flags
            )
            views.setOnClickPendingIntent(R.id.btn_play, playPendingIntent)

            // Pause button
            val pauseIntent = Intent(context, MusicWidgetProvider::class.java).apply {
                action = ACTION_PAUSE
            }
            val pausePendingIntent = PendingIntent.getBroadcast(
                context,
                102,
                pauseIntent,
                flags
            )
            views.setOnClickPendingIntent(R.id.btn_pause, pausePendingIntent)

            // Previous button
            val prevIntent = Intent(context, MusicWidgetProvider::class.java).apply {
                action = ACTION_PREV
            }
            val prevPendingIntent = PendingIntent.getBroadcast(
                context,
                103,
                prevIntent,
                flags
            )
            views.setOnClickPendingIntent(R.id.btn_prev, prevPendingIntent)

            // Next button
            val nextIntent = Intent(context, MusicWidgetProvider::class.java).apply {
                action = ACTION_NEXT
            }
            val nextPendingIntent = PendingIntent.getBroadcast(
                context,
                104,
                nextIntent,
                flags
            )
            views.setOnClickPendingIntent(R.id.btn_next, nextPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_PLAY -> {
                MediaControlHelper.executeAction(context, MediaControlHelper.MediaAction.PLAY)
            }
            ACTION_PAUSE -> {
                MediaControlHelper.executeAction(context, MediaControlHelper.MediaAction.PAUSE)
            }
            ACTION_PREV -> {
                MediaControlHelper.executeAction(context, MediaControlHelper.MediaAction.PREVIOUS)
            }
            ACTION_NEXT -> {
                MediaControlHelper.executeAction(context, MediaControlHelper.MediaAction.NEXT)
            }
        }
    }
}
