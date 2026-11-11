package com.example.menu_app.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.menu_app.R
import com.example.menu_app.auth.UserManager
import com.example.menu_app.databinding.ActivityGetStartedBinding

class GetStartedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGetStartedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Already signed in? Skip straight to the app.
        if (UserManager.isUserLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        binding = ActivityGetStartedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        binding.btnGetStarted.setOnClickListener { openLogin() }
        binding.btnGoogleStart.setOnClickListener { openLogin() }
        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, ActivityRegister::class.java))
        }
    }

    private fun openLogin() {
        startActivity(Intent(this, AuthActivity::class.java))
    }
}