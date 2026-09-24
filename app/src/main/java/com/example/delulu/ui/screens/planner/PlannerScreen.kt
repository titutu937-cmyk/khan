package com.example.delulu.ui.screens.planner

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.delulu.data.model.TimetableEntity
import com.example.delulu.ui.components.*
import com.example.delulu.viewmodel.DeluluViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    viewModel: DeluluViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentRoutine by viewModel.currentRoutine.collectAsStateWithLifecycle()
    val timetable by viewModel.routineTimetable.collectAsStateWithLifecycle()
    val allSubjects by viewModel.allSubjects.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<TimetableEntity?>(null) }

    val routines = listOf("School Day", "Weekend", "Holiday", "Exam Preparation")

    DeluluBackgroundWrapper(screen = DeluluScreen.PLANNER, modifier = modifier) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Study Planner & Timetable",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Routine: $currentRoutine",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    val file = viewModel.pdfService.exportTimetableToPdf(currentRoutine, timetable)
                                    if (file != null) {
                                        Toast.makeText(context, "Timetable PDF exported!", Toast.LENGTH_SHORT).show()
                                        viewModel.pdfService.sharePdf(file)
                                    }
                                }
                            },
                            modifier = Modifier.testTag("export_timetable_pdf_button")
                        ) {
                            Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = "Export Timetable PDF")
                        }
                        IconButton(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.testTag("add_timetable_slot_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Timetable Slot")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                // 1. Routine Selector Chips
                item {
                    DeluluCard {
                        Text(
                            text = "Switch Study Schedule Mode",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            routines.forEach { r ->
                                FilterChip(
                                    selected = currentRoutine == r,
                                    onClick = { viewModel.setRoutine(r) },
                                    label = { Text(r, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                // 2. Timetable List
                item {
                    SectionHeader(
                        title = "$currentRoutine Schedule (${timetable.size} periods)",
                        subtitle = "Adapts automatically to holidays, weekends, and exam periods."
                    )
                }

                if (timetable.isEmpty()) {
                    item {
                        DeluluCard {
                            Text(
                                text = "No schedule items for this routine yet. Tap '+' in top bar to plan your daily schedule!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(timetable, key = { it.id }) { item ->
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                            tonalElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 5.dp)
                                .testTag("timetable_item_${item.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.padding(end = 14.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = item.startTime,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "to ${item.endTime}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${item.category} • ${item.subject}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = { entryToDelete = item }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete timetable slot",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Timetable Slot Dialog
    if (showAddDialog) {
        AddTimetableDialog(
            routine = currentRoutine,
            subjects = allSubjects.map { it.name },
            onDismiss = { showAddDialog = false },
            onSave = { title, start, end, category, subj ->
                viewModel.saveTimetableEntry(
                    TimetableEntity(
                        dayOfWeek = "Everyday",
                        routineType = currentRoutine,
                        title = title,
                        startTime = start,
                        endTime = end,
                        subject = subj,
                        category = category
                    )
                )
                showAddDialog = false
            }
        )
    }

    // Safe Confirmation Dialog for Deletion
    entryToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Delete Timetable Slot") },
            text = { Text("Are you sure you want to remove '${entry.title}' (${entry.startTime} - ${entry.endTime}) from your $currentRoutine schedule?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTimetableEntry(entry)
                        entryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("DELETE")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { entryToDelete = null }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
fun AddTimetableDialog(
    routine: String,
    subjects: List<String>,
    onDismiss: () -> Unit,
    onSave: (title: String, start: String, end: String, category: String, subject: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("04:00 PM") }
    var endTime by remember { mutableStateOf("05:30 PM") }
    var category by remember { mutableStateOf("Self Study") }
    var subject by remember { mutableStateOf(subjects.firstOrNull() ?: "General") }

    val categories = listOf("Self Study", "School", "Tuition", "Coaching", "Homework", "Break", "Sleep", "Personal")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to $routine Schedule") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Activity Title (e.g. Maths Practice)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start Time") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End Time") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (Study, Tuition, Break, etc.)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && startTime.isNotBlank() && endTime.isNotBlank()) {
                        onSave(title, startTime, endTime, category, subject)
                    }
                }
            ) {
                Text("Save Slot")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
