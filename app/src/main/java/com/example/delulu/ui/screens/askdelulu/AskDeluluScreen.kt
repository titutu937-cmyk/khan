package com.example.delulu.ui.screens.askdelulu

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.delulu.ui.components.*
import com.example.delulu.viewmodel.DeluluViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskDeluluScreen(
    viewModel: DeluluViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val questionInput by viewModel.askQuestionInput.collectAsStateWithLifecycle()
    val selectedSubject by viewModel.askSelectedSubject.collectAsStateWithLifecycle()
    val isLoading by viewModel.askIsLoading.collectAsStateWithLifecycle()
    val solution by viewModel.askCurrentSolution.collectAsStateWithLifecycle()
    val errorMessage by viewModel.askErrorMessage.collectAsStateWithLifecycle()
    val savedQuestions by viewModel.savedQuestions.collectAsStateWithLifecycle()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showSavedDrawer by remember { mutableStateOf(false) }
    var exportedPdfFile by remember { mutableStateOf<File?>(null) }
    var isExportingPdf by remember { mutableStateOf(false) }

    // Zero-permission modern Android Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            viewModel.askQuestionInput.value = "Solve equation and steps from selected diagram/image."
            viewModel.solveCurrentQuestion(imagePath = uri.toString())
        }
    }

    val subjects = listOf("Auto Detect", "Mathematics", "Science", "English", "Social Science", "Computer Science")

    DeluluBackgroundWrapper(screen = DeluluScreen.ASK_DELULU, modifier = modifier) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Ask Delulu AI",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isOnline) "Cloud AI Connected" else "Offline Curriculum Engine Active",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isOnline) DeluluEmerald else DeluluAmber
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showSavedDrawer = !showSavedDrawer },
                            modifier = Modifier.testTag("saved_questions_button")
                        ) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ) {
                                Text("${savedQuestions.size}")
                            }
                            Icon(
                                imageVector = Icons.Default.BookmarkBorder,
                                contentDescription = "Saved Questions"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
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
                // 1. Transparent Network Capability Indicator
                item {
                    OfflineBanner(isOffline = !isOnline)
                }

                // 2. Question Input Section
                item {
                    DeluluCard(modifier = Modifier.testTag("ask_question_input_card")) {
                        Text(
                            text = "Enter Your Question",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Subject chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            subjects.take(3).forEach { subj ->
                                FilterChip(
                                    selected = selectedSubject == subj,
                                    onClick = { viewModel.askSelectedSubject.value = subj },
                                    label = { Text(subj, fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = questionInput,
                            onValueChange = { viewModel.askQuestionInput.value = it },
                            placeholder = {
                                Text("Type or paste any homework question, concept, or formula here...")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("question_text_input"),
                            shape = RoundedCornerShape(16.dp)
                        )

                        // Image preview if selected
                        selectedImageUri?.let { uri ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Selected question image",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Question image attached",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { selectedImageUri = null }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove image",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Actions: Choose Photo, Solve Question
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("pick_photo_button"),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Photo / Gallery", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { viewModel.solveCurrentQuestion(selectedImageUri?.toString()) },
                                enabled = !isLoading,
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("solve_question_button"),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Solve Now")
                                }
                            }
                        }
                    }
                }

                // Error Message if any
                errorMessage?.let { err ->
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = err,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                // 3. Solved Solution Display
                solution?.let { sol ->
                    item {
                        DeluluCard(modifier = Modifier.testTag("solution_card")) {
                            // Header tags
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "${sol.subject} • ${sol.topic}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (sol.isOfflineResult) DeluluAmber.copy(alpha = 0.2f) else DeluluEmerald.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = if (sol.isOfflineResult) "Offline Engine" else "Cloud AI",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (sol.isOfflineResult) DeluluAmber else DeluluEmerald,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Final Answer
                            Text(
                                text = "Final Answer:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DeluluEmerald
                                )
                            )
                            Text(
                                text = sol.finalAnswer,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            // Step-by-Step
                            Text(
                                text = "Step-by-Step Explanation:",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            sol.stepByStep.forEach { step ->
                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 3.dp)
                                )
                            }

                            // Important Points
                            if (sol.importantPoints.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Key Curriculum Points:",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                sol.importantPoints.forEach { pt ->
                                    Text(
                                        text = "• $pt",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }

                            // Common Mistake
                            if (sol.commonMistake.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    color = DeluluAmber.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "⚠️ Common Exam Mistake to Avoid:",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = DeluluAmber
                                            )
                                        )
                                        Text(
                                            text = sol.commonMistake,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }

                            // Practice Question
                            if (sol.practiceQuestion.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "🎯 Practice Question:",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = sol.practiceQuestion,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Action buttons: Save Solution & Export to PDF
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.saveCurrentQuestionAndAnswer()
                                        Toast.makeText(context, "Saved to offline library!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("save_solution_button"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Save Solution", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        isExportingPdf = true
                                        coroutineScope.launch {
                                            val qText = viewModel.askQuestionInput.value.ifBlank { "Solved Question" }
                                            val file = viewModel.pdfService.exportQuestionAnswerToPdf(qText, sol)
                                            isExportingPdf = false
                                            if (file != null) {
                                                exportedPdfFile = file
                                                Toast.makeText(context, "PDF generated offline: ${file.name}", Toast.LENGTH_LONG).show()
                                                viewModel.pdfService.sharePdf(file)
                                            } else {
                                                Toast.makeText(context, "Failed to create PDF", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("export_pdf_button"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (isExportingPdf) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.PictureAsPdf,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Export PDF", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Saved Questions Drawer / List
                if (showSavedDrawer) {
                    item {
                        SectionHeader(
                            title = "Saved Offline Questions (${savedQuestions.size})",
                            actionText = "Close",
                            onActionClick = { showSavedDrawer = false }
                        )
                    }

                    if (savedQuestions.isEmpty()) {
                        item {
                            DeluluCard {
                                Text(
                                    text = "No saved questions yet. When you solve a question, tap 'Save Solution' to keep it offline.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(savedQuestions) { q ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = q.question,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                        )
                                        Text(
                                            text = "${q.subject} • ${q.topic}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.askQuestionInput.value = q.question
                                            viewModel.solveCurrentQuestion()
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Review Question",
                                            tint = MaterialTheme.colorScheme.primary
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
}
