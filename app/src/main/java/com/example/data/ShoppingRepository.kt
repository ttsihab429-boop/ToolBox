package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class ShoppingItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val quantity: Double = 1.0,
    val unit: String = "pcs",
    val estimatedPrice: Double = 0.0,
    val isBought: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

class ShoppingRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("toolbox_shopping_storage", Context.MODE_PRIVATE)

    private val _items = MutableStateFlow<List<ShoppingItem>>(emptyList())
    val items: StateFlow<List<ShoppingItem>> = _items.asStateFlow()

    init {
        loadItems()
    }

    private fun loadItems() {
        val raw = prefs.getString("shopping_json", null) ?: return
        try {
            val array = JSONArray(raw)
            val list = mutableListOf<ShoppingItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ShoppingItem(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        quantity = obj.optDouble("quantity", 1.0),
                        unit = obj.optString("unit", "pcs"),
                        estimatedPrice = obj.optDouble("estimatedPrice", 0.0),
                        isBought = obj.optBoolean("isBought", false),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            _items.value = list
        } catch (_: Exception) {
            _items.value = emptyList()
        }
    }

    private fun persist(list: List<ShoppingItem>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("name", item.name)
            obj.put("quantity", item.quantity)
            obj.put("unit", item.unit)
            obj.put("estimatedPrice", item.estimatedPrice)
            obj.put("isBought", item.isBought)
            obj.put("timestamp", item.timestamp)
            array.put(obj)
        }
        prefs.edit().putString("shopping_json", array.toString()).apply()
        _items.value = list
    }

    fun addItem(name: String, quantity: Double, unit: String, price: Double) {
        val newItem = ShoppingItem(
            name = name.trim(),
            quantity = quantity,
            unit = unit.trim(),
            estimatedPrice = price
        )
        val current = _items.value.toMutableList()
        current.add(0, newItem)
        persist(current)
    }

    fun toggleBought(id: String) {
        val current = _items.value.map {
            if (it.id == id) it.copy(isBought = !it.isBought) else it
        }
        persist(current)
    }

    fun updateItem(id: String, name: String, quantity: Double, unit: String, price: Double) {
        val current = _items.value.map {
            if (it.id == id) it.copy(
                name = name.trim(),
                quantity = quantity,
                unit = unit.trim(),
                estimatedPrice = price
            ) else it
        }
        persist(current)
    }

    fun deleteItem(id: String) {
        val current = _items.value.filter { it.id != id }
        persist(current)
    }

    fun clearBought() {
        val current = _items.value.filter { !it.isBought }
        persist(current)
    }

    fun clearAll() {
        prefs.edit().remove("shopping_json").apply()
        _items.value = emptyList()
    }
}
