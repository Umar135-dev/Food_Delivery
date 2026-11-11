package com.example.menu_app.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.menu_app.R
import com.example.menu_app.databinding.ActivityMainBinding
import com.example.menu_app.fragments.About
import com.example.menu_app.fragments.Drinks
import com.example.menu_app.fragments.FoodFragment
import com.example.menu_app.model.CartManager
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import com.example.menu_app.model.foodList
import org.json.JSONObject
import kotlin.math.roundToLong

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onResume() {
        super.onResume()
        updateCartBadge()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        com.example.menu_app.data.CartManager.init(applicationContext)
        installSplashScreen()
        super.onCreate(savedInstanceState)








        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        dumpMenuForSeeding()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Header buttons
        binding.navCart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
        binding.ivProfile.setOnClickListener { showProfileMenu(it) }

        if (savedInstanceState == null) {
            replaceFragment(FoodFragment())
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_food -> {
                    replaceFragment(FoodFragment())
                    true
                }
                R.id.nav_drinks -> {
                    replaceFragment(Drinks())
                    true
                }
                R.id.navCart -> {
                    startActivity(Intent(this, CartActivity::class.java))
                    true
                }
                R.id.nav_about -> {
                    replaceFragment(About())
                    true
                }
                else -> false
            }
        }

        updateCartBadge()
    }


//    private fun dumpMenuForSeeding() {
//        foodList.forEach { f ->
//            val dollars = f.price.replace("[^\\d.]".toRegex(), "").toDoubleOrNull() ?: 0.0
//            val json = JSONObject()
//                .put("id", f.id.toString())
//                .put("name", f.name)
//                .put("description", f.description)
//                .put("priceCents", (dollars * 100).roundToLong())
//                .put("available", true)
//            Log.d("MENU_SEED", json.toString())
//        }
//    }

    private fun showProfileMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menuInflater.inflate(R.menu.profile_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                R.id.menu_logout -> {
                    confirmLogout()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Log Out") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Updates the count on the top cart button and the bottom nav badge
    fun updateCartBadge() {
        val totalCount = CartManager.getTotalItemsCount()

        // Top cart badge
        if (totalCount > 0) {
            binding.tvCartBadge.visibility = View.VISIBLE
            binding.tvCartBadge.text = if (totalCount > 99) "99+" else totalCount.toString()
        } else {
            binding.tvCartBadge.visibility = View.GONE
        }

        // Bottom nav badge
        val badge = binding.bottomNavigation.getOrCreateBadge(R.id.navCart)
        if (totalCount > 0) {
            badge.isVisible = true
            badge.number = totalCount
            badge.backgroundColor = resources.getColor(android.R.color.holo_red_dark, theme)
            badge.badgeTextColor = resources.getColor(android.R.color.white, theme)
        } else {
            badge.isVisible = false
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}