package com.example.data.business

import android.content.Context
import com.example.data.business.sync.BusinessSyncManager
import com.example.data.business.sync.BusinessSyncState
import com.example.data.business.sync.SyncResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BusinessRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val dao = db.businessDao()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val businesses: Flow<List<BusinessProfile>> = dao.getAllBusinesses()
    val syncManager = BusinessSyncManager(context)
    val syncState: StateFlow<BusinessSyncState> = syncManager.syncState

    init {
        scope.launch {
            ensureDefaultBusiness()
        }
    }

    private suspend fun ensureDefaultBusiness() {
        if (dao.getBusinessCount() == 0) {
            val defaultBiz = BusinessProfile(
                name = "My Business (আমার ব্যবসা)",
                businessType = "Retail Store",
                currency = "৳",
                isDefault = true
            )
            val id = dao.insertBusiness(defaultBiz)
            enqueueSync(
                businessId = id,
                entityType = "PROFILE",
                entityId = id,
                cloudId = "biz_$id",
                action = "UPSERT",
                payloadJson = defaultBiz.copy(id = id).toJson()
            )
        }
    }

    suspend fun triggerSync(isManual: Boolean = true): SyncResult = syncManager.triggerSync(isManual)

    private suspend fun enqueueSync(
        businessId: Long,
        entityType: String,
        entityId: Long,
        cloudId: String,
        action: String,
        payloadJson: String = ""
    ) {
        val existing = dao.getSyncItemForEntity(entityType, entityId)
        val item = SyncQueueItem(
            id = existing?.id ?: 0L,
            businessId = businessId,
            entityType = entityType,
            entityId = entityId,
            cloudId = cloudId,
            action = action,
            payloadJson = payloadJson,
            timestamp = System.currentTimeMillis(),
            status = "PENDING"
        )
        dao.insertSyncItem(item)
    }

    // --- Business Profiles ---
    suspend fun addBusiness(name: String, type: String = "Retail", currency: String = "৳", phone: String = ""): Long =
        withContext(Dispatchers.IO) {
            val profile = BusinessProfile(
                name = name.ifBlank { "New Business" },
                businessType = type,
                currency = currency.ifBlank { "৳" },
                phone = phone
            )
            val id = dao.insertBusiness(profile)
            enqueueSync(
                businessId = id,
                entityType = "PROFILE",
                entityId = id,
                cloudId = "biz_$id",
                action = "UPSERT",
                payloadJson = profile.copy(id = id).toJson()
            )
            id
        }

    suspend fun updateBusiness(business: BusinessProfile) = withContext(Dispatchers.IO) {
        dao.updateBusiness(business)
        enqueueSync(
            businessId = business.id,
            entityType = "PROFILE",
            entityId = business.id,
            cloudId = "biz_${business.id}",
            action = "UPSERT",
            payloadJson = business.toJson()
        )
    }

    suspend fun deleteBusiness(businessId: Long) = withContext(Dispatchers.IO) {
        enqueueSync(
            businessId = businessId,
            entityType = "PROFILE",
            entityId = businessId,
            cloudId = "biz_$businessId",
            action = "DELETE"
        )
        dao.deleteAllTransactionsForBusiness(businessId)
        dao.deleteAllCustomersForBusiness(businessId)
        dao.deleteAllSuppliersForBusiness(businessId)
        dao.deleteAllProductsForBusiness(businessId)
        dao.deleteBusiness(businessId)
        dao.clearQueueForBusiness(businessId)
        ensureDefaultBusiness()
    }

    // --- Transactions & Ledger ---
    fun getTransactions(businessId: Long): Flow<List<BusinessTransaction>> =
        dao.getTransactions(businessId)

    suspend fun getTransactionById(id: Long): BusinessTransaction? = withContext(Dispatchers.IO) {
        dao.getTransactionById(id)
    }

    suspend fun saveTransaction(
        id: Long = 0,
        businessId: Long,
        type: String, // SALE, PURCHASE, EXPENSE, CUSTOMER_PAYMENT, SUPPLIER_PAYMENT
        title: String,
        amount: Double,
        paidAmount: Double,
        partyName: String = "",
        partyPhone: String = "",
        note: String = "",
        timestamp: Long = System.currentTimeMillis(),
        customerId: Long? = null,
        supplierId: Long? = null,
        productId: Long? = null,
        quantity: Double = 0.0,
        unitPrice: Double = 0.0
    ): Long = withContext(Dispatchers.IO) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dateString = dateFormat.format(Date(timestamp))

        // Step 1: If updating an existing transaction with a product, revert old stock adjustment
        if (id != 0L) {
            val oldTransaction = dao.getTransactionById(id)
            if (oldTransaction != null && oldTransaction.productId != null && oldTransaction.quantity > 0) {
                val oldProduct = dao.getProductById(oldTransaction.productId)
                if (oldProduct != null) {
                    val restoredStock = when (oldTransaction.type) {
                        "SALE" -> (oldProduct.currentQuantity + oldTransaction.quantity).coerceAtLeast(0.0)
                        "PURCHASE" -> (oldProduct.currentQuantity - oldTransaction.quantity).coerceAtLeast(0.0)
                        else -> oldProduct.currentQuantity
                    }
                    dao.updateProductQuantity(oldProduct.id, restoredStock)
                }
            }
        }

        val entry = BusinessTransaction(
            id = id,
            businessId = businessId,
            type = type,
            title = title.ifBlank { type },
            amount = amount.coerceAtLeast(0.0),
            paidAmount = paidAmount.coerceAtLeast(0.0),
            partyName = partyName.trim(),
            partyPhone = partyPhone.trim(),
            note = note.trim(),
            timestamp = timestamp,
            dateString = dateString,
            customerId = customerId,
            supplierId = supplierId,
            productId = productId,
            quantity = quantity.coerceAtLeast(0.0),
            unitPrice = unitPrice.coerceAtLeast(0.0)
        )

        val transactionId = if (id == 0L) {
            dao.insertTransaction(entry)
        } else {
            dao.updateTransaction(entry)
            id
        }

        // Step 2: Apply new stock adjustment consistently
        if (productId != null && quantity > 0) {
            val product = dao.getProductById(productId)
            if (product != null) {
                val newStock = when (type) {
                    "SALE" -> (product.currentQuantity - quantity).coerceAtLeast(0.0)
                    "PURCHASE" -> (product.currentQuantity + quantity).coerceAtLeast(0.0)
                    else -> product.currentQuantity
                }
                dao.updateProductQuantity(productId, newStock)
                // Also queue updated product stock
                val updatedProduct = product.copy(currentQuantity = newStock)
                enqueueSync(
                    businessId = businessId,
                    entityType = "PRODUCT",
                    entityId = productId,
                    cloudId = "prod_$productId",
                    action = "UPSERT",
                    payloadJson = updatedProduct.toJson()
                )
            }
        }

        // Enqueue transaction sync
        enqueueSync(
            businessId = businessId,
            entityType = "TRANSACTION",
            entityId = transactionId,
            cloudId = "tx_$transactionId",
            action = "UPSERT",
            payloadJson = entry.copy(id = transactionId).toJson()
        )

        transactionId
    }

    suspend fun deleteTransaction(transaction: BusinessTransaction) = withContext(Dispatchers.IO) {
        // Revert product stock if applicable
        if (transaction.productId != null && transaction.quantity > 0) {
            val product = dao.getProductById(transaction.productId)
            if (product != null) {
                val restoredStock = when (transaction.type) {
                    "SALE" -> (product.currentQuantity + transaction.quantity).coerceAtLeast(0.0)
                    "PURCHASE" -> (product.currentQuantity - transaction.quantity).coerceAtLeast(0.0)
                    else -> product.currentQuantity
                }
                dao.updateProductQuantity(product.id, restoredStock)
                val updatedProduct = product.copy(currentQuantity = restoredStock)
                enqueueSync(
                    businessId = transaction.businessId,
                    entityType = "PRODUCT",
                    entityId = product.id,
                    cloudId = "prod_${product.id}",
                    action = "UPSERT",
                    payloadJson = updatedProduct.toJson()
                )
            }
        }
        enqueueSync(
            businessId = transaction.businessId,
            entityType = "TRANSACTION",
            entityId = transaction.id,
            cloudId = "tx_${transaction.id}",
            action = "DELETE"
        )
        dao.deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Long) = withContext(Dispatchers.IO) {
        val transaction = dao.getTransactionById(id)
        if (transaction != null) {
            deleteTransaction(transaction)
        } else {
            dao.deleteTransactionById(id)
        }
    }

    // --- Payment Tracking ---
    suspend fun recordCustomerPayment(
        businessId: Long,
        customerId: Long,
        customerName: String,
        customerPhone: String,
        amount: Double,
        note: String = "",
        timestamp: Long = System.currentTimeMillis()
    ): Long = withContext(Dispatchers.IO) {
        saveTransaction(
            businessId = businessId,
            type = "CUSTOMER_PAYMENT",
            title = "Payment Received / বাকি আদায় - $customerName",
            amount = amount,
            paidAmount = amount,
            partyName = customerName,
            partyPhone = customerPhone,
            note = note,
            timestamp = timestamp,
            customerId = customerId
        )
    }

    suspend fun recordSupplierPayment(
        businessId: Long,
        supplierId: Long,
        supplierName: String,
        supplierPhone: String,
        amount: Double,
        note: String = "",
        timestamp: Long = System.currentTimeMillis()
    ): Long = withContext(Dispatchers.IO) {
        saveTransaction(
            businessId = businessId,
            type = "SUPPLIER_PAYMENT",
            title = "Supplier Payment / মহাজন পরিশোধ - $supplierName",
            amount = amount,
            paidAmount = amount,
            partyName = supplierName,
            partyPhone = supplierPhone,
            note = note,
            timestamp = timestamp,
            supplierId = supplierId
        )
    }

    // --- Customers ---
    fun getCustomers(businessId: Long): Flow<List<Customer>> =
        dao.getAllCustomers(businessId)

    suspend fun getCustomerById(id: Long): Customer? = withContext(Dispatchers.IO) {
        dao.getCustomerById(id)
    }

    suspend fun saveCustomer(
        id: Long = 0,
        businessId: Long,
        name: String,
        phone: String = "",
        address: String = "",
        openingBalance: Double = 0.0,
        note: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val entry = Customer(
            id = id,
            businessId = businessId,
            name = name.trim().ifBlank { "Customer" },
            phone = phone.trim(),
            address = address.trim(),
            openingBalance = openingBalance.coerceAtLeast(0.0),
            note = note.trim()
        )
        val customerId = if (id == 0L) {
            dao.insertCustomer(entry)
        } else {
            dao.updateCustomer(entry)
            id
        }
        enqueueSync(
            businessId = businessId,
            entityType = "CUSTOMER",
            entityId = customerId,
            cloudId = "cust_$customerId",
            action = "UPSERT",
            payloadJson = entry.copy(id = customerId).toJson()
        )
        customerId
    }

    suspend fun deleteCustomer(customer: Customer) = withContext(Dispatchers.IO) {
        enqueueSync(
            businessId = customer.businessId,
            entityType = "CUSTOMER",
            entityId = customer.id,
            cloudId = "cust_${customer.id}",
            action = "DELETE"
        )
        dao.deleteCustomer(customer)
    }

    suspend fun deleteCustomerById(id: Long) = withContext(Dispatchers.IO) {
        val customer = dao.getCustomerById(id)
        if (customer != null) {
            deleteCustomer(customer)
        } else {
            dao.deleteCustomerById(id)
        }
    }

    fun getTransactionsByCustomer(businessId: Long, customerId: Long): Flow<List<BusinessTransaction>> =
        dao.getTransactionsByCustomer(businessId, customerId)

    /**
     * Calculates customer's net due balance:
     * openingBalance + sum(SALE dues) - sum(CUSTOMER_PAYMENT amount)
     */
    fun calculateCustomerDue(customer: Customer, allTransactions: List<BusinessTransaction>): Double {
        val customerTransactions = allTransactions.filter { it.customerId == customer.id }
        val salesDues = customerTransactions
            .filter { it.type == "SALE" }
            .sumOf { (it.amount - it.paidAmount).coerceAtLeast(0.0) }
        val payments = customerTransactions
            .filter { it.type == "CUSTOMER_PAYMENT" }
            .sumOf { it.amount }
        return (customer.openingBalance + salesDues - payments).coerceAtLeast(0.0)
    }

    // --- Suppliers ---
    fun getSuppliers(businessId: Long): Flow<List<Supplier>> =
        dao.getAllSuppliers(businessId)

    suspend fun getSupplierById(id: Long): Supplier? = withContext(Dispatchers.IO) {
        dao.getSupplierById(id)
    }

    suspend fun saveSupplier(
        id: Long = 0,
        businessId: Long,
        name: String,
        companyName: String = "",
        phone: String = "",
        address: String = "",
        openingBalance: Double = 0.0,
        note: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val entry = Supplier(
            id = id,
            businessId = businessId,
            name = name.trim().ifBlank { "Supplier" },
            companyName = companyName.trim(),
            phone = phone.trim(),
            address = address.trim(),
            openingBalance = openingBalance.coerceAtLeast(0.0),
            note = note.trim()
        )
        val supplierId = if (id == 0L) {
            dao.insertSupplier(entry)
        } else {
            dao.updateSupplier(entry)
            id
        }
        enqueueSync(
            businessId = businessId,
            entityType = "SUPPLIER",
            entityId = supplierId,
            cloudId = "supp_$supplierId",
            action = "UPSERT",
            payloadJson = entry.copy(id = supplierId).toJson()
        )
        supplierId
    }

    suspend fun deleteSupplier(supplier: Supplier) = withContext(Dispatchers.IO) {
        enqueueSync(
            businessId = supplier.businessId,
            entityType = "SUPPLIER",
            entityId = supplier.id,
            cloudId = "supp_${supplier.id}",
            action = "DELETE"
        )
        dao.deleteSupplier(supplier)
    }

    suspend fun deleteSupplierById(id: Long) = withContext(Dispatchers.IO) {
        val supplier = dao.getSupplierById(id)
        if (supplier != null) {
            deleteSupplier(supplier)
        } else {
            dao.deleteSupplierById(id)
        }
    }

    fun getTransactionsBySupplier(businessId: Long, supplierId: Long): Flow<List<BusinessTransaction>> =
        dao.getTransactionsBySupplier(businessId, supplierId)

    /**
     * Calculates supplier's net payable balance:
     * openingBalance + sum(PURCHASE dues) - sum(SUPPLIER_PAYMENT amount)
     */
    fun calculateSupplierPayable(supplier: Supplier, allTransactions: List<BusinessTransaction>): Double {
        val supplierTransactions = allTransactions.filter { it.supplierId == supplier.id }
        val purchaseDues = supplierTransactions
            .filter { it.type == "PURCHASE" }
            .sumOf { (it.amount - it.paidAmount).coerceAtLeast(0.0) }
        val payments = supplierTransactions
            .filter { it.type == "SUPPLIER_PAYMENT" }
            .sumOf { it.amount }
        return (supplier.openingBalance + purchaseDues - payments).coerceAtLeast(0.0)
    }

    // --- Products & Inventory ---
    fun getProducts(businessId: Long): Flow<List<ProductItem>> =
        dao.getAllProducts(businessId)

    suspend fun getProductById(id: Long): ProductItem? = withContext(Dispatchers.IO) {
        dao.getProductById(id)
    }

    suspend fun saveProduct(
        id: Long = 0,
        businessId: Long,
        name: String,
        sku: String = "",
        category: String = "",
        purchasePrice: Double = 0.0,
        sellingPrice: Double = 0.0,
        currentQuantity: Double = 0.0,
        unit: String = "pcs",
        lowStockThreshold: Double = 5.0
    ): Long = withContext(Dispatchers.IO) {
        val entry = ProductItem(
            id = id,
            businessId = businessId,
            name = name.trim().ifBlank { "Product" },
            sku = sku.trim(),
            category = category.trim(),
            purchasePrice = purchasePrice.coerceAtLeast(0.0),
            sellingPrice = sellingPrice.coerceAtLeast(0.0),
            currentQuantity = currentQuantity.coerceAtLeast(0.0),
            unit = unit.trim().ifBlank { "pcs" },
            lowStockThreshold = lowStockThreshold.coerceAtLeast(0.0)
        )
        val productId = if (id == 0L) {
            dao.insertProduct(entry)
        } else {
            dao.updateProduct(entry)
            id
        }
        enqueueSync(
            businessId = businessId,
            entityType = "PRODUCT",
            entityId = productId,
            cloudId = "prod_$productId",
            action = "UPSERT",
            payloadJson = entry.copy(id = productId).toJson()
        )
        productId
    }

    suspend fun adjustProductStock(productId: Long, delta: Double) = withContext(Dispatchers.IO) {
        val product = dao.getProductById(productId)
        if (product != null) {
            val newQuantity = (product.currentQuantity + delta).coerceAtLeast(0.0)
            dao.updateProductQuantity(productId, newQuantity)
            val updated = product.copy(currentQuantity = newQuantity)
            enqueueSync(
                businessId = product.businessId,
                entityType = "PRODUCT",
                entityId = productId,
                cloudId = "prod_$productId",
                action = "UPSERT",
                payloadJson = updated.toJson()
            )
        }
    }

    suspend fun deleteProduct(product: ProductItem) = withContext(Dispatchers.IO) {
        enqueueSync(
            businessId = product.businessId,
            entityType = "PRODUCT",
            entityId = product.id,
            cloudId = "prod_${product.id}",
            action = "DELETE"
        )
        dao.deleteProduct(product)
    }

    suspend fun deleteProductById(id: Long) = withContext(Dispatchers.IO) {
        val product = dao.getProductById(id)
        if (product != null) {
            deleteProduct(product)
        } else {
            dao.deleteProductById(id)
        }
    }

    fun getTransactionsByProduct(businessId: Long, productId: Long): Flow<List<BusinessTransaction>> =
        dao.getTransactionsByProduct(businessId, productId)
}

// JSON Serialization Helpers
private fun BusinessProfile.toJson(): String = JSONObject().apply {
    put("id", id)
    put("name", name)
    put("businessType", businessType)
    put("currency", currency)
    put("phone", phone)
    put("createdAt", createdAt)
    put("isDefault", isDefault)
}.toString()

private fun BusinessTransaction.toJson(): String = JSONObject().apply {
    put("id", id)
    put("businessId", businessId)
    put("type", type)
    put("title", title)
    put("amount", amount)
    put("paidAmount", paidAmount)
    put("partyName", partyName)
    put("partyPhone", partyPhone)
    put("note", note)
    put("timestamp", timestamp)
    put("dateString", dateString)
    put("customerId", customerId ?: JSONObject.NULL)
    put("supplierId", supplierId ?: JSONObject.NULL)
    put("productId", productId ?: JSONObject.NULL)
    put("quantity", quantity)
    put("unitPrice", unitPrice)
}.toString()

private fun Customer.toJson(): String = JSONObject().apply {
    put("id", id)
    put("businessId", businessId)
    put("name", name)
    put("phone", phone)
    put("address", address)
    put("openingBalance", openingBalance)
    put("note", note)
    put("createdAt", createdAt)
}.toString()

private fun Supplier.toJson(): String = JSONObject().apply {
    put("id", id)
    put("businessId", businessId)
    put("name", name)
    put("companyName", companyName)
    put("phone", phone)
    put("address", address)
    put("openingBalance", openingBalance)
    put("note", note)
    put("createdAt", createdAt)
}.toString()

private fun ProductItem.toJson(): String = JSONObject().apply {
    put("id", id)
    put("businessId", businessId)
    put("name", name)
    put("sku", sku)
    put("category", category)
    put("purchasePrice", purchasePrice)
    put("sellingPrice", sellingPrice)
    put("currentQuantity", currentQuantity)
    put("unit", unit)
    put("lowStockThreshold", lowStockThreshold)
    put("createdAt", createdAt)
}.toString()
