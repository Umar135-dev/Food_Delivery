package com.example.menu_app.activities

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.menu_app.R
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val user = FirebaseAuth.getInstance().currentUser
        findViewById<TextView>(R.id.tvProfileName).text = user?.displayName ?: "No name"
        findViewById<TextView>(R.id.tvProfileEmail).text = user?.email ?: "No email"
        findViewById<TextView>(R.id.tvBack).setOnClickListener { finish() }
    }
}