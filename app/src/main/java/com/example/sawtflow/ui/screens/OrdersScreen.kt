package com.example.sawtflow.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sawtflow.data.model.AppLanguage
import com.example.sawtflow.data.model.Order
import com.example.sawtflow.ui.theme.AmberAccent
import com.example.sawtflow.ui.theme.CyanAccent
import com.example.sawtflow.ui.theme.GreenSuccess
import com.example.sawtflow.ui.theme.NavyCard
import com.example.sawtflow.ui.theme.NavyLight
import com.example.sawtflow.ui.theme.NavyPrimary
import com.example.sawtflow.ui.theme.NavySurface
import com.example.sawtflow.ui.viewmodel.AppTab
import com.example.sawtflow.ui.viewmodel.SawtFlowViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrdersScreen(
    viewModel: SawtFlowViewModel,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.orders.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedOrderIds by remember { mutableStateOf(setOf<String>()) }
    var selectedBatchStatus by remember { mutableStateOf("Verified") }
    var statusDropdownExpanded by remember { mutableStateOf(false) }
    var batchFeedbackMessage by remember { mutableStateOf<String?>(null) }
    val isAr = language == AppLanguage.AR

    val filterOptions = listOf("All", "Pending", "Verified", "Completed", "In transit", "Delivered", "Delayed", "Canceled", "Ready for pickup")
    val batchStatusOptions = listOf("Pending", "Verified", "Completed")

    val filteredOrders = orders.filter { order ->
        val matchesSearch = order.id.contains(searchQuery, ignoreCase = true) ||
                order.status.contains(searchQuery, ignoreCase = true) ||
                order.city.contains(searchQuery, ignoreCase = true)
        val matchesFilter = if (selectedFilter == "All") true else order.status.equals(selectedFilter, ignoreCase = true)
        matchesSearch && matchesFilter
    }
    val allFilteredSelected = filteredOrders.isNotEmpty() && filteredOrders.all { it.id in selectedOrderIds }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyPrimary)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = if (isAr) "قاعدة بيانات الطلبات / 11 سجلاً موثقاً" else "OPERATIONAL ORDERS DATABASE / 11 RECORDS",
                color = CyanAccent,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = if (isAr) "سجلات العمليات والأدلة" else "Operational Evidence Base",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = if (isAr)
                    "انقر على أي طلب للبحث فيه مباشرة في استوديو SawtFlow والتحقق من الاستجابة."
                else
                    "Tap any order to test lookup immediately in SawtFlow Studio.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp
            )
        }

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(if (isAr) "ابحث برقم الطلب (مثال: SF-1048) أو المدينة..." else "Search by Order ID (e.g. SF-1048) or city...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = CyanAccent) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_orders_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = NavySurface,
                unfocusedContainerColor = NavySurface,
                focusedBorderColor = CyanAccent,
                unfocusedBorderColor = NavyLight,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        // Filter chips
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            filterOptions.forEach { filter ->
                val isSelected = selectedFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = filter },
                    label = {
                        Text(
                            text = filter,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanAccent,
                        selectedLabelColor = NavyPrimary,
                        containerColor = NavySurface,
                        labelColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        selectedBorderColor = CyanAccent,
                        borderColor = NavyLight
                    )
                )
            }
        }

        // Batch Selection & Status Action Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("orders_batch_toolbar"),
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                selectedOrderIds = if (allFilteredSelected) {
                                    selectedOrderIds - filteredOrders.map { it.id }.toSet()
                                } else {
                                    selectedOrderIds + filteredOrders.map { it.id }.toSet()
                                }
                            }
                            .padding(end = 8.dp)
                    ) {
                        Checkbox(
                            checked = allFilteredSelected,
                            onCheckedChange = { checked ->
                                selectedOrderIds = if (checked) {
                                    selectedOrderIds + filteredOrders.map { it.id }.toSet()
                                } else {
                                    selectedOrderIds - filteredOrders.map { it.id }.toSet()
                                }
                            },
                            modifier = Modifier.testTag("select_all_orders_checkbox"),
                            colors = CheckboxDefaults.colors(
                                checkedColor = CyanAccent,
                                uncheckedColor = Color.White.copy(alpha = 0.6f),
                                checkmarkColor = NavyPrimary
                            )
                        )
                        Text(
                            text = if (isAr) "تحديد الكل" else "Select All",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (selectedOrderIds.isNotEmpty()) GreenSuccess.copy(alpha = 0.16f) else NavyCard)
                            .border(
                                1.dp,
                                if (selectedOrderIds.isNotEmpty()) GreenSuccess else NavyLight,
                                RoundedCornerShape(999.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isAr) "${selectedOrderIds.size} محدد" else "${selectedOrderIds.size} selected",
                            color = if (selectedOrderIds.isNotEmpty()) GreenSuccess else Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { statusDropdownExpanded = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("batch_status_dropdown_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = NavyCard,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = selectedBatchStatus,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = if (isAr) "اختر الحالة" else "Select Status",
                                tint = CyanAccent
                            )
                        }

                        DropdownMenu(
                            expanded = statusDropdownExpanded,
                            onDismissRequest = { statusDropdownExpanded = false },
                            modifier = Modifier.background(NavyCard)
                        ) {
                            batchStatusOptions.forEach { statusOption ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = statusOption,
                                            color = Color.White,
                                            fontWeight = if (statusOption == selectedBatchStatus) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        selectedBatchStatus = statusOption
                                        statusDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val updatedCount = selectedOrderIds.size
                            viewModel.batchUpdateOrdersStatus(selectedOrderIds, selectedBatchStatus)
                            selectedOrderIds = emptySet()
                            batchFeedbackMessage = if (isAr) {
                                "تم تحديث حالة $updatedCount طلبات إلى $selectedBatchStatus ✓"
                            } else {
                                "Updated $updatedCount order(s) to $selectedBatchStatus ✓"
                            }
                        },
                        enabled = selectedOrderIds.isNotEmpty(),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("apply_batch_status_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = NavyPrimary,
                            disabledContainerColor = NavyCard,
                            disabledContentColor = Color.White.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (isAr) "تطبيق" else "Apply",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = batchFeedbackMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GreenSuccess.copy(alpha = 0.16f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = GreenSuccess,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = batchFeedbackMessage.orEmpty(),
                        color = GreenSuccess,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // List of orders
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredOrders, key = { it.id }) { order ->
                val isChecked = order.id in selectedOrderIds
                OrderItemCard(
                    order = order,
                    isAr = isAr,
                    isSelected = isChecked,
                    onToggleSelect = { checked ->
                        selectedOrderIds = if (checked) {
                            selectedOrderIds + order.id
                        } else {
                            selectedOrderIds - order.id
                        }
                    },
                    onSelect = {
                        viewModel.updateOrderInput(order.id)
                        viewModel.retrieveOrder(order.id)
                        viewModel.selectTab(AppTab.STUDIO)
                    }
                )
            }
        }
    }
}

@Composable
fun OrderItemCard(
    order: Order,
    isAr: Boolean,
    isSelected: Boolean,
    onToggleSelect: (Boolean) -> Unit,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) {
                    Modifier.border(1.5.dp, CyanAccent, RoundedCornerShape(12.dp))
                } else {
                    Modifier
                }
            )
            .clickable { onSelect() }
            .testTag("order_item_${order.id}"),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = onToggleSelect,
                        modifier = Modifier.testTag("order_checkbox_${order.id}"),
                        colors = CheckboxDefaults.colors(
                            checkedColor = CyanAccent,
                            uncheckedColor = Color.White.copy(alpha = 0.6f),
                            checkmarkColor = NavyPrimary
                        )
                    )
                    Text(
                        text = order.id,
                        color = CyanAccent,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• ${order.city}",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (order.status) {
                                "Delivered", "Completed", "Verified" -> GreenSuccess
                                "Canceled" -> Color(0xFFBA1A1A)
                                "Delayed", "Pending" -> AmberAccent
                                else -> CyanAccent.copy(alpha = 0.2f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = order.status,
                        color = when (order.status) {
                            "Delivered", "Completed", "Verified", "Canceled" -> Color.White
                            "Delayed", "Pending" -> NavyPrimary
                            else -> CyanAccent
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(color = NavyLight)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isAr) "الموعد المتوقع" else "ETA",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = order.eta,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isAr) "آخر فحص" else "LAST SCAN",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = order.lastScan,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }

            // Test in studio button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isAr) "فتح في الاستوديو" else "Open in Studio",
                    color = AmberAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = AmberAccent,
                    modifier = Modifier.height(14.dp)
                )
            }
        }
    }
}
