package com.example.reloj.presentation

import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.example.reloj.presentation.theme.GesticksTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GesticksWatchApp()
        }
    }
}

data class WatchTicket(val id: String, val title: String, val priority: String, val isResolved: Boolean = false)

@Composable
fun GesticksWatchApp() {
    GesticksTheme {
        AppScaffold {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            val scope = rememberCoroutineScope()
            val view = LocalView.current
            
            var tickets by remember { 
                mutableStateOf(listOf(
                    WatchTicket("1", "Error Servidor", "Crítica"),
                    WatchTicket("2", "Ajuste UI", "Media"),
                    WatchTicket("3", "Bug Login", "Alta"),
                    WatchTicket("4", "Wifi Caído", "Crítica")
                ))
            }

            ScreenScaffold(scrollState = listState) { contentPadding ->
                TransformingLazyColumn(
                    contentPadding = contentPadding,
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Gestix",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            CircularProgressIndicator(
                                progress = { (tickets.count { it.isResolved }.toFloat() / tickets.size.toFloat()) },
                                modifier = Modifier.size(40.dp).padding(4.dp),
                                colors = ProgressIndicatorDefaults.colors(
                                    trackColor = MaterialTheme.colorScheme.surfaceContainer
                                ),
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = "Pendientes: ${tickets.count { !it.isResolved }}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    items(tickets.size) { index ->
                        val ticket = tickets[index]
                        
                        // Animación de desaparición
                        AnimatedVisibility(
                            visible = !ticket.isResolved,
                            exit = shrinkVertically(animationSpec = tween(500)) + fadeOut() + slideOutHorizontally()
                        ) {
                            TicketCard(
                                ticket = ticket,
                                transformationSpec = transformationSpec,
                                onResolve = {
                                    // Haptics personalizados por prioridad
                                    triggerHapticFeedback(view.context, ticket.priority)
                                    
                                    scope.launch {
                                        delay(500) // Tiempo para ver la animación
                                        tickets = tickets.toMutableList().also {
                                            it[index] = it[index].copy(isResolved = true)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

fun triggerHapticFeedback(context: Context, priority: String) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (vibrator.hasVibrator()) {
        when (priority) {
            "Crítica" -> {
                // Doble pulso fuerte
                val pattern = longArrayOf(0, 100, 50, 100)
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            }
            "Alta" -> {
                // Pulso largo
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            }
            else -> {
                // Pulso corto
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        }
    }
}

@Composable
fun TransformingLazyColumnItemScope.TicketCard(
    ticket: WatchTicket,
    transformationSpec: androidx.wear.compose.material3.lazy.TransformationSpec,
    onResolve: () -> Unit
) {
    Card(
        onClick = onResolve,
        modifier = Modifier
            .fillMaxWidth()
            .transformedHeight(this, transformationSpec)
            .padding(bottom = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${ticket.id}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                // Punto de color de prioridad
                val dotColor = when (ticket.priority) {
                    "Crítica" -> Color(0xFFFF6B6B)
                    "Alta" -> Color(0xFFFFD100)
                    else -> Color(0xFF2DC4D4)
                }
                androidx.compose.foundation.Canvas(modifier = Modifier.size(8.dp)) {
                    drawCircle(color = dotColor)
                }
            }
            Text(
                text = ticket.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = ticket.priority,
                style = MaterialTheme.typography.bodySmall,
                color = when (ticket.priority) {
                    "Crítica" -> Color(0xFFFF6B6B)
                    "Alta" -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.secondary
                }
            )
        }
    }
}

@WearPreviewDevices
@Composable
fun WatchPreview() {
    GesticksWatchApp()
}
