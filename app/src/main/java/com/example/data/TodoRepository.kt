package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class TodoPriority { LOW, MEDIUM, HIGH }

data class TodoItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isDone: Boolean = false,
    val priority: TodoPriority = TodoPriority.MEDIUM,
    val timestamp: Long = System.currentTimeMillis()
)

class TodoRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("toolbox_todo_storage", Context.MODE_PRIVATE)

    private val _items = MutableStateFlow<List<TodoItem>>(emptyList())
    val items: StateFlow<List<TodoItem>> = _items.asStateFlow()

    init {
        loadItems()
    }

    private fun loadItems() {
        val raw = prefs.getString("todo_json", null) ?: return
        try {
            val array = JSONArray(raw)
            val list = mutableListOf<TodoItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    TodoItem(
                        id = obj.getString("id"),
                        text = obj.getString("text"),
                        isDone = obj.optBoolean("isDone", false),
                        priority = try {
                            TodoPriority.valueOf(obj.optString("priority", "MEDIUM"))
                        } catch (_: Exception) {
                            TodoPriority.MEDIUM
                        },
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            _items.value = list
        } catch (_: Exception) {
            _items.value = emptyList()
        }
    }

    private fun persist(list: List<TodoItem>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("text", item.text)
            obj.put("isDone", item.isDone)
            obj.put("priority", item.priority.name)
            obj.put("timestamp", item.timestamp)
            array.put(obj)
        }
        prefs.edit().putString("todo_json", array.toString()).apply()
        _items.value = list
    }

    fun addItem(text: String, priority: TodoPriority = TodoPriority.MEDIUM) {
        val newItem = TodoItem(text = text.trim(), priority = priority)
        val current = _items.value.toMutableList()
        current.add(0, newItem)
        persist(current)
    }

    fun toggleDone(id: String) {
        val current = _items.value.map {
            if (it.id == id) it.copy(isDone = !it.isDone) else it
        }
        persist(current)
    }

    fun updateItem(id: String, newText: String, priority: TodoPriority) {
        val current = _items.value.map {
            if (it.id == id) it.copy(text = newText.trim(), priority = priority) else it
        }
        persist(current)
    }

    fun deleteItem(id: String) {
        val current = _items.value.filter { it.id != id }
        persist(current)
    }

    fun clearCompleted() {
        val current = _items.value.filter { !it.isDone }
        persist(current)
    }

    fun clearAll() {
        prefs.edit().remove("todo_json").apply()
        _items.value = emptyList()
    }
}
