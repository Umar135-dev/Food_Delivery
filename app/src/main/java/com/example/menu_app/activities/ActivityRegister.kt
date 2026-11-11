package com.example.menu_app.activities
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.menu_app.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class ActivityRegister : AppCompatActivity() {

    // Declare the Firebase Auth instance variable
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        // Initialize the Firebase Auth instance
        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val nameInput = findViewById<EditText>(R.id.nameTxt)
        val emailInput = findViewById<EditText>(R.id.emailTxt)
        val passwordInput = findViewById<EditText>(R.id.passowrdTxt)
        val confirmPasswordInput = findViewById<EditText>(R.id.confirmpasswordTxt)
        val buttonInput = findViewById<Button>(R.id.signupTxt)
        val loginLink = findViewById<TextView>(R.id.loginLinkTxt)

        // "Already have an account? Log in" -> back to the login screen
        loginLink.setOnClickListener {
            finish()
        }

        buttonInput.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(this, "The Passwords do not match. Please try again.", Toast.LENGTH_SHORT).show()
            } else if (password.length < 6) {
                // Firebase requires passwords to be at least 6 characters long
                Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
            } else {
                // Create a new user with email and password
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val profile = UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build()

                            // Save the name first, then verify, sign out and go back
                            user?.updateProfile(profile)?.addOnCompleteListener {
                                user.sendEmailVerification()
                                auth.signOut()
                                Toast.makeText(
                                    this,
                                    "Registered! Please verify your email, then log in.",
                                    Toast.LENGTH_LONG
                                ).show()
                                finish()
                            }
                        } else {
                            // Registration failed (e.g., email already exists or is malformed)
                            val errorMessage = task.exception?.message ?: "Registration Failed"
                            Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }
    }
}