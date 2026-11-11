package com.example.sawtflow.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
    val isAr = language == AppLanguage.AR

    val filterOptions = listOf("All", "In transit", "Delivered", "Delayed", "Canceled", "Ready for pickup")

    val filteredOrders = orders.filter { order ->
        val matchesSearch = order.id.contains(searchQuery, ignoreCase = true) ||
                order.status.contains(searchQuery, ignoreCase = true) ||
                order.city.contains(searchQuery, ignoreCase = true)
        val matchesFilter = if (selectedFilter == "All") true else order.status.equals(selectedFilter, ignoreCase = true)
        matchesSearch && matchesFilter
    }

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

        // List of orders
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredOrders) { order ->
                OrderItemCard(
                    order = order,
                    isAr = isAr,
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
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                                "Delivered" -> GreenSuccess
                                "Canceled" -> Color(0xFFBA1A1A)
                                "Delayed" -> AmberAccent
                                else -> CyanAccent.copy(alpha = 0.2f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = order.status,
                        color = if (order.status == "Delivered" || order.status == "Canceled") Color.White else CyanAccent,
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
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = AmberAccent,
                    modifier = Modifier.height(14.dp)
                )
            }
        }
    }
}
