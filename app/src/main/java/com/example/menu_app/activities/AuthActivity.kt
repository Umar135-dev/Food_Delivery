package com.example.menu_app.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.menu_app.R
import com.example.menu_app.auth.UserManager
import com.example.menu_app.databinding.ActivityAuthBinding
import com.example.menu_app.utils.addSpringTouchEffect
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If a session already exists, skip the login screen.
        if (UserManager.isUserLoggedIn()) {
            navigateToDashboard()
            return
        }

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSubmitAuth.addSpringTouchEffect()

        // Google sign-in
        binding.btnGoogle.setOnClickListener { signInWithGoogle() }

        // Open the register screen
        binding.tvToggleAuthMode.setOnClickListener {
            startActivity(Intent(this, ActivityRegister::class.java))
        }

        // Email and password sign-in
        binding.btnSubmitAuth.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            setLoadingState(true)

            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    setLoadingState(false)
                    if (task.isSuccessful) {
                        val user = firebaseAuth.currentUser
                        if (user != null && !user.isEmailVerified) {
                            // Registration sends a verification email, so enforce it here.
                            firebaseAuth.signOut()
                            Toast.makeText(
                                this,
                                "Please verify your email first, then log in.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            navigateToDashboard()
                        }
                    } else {
                        Toast.makeText(
                            this,
                            "Authentication Failed: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        // Password reset
        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please enter your email to reset password!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Password reset email sent. Check your inbox.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        task.exception?.localizedMessage ?: "Could not send reset email.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // ------------------------------------------------------------ Google sign-in

    private fun signInWithGoogle() {
        setGoogleLoadingState(true)

        lifecycleScope.launch {
            try {
                val credentialManager = CredentialManager.create(this@AuthActivity)

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    context = this@AuthActivity,
                    request = request
                )

                val credential = result.credential

                if (
                    credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    firebaseAuthWithGoogle(googleCredential.idToken)
                } else {
                    setGoogleLoadingState(false)
                    Toast.makeText(
                        this@AuthActivity,
                        "Google credential not supported.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: GetCredentialCancellationException) {
                // The user closed the Google account picker.
                setGoogleLoadingState(false)

            } catch (e: GetCredentialException) {
                setGoogleLoadingState(false)
                Toast.makeText(
                    this@AuthActivity,
                    e.localizedMessage ?: "Google sign-in failed.",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {
                setGoogleLoadingState(false)
                Toast.makeText(
                    this@AuthActivity,
                    e.localizedMessage ?: "Something went wrong.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

        firebaseAuth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener(this) { task ->
                setGoogleLoadingState(false)

                if (task.isSuccessful) {
                    Toast.makeText(this, "Google sign-in successful!", Toast.LENGTH_SHORT).show()
                    navigateToDashboard()
                } else {
                    Toast.makeText(
                        this,
                        task.exception?.localizedMessage ?: "Firebase authentication failed.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    // ------------------------------------------------------------ UI state

    private fun setGoogleLoadingState(isLoading: Boolean) {
        binding.authProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnGoogle.isEnabled = !isLoading
        binding.btnSubmitAuth.isEnabled = !isLoading
        binding.tvToggleAuthMode.isEnabled = !isLoading
        binding.tvForgotPassword.isEnabled = !isLoading
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.authProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSubmitAuth.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
    }

    private fun navigateToDashboard() {
        startActivity(Intent(this, MainActivity::class.java))
        finish() // Remove this screen from the back stack so Back can't return to login.
    }
}