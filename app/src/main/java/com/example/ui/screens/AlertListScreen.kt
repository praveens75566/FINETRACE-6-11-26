package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Alert
import com.example.data.model.SymbolInfo
import com.example.data.model.formatPriceDynamic
import com.example.data.model.getDisplayDecimals
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertListScreen(
    viewModel: MainViewModel
) {
    val alerts by viewModel.alertList.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingAlert by remember { mutableStateOf<Alert?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // All, Active, Triggered

    val totalAlerts = alerts.size
    val activeAlerts = alerts.count { it.isActive }
    val triggeredAlerts = totalAlerts - activeAlerts

    val isLight = (MaterialTheme.colorScheme.background.red + MaterialTheme.colorScheme.background.green + MaterialTheme.colorScheme.background.blue) > 1.5f

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(14.dp),
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Alert", modifier = Modifier.size(24.dp))
            }
        },
        bottomBar = {
            if (alerts.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    tonalElevation = 8.dp,
                    color = if (isLight) Color.White.copy(alpha = 0.95f) else Color(0xFF0C0F12).copy(alpha = 0.95f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "BATCH ACTIONS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.75.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "Manage $totalAlerts triggers",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = { viewModel.activateAllAlerts() },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Resume All", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }

                            TextButton(
                                onClick = { viewModel.deactivateAllAlerts() },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pause All", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }

                            IconButton(
                                onClick = { viewModel.deleteAllAlerts() },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(AlertCritical.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteForever,
                                    contentDescription = "Delete All Alerts",
                                    tint = AlertCritical,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Interactive visual telemetry heads-up pane
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusHudItem(
                    title = "Total Rules",
                    value = totalAlerts.toString(),
                    icon = Icons.Default.Notifications,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatusHudItem(
                    title = "Active Watch",
                    value = activeAlerts.toString(),
                    icon = Icons.Default.PlayArrow,
                    tint = AlertActive,
                    modifier = Modifier.weight(1f)
                )
                StatusHudItem(
                    title = "Paused / Logged",
                    value = triggeredAlerts.toString(),
                    icon = Icons.Default.Pause,
                    tint = AlertExpired,
                    modifier = Modifier.weight(1f)
                )
            }

            // Elegant search card with soft overlay container
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Alerts by Symbol...", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (isLight) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f) else Color(0xFF222831),
                    unfocusedContainerColor = if (isLight) Color(0xFFF8F9FA) else Color(0xFF0F1216),
                    focusedContainerColor = if (isLight) Color.White else Color(0xFF0C0E11)
                ),
                singleLine = true
            )

            // Distinct filter row using premium stylized capsule chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Active", "Triggered").forEach { filter ->
                    val isSelected = selectedFilter == filter
                    val containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        if (isLight) Color(0xFFF1F3F5) else Color(0xFF14171B)
                    }
                    val labelColor = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    }
                    val borderStroke = if (isSelected) {
                        null
                    } else {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    }
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(containerColor)
                            .then(if (borderStroke != null) Modifier.border(borderStroke, RoundedCornerShape(10.dp)) else Modifier)
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filter.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = labelColor
                        )
                    }
                }
            }

            // Filter process variables
            val filteredAlerts = alerts.filter { alert ->
                val matchesSearch = alert.symbol.contains(searchQuery, ignoreCase = true) ||
                        alert.title.contains(searchQuery, ignoreCase = true)
                val matchesFilter = when (selectedFilter) {
                    "Active" -> alert.isActive
                    "Triggered" -> !alert.isActive
                    else -> true
                }
                matchesSearch && matchesFilter
            }

            if (filteredAlerts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(40.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsNone,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "No Alert Rules Found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty() || selectedFilter != "All") 
                                "Try adjusting your filter keywords or checking different status categories."
                                else "Define live crossing/threshold triggers to monitor target indices in real-time.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        if (alerts.isEmpty()) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { showCreateDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Create First Alert", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredAlerts, key = { it.id }) { alert ->
                        AlertRuleItem(
                            alert = alert,
                            onToggleActive = { viewModel.toggleAlertActive(alert.id, it) },
                            onDelete = { viewModel.deleteAlert(alert.id) },
                            onEditClick = { editingAlert = alert }
                        )
                    }
                }
            }
        }
    }

    // Modal creation dialog sheet
    if (showCreateDialog) {
        CreateAlertDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { symbol, cond, price, isOneTime, priority, msg ->
                viewModel.createAlert(
                    symbol = symbol,
                    condition = cond,
                    targetPrice = price,
                    title = "$symbol crossed target",
                    message = msg.ifBlank { "Crossing detected. Price exceeded $price threshold." },
                    isOneTime = isOneTime,
                    priority = priority,
                    colorTagIndex = 0
                )
                showCreateDialog = false
            }
        )
    }

    // Modal edit dialog sheet
    if (editingAlert != null) {
        EditAlertDialog(
            alert = editingAlert!!,
            onDismiss = { editingAlert = null },
            onUpdate = { id, symbol, cond, price, isOneTime, priority, msg ->
                viewModel.updateAlert(
                    id = id,
                    symbol = symbol,
                    condition = cond,
                    targetPrice = price,
                    title = "$symbol crossed target",
                    message = msg.ifBlank { "Crossing detected. Price exceeded $price threshold." },
                    isActive = editingAlert!!.isActive,
                    isOneTime = isOneTime,
                    priority = priority
                )
                editingAlert = null
            }
        )
    }
}

@Composable
fun StatusHudItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val isLight = (MaterialTheme.colorScheme.background.red + MaterialTheme.colorScheme.background.green + MaterialTheme.colorScheme.background.blue) > 1.5f
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLight) Color.White else Color(0xFF101317)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(tint.copy(alpha = 0.12f))
                    .border(1.dp, tint.copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(16.dp)
                )
            }
            Column {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = PriceTextFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun AlertRuleItem(
    alert: Alert,
    onToggleActive: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onEditClick: () -> Unit
) {
    val info = SymbolInfo.find(alert.symbol)
    val formattedPrice = alert.targetPrice.formatPriceDynamic(info.getDisplayDecimals())

    val condLabel = when (alert.condition) {
        "CROSSING_UP" -> "UPWARD CROSSING"
        "CROSSING_DOWN" -> "DOWNWARD CROSSING"
        else -> "ANY CROSSING"
    }

    val condIcon = when (alert.condition) {
        "CROSSING_UP" -> Icons.Default.TrendingUp
        "CROSSING_DOWN" -> Icons.Default.TrendingDown
        else -> Icons.Default.CompareArrows
    }

    val themeColor = when (alert.priority) {
        "LOW" -> AlertExpired
        "MEDIUM" -> AlertTriggered
        "HIGH" -> AlertActive
        "CRITICAL" -> AlertCritical
        else -> AlertActive
    }

    val isLight = (MaterialTheme.colorScheme.background.red + MaterialTheme.colorScheme.background.green + MaterialTheme.colorScheme.background.blue) > 1.5f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable { onEditClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (alert.isActive) {
                if (isLight) Color.White else Color(0xFF0F1115)
            } else {
                if (isLight) Color(0xFFF1F3F5) else Color(0xFF08090C)
            }
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (alert.isActive) 3.dp else 1.dp
        ),
        border = BorderStroke(
            width = if (alert.isActive && alert.priority == "CRITICAL") 1.5.dp else 1.dp,
            color = if (alert.isActive) {
                if (alert.priority == "CRITICAL") AlertCritical.copy(alpha = 0.8f)
                else themeColor.copy(alpha = 0.25f)
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min) // Critical: allows the colored left stripe to fill card height perfectly
        ) {
            // Priority vertical stripe on the far left of the card
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(
                        if (alert.isActive) themeColor else themeColor.copy(alpha = 0.3f)
                    )
            )

            // Main Content Area inside the card
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                // Top Row: Asset Icon details + Toggle/Delete Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Polished priority coin bubble
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (alert.isActive) themeColor.copy(alpha = 0.12f)
                                    else themeColor.copy(alpha = 0.05f)
                                )
                                .border(
                                    1.dp, 
                                    if (alert.isActive) themeColor.copy(alpha = 0.35f) else themeColor.copy(alpha = 0.15f), 
                                    RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (info.symbol.length >= 2) info.symbol.take(2).uppercase() else info.symbol,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                ),
                                color = if (alert.isActive) themeColor else themeColor.copy(alpha = 0.5f)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = alert.symbol,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = if (alert.isActive) MaterialTheme.colorScheme.onSurface 
                                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                                )
                                // Smart Compact Priority Label Pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(30.dp))
                                        .background(
                                            if (alert.isActive) themeColor.copy(alpha = 0.15f)
                                            else themeColor.copy(alpha = 0.05f)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = alert.priority,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = if (alert.isActive) themeColor else themeColor.copy(alpha = 0.5f)
                                    )
                                }
                            }
                            Text(
                                text = info.name,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Toggles & Admin Sweep Options
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Switch(
                            checked = alert.isActive,
                            onCheckedChange = { onToggleActive(it) },
                            modifier = Modifier.scale(0.82f),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = themeColor,
                                checkedTrackColor = themeColor.copy(alpha = 0.35f),
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier
                                .size(34.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    RoundedCornerShape(8.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Alert",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(34.dp)
                                .background(
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                                    RoundedCornerShape(8.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Alert",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Row: Directions & Precise Numeric limits
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (alert.isActive) themeColor.copy(alpha = 0.06f)
                                    else themeColor.copy(alpha = 0.02f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = condIcon,
                                contentDescription = null,
                                tint = if (alert.isActive) themeColor else themeColor.copy(alpha = 0.4f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = condLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.75.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                            )
                            Text(
                                text = if (alert.isOneTime) "One-time dispatch" else "Continuous stream",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "THRESHOLD LIMIT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                letterSpacing = 0.75.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "$$formattedPrice",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = PriceTextFontFamily,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            ),
                            color = if (alert.isActive) themeColor else themeColor.copy(alpha = 0.5f)
                        )
                    }
                }

                // Inline Customized Memo Note If Defined
                if (alert.message.isNotBlank() && !alert.message.startsWith("Crossing detected.")) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isLight) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                else Color(0xFF13161B).copy(alpha = 0.7f)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null,
                            tint = if (alert.isActive) themeColor.copy(alpha = 0.7f) else themeColor.copy(alpha = 0.3f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = alert.message,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlertDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, Double, Boolean, String, String) -> Unit
) {
    var selectedSymbol by remember { mutableStateOf(SymbolInfo.ALL.first().symbol) }
    var condition by remember { mutableStateOf("CROSSING") } // "CROSSING", "CROSSING_UP", "CROSSING_DOWN"
    var targetPriceInput by remember { mutableStateOf("") }
    var messageInput by remember { mutableStateOf("") }
    var isOneTime by remember { mutableStateOf(true) }
    var priority by remember { mutableStateOf("HIGH") } // "LOW", "MEDIUM", "HIGH", "CRITICAL"
    
    var isExpandedSymbol by remember { mutableStateOf(false) }

    // State validation helper check
    val isValidPrice = remember(targetPriceInput) {
        val parsed = targetPriceInput.toDoubleOrNull()
        parsed != null && parsed > 0.0
    }

    val isLight = (MaterialTheme.colorScheme.background.red + MaterialTheme.colorScheme.background.green + MaterialTheme.colorScheme.background.blue) > 1.5f

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val priceVal = targetPriceInput.toDoubleOrNull()
                    if (priceVal != null && priceVal > 0) {
                        onCreate(selectedSymbol, condition, priceVal, isOneTime, priority, messageInput)
                    }
                },
                enabled = isValidPrice,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
            ) {
                Text("Create Alert", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    "New Analytics Trigger",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Dropdown asset picker
                ExposedDropdownMenuBox(
                    expanded = isExpandedSymbol,
                    onExpandedChange = { isExpandedSymbol = !isExpandedSymbol }
                ) {
                    OutlinedTextField(
                        value = selectedSymbol,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Selected Asset") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpandedSymbol) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = if (isLight) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = isExpandedSymbol,
                        onDismissRequest = { isExpandedSymbol = false }
                    ) {
                        SymbolInfo.ALL.forEach { s ->
                            DropdownMenuItem(
                                text = { Text("${s.symbol} — ${s.name}") },
                                onClick = {
                                    selectedSymbol = s.symbol
                                    isExpandedSymbol = false
                                }
                            )
                        }
                    }
                }

                // Inline Condition Segmented Row
                Column {
                    Text(
                        "Trigger Condition",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("CROSSING", "CROSSING_UP", "CROSSING_DOWN").forEach { cond ->
                            val isSel = condition == cond
                            val activeSelectionColor = MaterialTheme.colorScheme.primary
                            val inactiveSelectionColor = if (isLight) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) activeSelectionColor else inactiveSelectionColor)
                                    .clickable { condition = cond }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cond.replace("CROSSING_", "").replace("CROSSING", "BOTH"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Numeric input field with instant validation feedback highlight
                OutlinedTextField(
                    value = targetPriceInput,
                    onValueChange = { targetPriceInput = it },
                    label = { Text("Target Threshold Price") },
                    placeholder = { Text("e.g. 2318.50") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    isError = targetPriceInput.isNotEmpty() && !isValidPrice,
                    supportingText = {
                        if (targetPriceInput.isNotEmpty() && !isValidPrice) {
                            Text(
                                "Invalid numeric format. Ensure a clean format (e.g. 1.08250)",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                )

                // Optional Memo Note
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    label = { Text("Alert Message (Optional)") },
                    placeholder = { Text("e.g. Resistance level buy limit reached") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = false,
                    maxLines = 2
                )

                // Inline Priority selection Pill rows
                Column {
                    Text(
                        "Priority Rank",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("LOW", "MEDIUM", "HIGH", "CRITICAL").forEach { prio ->
                            val isSel = priority == prio
                            val toneColor = when (prio) {
                                "LOW" -> AlertExpired
                                "MEDIUM" -> AlertTriggered
                                "HIGH" -> AlertActive
                                "CRITICAL" -> AlertCritical
                                else -> AlertActive
                            }
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSel) toneColor else {
                                            if (isLight) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                        }
                                    )
                                    .clickable { priority = prio }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = prio,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // One-time execution option Switch inside neat bubble card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isLight) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "One-Time Execution",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Auto-disables the target immediately after firing",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    Switch(
                        checked = isOneTime,
                        onCheckedChange = { isOneTime = it },
                        modifier = Modifier.scale(0.85f)
                    )
                }
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAlertDialog(
    alert: Alert,
    onDismiss: () -> Unit,
    onUpdate: (id: Int, symbol: String, condition: String, price: Double, isOneTime: Boolean, priority: String, message: String) -> Unit
) {
    var selectedSymbol by remember { mutableStateOf(alert.symbol) }
    var condition by remember { mutableStateOf(alert.condition) } // "CROSSING", "CROSSING_UP", "CROSSING_DOWN"
    var targetPriceInput by remember { mutableStateOf(alert.targetPrice.toString()) }
    var messageInput by remember { mutableStateOf(alert.message) }
    var isOneTime by remember { mutableStateOf(alert.isOneTime) }
    var priority by remember { mutableStateOf(alert.priority) } // "LOW", "MEDIUM", "HIGH", "CRITICAL"
    
    var isExpandedSymbol by remember { mutableStateOf(false) }

    // State validation helper check
    val isValidPrice = remember(targetPriceInput) {
        val parsed = targetPriceInput.toDoubleOrNull()
        parsed != null && parsed > 0.0
    }

    val isLight = (MaterialTheme.colorScheme.background.red + MaterialTheme.colorScheme.background.green + MaterialTheme.colorScheme.background.blue) > 1.5f

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val priceVal = targetPriceInput.toDoubleOrNull()
                    if (priceVal != null && priceVal > 0) {
                        onUpdate(alert.id, selectedSymbol, condition, priceVal, isOneTime, priority, messageInput)
                    }
                },
                enabled = isValidPrice,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    "Edit Asset Trigger",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Dropdown asset picker
                ExposedDropdownMenuBox(
                    expanded = isExpandedSymbol,
                    onExpandedChange = { isExpandedSymbol = !isExpandedSymbol }
                ) {
                    OutlinedTextField(
                        value = selectedSymbol,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Selected Asset") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpandedSymbol) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = if (isLight) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = isExpandedSymbol,
                        onDismissRequest = { isExpandedSymbol = false }
                    ) {
                        SymbolInfo.ALL.forEach { s ->
                            DropdownMenuItem(
                                text = { Text("${s.symbol} — ${s.name}") },
                                onClick = {
                                    selectedSymbol = s.symbol
                                    isExpandedSymbol = false
                                }
                            )
                        }
                    }
                }

                // Inline Condition Segmented Row
                Column {
                    Text(
                        "Trigger Condition",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("CROSSING", "CROSSING_UP", "CROSSING_DOWN").forEach { cond ->
                            val isSel = condition == cond
                            val activeSelectionColor = MaterialTheme.colorScheme.primary
                            val inactiveSelectionColor = if (isLight) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) activeSelectionColor else inactiveSelectionColor)
                                    .clickable { condition = cond }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cond.replace("CROSSING_", "").replace("CROSSING", "BOTH"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Numeric input field with instant validation feedback highlight
                OutlinedTextField(
                    value = targetPriceInput,
                    onValueChange = { targetPriceInput = it },
                    label = { Text("Target Threshold Price") },
                    placeholder = { Text("e.g. 2318.50") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    isError = targetPriceInput.isNotEmpty() && !isValidPrice,
                    supportingText = {
                        if (targetPriceInput.isNotEmpty() && !isValidPrice) {
                            Text(
                                "Invalid numeric format. Ensure a clean format (e.g. 1.08250)",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                )

                // Optional Memo Note
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    label = { Text("Alert Message (Optional)") },
                    placeholder = { Text("e.g. Resistance level buy limit reached") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = false,
                    maxLines = 2
                )

                // Inline Priority selection Pill rows
                Column {
                    Text(
                        "Priority Rank",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("LOW", "MEDIUM", "HIGH", "CRITICAL").forEach { prio ->
                            val isSel = priority == prio
                            val toneColor = when (prio) {
                                "LOW" -> AlertExpired
                                "MEDIUM" -> AlertTriggered
                                "HIGH" -> AlertActive
                                "CRITICAL" -> AlertCritical
                                else -> AlertActive
                            }
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSel) toneColor else {
                                            if (isLight) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                        }
                                    )
                                    .clickable { priority = prio }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = prio,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // One-time execution option Switch inside neat bubble card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isLight) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "One-Time Execution",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Auto-disables the target immediately after firing",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    Switch(
                        checked = isOneTime,
                        onCheckedChange = { isOneTime = it },
                        modifier = Modifier.scale(0.85f)
                    )
                }
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

