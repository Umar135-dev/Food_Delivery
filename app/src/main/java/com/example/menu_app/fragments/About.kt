package com.example.menu_app.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.menu_app.R
import com.example.menu_app.auth.UserManager
import com.example.menu_app.activities.AuthActivity
import com.example.menu_app.databinding.FragmentAboutBinding
import com.example.menu_app.activities.OrderHistoryActivity



class About : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val email = UserManager.currentUser?.email
        if (email != null) {
            binding.txtPrivacyPolicy.text = getString(R.string.about_legal)
            binding.tvProfileEmail.text = email
            val name = email.substringBefore("@")
                .replaceFirstChar { it.uppercase() }
            binding.tvProfileName.text = name
            binding.tvAvatarLetter.text = name.take(1)
        }

        binding.btnOrderHistory.setOnClickListener {
            startActivity(Intent(requireContext(), OrderHistoryActivity::class.java))
        }



        binding.btnLogout.setOnClickListener {
            UserManager.logout()
            val intent = Intent(requireContext(), AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}