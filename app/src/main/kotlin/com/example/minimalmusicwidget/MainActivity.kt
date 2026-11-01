package com.example.minimalmusicwidget

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var tvPermissionStatus: TextView
    private lateinit var tvStatusBadge: TextView
    private lateinit var btnGrantPermission: Button
    private lateinit var switchSongTitle: Switch
    private lateinit var tvToggleDesc: TextView
    private lateinit var etHeaderText: EditText
    private lateinit var btnSaveText: Button
    private lateinit var previewTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupWidgetPreviewControls()
        setupPermissionSection()
        setupHeaderSettings()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
        MediaListenerService.instance?.updateSongInfoFromControllers()
        refreshPreviewTitle()
    }

    private fun initViews() {
        tvPermissionStatus = findViewById(R.id.tv_permission_status)
        tvStatusBadge = findViewById(R.id.tv_status_badge)
        btnGrantPermission = findViewById(R.id.btn_grant_permission)
        switchSongTitle = findViewById(R.id.switch_song_title)
        tvToggleDesc = findViewById(R.id.tv_toggle_desc)
        etHeaderText = findViewById(R.id.et_header_text)
        btnSaveText = findViewById(R.id.btn_save_text)
        previewTitle = findViewById(R.id.widget_title)
    }

    private fun setupWidgetPreviewControls() {
        // Wire up live preview control buttons
        val btnPlay = findViewById<ImageButton>(R.id.btn_play)
        val btnPause = findViewById<ImageButton>(R.id.btn_pause)
        val btnPrev = findViewById<ImageButton>(R.id.btn_prev)
        val btnNext = findViewById<ImageButton>(R.id.btn_next)

        btnPlay.setOnClickListener {
            MediaControlHelper.executeAction(this, MediaControlHelper.MediaAction.PLAY)
            Toast.makeText(this, "▶ Play command sent", Toast.LENGTH_SHORT).show()
            postRefreshPreview()
        }

        btnPause.setOnClickListener {
            MediaControlHelper.executeAction(this, MediaControlHelper.MediaAction.PAUSE)
            Toast.makeText(this, "❚❚ Pause command sent", Toast.LENGTH_SHORT).show()
            postRefreshPreview()
        }

        btnPrev.setOnClickListener {
            MediaControlHelper.executeAction(this, MediaControlHelper.MediaAction.PREVIOUS)
            Toast.makeText(this, "◀ Previous command sent", Toast.LENGTH_SHORT).show()
            postRefreshPreview()
        }

        btnNext.setOnClickListener {
            MediaControlHelper.executeAction(this, MediaControlHelper.MediaAction.NEXT)
            Toast.makeText(this, "▶ Next command sent", Toast.LENGTH_SHORT).show()
            postRefreshPreview()
        }
    }

    private fun postRefreshPreview() {
        previewTitle.postDelayed({
            MediaListenerService.instance?.updateSongInfoFromControllers()
            refreshPreviewTitle()
        }, 400)
    }

    private fun setupPermissionSection() {
        btnGrantPermission.setOnClickListener {
            try {
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Unable to open notification settings: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updatePermissionStatus() {
        val isEnabled = MediaListenerService.isNotificationServiceEnabled(this)
        if (isEnabled) {
            tvPermissionStatus.text = "Media Session Access Active"
            tvStatusBadge.text = "ACTIVE"
            tvStatusBadge.setBackgroundColor(Color.parseColor("#00FF66"))
            tvStatusBadge.setTextColor(Color.parseColor("#000000"))
            btnGrantPermission.visibility = View.GONE
        } else {
            tvPermissionStatus.text = "Notification Access Required"
            tvStatusBadge.text = "ACTION NEEDED"
            tvStatusBadge.setBackgroundColor(Color.parseColor("#FFAA00"))
            tvStatusBadge.setTextColor(Color.parseColor("#000000"))
            btnGrantPermission.visibility = View.VISIBLE
        }
    }

    private fun setupHeaderSettings() {
        val prefs = getSharedPreferences(MusicWidgetProvider.PREFS_NAME, Context.MODE_PRIVATE)

        val useSongTitle = prefs.getBoolean(
            MusicWidgetProvider.PREF_USE_SONG_TITLE,
            MusicWidgetProvider.DEFAULT_USE_SONG_TITLE
        )
        val customText = prefs.getString(
            MusicWidgetProvider.PREF_HEADER_TEXT,
            MusicWidgetProvider.DEFAULT_HEADER_TEXT
        ) ?: MusicWidgetProvider.DEFAULT_HEADER_TEXT

        switchSongTitle.isChecked = useSongTitle
        etHeaderText.setText(customText)
        updateHeaderUIState(useSongTitle)
        refreshPreviewTitle()

        // Toggle listener: Song Title vs Custom Text
        switchSongTitle.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(MusicWidgetProvider.PREF_USE_SONG_TITLE, isChecked).apply()
            updateHeaderUIState(isChecked)
            refreshPreviewTitle()
            MusicWidgetProvider.updateAllWidgets(this)

            val msg = if (isChecked) {
                "Widget set to show Song Title"
            } else {
                "Widget set to show Custom Text"
            }
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // Save custom text button
        btnSaveText.setOnClickListener {
            val newText = etHeaderText.text.toString().trim()
            prefs.edit().putString(MusicWidgetProvider.PREF_HEADER_TEXT, newText).apply()
            refreshPreviewTitle()
            MusicWidgetProvider.updateAllWidgets(this)
            Toast.makeText(this, "Custom heading text updated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateHeaderUIState(useSongTitle: Boolean) {
        if (useSongTitle) {
            tvToggleDesc.text = "Displaying active song title (from Spotify, YouTube Music, etc.)"
        } else {
            tvToggleDesc.text = "Displaying your custom text below"
        }
    }

    private fun refreshPreviewTitle() {
        previewTitle.text = MusicWidgetProvider.getDisplayText(this)
    }
}
