package com.example.menu_app.utils

import android.app.Activity
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.menu_app.R

/** Same edge-to-edge padding code you already use in ActivityRegister, in one place. */
fun Activity.applySystemBarInsets(rootId: Int = R.id.main) {
    val root = findViewById<View>(rootId) ?: return
    ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
        insets
    }
}
