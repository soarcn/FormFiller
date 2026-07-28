package com.cocosw.formfiller.example.ui.login

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.insets.ColorProtection
import androidx.core.view.insets.ProtectionLayout

import com.cocosw.formfiller.example.R

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.enableEdgeToEdge(window)
        setContentView(R.layout.activity_login)
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = false

        findViewById<ProtectionLayout>(R.id.system_bar_protection).setProtections(
            listOf(
                ColorProtection(
                    WindowInsetsCompat.Side.TOP,
                    ContextCompat.getColor(this, R.color.colorPrimaryDark),
                ),
            ),
        )

        val contentPadding = resources.getDimensionPixelSize(R.dimen.content_padding)
        val container = findViewById<View>(R.id.container)
        ViewCompat.setOnApplyWindowInsetsListener(container) { view, windowInsets ->
            val systemBars = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
            )
            view.setPadding(
                contentPadding + systemBars.left,
                contentPadding + systemBars.top,
                contentPadding + systemBars.right,
                contentPadding + systemBars.bottom,
            )
            windowInsets
        }
        ViewCompat.requestApplyInsets(container)
    }
}
