package com.example.gesticks.ui.view

import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.gesticks.R
import com.example.gesticks.databinding.FragmentProfileBinding
import com.example.gesticks.databinding.ItemProfileOptionBinding

import androidx.fragment.app.viewModels
import com.example.gesticks.ui.viewmodel.ProfileViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupMenuOptions()
        setupListeners()
        setupAnimations()
        observeViewModel()
        
        viewModel.loadUserData()
    }

    private fun observeViewModel() {
        viewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.tvName.text = name
        }
        viewModel.userEmail.observe(viewLifecycleOwner) { email ->
            binding.tvEmail.text = email
        }
        viewModel.totalTickets.observe(viewLifecycleOwner) { count ->
            (binding.statsContainer.getChildAt(0) as LinearLayout).let { 
                (it.getChildAt(0) as TextView).text = count.toString()
            }
        }
        viewModel.resolvedTickets.observe(viewLifecycleOwner) { count ->
            (binding.statsContainer.getChildAt(2) as LinearLayout).let { 
                (it.getChildAt(0) as TextView).text = count.toString()
            }
        }
        viewModel.activeTickets.observe(viewLifecycleOwner) { count ->
            (binding.statsContainer.getChildAt(4) as LinearLayout).let { 
                (it.getChildAt(0) as TextView).text = count.toString()
            }
        }
        binding.statsContainer.elevation = 12f
        binding.statsContainer.translationZ = 4f
    }

    private fun setupMenuOptions() {
        // Password
        setupOption(binding.optPassword, R.drawable.ic_lock, R.string.change_password, R.color.accent_yellow)
        // Smartwatch
        setupOption(binding.optSmartwatch, R.drawable.ic_watch, R.string.setup_smartwatch, R.color.subtitle_teal)
        // Voice
        setupOption(binding.optVoice, R.drawable.ic_mic, R.string.connect_voice, R.color.accent_yellow)
        // Notifications
        setupOption(binding.optNotifications, R.drawable.ic_notifications, R.string.notifications, R.color.subtitle_teal)
        // Settings
        setupOption(binding.optSettings, R.drawable.ic_settings, R.string.settings_title, R.color.accent_yellow)
        // Privacy
        setupOption(binding.optPrivacy, R.drawable.ic_list, R.string.privacy_policy, R.color.accent_yellow)
    }

    private fun setupOption(itemBinding: ItemProfileOptionBinding, iconRes: Int, textRes: Int, colorRes: Int) {
        itemBinding.ivIcon.setImageResource(iconRes)
        itemBinding.ivIcon.setColorFilter(resources.getColor(colorRes, null))
        itemBinding.tvOptionText.setText(textRes)
        itemBinding.iconContainer.backgroundTintList = resources.getColorStateList(colorRes, null).withAlpha(32)

        itemBinding.root.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            showSimpleDialog(getString(textRes))
        }
    }

    private fun setupListeners() {
        binding.btnEditAvatar.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            showSimpleDialog("Editar Avatar")
        }

        binding.btnSupport.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            showSimpleDialog("Centro de Ayuda")
        }

        binding.btnLogout.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            logout()
        }
    }

    private fun setupAnimations() {
        binding.header.alpha = 0f
        binding.header.translationY = 50f
        binding.avatarContainer.scaleX = 0.8f
        binding.avatarContainer.scaleY = 0.8f
        binding.statsContainer.alpha = 0f
        binding.content.alpha = 0f

        binding.header.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(800)
            .withEndAction {
                binding.avatarContainer.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(500)
                    .start()
                
                binding.statsContainer.animate()
                    .alpha(1f)
                    .setDuration(600)
                    .start()
            }
            .start()

        binding.content.animate()
            .alpha(1f)
            .setDuration(800)
            .setStartDelay(400)
            .start()
    }

    private fun showSimpleDialog(title: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage("¿Deseas continuar?")
            .setPositiveButton("Aceptar", null)
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun logout() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
