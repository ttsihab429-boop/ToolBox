package com.example.ui.screens.everyday

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.PreferencesManager
import com.example.data.TodoItem
import com.example.data.TodoPriority
import com.example.data.TodoRepository
import com.example.localization.AppLanguage
import com.example.ui.components.ToolActions
import com.example.ui.components.ToolBoxTopBar
import com.example.ui.components.ToolInputField
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.RedAccent

@Composable
fun TodoScreen(
    language: AppLanguage,
    prefs: PreferencesManager,
    todoRepository: TodoRepository,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val isBn = language == AppLanguage.BANGLA
    val items by todoRepository.items.collectAsState()
    var newTaskText by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(TodoPriority.MEDIUM) }

    Scaffold(
        topBar = {
            ToolBoxTopBar(
                title = if (isBn) "টু-ডু লিস্ট" else "To-Do List",
                onBackClick = onBackClick,
                isFavorite = prefs.isFavorite("todo_list"),
                onFavoriteToggle = { prefs.toggleFavorite("todo_list") },
                actions = {
                    if (items.any { it.isDone }) {
                        IconButton(
                            onClick = { todoRepository.clearCompleted() },
                            modifier = Modifier.testTag("clear_completed_todo_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear Completed",
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
            // Task input row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToolInputField(
                    value = newTaskText,
                    onValueChange = { newTaskText = it },
                    label = if (isBn) "নতুন কাজ যোগ করুন..." else "Add a new task...",
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (newTaskText.isNotBlank()) {
                            ToolActions.triggerHaptic(context, prefs.hapticEnabled.value)
                            todoRepository.addItem(newTaskText, selectedPriority)
                            newTaskText = ""
                        }
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(RedAccent)
                        .testTag("submit_todo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Priority Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TodoPriority.entries.forEach { p ->
                    FilterChip(
                        selected = selectedPriority == p,
                        onClick = { selectedPriority = p },
                        label = {
                            Text(
                                text = when (p) {
                                    TodoPriority.LOW -> if (isBn) "কম" else "Low"
                                    TodoPriority.MEDIUM -> if (isBn) "মাঝারি" else "Medium"
                                    TodoPriority.HIGH -> if (isBn) "জরুরি" else "High"
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (p) {
                                TodoPriority.HIGH -> RedAccent
                                TodoPriority.MEDIUM -> Color(0xFFF57C00)
                                TodoPriority.LOW -> Color(0xFF388E3C)
                            },
                            containerColor = DarkSurfaceElevated
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Task list
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isBn) "কোনো কাজ বাকি নেই!" else "No tasks added yet!",
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
                                    checked = item.isDone,
                                    onCheckedChange = {
                                        ToolActions.triggerHaptic(context, prefs.hapticEnabled.value)
                                        todoRepository.toggleDone(item.id)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = RedAccent,
                                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag("todo_checkbox_${item.id}")
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.text,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None,
                                            fontWeight = if (item.isDone) FontWeight.Normal else FontWeight.Medium
                                        ),
                                        color = if (item.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = item.priority.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = when (item.priority) {
                                            TodoPriority.HIGH -> RedAccent
                                            TodoPriority.MEDIUM -> Color(0xFFFFA726)
                                            TodoPriority.LOW -> Color(0xFF66BB6A)
                                        }
                                    )
                                }

                                IconButton(
                                    onClick = { todoRepository.deleteItem(item.id) },
                                    modifier = Modifier.size(36.dp).testTag("delete_todo_${item.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete task",
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
