package com.example.ui.screens.everyday

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Note
import com.example.data.NotesRepository
import com.example.data.PreferencesManager
import com.example.localization.AppLanguage
import com.example.ui.components.PrimaryActionButton
import com.example.ui.components.SecondaryActionButton
import com.example.ui.components.ToolBoxTopBar
import com.example.ui.components.ToolInputField
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RedAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotesScreen(
    language: AppLanguage,
    prefs: PreferencesManager,
    notesRepository: NotesRepository,
    onBackClick: () -> Unit
) {
    val isBn = language == AppLanguage.BANGLA
    val notes by notesRepository.notes.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var noteToEdit by remember { mutableStateOf<Note?>(null) }
    var isCreatingNote by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }

    val filteredNotes = remember(notes, searchQuery) {
        if (searchQuery.isBlank()) notes
        else notes.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.content.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            ToolBoxTopBar(
                title = if (isBn) "কুইক নোটস" else "Quick Notes",
                onBackClick = onBackClick,
                isFavorite = prefs.isFavorite("notes"),
                onFavoriteToggle = { prefs.toggleFavorite("notes") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isCreatingNote = true },
                containerColor = RedAccent,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_note_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Note")
            }
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("notes_search_field"),
                placeholder = { Text(if (isBn) "নোট খুঁজুন..." else "Search notes...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RedAccent,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredNotes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isBn) "কোনো নোট নেই" else "No notes found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isBn) "+ বাটনে ট্যাপ করে নতুন নোট লিখুন" else "Tap the + button to create a note",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredNotes, key = { it.id }) { note ->
                        val dateStr = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
                            .format(Date(note.timestamp))

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { noteToEdit = note }
                                .testTag("note_item_${note.id}"),
                            shape = RoundedCornerShape(16.dp),
                            color = DarkSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = note.title.ifBlank { "Untitled Note" },
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { noteToDelete = note },
                                        modifier = Modifier.size(32.dp).testTag("delete_note_${note.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Note",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = note.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = dateStr,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RedAccent.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Create / Edit Dialog
    if (isCreatingNote || noteToEdit != null) {
        val editing = noteToEdit
        var title by remember { mutableStateOf(editing?.title.orEmpty()) }
        var content by remember { mutableStateOf(editing?.content.orEmpty()) }

        AlertDialog(
            onDismissRequest = {
                isCreatingNote = false
                noteToEdit = null
            },
            title = {
                Text(
                    text = if (editing != null) (if (isBn) "নোট সম্পাদনা" else "Edit Note")
                    else (if (isBn) "নতুন নোট" else "New Note"),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column {
                    ToolInputField(
                        value = title,
                        onValueChange = { title = it },
                        label = if (isBn) "শিরোনাম" else "Title",
                        placeholder = "Note title..."
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ToolInputField(
                        value = content,
                        onValueChange = { content = it },
                        label = if (isBn) "বিস্তারিত" else "Content",
                        placeholder = "Write your thoughts...",
                        singleLine = false,
                        maxLines = 6
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (content.isNotBlank() || title.isNotBlank()) {
                            val newNote = editing?.copy(
                                title = title.trim(),
                                content = content.trim(),
                                timestamp = System.currentTimeMillis()
                            ) ?: Note(
                                title = title.trim(),
                                content = content.trim()
                            )
                            notesRepository.saveNote(newNote)
                        }
                        isCreatingNote = false
                        noteToEdit = null
                    }
                ) {
                    Text(if (isBn) "সংরক্ষণ" else "Save", color = RedAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        isCreatingNote = false
                        noteToEdit = null
                    }
                ) {
                    Text(if (isBn) "বাতিল" else "Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }

    // Delete Confirmation Dialog
    if (noteToDelete != null) {
        val target = noteToDelete!!
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text(if (isBn) "নোট মুছবেন?" else "Delete Note?") },
            text = { Text(if (isBn) "আপনি কি নিশ্চিত যে এই নোটটি মুছে ফেলতে চান?" else "Are you sure you want to delete this note?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        notesRepository.deleteNote(target.id)
                        noteToDelete = null
                    }
                ) {
                    Text(if (isBn) "মুছুন" else "Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }
}
