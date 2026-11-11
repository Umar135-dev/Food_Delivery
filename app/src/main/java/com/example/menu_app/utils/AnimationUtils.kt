package com.example.menu_app.utils
import android.view.MotionEvent
import android.view.View
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce

fun View.addSpringTouchEffect() {
    // Create scaling animations for X and Y axes
    val scaleXAnim = SpringAnimation(this, SpringAnimation.SCALE_X, 1f).apply {
        spring.stiffness = SpringForce.STIFFNESS_LOW
        spring.dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
    }
    val scaleYAnim = SpringAnimation(this, SpringAnimation.SCALE_Y, 1f).apply {
        spring.stiffness = SpringForce.STIFFNESS_LOW
        spring.dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
    }

    setOnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Shrink slightly when pressed down
                scaleXAnim.animateToFinalPosition(0.9f)
                scaleYAnim.animateToFinalPosition(0.9f)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // Snap back with a bounce when released
                scaleXAnim.animateToFinalPosition(1f)
                scaleYAnim.animateToFinalPosition(1f)
                performClick()
            }
        }
        false // Allow normal click listeners to still fire
    }
}
