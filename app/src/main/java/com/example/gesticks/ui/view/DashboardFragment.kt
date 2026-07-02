package com.example.gesticks.ui.view

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.gesticks.databinding.FragmentDashboardBinding
import com.example.gesticks.ui.viewmodel.DashboardViewModel

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupAnimations()
        setupListeners()
        observeViewModel()
        
        viewModel.loadData()
    }

    private fun setupAnimations() {
        // Initial states
        binding.header.alpha = 0f
        binding.header.translationY = 30f
        binding.tvSummaryTitle.alpha = 0f
        binding.tvSummaryTitle.translationX = 30f
        
        binding.cardOpen.alpha = 0f
        binding.cardOpen.translationY = 30f
        binding.cardProcess.alpha = 0f
        binding.cardProcess.translationY = 30f
        binding.cardResolved.alpha = 0f
        binding.cardResolved.translationY = 30f

        // Entrance sequence
        binding.header.animate().alpha(1f).translationY(0f).setDuration(600).start()
        
        binding.tvSummaryTitle.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(600)
            .setStartDelay(200)
            .start()

        binding.cardOpen.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(350).start()
        binding.cardProcess.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(500).start()
        binding.cardResolved.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(650).start()
    }

    private fun setupListeners() {
        binding.btnNotifications.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }

    private fun observeViewModel() {
        viewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.tvGreeting.text = "Hola, $name"
        }
        viewModel.countOpen.observe(viewLifecycleOwner) { count ->
            animateCounter(binding.tvCountOpen, count)
        }
        viewModel.countProcess.observe(viewLifecycleOwner) { count ->
            animateCounter(binding.tvCountProcess, count)
        }
        viewModel.countResolved.observe(viewLifecycleOwner) { count ->
            animateCounter(binding.tvCountResolved, count)
        }
    }

    private fun animateCounter(textView: TextView, targetValue: Int) {
        val animator = ValueAnimator.ofInt(0, targetValue)
        animator.duration = 1500
        animator.startDelay = 800
        animator.addUpdateListener { animation ->
            textView.text = animation.animatedValue.toString()
        }
        animator.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
