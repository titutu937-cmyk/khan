package com.example.delulu.ui.screens.study

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.delulu.data.model.NoteEntity
import com.example.delulu.data.model.TestEntity
import com.example.delulu.service.GeneratedQuestionItem
import com.example.delulu.ui.components.*
import com.example.delulu.viewmodel.DeluluViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    viewModel: DeluluViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val allNotes by viewModel.allNotes.collectAsStateWithLifecycle()
    val allSubjects by viewModel.allSubjects.collectAsStateWithLifecycle()
    val allTests by viewModel.allTests.collectAsStateWithLifecycle()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Notes, 1: Practice / Question Gen, 2: Mock Test, 3: Sample Papers
    var searchNoteQuery by remember { mutableStateOf("") }
    var showAddNoteDialog by remember { mutableStateOf(false) }

    // Mock Test State
    var isTestActive by remember { mutableStateOf(false) }
    var testSubject by remember { mutableStateOf("Science") }
    var currentTestQuestions by remember { mutableStateOf<List<GeneratedQuestionItem>>(emptyList()) }
    var currentQuestionIdx by remember { mutableIntStateOf(0) }
    var selectedAnswers by remember { mutableStateOf<MutableMap<Int, String>>(mutableMapOf()) }
    var testFinishedScore by remember { mutableStateOf<Int?>(null) }
    var testWeakTopic by remember { mutableStateOf("") }

    val filteredNotes = remember(allNotes, searchNoteQuery) {
        if (searchNoteQuery.isBlank()) allNotes
        else allNotes.filter {
            it.title.contains(searchNoteQuery, ignoreCase = true) ||
            it.content.contains(searchNoteQuery, ignoreCase = true) ||
            it.subject.contains(searchNoteQuery, ignoreCase = true) ||
            it.tags.contains(searchNoteQuery, ignoreCase = true)
        }
    }

    DeluluBackgroundWrapper(screen = DeluluScreen.STUDY, modifier = modifier) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Study Hub",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${profile.studentClass} • ${profile.board}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        if (activeTab == 0) {
                            IconButton(
                                onClick = { showAddNoteDialog = true },
                                modifier = Modifier.testTag("add_note_button")
                            ) {
                                Icon(imageVector = Icons.Default.NoteAdd, contentDescription = "Add Note")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Secondary Tab Selector
                SecondaryTabRow(
                    selectedTabIndex = activeTab,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("Notes (${filteredNotes.size})", fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("Question Gen", fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        text = { Text("Mock Test", fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = activeTab == 3,
                        onClick = { activeTab = 3 },
                        text = { Text("Sample Papers", fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }

                // Main Content Body based on tab
                when (activeTab) {
                    0 -> NotesTabContent(
                        notes = filteredNotes,
                        searchQuery = searchNoteQuery,
                        onSearchChange = { searchNoteQuery = it },
                        onToggleFavorite = { viewModel.toggleNoteFavorite(it) },
                        onDeleteNote = { viewModel.deleteNote(it) },
                        onExportPdf = { note ->
                            coroutineScope.launch {
                                val file = viewModel.pdfService.exportNoteToPdf(note)
                                if (file != null) {
                                    Toast.makeText(context, "Note PDF exported: ${file.name}", Toast.LENGTH_SHORT).show()
                                    viewModel.pdfService.sharePdf(file)
                                }
                            }
                        }
                    )
                    1 -> QuestionGeneratorTabContent(
                        viewModel = viewModel,
                        studentClass = profile.studentClass
                    )
                    2 -> MockTestTabContent(
                        viewModel = viewModel,
                        allTests = allTests,
                        isTestActive = isTestActive,
                        testSubject = testSubject,
                        onSubjectChange = { testSubject = it },
                        onStartTest = {
                            coroutineScope.launch {
                                val generated = viewModel.aiService.generateQuestions(
                                    subject = testSubject,
                                    topic = "Curriculum Chapter Revision",
                                    studentClass = profile.studentClass,
                                    count = 5,
                                    type = "Mixed",
                                    difficulty = "Medium"
                                )
                                currentTestQuestions = generated
                                currentQuestionIdx = 0
                                selectedAnswers = mutableMapOf()
                                testFinishedScore = null
                                isTestActive = true
                            }
                        },
                        questions = currentTestQuestions,
                        currentQuestionIdx = currentQuestionIdx,
                        selectedAnswers = selectedAnswers,
                        onSelectAnswer = { qIdx, ans ->
                            selectedAnswers = selectedAnswers.toMutableMap().apply { put(qIdx, ans) }
                        },
                        onNextQuestion = { currentQuestionIdx++ },
                        onSubmitTest = {
                            var correct = 0
                            val weakList = mutableListOf<String>()
                            currentTestQuestions.forEachIndexed { idx, q ->
                                val ans = selectedAnswers[idx]
                                if (ans != null && (ans.startsWith(q.correctAnswer.take(1)) || ans.equals(q.correctAnswer, ignoreCase = true))) {
                                    correct++
                                } else {
                                    weakList.add(q.topic)
                                }
                            }
                            val pct = (correct.toFloat() / currentTestQuestions.size.toFloat()) * 100f
                            val weakStr = if (weakList.isNotEmpty()) weakList.distinct().joinToString(", ") else "None! Excellent mastery"
                            testFinishedScore = correct
                            testWeakTopic = weakStr

                            // Save to SQLite
                            viewModel.saveTestResult(
                                TestEntity(
                                    title = "$testSubject Weekly Mock Test",
                                    subject = testSubject,
                                    studentClass = profile.studentClass,
                                    totalMarks = currentTestQuestions.size,
                                    obtainedMarks = correct,
                                    percentage = pct,
                                    weakTopics = weakStr,
                                    completedAt = System.currentTimeMillis()
                                )
                            )
                        },
                        finishedScore = testFinishedScore,
                        weakTopic = testWeakTopic,
                        onResetTest = {
                            isTestActive = false
                            testFinishedScore = null
                        }
                    )
                    3 -> SamplePapersTabContent(studentClass = profile.studentClass, board = profile.board)
                }
            }
        }
    }

    // Add Note Dialog
    if (showAddNoteDialog) {
        AddNoteDialog(
            subjects = allSubjects.map { it.name },
            onDismiss = { showAddNoteDialog = false },
            onSave = { title, content, subject, chapter, tags ->
                viewModel.saveNote(
                    NoteEntity(
                        title = title,
                        content = content,
                        subject = subject,
                        chapter = chapter,
                        tags = tags,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                showAddNoteDialog = false
            }
        )
    }
}

@Composable
fun NotesTabContent(
    notes: List<NoteEntity>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onToggleFavorite: (NoteEntity) -> Unit,
    onDeleteNote: (NoteEntity) -> Unit,
    onExportPdf: (NoteEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search notes by title, topic, or tags...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .testTag("search_notes_input"),
                shape = RoundedCornerShape(16.dp)
            )
        }

        if (notes.isEmpty()) {
            item {
                DeluluCard {
                    Text(
                        text = "No notes found. Tap '+' above to create rich study notes offline!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(notes, key = { it.id }) { note ->
                DeluluCard(modifier = Modifier.testTag("note_card_${note.id}")) {
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
                                text = "${note.subject} • ${note.chapter.ifBlank { "General" }}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Row {
                            IconButton(onClick = { onToggleFavorite(note) }) {
                                Icon(
                                    imageVector = if (note.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Favorite",
                                    tint = if (note.isFavorite) DeluluAmber else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onExportPdf(note) }) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = "Export PDF",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = { onDeleteNote(note) }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 4
                    )

                    if (note.tags.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tags: ${note.tags}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionGeneratorTabContent(
    viewModel: DeluluViewModel,
    studentClass: String
) {
    var subject by remember { mutableStateOf("Science") }
    var topic by remember { mutableStateOf("Light - Reflection & Refraction") }
    var questionType by remember { mutableStateOf("Mixed") }
    var difficulty by remember { mutableStateOf("Medium") }
    var generatedList by remember { mutableStateOf<List<GeneratedQuestionItem>>(emptyList()) }
    var isGenerating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            DeluluCard {
                Text(
                    text = "Generate Curriculum Questions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Subject Topic / Chapter") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subject row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Science", "Mathematics", "Social Science").forEach { subj ->
                        FilterChip(
                            selected = subject == subj,
                            onClick = { subject = subj },
                            label = { Text(subj, fontSize = 11.sp) }
                        )
                    }
                }

                // Difficulty row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Easy", "Medium", "Hard").forEach { diff ->
                        FilterChip(
                            selected = difficulty == diff,
                            onClick = { difficulty = diff },
                            label = { Text(diff, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        isGenerating = true
                        coroutineScope.launch {
                            val res = viewModel.aiService.generateQuestions(
                                subject = subject,
                                topic = topic,
                                studentClass = studentClass,
                                count = 4,
                                type = questionType,
                                difficulty = difficulty
                            )
                            generatedList = res
                            isGenerating = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("generate_practice_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Practice Set")
                    }
                }
            }
        }

        items(generatedList) { q ->
            DeluluCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "${q.type} • ${q.marks} Mark${if (q.marks > 1) "s" else ""}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = q.topic,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = q.questionText,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )

                if (q.options.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    q.options.forEach { opt ->
                        Text(
                            text = opt,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Correct Answer: ${q.correctAnswer}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeluluEmerald
                    )
                )
                Text(
                    text = "Explanation: ${q.explanation}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MockTestTabContent(
    viewModel: DeluluViewModel,
    allTests: List<TestEntity>,
    isTestActive: Boolean,
    testSubject: String,
    onSubjectChange: (String) -> Unit,
    onStartTest: () -> Unit,
    questions: List<GeneratedQuestionItem>,
    currentQuestionIdx: Int,
    selectedAnswers: Map<Int, String>,
    onSelectAnswer: (Int, String) -> Unit,
    onNextQuestion: () -> Unit,
    onSubmitTest: () -> Unit,
    finishedScore: Int?,
    weakTopic: String,
    onResetTest: () -> Unit
) {
    if (!isTestActive) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            item {
                DeluluCard {
                    Text(
                        text = "Weekly Curriculum Mock Test",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Take a timed test with automatic score calculation and diagnosis of weak topics.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Choose Subject:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Science", "Mathematics", "English").forEach { s ->
                            FilterChip(
                                selected = testSubject == s,
                                onClick = { onSubjectChange(s) },
                                label = { Text(s) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onStartTest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_mock_test_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Timed Mock Test (5 Questions)")
                    }
                }
            }

            // Test History & Weekly Scoreboard
            item {
                SectionHeader(title = "Recorded Test Scores (${allTests.size})")
            }

            if (allTests.isEmpty()) {
                item {
                    DeluluCard {
                        Text(
                            text = "No test records yet. Start a mock test above to record genuine progress!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(allTests) { t ->
                    DeluluCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = t.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${t.subject} • Score: ${t.obtainedMarks}/${t.totalMarks} (${t.percentage.toInt()}%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (t.percentage >= 60f) DeluluEmerald else DeluluAmber
                                )
                                if (t.weakTopics.isNotBlank() && t.weakTopics != "None! Excellent mastery") {
                                    Text(
                                        text = "Weak Topic: ${t.weakTopics}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DeluluRose
                                    )
                                }
                            }
                            Text(
                                text = "${t.percentage.toInt()}%",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (t.percentage >= 60f) DeluluEmerald else DeluluAmber
                            )
                        }
                    }
                }
            }
        }
    } else if (finishedScore != null) {
        // Test Result Screen
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            item {
                DeluluCard {
                    Text(
                        text = "🎉 Mock Test Results",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val pct = ((finishedScore.toFloat() / questions.size.toFloat()) * 100f).toInt()
                    Text(
                        text = "Score: $finishedScore / ${questions.size} ($pct%)",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (pct >= 60) DeluluEmerald else DeluluAmber
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Diagnostic Feedback on Weak Topics:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = weakTopic,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onResetTest,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Back to Test Hub")
                    }
                }
            }
        }
    } else {
        // Active Question Taking Screen
        val currentQ = questions.getOrNull(currentQuestionIdx)
        if (currentQ != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                item {
                    DeluluCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Question ${currentQuestionIdx + 1} of ${questions.size}",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = currentQ.topic,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = currentQ.questionText,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (currentQ.options.isNotEmpty()) {
                            currentQ.options.forEach { opt ->
                                val isSelected = selectedAnswers[currentQuestionIdx] == opt
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { onSelectAnswer(currentQuestionIdx, opt) }
                                ) {
                                    Text(
                                        text = opt,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            }
                        } else {
                            OutlinedTextField(
                                value = selectedAnswers[currentQuestionIdx] ?: "",
                                onValueChange = { onSelectAnswer(currentQuestionIdx, it) },
                                placeholder = { Text("Type your answer here...") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        if (currentQuestionIdx < questions.size - 1) {
                            Button(
                                onClick = onNextQuestion,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Next Question")
                            }
                        } else {
                            Button(
                                onClick = onSubmitTest,
                                colors = ButtonDefaults.buttonColors(containerColor = DeluluEmerald),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Submit Test & View Diagnosis")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SamplePapersTabContent(studentClass: String, board: String) {
    val samplePapers = listOf(
        Pair("CBSE Class 10 Science Model Paper 2025", "Physics & Chemistry • Easy"),
        Pair("CBSE Class 10 Mathematics Standard Paper 2025", "Algebra & Geometry • Hard"),
        Pair("English Language & Literature Sample Set 1", "Reading & Writing • Medium"),
        Pair("Social Science Mock Practice Paper", "History & Geography • Medium")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            DeluluCard {
                Text(
                    text = "Curriculum Sample Papers & Imports",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Import or review your official curriculum sample question papers offline without relying on copyrighted third-party scraping.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(samplePapers) { paper ->
            DeluluCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = paper.first,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = paper.second,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.FileDownloadDone,
                            contentDescription = "Available Offline",
                            tint = DeluluEmerald
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddNoteDialog(
    subjects: List<String>,
    onDismiss: () -> Unit,
    onSave: (title: String, content: String, subject: String, chapter: String, tags: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedSubj by remember { mutableStateOf(subjects.firstOrNull() ?: "General") }
    var chapter by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Study Note") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Note Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = chapter,
                    onValueChange = { chapter = it },
                    label = { Text("Chapter / Topic") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content / Key points") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (comma separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        onSave(title, content, selectedSubj, chapter, tags)
                    }
                }
            ) {
                Text("Save Note")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
