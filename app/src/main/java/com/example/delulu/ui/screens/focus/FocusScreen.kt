package com.example.delulu.ui.screens.focus

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
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
fun FocusScreen(
    viewModel: DeluluViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    val secondsLeft by viewModel.timerSecondsLeft.collectAsStateWithLifecycle()
    val initialSeconds by viewModel.timerInitialSeconds.collectAsStateWithLifecycle()
    val timerMode by viewModel.timerMode.collectAsStateWithLifecycle()
    val timerSubject by viewModel.timerSubject.collectAsStateWithLifecycle()
    val isBreakMode by viewModel.isBreakMode.collectAsStateWithLifecycle()
    val allSubjects by viewModel.allSubjects.collectAsStateWithLifecycle()
    val todayStudyMinutes by viewModel.todayStudyMinutes.collectAsStateWithLifecycle()

    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    val progressFraction = if (initialSeconds > 0 && timerMode != "Stopwatch") {
        (secondsLeft.toFloat() / initialSeconds.toFloat()).coerceIn(0f, 1f)
    } else 1f

    val animatedProgress by animateFloatAsState(targetValue = progressFraction, label = "timer_progress")

    DeluluBackgroundWrapper(screen = DeluluScreen.FOCUS, modifier = modifier) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Focus & Study Timer",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Today's Focus: $todayStudyMinutes mins logged",
                                style = MaterialTheme.typography.bodySmall,
                                color = DeluluEmerald
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
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                // 1. Timer Preset Modes
                item {
                    DeluluCard {
                        Text(
                            text = "Timer Preset Modes",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = timerMode == "Pomodoro",
                                onClick = { viewModel.setTimerPreset("Pomodoro", 25, timerSubject) },
                                label = { Text("Pomodoro (25m)", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = timerMode == "Deep Study",
                                onClick = { viewModel.setTimerPreset("Deep Study", 45, timerSubject) },
                                label = { Text("Deep (45m)", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = timerMode == "Stopwatch",
                                onClick = { viewModel.setTimerPreset("Stopwatch", 0, timerSubject) },
                                label = { Text("Stopwatch", fontSize = 11.sp) }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Subject chips
                        Text(
                            text = "Focus Subject: $timerSubject",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            allSubjects.take(3).forEach { subj ->
                                FilterChip(
                                    selected = timerSubject == subj.name,
                                    onClick = { viewModel.timerSubject.value = subj.name },
                                    label = { Text(subj.name, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                // 2. Beautiful Circular Timer Dial
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .testTag("timer_dial_box"),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Background track
                            drawCircle(
                                color = Color(0x334F46E5),
                                style = Stroke(width = 16.dp.toPx())
                            )
                            // Animated active arc
                            drawArc(
                                color = if (isBreakMode) DeluluEmerald else DeluluPrimary,
                                startAngle = -90f,
                                sweepAngle = animatedProgress * 360f,
                                useCenter = false,
                                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = timeFormatted,
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 2.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isBreakMode) DeluluEmerald.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = if (isBreakMode) "Break Interval" else "$timerSubject Focus",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isBreakMode) DeluluEmerald else MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // 3. Play / Pause / Reset Controls
                item {
                    Spacer(modifier = Modifier.height(30.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalIconButton(
                            onClick = { viewModel.resetTimer() },
                            modifier = Modifier
                                .size(56.dp)
                                .testTag("reset_timer_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset Timer", modifier = Modifier.size(26.dp))
                        }

                        Button(
                            onClick = {
                                if (isRunning) {
                                    viewModel.pauseTimer()
                                } else {
                                    viewModel.startTimer()
                                }
                            },
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .testTag("toggle_timer_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRunning) DeluluAmber else DeluluPrimary
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isRunning) "Pause" else "Start",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        FilledTonalIconButton(
                            onClick = {
                                viewModel.logStudySession(timerSubject, 15, timerMode)
                                Toast.makeText(context, "Logged 15 mins to offline history!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .testTag("manual_log_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Quick Log Session", modifier = Modifier.size(26.dp))
                        }
                    }
                }

                // 4. Offline Storage Guarantee Note
                item {
                    Spacer(modifier = Modifier.height(30.dp))
                    DeluluCard(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = DeluluEmerald)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "100% Offline: Every completed study block is recorded in your local database with no internet required.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
