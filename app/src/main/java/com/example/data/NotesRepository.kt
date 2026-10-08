package com.example.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

@Immutable
data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val colorTag: String = "#E53935"
)

class NotesRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("toolbox_notes_storage", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    init {
        scope.launch(Dispatchers.IO) {
            loadNotes()
        }
    }

    private fun loadNotes() {
        val raw = prefs.getString("notes_json", null) ?: return
        try {
            val array = JSONArray(raw)
            val list = ArrayList<Note>(array.length())
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    Note(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        content = obj.getString("content"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        colorTag = obj.optString("colorTag", "#E53935")
                    )
                )
            }
            _notes.value = list.sortedByDescending { it.timestamp }
        } catch (_: Exception) {
            _notes.value = emptyList()
        }
    }

    private fun persist(list: List<Note>) {
        val sorted = list.sortedByDescending { it.timestamp }
        _notes.value = sorted
        scope.launch {
            try {
                val array = JSONArray()
                for (item in sorted) {
                    val obj = JSONObject()
                    obj.put("id", item.id)
                    obj.put("title", item.title)
                    obj.put("content", item.content)
                    obj.put("timestamp", item.timestamp)
                    obj.put("colorTag", item.colorTag)
                    array.put(obj)
                }
                prefs.edit().putString("notes_json", array.toString()).apply()
            } catch (_: Exception) {}
        }
    }

    fun saveNote(note: Note) {
        val current = _notes.value.toMutableList()
        val index = current.indexOfFirst { it.id == note.id }
        if (index != -1) {
            current[index] = note.copy(timestamp = System.currentTimeMillis())
        } else {
            current.add(0, note)
        }
        persist(current)
    }

    fun deleteNote(id: String) {
        val current = _notes.value.filter { it.id != id }
        persist(current)
    }

    fun clearAllNotes() {
        _notes.value = emptyList()
        scope.launch {
            prefs.edit().remove("notes_json").apply()
        }
    }
}
