package com.example.gesticks.ui.adapter

import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.gesticks.R
import com.example.gesticks.data.model.Ticket
import com.example.gesticks.databinding.ItemTicketBinding

class TicketAdapter(private val tickets: List<Ticket>) : RecyclerView.Adapter<TicketAdapter.TicketViewHolder>() {

    class TicketViewHolder(val binding: ItemTicketBinding) : RecyclerView.ViewHolder(binding.root) {
        var isFlipped = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val binding = ItemTicketBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TicketViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        val ticket = tickets[position]
        val context = holder.itemView.context

        with(holder.binding) {
            // Front Data
            tvTicketId.text = "#${ticket.id}"
            tvTicketDate.text = ticket.date
            tvTicketTitle.text = ticket.title
            tvStatusBadge.text = ticket.status.replaceFirstChar { it.uppercase() }
            tvPriorityValue.text = ticket.priority.replaceFirstChar { it.uppercase() }

            // Back Data
            tvTicketDescription.text = ticket.description

            // Status color logic
            val statusColor = when (ticket.status.lowercase()) {
                "pendiente" -> ContextCompat.getColor(context, R.color.status_in_progress)
                "abierto" -> ContextCompat.getColor(context, R.color.status_open)
                "resuelto" -> ContextCompat.getColor(context, R.color.status_closed)
                else -> ContextCompat.getColor(context, R.color.status_closed)
            }

            vStatusBg.background.setTint(statusColor)
            tvPriorityValue.setTextColor(statusColor)

            // Priority icon logic
            val iconRes = when (ticket.priority.lowercase()) {
                "critica" -> R.drawable.ic_alert_circle
                "alta" -> R.drawable.ic_warning
                "media" -> R.drawable.ic_info_circle
                else -> R.drawable.ic_check_circle
            }
            ivPriorityIcon.setImageResource(iconRes)
            ivPriorityIcon.setColorFilter(statusColor)

            // Reset state
            holder.isFlipped = false
            frontLayout.visibility = View.VISIBLE
            backLayout.visibility = View.GONE
            cardTicket.rotationY = 0f

            // Click listener for 3D Flip
            holder.itemView.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                flipCard(holder)
            }
            
            // Initial animation
            holder.itemView.alpha = 0f
            holder.itemView.translationY = 50f
            holder.itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(position * 50L)
                .start()
        }
    }

    private fun flipCard(holder: TicketViewHolder) {
        val binding = holder.binding
        val card = binding.cardTicket
        
        card.animate()
            .rotationY(90f)
            .setDuration(400)
            .withEndAction {
                if (holder.isFlipped) {
                    binding.frontLayout.visibility = View.VISIBLE
                    binding.backLayout.visibility = View.GONE
                    holder.isFlipped = false
                } else {
                    binding.frontLayout.visibility = View.GONE
                    binding.backLayout.visibility = View.VISIBLE
                    holder.isFlipped = true
                }
                
                card.rotationY = -90f
                card.animate()
                    .rotationY(0f)
                    .setDuration(400)
                    .start()
            }
            .start()
    }

    override fun getItemCount() = tickets.size
}
