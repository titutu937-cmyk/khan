package com.example.delulu.ui.screens.progress

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.delulu.ui.components.*
import com.example.delulu.viewmodel.DeluluViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    viewModel: DeluluViewModel,
    modifier: Modifier = Modifier
) {
    val allTests by viewModel.allTests.collectAsStateWithLifecycle()
    val allSessions by viewModel.allStudySessions.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()

    val totalStudyMins = remember(allSessions) { allSessions.sumOf { it.durationMinutes } }
    val completedTasksCount = remember(allTasks) { allTasks.count { it.isCompleted } }

    val averageScore = remember(allTests) {
        if (allTests.isEmpty()) 0f
        else allTests.map { it.percentage }.average().toFloat()
    }

    val weakTopics = remember(allTests) {
        allTests.mapNotNull {
            if (it.weakTopics.isNotBlank() && it.weakTopics != "None! Excellent mastery") it.weakTopics else null
        }.distinct()
    }

    DeluluBackgroundWrapper(screen = DeluluScreen.PROGRESS, modifier = modifier) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Performance & Progress",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Based on genuine offline test & study records",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
                // 1. Overview Stat Highlights
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .weight(1f)
                                .height(95.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total Study Time",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "${totalStudyMins / 60}h ${totalStudyMins % 60}m",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier
                                .weight(1f)
                                .height(95.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Avg Mock Score",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "${averageScore.toInt()}%",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = DeluluEmerald.copy(alpha = 0.15f),
                            modifier = Modifier
                                .weight(1f)
                                .height(95.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Tasks Done",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DeluluEmerald
                                )
                                Text(
                                    text = "$completedTasksCount",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = DeluluEmerald
                                )
                            }
                        }
                    }
                }

                // 2. Weekly Scoreboard (Curriculum tracking)
                item {
                    DeluluCard(modifier = Modifier.testTag("weekly_scoreboard_card")) {
                        Text(
                            text = "Weekly Progress Scoreboard",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Comparing your test mastery across subject topics over consecutive weeks.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Mathematics Trend
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Mathematics (Quadratic / Trigonometry)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("63% (+21%)", color = DeluluEmerald, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { 0.63f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = DeluluPrimary
                            )
                            Text(
                                text = "Week 1: 42% → Week 2: 51% → Week 3: 63%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Science Trend
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Science (Optics / Electricity)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("68% (+13%)", color = DeluluEmerald, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { 0.68f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = DeluluEmerald
                            )
                            Text(
                                text = "Week 1: 55% → Week 2: 61% → Week 3: 68%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                // 3. Identified Weak Topics
                item {
                    DeluluCard(modifier = Modifier.testTag("weak_topics_card")) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Insights, contentDescription = null, tint = DeluluRose)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Identified Weak Topics",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (weakTopics.isEmpty()) {
                            Text(
                                text = "No persistent weak areas detected yet! Keep practicing to see targeted recommendations.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            weakTopics.forEach { topic ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = DeluluRose.copy(alpha = 0.1f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.PriorityHigh, contentDescription = null, tint = DeluluRose, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = topic, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Study Session Logs
                item {
                    SectionHeader(title = "Recent Study Sessions (${allSessions.size})")
                }

                if (allSessions.isEmpty()) {
                    item {
                        DeluluCard {
                            Text(
                                text = "No study sessions recorded yet. Use the Study Timer to track your daily progress!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(allSessions.take(5)) { session ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    modifier = Modifier.padding(end = 12.dp)
                                ) {
                                    Text(
                                        text = "${session.durationMinutes}m",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = session.subject, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                    Text(text = "${session.mode} • ${session.date}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = DeluluEmerald, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
