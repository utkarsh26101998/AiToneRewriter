package com.utkarsh.aitonerewriter.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.utkarsh.aitonerewriter.R
import com.utkarsh.aitonerewriter.ai.AIRewriteEngine
import com.utkarsh.aitonerewriter.model.Mood
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Foreground service that manages the floating widget overlay.
 * Displays a draggable chat-head style bubble that expands into
 * a mood selection popup on tap.
 */
class FloatingWidgetService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var moodPopup: View
    private lateinit var floatingParams: WindowManager.LayoutParams
    private lateinit var popupParams: WindowManager.LayoutParams

    private val aiEngine = AIRewriteEngine()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var isPopupVisible = false
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    companion object {
        private const val CHANNEL_ID = "floating_widget_channel"
        private const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, FloatingWidgetService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, FloatingWidgetService::class.java))
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        setupFloatingBubble()
        setupMoodPopup()
    }

    override fun onDestroy() {
        serviceScope.cancel()
        try {
            windowManager.removeView(floatingView)
            if (isPopupVisible) {
                windowManager.removeView(moodPopup)
            }
        } catch (_: Exception) {}
        super.onDestroy()
    }

    // ─── Notification ────────────────────────────────────────────────

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AI Tone Rewriter",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the floating widget active"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AI Tone Rewriter")
            .setContentText("Tap the floating bubble to rewrite messages")
            .setSmallIcon(R.drawable.ic_bubble)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    // ─── Floating Bubble ─────────────────────────────────────────────

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    private fun setupFloatingBubble() {
        floatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_bubble, null)

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        floatingParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 300
        }

        windowManager.addView(floatingView, floatingParams)

        // Drag + tap handling
        floatingView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = floatingParams.x
                    initialY = floatingParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    floatingParams.x = initialX + (event.rawX - initialTouchX).toInt()
                    floatingParams.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(floatingView, floatingParams)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val deltaX = Math.abs(event.rawX - initialTouchX)
                    val deltaY = Math.abs(event.rawY - initialTouchY)
                    // If barely moved, treat as a tap
                    if (deltaX < 10 && deltaY < 10) {
                        toggleMoodPopup()
                    }
                    true
                }
                else -> false
            }
        }
    }

    // ─── Mood Popup ──────────────────────────────────────────────────

    @SuppressLint("InflateParams")
    private fun setupMoodPopup() {
        moodPopup = LayoutInflater.from(this).inflate(R.layout.layout_mood_popup, null)

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        popupParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        // Populate mood buttons
        val moodContainer = moodPopup.findViewById<LinearLayout>(R.id.moodContainer)
        Mood.entries.forEach { mood ->
            val moodButton = LayoutInflater.from(this)
                .inflate(R.layout.item_mood_button, moodContainer, false) as TextView
            moodButton.text = "${mood.emoji} ${mood.displayName}"
            moodButton.setOnClickListener { onMoodSelected(mood) }
            moodContainer.addView(moodButton)
        }

        // Close button
        moodPopup.findViewById<View>(R.id.btnClose)?.setOnClickListener {
            hideMoodPopup()
        }
    }

    private fun toggleMoodPopup() {
        if (isPopupVisible) {
            hideMoodPopup()
        } else {
            showMoodPopup()
        }
    }

    private fun showMoodPopup() {
        if (!isPopupVisible) {
            windowManager.addView(moodPopup, popupParams)
            isPopupVisible = true
        }
    }

    private fun hideMoodPopup() {
        if (isPopupVisible) {
            windowManager.removeView(moodPopup)
            isPopupVisible = false
        }
    }

    // ─── Rewrite Logic ───────────────────────────────────────────────

    private fun onMoodSelected(mood: Mood) {
        hideMoodPopup()

        val accessibilityService = TextAccessibilityService.instance
        if (accessibilityService == null) {
            Toast.makeText(this, "Accessibility service not active", Toast.LENGTH_SHORT).show()
            return
        }

        val currentText = accessibilityService.captureCurrentText()
        if (currentText.isNullOrBlank()) {
            Toast.makeText(this, "No text found in input field", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "${mood.emoji} Rewriting...", Toast.LENGTH_SHORT).show()

        serviceScope.launch {
            val result = aiEngine.rewrite(currentText, mood)
            result.onSuccess { rewrittenText ->
                val replaced = accessibilityService.replaceText(rewrittenText)
                if (!replaced) {
                    Toast.makeText(
                        this@FloatingWidgetService,
                        "Could not replace text",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            result.onFailure { error ->
                Toast.makeText(
                    this@FloatingWidgetService,
                    "Error: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
