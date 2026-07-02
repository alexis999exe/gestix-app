package com.example.gesticks.ui.adapter

import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.gesticks.R
import com.example.gesticks.data.model.Ticket
import com.example.gesticks.databinding.ItemTicketBinding

class TicketAdapter(private val tickets: List<Ticket>) : RecyclerView.Adapter<TicketAdapter.TicketViewHolder>() {

    class TicketViewHolder(val binding: ItemTicketBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val binding = ItemTicketBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TicketViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        val ticket = tickets[position]
        val context = holder.itemView.context

        with(holder.binding) {
            tvTicketId.text = "#${ticket.id}"
            tvTicketDate.text = ticket.date
            tvTicketTitle.text = ticket.title
            tvStatusBadge.text = ticket.status
            tvPriorityValue.text = ticket.priority.replaceFirstChar { it.uppercase() }

            // Status color logic
            val statusColor = when (ticket.status) {
                "En proceso" -> ContextCompat.getColor(context, R.color.status_in_progress)
                "Abierto" -> ContextCompat.getColor(context, R.color.status_open)
                "Cerrado" -> ContextCompat.getColor(context, R.color.status_closed)
                else -> ContextCompat.getColor(context, R.color.status_closed)
            }

            vStatusBg.background.setTint(statusColor)
            tvPriorityValue.setTextColor(statusColor)

            // Priority icon logic
            val iconRes = when (ticket.priority.lowercase()) {
                "crítica" -> R.drawable.ic_alert_circle
                "alta" -> R.drawable.ic_warning
                "media" -> R.drawable.ic_info_circle
                else -> R.drawable.ic_check_circle
            }
            ivPriorityIcon.setImageResource(iconRes)
            ivPriorityIcon.setColorFilter(statusColor)

            // Entrada Animada Premium (Rotación 3D + Slide)
            holder.itemView.alpha = 0f
            holder.itemView.translationX = -100f
            holder.itemView.rotationY = -15f
            
            holder.itemView.animate()
                .alpha(1f)
                .translationX(0f)
                .rotationY(0f)
                .setDuration(600)
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()

            holder.itemView.setOnLongClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                
                // 1. Fase de Vibración
                val shake = AnimationUtils.loadAnimation(context, R.anim.shake_ticket)
                it.startAnimation(shake)
                
                it.postDelayed({
                    // 2. Aparece el Sello con Impacto
                    tvCompletedStamp.visibility = android.view.View.VISIBLE
                    tvCompletedStamp.alpha = 0f
                    tvCompletedStamp.scaleX = 5f
                    tvCompletedStamp.scaleY = 5f
                    
                    tvCompletedStamp.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200)
                        .withEndAction {
                            it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            
                            // 3. Fase de Ruptura y Caída
                            it.animate()
                                .translationY(2000f) // Cae al vacío
                                .rotation(35f)      // Rota mientras cae
                                .scaleX(0.7f)
                                .scaleY(0.7f)
                                .alpha(0f)
                                .setDuration(800)
                                .setInterpolator(android.view.animation.AccelerateInterpolator())
                                .withEndAction {
                                    // Aquí se eliminaría de la lista real en producción
                                    it.visibility = android.view.View.GONE
                                }
                                .start()
                        }
                        .start()
                }, 500)
                true
            }

            holder.itemView.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                // Animación de pulso
                it.animate()
                    .scaleX(0.96f)
                    .scaleY(0.96f)
                    .setDuration(100)
                    .withEndAction {
                        it.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                    }
                    .start()
            }
        }
    }

    override fun getItemCount() = tickets.size
}
