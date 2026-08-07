package com.example.gesticks.ui.view

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.gesticks.R
import com.example.gesticks.databinding.DialogSuccessGifBinding
import com.example.gesticks.databinding.FragmentCreateTicketBinding
import com.example.gesticks.ui.viewmodel.CreateTicketViewModel

class CreateTicketFragment : Fragment() {

    private var _binding: FragmentCreateTicketBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CreateTicketViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateTicketBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupAnimations()
        setupListeners()
        observeViewModel()
    }

    private fun setupAnimations() {
        // Entrance animations
        binding.header.alpha = 0f
        binding.header.translationY = 50f
        binding.content.translationY = 100f

        binding.header.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(800)
            .start()

        binding.content.animate()
            .translationY(0f)
            .setDuration(1000)
            .setStartDelay(300)
            .start()
    }

    private fun setupListeners() {

        binding.btnPriorityLow.setOnClickListener { selectPriority("baja") }
        binding.btnPriorityMedium.setOnClickListener { selectPriority("media") }
        binding.btnPriorityHigh.setOnClickListener { selectPriority("alta") }
        binding.btnPriorityCritical.setOnClickListener { selectPriority("crítica") }

        binding.btnSubmit.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val description = binding.etDescription.text.toString()
            
            if (title.isNotEmpty() && description.isNotEmpty()) {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                viewModel.createTicket(title, description)
            } else {
                // Toast o error
            }
        }

        binding.btnCancel.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            // Lógica de cancelar
        }
    }

    private fun selectPriority(priority: String) {
        requireView().performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        viewModel.setPriority(priority)
    }

    private fun observeViewModel() {
        viewModel.priority.observe(viewLifecycleOwner) { priority ->
            updatePriorityUI(priority)
        }
        
        viewModel.createResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                showSuccessAnimation()
                binding.etTitle.text.clear()
                binding.etDescription.text.clear()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnSubmit.isEnabled = !isLoading
            binding.btnSubmit.alpha = if (isLoading) 0.5f else 1.0f
        }
    }

    private fun showSuccessAnimation() {
        val dialogBinding = DialogSuccessGifBinding.inflate(layoutInflater)
        // Ahora usamos el mainContainer para asegurar que el GIF esté encima de todo
        val rootView = binding.root as ViewGroup
        
        Glide.with(this)
            .asGif()
            .load(R.drawable.success_animation)
            .placeholder(R.drawable.ic_check_circle)
            .error(R.drawable.ic_check_circle)
            .into(dialogBinding.ivSuccessGif)

        rootView.addView(dialogBinding.root)

        dialogBinding.root.postDelayed({
            rootView.removeView(dialogBinding.root)
        }, 3500)
    }

    private fun updatePriorityUI(selectedPriority: String) {
        val buttons = listOf(
            binding.btnPriorityLow to "baja",
            binding.btnPriorityMedium to "media",
            binding.btnPriorityHigh to "alta",
            binding.btnPriorityCritical to "crítica"
        )

        buttons.forEach { (button, priority) ->
            val isSelected = priority == selectedPriority
            button.isSelected = isSelected
            button.setTextColor(
                if (isSelected) ContextCompat.getColor(requireContext(), R.color.primary_dark)
                else ContextCompat.getColor(requireContext(), R.color.text_grey)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
