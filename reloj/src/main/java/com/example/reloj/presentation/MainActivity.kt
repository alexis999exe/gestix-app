package com.example.reloj.presentation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.example.reloj.data.model.Ticket
import com.example.reloj.presentation.theme.GesticksTheme
import com.example.reloj.presentation.viewmodel.TicketViewModel
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import java.util.Locale

class MainActivity : ComponentActivity(), DataClient.OnDataChangedListener {
    private lateinit var viewModel: TicketViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        setContent {
            viewModel = viewModel()
            checkExistingData()
            GesticksWatchApp(viewModel)
        }
    }

    override fun onResume() {
        super.onResume()
        Wearable.getDataClient(this).addListener(this)
    }

    override fun onPause() {
        super.onPause()
        Wearable.getDataClient(this).removeListener(this)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/tickets") {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val token = dataMap.getString("auth_token")
                val userId = dataMap.getInt("user_id", -1)
                
                if (userId != -1) viewModel.setUserId(userId)
                if (token != null) viewModel.setToken(token)
                
                val ticketsJson = dataMap.getString("tickets_json")
                if (ticketsJson != null && viewModel.isLoggedIn) {
                    viewModel.updateTicketsFromJson(ticketsJson)
                } else if (viewModel.isLoggedIn) {
                    viewModel.fetchTickets()
                }
            }
        }
    }

    private fun checkExistingData() {
        Wearable.getDataClient(this).dataItems.addOnSuccessListener { dataItems ->
            dataItems.forEach { item ->
                if (item.uri.path == "/tickets") {
                    val dataMap = DataMapItem.fromDataItem(item).dataMap
                    val token = dataMap.getString("auth_token")
                    val userId = dataMap.getInt("user_id", -1)
                    
                    if (userId != -1) viewModel.setUserId(userId)
                    if (token != null) viewModel.setToken(token)
                    
                    val ticketsJson = dataMap.getString("tickets_json")
                    if (ticketsJson != null) viewModel.updateTicketsFromJson(ticketsJson)
                }
            }
            dataItems.release()
        }
    }

    private fun createNotificationChannel() {
        val name = "Ticket Notifications"
        val channel = NotificationChannel("TICKETS", name, NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

@Composable
fun GesticksWatchApp(viewModel: TicketViewModel) {
    var currentScreen by remember { mutableStateOf("waiting") }
    var selectedTicket by remember { mutableStateOf<Ticket?>(null) }
    val context = LocalContext.current

    LaunchedEffect(viewModel.isLoggedIn) {
        if (viewModel.isLoggedIn) {
            currentScreen = "list"
            viewModel.fetchTickets()
        } else {
            currentScreen = "waiting"
        }
    }

    LaunchedEffect(viewModel.tickets) {
        if (viewModel.tickets.isNotEmpty()) {
            val hasCritical = viewModel.tickets.any { it.priority.lowercase() == "critica" }
            if (hasCritical) {
                showTicketNotification(context, viewModel.tickets.first())
                triggerHapticFeedback(context, "critica")
            }
        }
    }

    GesticksTheme {
        AppScaffold {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                when (currentScreen) {
                    "waiting" -> WaitingScreen()
                    "list" -> TicketListScreen(
                        viewModel = viewModel,
                        onTicketClick = {
                            selectedTicket = it
                            currentScreen = "detail"
                        }
                    )
                    "detail" -> TicketDetailScreen(
                        ticket = selectedTicket!!,
                        onBack = { currentScreen = "list" }
                    ) { id, status ->
                        viewModel.updateStatus(id, status)
                        currentScreen = "list"
                    }
                }
            }
        }
    }
}

@Composable
fun WaitingScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(60.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Esperando Sesión",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Inicia sesión en tu celular para sincronizar",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun TicketListScreen(viewModel: TicketViewModel, onTicketClick: (Ticket) -> Unit) {
    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()

    ScreenScaffold(scrollState = listState) { contentPadding ->
        TransformingLazyColumn(
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding(),
                bottom = 40.dp,
                start = 8.dp,
                end = 8.dp
            ),
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Gestix",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Box(modifier = Modifier.height(2.dp).width(30.dp).background(MaterialTheme.colorScheme.secondary, CircleShape))
                }
            }
            
            if (viewModel.error == "Sin conexión") {
                item {
                    Card(
                        onClick = {},
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF422006)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Text("Modo Sin Conexión", style = MaterialTheme.typography.labelSmall, color = Color.Yellow)
                    }
                }
            }

            items(viewModel.tickets.size) { index ->
                val ticket = viewModel.tickets[index]
                TicketItem(ticket, transformationSpec, onTicketClick)
            }
            
            if (viewModel.tickets.isEmpty() && !viewModel.isLoading) {
                item {
                    Text(
                        "No tienes tickets\npendientes",
                        modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun TransformingLazyColumnItemScope.TicketItem(
    ticket: Ticket,
    transformationSpec: androidx.wear.compose.material3.lazy.TransformationSpec,
    onTicketClick: (Ticket) -> Unit
) {
    val priorityColor = when (ticket.priority.lowercase()) {
        "critica" -> Color(0xFFFF6B6B)
        "alta" -> Color(0xFFFFD100)
        else -> Color(0xFF2DC4D4)
    }

    Card(
        onClick = { onTicketClick(ticket) },
        modifier = Modifier
            .fillMaxWidth()
            .transformedHeight(this, transformationSpec)
            .padding(bottom = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "#${ticket.id}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(priorityColor, CircleShape)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                ticket.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                ticket.status.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun TicketDetailScreen(ticket: Ticket, onBack: () -> Unit, onStatusUpdate: (Int, String) -> Unit) {
    val scrollState = rememberTransformingLazyColumnState()
    
    ScreenScaffold(scrollState = scrollState) { contentPadding ->
        TransformingLazyColumn(
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding(),
                bottom = 40.dp,
                start = 12.dp,
                end = 12.dp
            ),
            state = scrollState,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "#${ticket.id}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        ticket.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val dotColor = when(ticket.priority.lowercase()) {
                            "critica" -> Color(0xFFFF6B6B)
                            "alta" -> Color(0xFFFFD100)
                            else -> Color(0xFF2DC4D4)
                        }
                        Box(modifier = Modifier.size(6.dp).background(dotColor, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            ticket.priority.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                Text(
                    "ACTUALIZAR ESTADO",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            item {
                StatusButton("En Progreso", Color(0xFFFFD100)) { onStatusUpdate(ticket.id, "en progreso") }
            }
            item {
                StatusButton("En Espera", Color(0xFF1B3A5C)) { onStatusUpdate(ticket.id, "en espera") }
            }
            item {
                StatusButton("Resuelto", Color(0xFF2DC4D4)) { onStatusUpdate(ticket.id, "resuelto") }
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("VOLVER", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun StatusButton(label: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.1f),
            contentColor = if (color == Color(0xFF1B3A5C)) Color.White else color
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

fun showTicketNotification(context: Context, ticket: Ticket) {
    val builder = NotificationCompat.Builder(context, "TICKETS")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Gestix: Nuevo Ticket")
        .setContentText("Folio #${ticket.id} - ${ticket.priority}")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
    
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(ticket.id, builder.build())
}

fun triggerHapticFeedback(context: Context, priority: String) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (vibrator.hasVibrator()) {
        when (priority.lowercase()) {
            "critica" -> {
                val pattern = longArrayOf(0, 200, 100, 200)
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            }
            else -> {
                vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        }
    }
}
