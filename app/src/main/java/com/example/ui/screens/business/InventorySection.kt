package com.example.ui.screens.business

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import com.example.data.business.BusinessProfile
import com.example.data.business.BusinessRepository
import com.example.data.business.ProductItem
import com.example.localization.AppLanguage
import com.example.localization.Strings
import com.example.ui.components.ToolActions
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RedAccent
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun InventorySection(
    currentBusiness: BusinessProfile,
    businessRepository: BusinessRepository,
    language: AppLanguage,
    prefs: PreferencesManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currency = currentBusiness.currency

    val products by businessRepository.getProducts(currentBusiness.id).collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var stockFilter by remember { mutableStateOf<String?>(null) } // null = ALL, "LOW", "OUT"

    // Dialog states
    var showAddProductDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<ProductItem?>(null) }
    var productToDelete by remember { mutableStateOf<ProductItem?>(null) }
    var productToAdjust by remember { mutableStateOf<ProductItem?>(null) }

    // Summary calculations
    val totalStockValuation = remember(products) {
        products.sumOf { it.currentQuantity * it.purchasePrice }
    }
    val lowStockCount = remember(products) {
        products.count { it.isLowStock && !it.isOutOfStock }
    }
    val outOfStockCount = remember(products) {
        products.count { it.isOutOfStock }
    }

    val filteredProducts = remember(products, searchQuery, stockFilter) {
        products.filter { product ->
            val matchesQuery = searchQuery.isBlank() ||
                product.name.contains(searchQuery, ignoreCase = true) ||
                product.sku.contains(searchQuery, ignoreCase = true) ||
                product.category.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (stockFilter) {
                "LOW" -> product.isLowStock && !product.isOutOfStock
                "OUT" -> product.isOutOfStock
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = DarkSurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (language == AppLanguage.BANGLA) "মোট পণ্যের মজুদ মূল্য (Stock Value)" else "Total Stock Valuation",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$currency ${formatMoney(totalStockValuation)}",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${products.size} ${if (language == AppLanguage.BANGLA) "আইটেম তালিকাভুক্ত" else "Products in Catalog"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = {
                        productToEdit = null
                        showAddProductDialog = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                    modifier = Modifier.testTag("add_product_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (language == AppLanguage.BANGLA) "নতুন পণ্য" else "Product", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Search Bar & Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        if (language == AppLanguage.BANGLA) "পণ্য বা বারকোড দিয়ে খুঁজুন…" else "Search product or SKU…",
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_product_field"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RedAccent,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                )
            )
        }

        // Filter chips row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = stockFilter == null,
                onClick = { stockFilter = null },
                label = { Text(if (language == AppLanguage.BANGLA) "সব (${products.size})" else "All (${products.size})", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = RedAccent,
                    containerColor = DarkSurface
                )
            )

            if (lowStockCount > 0) {
                FilterChip(
                    selected = stockFilter == "LOW",
                    onClick = { stockFilter = if (stockFilter == "LOW") null else "LOW" },
                    label = { Text(if (language == AppLanguage.BANGLA) "কম স্টক ($lowStockCount)" else "Low ($lowStockCount)", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFF9800),
                        containerColor = DarkSurface
                    )
                )
            }

            if (outOfStockCount > 0) {
                FilterChip(
                    selected = stockFilter == "OUT",
                    onClick = { stockFilter = if (stockFilter == "OUT") null else "OUT" },
                    label = { Text(if (language == AppLanguage.BANGLA) "স্টক শেষ ($outOfStockCount)" else "Out ($outOfStockCount)", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RedAccent,
                        containerColor = DarkSurface
                    )
                )
            }
        }

        // Product Items List
        if (filteredProducts.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (language == AppLanguage.BANGLA) "কোনো পণ্য পাওয়া যায়নি" else "No products found",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (language == AppLanguage.BANGLA) "নতুন পণ্য যোগ করতে উপরের '+ নতুন পণ্য' চাপুন" else "Tap '+ Product' to add inventory items and stock thresholds",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    ProductCardItem(
                        product = product,
                        currency = currency,
                        language = language,
                        onAdjustStock = {
                            productToAdjust = product
                        },
                        onEdit = {
                            productToEdit = product
                            showAddProductDialog = true
                        },
                        onDelete = {
                            productToDelete = product
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // Add / Edit Product Dialog
    if (showAddProductDialog) {
        AddEditProductDialog(
            productToEdit = productToEdit,
            currency = currency,
            language = language,
            onDismiss = { showAddProductDialog = false },
            onSave = { name, sku, category, purchasePrice, sellingPrice, qty, unit, threshold ->
                scope.launch {
                    businessRepository.saveProduct(
                        id = productToEdit?.id ?: 0L,
                        businessId = currentBusiness.id,
                        name = name,
                        sku = sku,
                        category = category,
                        purchasePrice = purchasePrice,
                        sellingPrice = sellingPrice,
                        currentQuantity = qty,
                        unit = unit,
                        lowStockThreshold = threshold
                    )
                    ToolActions.triggerHaptic(context, prefs.hapticEnabled.value)
                    showAddProductDialog = false
                }
            }
        )
    }

    // Stock Adjustment Dialog
    if (productToAdjust != null) {
        val target = productToAdjust!!
        StockAdjustmentDialog(
            product = target,
            language = language,
            onDismiss = { productToAdjust = null },
            onApply = { delta ->
                scope.launch {
                    businessRepository.adjustProductStock(target.id, delta)
                    ToolActions.triggerHaptic(context, prefs.hapticEnabled.value)
                    productToAdjust = null
                }
            }
        )
    }

    // Delete Product Confirmation
    if (productToDelete != null) {
        val target = productToDelete!!
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text(if (language == AppLanguage.BANGLA) "পণ্য মুছবেন?" else "Delete Product?") },
            text = {
                Text(
                    if (language == AppLanguage.BANGLA)
                        "'${target.name}' পণ্যটি তালিকা থেকে মুছে ফেলতে চান?"
                    else
                        "Are you sure you want to delete '${target.name}'?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            businessRepository.deleteProduct(target)
                            productToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
                ) {
                    Text(Strings.delete(language))
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text(Strings.cancel(language))
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }
}

@Composable
fun ProductCardItem(
    product: ProductItem,
    currency: String,
    language: AppLanguage,
    onAdjustStock: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val stockColor = when {
        product.isOutOfStock -> RedAccent
        product.isLowStock -> Color(0xFFFF9800)
        else -> GreenSuccess
    }

    val stockLabel = when {
        product.isOutOfStock -> if (language == AppLanguage.BANGLA) "স্টক শেষ" else "Out of Stock"
        product.isLowStock -> if (language == AppLanguage.BANGLA) "কম স্টক" else "Low Stock"
        else -> if (language == AppLanguage.BANGLA) "মজুদ আছে" else "In Stock"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(stockColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = stockColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (product.sku.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = DarkSurfaceVariant
                            ) {
                                Text(
                                    text = product.sku,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    if (product.category.isNotBlank()) {
                        Text(
                            text = product.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${if (language == AppLanguage.BANGLA) "কেনা:" else "Buy:"} $currency ${formatMoney(product.purchasePrice)} • ${if (language == AppLanguage.BANGLA) "বিক্রি:" else "Sell:"} $currency ${formatMoney(product.sellingPrice)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${formatQuantity(product.currentQuantity)} ${product.unit}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = stockColor
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = stockColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = stockLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = stockColor,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RedAccent.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    }
                }

                OutlinedButton(
                    onClick = onAdjustStock,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (language == AppLanguage.BANGLA) "স্টক সমন্বয়" else "Adjust Stock", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun AddEditProductDialog(
    productToEdit: ProductItem?,
    currency: String,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (name: String, sku: String, category: String, purchasePrice: Double, sellingPrice: Double, qty: Double, unit: String, threshold: Double) -> Unit
) {
    var name by remember { mutableStateOf(productToEdit?.name ?: "") }
    var sku by remember { mutableStateOf(productToEdit?.sku ?: "") }
    var category by remember { mutableStateOf(productToEdit?.category ?: "") }
    var purchasePriceText by remember {
        mutableStateOf(productToEdit?.let { if (it.purchasePrice > 0) it.purchasePrice.toString() else "" } ?: "")
    }
    var sellingPriceText by remember {
        mutableStateOf(productToEdit?.let { if (it.sellingPrice > 0) it.sellingPrice.toString() else "" } ?: "")
    }
    var quantityText by remember {
        mutableStateOf(productToEdit?.let { it.currentQuantity.toString() } ?: "")
    }
    var unit by remember { mutableStateOf(productToEdit?.unit ?: "pcs") }
    var thresholdText by remember {
        mutableStateOf(productToEdit?.let { it.lowStockThreshold.toString() } ?: "5")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (productToEdit != null) {
                    if (language == AppLanguage.BANGLA) "পণ্য সম্পাদনা" else "Edit Product"
                } else {
                    if (language == AppLanguage.BANGLA) "নতুন পণ্য যোগ করুন" else "Add New Product"
                },
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (language == AppLanguage.BANGLA) "পণ্যের নাম *" else "Product Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RedAccent,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text(if (language == AppLanguage.BANGLA) "SKU / কোড" else "SKU / Barcode") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedAccent,
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant
                        )
                    )

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text(if (language == AppLanguage.BANGLA) "ক্যাটাগরি" else "Category") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedAccent,
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = purchasePriceText,
                        onValueChange = { purchasePriceText = it },
                        label = { Text("${if (language == AppLanguage.BANGLA) "কেনা দাম" else "Cost Price"} ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedAccent,
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant
                        )
                    )

                    OutlinedTextField(
                        value = sellingPriceText,
                        onValueChange = { sellingPriceText = it },
                        label = { Text("${if (language == AppLanguage.BANGLA) "বিক্রয় মূল্য" else "Sell Price"} ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedAccent,
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text(if (language == AppLanguage.BANGLA) "মজুদ পরিমাণ" else "Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedAccent,
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant
                        )
                    )

                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text(if (language == AppLanguage.BANGLA) "একক (যেমন: pcs, kg)" else "Unit") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedAccent,
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant
                        )
                    )
                }

                OutlinedTextField(
                    value = thresholdText,
                    onValueChange = { thresholdText = it },
                    label = { Text(if (language == AppLanguage.BANGLA) "কম স্টক সতর্কবার্তা সীমা" else "Low Stock Threshold") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RedAccent,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val cost = purchasePriceText.toDoubleOrNull() ?: 0.0
                        val sell = sellingPriceText.toDoubleOrNull() ?: 0.0
                        val qty = quantityText.toDoubleOrNull() ?: 0.0
                        val thresh = thresholdText.toDoubleOrNull() ?: 5.0
                        onSave(name, sku, category, cost, sell, qty, unit, thresh)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
            ) {
                Text(Strings.save(language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.cancel(language))
            }
        },
        containerColor = DarkSurfaceElevated
    )
}

@Composable
fun StockAdjustmentDialog(
    product: ProductItem,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onApply: (delta: Double) -> Unit
) {
    var isAdding by remember { mutableStateOf(true) }
    var adjustAmountText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "${product.name} - ${if (language == AppLanguage.BANGLA) "স্টক সমন্বয়" else "Adjust Stock"}",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "${if (language == AppLanguage.BANGLA) "বর্তমান স্টক:" else "Current Stock:"} ${formatQuantity(product.currentQuantity)} ${product.unit}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isAdding,
                        onClick = { isAdding = true },
                        label = { Text(if (language == AppLanguage.BANGLA) "+ স্টক যোগ করুন" else "+ Stock In") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenSuccess,
                            containerColor = DarkSurfaceVariant
                        )
                    )
                    FilterChip(
                        selected = !isAdding,
                        onClick = { isAdding = false },
                        label = { Text(if (language == AppLanguage.BANGLA) "- স্টক হ্রাস করুন" else "- Stock Out") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RedAccent,
                            containerColor = DarkSurfaceVariant
                        )
                    )
                }

                OutlinedTextField(
                    value = adjustAmountText,
                    onValueChange = { adjustAmountText = it },
                    label = { Text("${if (language == AppLanguage.BANGLA) "পরিমাণ" else "Quantity"} (${product.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isAdding) GreenSuccess else RedAccent,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = adjustAmountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        val delta = if (isAdding) amount else -amount
                        onApply(delta)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (isAdding) GreenSuccess else RedAccent)
            ) {
                Text(if (language == AppLanguage.BANGLA) "প্রয়োগ করুন" else "Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.cancel(language))
            }
        },
        containerColor = DarkSurfaceElevated
    )
}

fun formatQuantity(qty: Double): String {
    return if (qty % 1.0 == 0.0) {
        qty.toLong().toString()
    } else {
        String.format(Locale.US, "%.2f", qty)
    }
}
