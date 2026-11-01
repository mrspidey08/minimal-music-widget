package com.example.minimalmusicwidget

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class MediaListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "MediaListenerService"
        var instance: MediaListenerService? = null
            private set

        fun isNotificationServiceEnabled(context: Context): Boolean {
            val pkgName = context.packageName
            val flat = android.provider.Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: return false
            return flat.contains(pkgName)
        }
    }

    private var sessionManager: MediaSessionManager? = null
    private var sessionListener: MediaSessionManager.OnActiveSessionsChangedListener? = null
    private val controllerCallbacks = mutableMapOf<MediaController, MediaController.Callback>()

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d(TAG, "MediaListenerService created")
        setupSessionManager()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        Log.d(TAG, "MediaListenerService listener connected")
        setupSessionManager()
        updateSongInfoFromControllers()
    }

    private fun setupSessionManager() {
        try {
            if (sessionManager == null) {
                sessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
            }
            val component = ComponentName(this, MediaListenerService::class.java)

            if (sessionListener == null) {
                sessionListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
                    Log.d(TAG, "Active sessions changed: count = ${controllers?.size ?: 0}")
                    refreshControllers(controllers)
                }
                sessionManager?.addOnActiveSessionsChangedListener(sessionListener!!, component)
            }

            val currentControllers = sessionManager?.getActiveSessions(component)
            refreshControllers(currentControllers)
        } catch (e: Exception) {
            Log.w(TAG, "Error setting up session manager: ${e.message}")
        }
    }

    private fun refreshControllers(controllers: List<MediaController>?) {
        // Unregister previous callbacks
        for ((controller, callback) in controllerCallbacks) {
            try {
                controller.unregisterCallback(callback)
            } catch (e: Exception) {
                Log.w(TAG, "Error unregistering callback: ${e.message}")
            }
        }
        controllerCallbacks.clear()

        // Register callbacks on active controllers
        controllers?.forEach { controller ->
            val callback = object : MediaController.Callback() {
                override fun onMetadataChanged(metadata: MediaMetadata?) {
                    updateSongInfoFromControllers()
                }

                override fun onPlaybackStateChanged(state: PlaybackState?) {
                    updateSongInfoFromControllers()
                }

                override fun onSessionDestroyed() {
                    updateSongInfoFromControllers()
                }
            }
            try {
                controller.registerCallback(callback)
                controllerCallbacks[controller] = callback
            } catch (e: Exception) {
                Log.w(TAG, "Error registering callback: ${e.message}")
            }
        }

        updateSongInfoFromControllers()
    }

    fun updateSongInfoFromControllers() {
        try {
            val component = ComponentName(this, MediaListenerService::class.java)
            val controllers = sessionManager?.getActiveSessions(component)

            // Prefer playing session, otherwise session with metadata, or first available
            val activeController = controllers?.firstOrNull {
                it.playbackState?.state == PlaybackState.STATE_PLAYING
            } ?: controllers?.firstOrNull {
                it.metadata != null
            } ?: controllers?.firstOrNull()

            var songTitle: String? = null

            if (activeController != null) {
                val metadata = activeController.metadata
                val title = metadata?.getText(MediaMetadata.METADATA_KEY_TITLE)?.toString()
                if (!title.isNullOrBlank()) {
                    songTitle = title
                }
            }

            Log.d(TAG, "Detected song title: $songTitle")
            MusicWidgetProvider.saveSongTitle(this, songTitle)
        } catch (e: Exception) {
            Log.w(TAG, "Error updating song title: ${e.message}")
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        // Refresh when notifications update (most music apps update notifications on track change)
        updateSongInfoFromControllers()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        updateSongInfoFromControllers()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
        for ((controller, callback) in controllerCallbacks) {
            try {
                controller.unregisterCallback(callback)
            } catch (e: Exception) {
                Log.w(TAG, "Error unregistering callback on destroy: ${e.message}")
            }
        }
        controllerCallbacks.clear()
        try {
            sessionListener?.let { sessionManager?.removeOnActiveSessionsChangedListener(it) }
        } catch (e: Exception) {
            Log.w(TAG, "Error unregistering session listener: ${e.message}")
        }
    }
}
