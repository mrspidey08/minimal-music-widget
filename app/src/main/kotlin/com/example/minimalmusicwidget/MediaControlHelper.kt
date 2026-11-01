package com.example.minimalmusicwidget

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent

object MediaControlHelper {
    private const val TAG = "MediaControlHelper"

    enum class MediaAction {
        PLAY,
        PAUSE,
        PREVIOUS,
        NEXT
    }

    fun executeAction(context: Context, action: MediaAction) {
        var handledBySession = false

        // 1. Try active MediaSessions first if MediaListenerService is active
        try {
            val service = MediaListenerService.instance
            if (service != null) {
                val sessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
                val component = android.content.ComponentName(context, MediaListenerService::class.java)
                val controllers = sessionManager?.getActiveSessions(component)

                if (!controllers.isNullOrEmpty()) {
                    // Find the best controller (playing or most recently active)
                    val activeController = controllers.firstOrNull {
                        it.playbackState?.state == PlaybackState.STATE_PLAYING
                    } ?: controllers.first()

                    dispatchSessionAction(activeController, action)
                    handledBySession = true
                    Log.d(TAG, "Dispatched $action to session: ${activeController.packageName}")

                    // Refresh metadata after action
                    Handler(Looper.getMainLooper()).postDelayed({
                        service.updateSongInfoFromControllers()
                    }, 400)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error accessing MediaSession: ${e.message}")
        }

        // 2. Fallback / Universal dispatch via AudioManager and MediaButton Broadcast
        if (!handledBySession) {
            dispatchKeyEventFallback(context, action)
        }
    }

    private fun dispatchSessionAction(controller: MediaController, action: MediaAction) {
        val controls = controller.transportControls
        when (action) {
            MediaAction.PLAY -> controls.play()
            MediaAction.PAUSE -> controls.pause()
            MediaAction.PREVIOUS -> controls.skipToPrevious()
            MediaAction.NEXT -> controls.skipToNext()
        }
    }

    private fun dispatchKeyEventFallback(context: Context, action: MediaAction) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return

        val keyCode = when (action) {
            MediaAction.PLAY -> KeyEvent.KEYCODE_MEDIA_PLAY
            MediaAction.PAUSE -> KeyEvent.KEYCODE_MEDIA_PAUSE
            MediaAction.PREVIOUS -> KeyEvent.KEYCODE_MEDIA_PREVIOUS
            MediaAction.NEXT -> KeyEvent.KEYCODE_MEDIA_NEXT
        }

        val eventTime = SystemClock.uptimeMillis()
        val downEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0)
        val upEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, keyCode, 0)

        // Dispatch directly via AudioManager
        audioManager.dispatchMediaKeyEvent(downEvent)
        audioManager.dispatchMediaKeyEvent(upEvent)

        // Also broadcast ACTION_MEDIA_BUTTON for receivers listening to media button intents
        try {
            val mediaIntentDown = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
                putExtra(Intent.EXTRA_KEY_EVENT, downEvent)
            }
            context.sendOrderedBroadcast(mediaIntentDown, null)

            val mediaIntentUp = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
                putExtra(Intent.EXTRA_KEY_EVENT, upEvent)
            }
            context.sendOrderedBroadcast(mediaIntentUp, null)
        } catch (e: Exception) {
            Log.w(TAG, "Broadcast media button error: ${e.message}")
        }
    }
}
