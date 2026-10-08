package com.example.ui.screens.everyday

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.PreferencesManager
import com.example.data.ShoppingRepository
import com.example.localization.AppLanguage
import com.example.ui.components.ToolActions
import com.example.ui.components.ToolBoxTopBar
import com.example.ui.components.ToolInputField
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.RedAccent
import java.text.DecimalFormat

@Composable
fun ShoppingListScreen(
    language: AppLanguage,
    prefs: PreferencesManager,
    shoppingRepository: ShoppingRepository,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val isBn = language == AppLanguage.BANGLA
    val items by shoppingRepository.items.collectAsState()
    val df = remember { DecimalFormat("#,##0.00") }

    var itemName by remember { mutableStateOf("") }
    var itemQty by remember { mutableStateOf("1") }
    var itemUnit by remember { mutableStateOf("pcs") }
    var itemPrice by remember { mutableStateOf("") }

    val totalCost = items.sumOf { it.quantity * it.estimatedPrice }

    Scaffold(
        topBar = {
            ToolBoxTopBar(
                title = if (isBn) "বাজারের তালিকা" else "Shopping List",
                onBackClick = onBackClick,
                isFavorite = prefs.isFavorite("shopping_list"),
                onFavoriteToggle = { prefs.toggleFavorite("shopping_list") },
                actions = {
                    if (items.any { it.isBought }) {
                        IconButton(
                            onClick = { shoppingRepository.clearBought() },
                            modifier = Modifier.testTag("clear_bought_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear Bought",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Total Estimated Expense Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = DarkSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, RedAccent.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBn) "মোট আনুমানিক খরচ" else "Estimated Total Cost",
                            style = MaterialTheme.typography.labelSmall,
                            color = RedAccent
                        )
                        Text(
                            text = "৳ ${df.format(totalCost)}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "${items.count { it.isBought }} / ${items.size} ${if (isBn) "ক্রয়কৃত" else "bought"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Add Item Row
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ToolInputField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = if (isBn) "পণ্যের নাম" else "Item name",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (itemName.isNotBlank()) {
                                ToolActions.triggerHaptic(context, prefs.hapticEnabled.value)
                                shoppingRepository.addItem(
                                    name = itemName,
                                    quantity = itemQty.toDoubleOrNull() ?: 1.0,
                                    unit = itemUnit,
                                    price = itemPrice.toDoubleOrNull() ?: 0.0
                                )
                                itemName = ""
                                itemPrice = ""
                            }
                        },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(RedAccent)
                            .testTag("add_shopping_item_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ToolInputField(
                        modifier = Modifier.weight(1f),
                        value = itemQty,
                        onValueChange = { itemQty = it },
                        label = if (isBn) "পরিমাণ" else "Qty",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    ToolInputField(
                        modifier = Modifier.weight(1f),
                        value = itemUnit,
                        onValueChange = { itemUnit = it },
                        label = if (isBn) "একক (কেজি/টি)" else "Unit (kg/pcs)"
                    )
                    ToolInputField(
                        modifier = Modifier.weight(1.2f),
                        value = itemPrice,
                        onValueChange = { itemPrice = it },
                        label = if (isBn) "দর (৳/unit)" else "Price (৳)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Item List
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isBn) "বাজারের তালিকা খালি" else "Shopping list is empty",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        val itemTotal = item.quantity * item.estimatedPrice
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp)),
                            shape = RoundedCornerShape(14.dp),
                            color = DarkSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = item.isBought,
                                    onCheckedChange = {
                                        ToolActions.triggerHaptic(context, prefs.hapticEnabled.value)
                                        shoppingRepository.toggleBought(item.id)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = RedAccent)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            textDecoration = if (item.isBought) TextDecoration.LineThrough else TextDecoration.None,
                                            fontWeight = if (item.isBought) FontWeight.Normal else FontWeight.SemiBold
                                        ),
                                        color = if (item.isBought) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${item.quantity} ${item.unit}" + if (item.estimatedPrice > 0) " • ৳ ${df.format(itemTotal)}" else "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = { shoppingRepository.deleteItem(item.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete item",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
