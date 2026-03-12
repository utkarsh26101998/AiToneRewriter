package com.utkarsh.aitonerewriter

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.utkarsh.aitonerewriter.service.FloatingWidgetService

/**
 * Main Activity — handles permission setup and service launch.
 * Guides the user through enabling:
 * 1. Overlay (draw over other apps) permission
 * 2. Accessibility service
 * Then starts the floating widget.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var tvOverlayStatus: TextView
    private lateinit var tvAccessibilityStatus: TextView
    private lateinit var btnOverlay: Button
    private lateinit var btnAccessibility: Button
    private lateinit var btnLaunch: Button
    private lateinit var btnStop: Button

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        updateUI()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvOverlayStatus = findViewById(R.id.tvOverlayStatus)
        tvAccessibilityStatus = findViewById(R.id.tvAccessibilityStatus)
        btnOverlay = findViewById(R.id.btnOverlay)
        btnAccessibility = findViewById(R.id.btnAccessibility)
        btnLaunch = findViewById(R.id.btnLaunch)
        btnStop = findViewById(R.id.btnStop)

        btnOverlay.setOnClickListener { requestOverlayPermission() }
        btnAccessibility.setOnClickListener { openAccessibilitySettings() }
        btnLaunch.setOnClickListener { launchWidget() }
        btnStop.setOnClickListener { stopWidget() }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun updateUI() {
        val hasOverlay = Settings.canDrawOverlays(this)
        val hasAccessibility = isAccessibilityServiceEnabled()

        // Overlay status
        tvOverlayStatus.text = if (hasOverlay) "✅ Granted" else "❌ Not Granted"
        btnOverlay.isEnabled = !hasOverlay

        // Accessibility status
        tvAccessibilityStatus.text = if (hasAccessibility) "✅ Enabled" else "❌ Not Enabled"
        btnAccessibility.isEnabled = !hasAccessibility

        // Launch button
        val canLaunch = hasOverlay && hasAccessibility
        btnLaunch.isEnabled = canLaunch
        btnLaunch.alpha = if (canLaunch) 1f else 0.5f
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        overlayPermissionLauncher.launch(intent)
    }

    private fun openAccessibilitySettings() {
        Toast.makeText(
            this,
            "Enable \"AI Tone Rewriter\" in the accessibility list",
            Toast.LENGTH_LONG
        ).show()
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    private fun launchWidget() {
        FloatingWidgetService.start(this)
        Toast.makeText(this, "🚀 Floating widget launched!", Toast.LENGTH_SHORT).show()
        // Minimize app so user can go to messaging apps
        moveTaskToBack(true)
    }

    private fun stopWidget() {
        FloatingWidgetService.stop(this)
        Toast.makeText(this, "Widget stopped", Toast.LENGTH_SHORT).show()
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        )
        return enabledServices.any {
            it.resolveInfo.serviceInfo.packageName == packageName
        }
    }
}
