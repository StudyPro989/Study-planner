package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChapterEntity
import com.example.data.model.ChapterStatus
import com.example.engine.ScheduleProgressStatus
import com.example.ui.components.ChapterDetailDialog
import com.example.ui.components.DashboardStatsGrid
import com.example.ui.components.EditExamDateDialog
import com.example.ui.components.ExamCountdownCard
import com.example.ui.components.OverallProgressCard
import com.example.ui.components.StreakAndGamificationBanner
import com.example.ui.components.TaskCard
import com.example.ui.components.WeakAreasCard
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandBlueContainer
import com.example.ui.theme.OnBrandBlueContainer
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusOnTrackBg
import com.example.ui.theme.StatusOnTrackDot
import com.example.ui.theme.StatusOnTrackText
import com.example.ui.viewmodel.StudyUiState
import com.example.ui.viewmodel.StudyViewModel
import java.util.Calendar

@Composable
fun DashboardScreen(
    uiState: StudyUiState,
    viewModel: StudyViewModel,
    onNavigateToSchedule: () -> Unit,
    onNavigateToSubjects: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditExamDialog by remember { mutableStateOf(false) }
    var selectedChapterForDetail by remember { mutableStateOf<ChapterEntity?>(null) }

    // Today's tasks
    val todayTasks = uiState.allTasks.filter { it.date == uiState.todayDateStr }
    val completedTodayTasks = todayTasks.count { it.isCompleted }

    val greetingTime = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Greeting Section (Professional Polish Design)
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TODAY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.2.sp,
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$greetingTime, ${uiState.userSettings.userName} 👋",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }

                // Professional "ON TRACK" status pill badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = StatusOnTrackBg,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(StatusOnTrackDot, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (uiState.statusReport.status != ScheduleProgressStatus.ON_TRACK) "BEHIND" else "ON TRACK",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = StatusOnTrackText,
                                letterSpacing = 0.5.sp,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        // 2. Exam Countdown Card (Professional Polish Hero)
        item {
            ExamCountdownCard(
                examName = uiState.userSettings.examName,
                examDateMillis = uiState.userSettings.examDateMillis,
                daysRemaining = uiState.statusReport.daysRemaining,
                onEditExamDateClick = { showEditExamDialog = true }
            )
        }

        // 3. Overall Progress & Completed Cards (2-column layout)
        item {
            OverallProgressCard(
                progressPercent = uiState.statusReport.actualProgressPercent,
                completedChapters = uiState.statusReport.completedChaptersCount,
                totalChapters = uiState.statusReport.totalChaptersCount
            )
        }

        // 4. Statistics Grid Cards (Mastered, Continue, Revise, Days Remaining)
        item {
            DashboardStatsGrid(
                completedCount = uiState.statusReport.completedChaptersCount,
                continueCount = uiState.chapters.count { it.status == ChapterStatus.CONTINUE.name },
                reviseCount = uiState.statusReport.reviseChaptersCount,
                daysRemaining = uiState.statusReport.daysRemaining
            )
        }

        // 5. Weak Areas Alert Card
        item {
            WeakAreasCard(
                weakChapters = uiState.statusReport.weakChapters,
                subjects = uiState.subjects,
                onChapterClick = { chapter -> selectedChapterForDetail = chapter }
            )
        }

        // 6. Streak & Gamification Banner
        item {
            StreakAndGamificationBanner(
                streakDays = uiState.userSettings.studyStreak,
                xpPoints = uiState.userSettings.xpPoints,
                completedChaptersCount = uiState.statusReport.completedChaptersCount,
                revisionsCount = uiState.revisionHistory.size
            )
        }

        // 7. Today's Study Plan Header & Actions
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Today's Plan",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    if (todayTasks.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BrandBlueContainer
                        ) {
                            Text(
                                text = "${todayTasks.size} TASKS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OnBrandBlueContainer,
                                    letterSpacing = 0.5.sp,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = { viewModel.generateSchedule(7) },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Regenerate",
                        tint = BrandBlue,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Regenerate",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandBlue
                    )
                }
            }
        }

        // 8. Today's Study Plan Task Cards
        if (todayTasks.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(24.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = BrandBlue.copy(alpha = 0.1f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = BrandBlue,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No study tasks scheduled for today",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Generate a balanced revision plan calibrated to your upcoming exam date.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.generateSchedule(7) },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Generate Today's Plan", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(todayTasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    onToggleCompleted = { isDone -> viewModel.toggleTask(task.id, isDone) },
                    onTaskClick = {
                        val chapter = uiState.chapters.find { it.id == task.chapterId }
                        if (chapter != null) selectedChapterForDetail = chapter
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Dialogs
    if (showEditExamDialog) {
        EditExamDateDialog(
            currentExamName = uiState.userSettings.examName,
            currentExamDateMillis = uiState.userSettings.examDateMillis,
            onDismiss = { showEditExamDialog = false },
            onConfirm = { newName, newMillis ->
                viewModel.updateUserSettings(
                    uiState.userSettings.copy(
                        examName = newName,
                        examDateMillis = newMillis
                    )
                )
                viewModel.generateSchedule(7)
            }
        )
    }

    selectedChapterForDetail?.let { chapter ->
        ChapterDetailDialog(
            chapter = chapter,
            onDismiss = { selectedChapterForDetail = null },
            onSave = { updated ->
                viewModel.saveChapter(updated)
            },
            onDelete = {
                viewModel.deleteChapter(chapter.id)
            }
        )
    }
}

