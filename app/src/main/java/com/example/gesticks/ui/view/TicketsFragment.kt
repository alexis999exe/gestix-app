package com.example.gesticks.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gesticks.databinding.FragmentTicketsBinding
import com.example.gesticks.ui.adapter.TicketAdapter
import com.example.gesticks.ui.viewmodel.TicketsViewModel

class TicketsFragment : Fragment() {

    private var _binding: FragmentTicketsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TicketsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTicketsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupAnimations()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.rvTickets.layoutManager = LinearLayoutManager(context)
    }

    private fun setupAnimations() {
        binding.header.alpha = 0f
        binding.header.translationY = 30f
        
        binding.header.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(800)
            .start()
    }

    private fun observeViewModel() {
        viewModel.tickets.observe(viewLifecycleOwner) { tickets ->
            binding.rvTickets.adapter = TicketAdapter(tickets)
        }
        viewModel.activeCount.observe(viewLifecycleOwner) { count ->
            ((binding.llStats.getChildAt(0) as ViewGroup).getChildAt(0) as android.widget.TextView).text = count.toString()
        }
        viewModel.resolvedCount.observe(viewLifecycleOwner) { count ->
            ((binding.llStats.getChildAt(2) as ViewGroup).getChildAt(0) as android.widget.TextView).text = count.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
