package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChapterDifficulty
import com.example.data.model.ChapterEntity
import com.example.data.model.ChapterStatus
import com.example.ui.components.ChapterDetailDialog
import com.example.ui.components.ChapterRow
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusContinue
import com.example.ui.theme.StatusNotStarted
import com.example.ui.theme.StatusRevise
import com.example.ui.viewmodel.StudyUiState
import com.example.ui.viewmodel.StudyViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterListScreen(
    subjectId: String,
    uiState: StudyUiState,
    viewModel: StudyViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subject = uiState.subjects.find { it.id == subjectId }
    val chapters = uiState.chapters.filter { it.subjectId == subjectId }

    var selectedFilter by remember { mutableStateOf<String?>("ALL") }
    var selectedChapterForDetail by remember { mutableStateOf<ChapterEntity?>(null) }
    var showAddChapterDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val filteredChapters = remember(chapters, selectedFilter) {
        when (selectedFilter) {
            "COMPLETED" -> chapters.filter { it.status == ChapterStatus.COMPLETED.name }
            "CONTINUE" -> chapters.filter { it.status == ChapterStatus.CONTINUE.name }
            "REVISE" -> chapters.filter { it.status == ChapterStatus.REVISE.name }
            "NOT_STARTED" -> chapters.filter { it.status == ChapterStatus.NOT_STARTED.name }
            else -> chapters
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = subject?.name ?: "Subject Chapters",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Delete Subject", color = PriorityHigh) },
                                onClick = {
                                    showMenu = false
                                    showDeleteConfirmDialog = true
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddChapterDialog = true },
                containerColor = BrandBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_chapter_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Chapter")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Chapter", fontWeight = FontWeight.Bold)
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("chapter_list_screen"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Filter chips row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filterOptions = listOf(
                        "ALL" to "All (${chapters.size})",
                        "CONTINUE" to "Continue (${chapters.count { it.status == ChapterStatus.CONTINUE.name }})",
                        "REVISE" to "Revise (${chapters.count { it.status == ChapterStatus.REVISE.name }})",
                        "COMPLETED" to "Completed (${chapters.count { it.status == ChapterStatus.COMPLETED.name }})",
                        "NOT_STARTED" to "Not Started (${chapters.count { it.status == ChapterStatus.NOT_STARTED.name }})"
                    )

                    filterOptions.forEach { (key, label) ->
                        val isSelected = selectedFilter == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = key },
                            label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            if (filteredChapters.isEmpty()) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp)
                    ) {
                        Text(
                            text = "No chapters found in this filter.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            } else {
                items(filteredChapters, key = { it.id }) { chapter ->
                    ChapterRow(
                        chapter = chapter,
                        onStatusChange = { newStatus ->
                            viewModel.updateChapterStatus(chapter.id, newStatus)
                        },
                        onClick = {
                            selectedChapterForDetail = chapter
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Chapter Detail Dialog
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

    // Add Chapter Dialog
    if (showAddChapterDialog && subject != null) {
        AddChapterDialog(
            nextChapterNumber = chapters.size + 1,
            onDismiss = { showAddChapterDialog = false },
            onAdd = { name, difficulty ->
                val newChapter = ChapterEntity(
                    subjectId = subject.id,
                    chapterNumber = chapters.size + 1,
                    name = name,
                    difficulty = difficulty.name,
                    status = ChapterStatus.NOT_STARTED.name,
                    progressPercent = 0,
                    confidenceRating = 3,
                    totalSessionsEstimated = if (difficulty == ChapterDifficulty.HARD) 4 else 3
                )
                viewModel.saveChapter(newChapter)
                showAddChapterDialog = false
            }
        )
    }

    // Delete Subject Confirm Dialog
    if (showDeleteConfirmDialog && subject != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Subject?") },
            text = { Text("Are you sure you want to delete '${subject.name}' and all its ${chapters.size} chapters? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSubject(subject.id)
                        showDeleteConfirmDialog = false
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PriorityHigh)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AddChapterDialog(
    nextChapterNumber: Int,
    onDismiss: () -> Unit,
    onAdd: (name: String, difficulty: ChapterDifficulty) -> Unit
) {
    var chapterName by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf(ChapterDifficulty.MEDIUM) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Chapter $nextChapterNumber", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                OutlinedTextField(
                    value = chapterName,
                    onValueChange = { chapterName = it },
                    label = { Text("Chapter Title") },
                    placeholder = { Text("e.g. Electromagnetic Waves, Thermodynamics") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Difficulty Level",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ChapterDifficulty.entries.forEach { diff ->
                        val isSelected = selectedDifficulty == diff
                        val color = when (diff) {
                            ChapterDifficulty.EASY -> StatusCompleted
                            ChapterDifficulty.MEDIUM -> StatusContinue
                            ChapterDifficulty.HARD -> PriorityHigh
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) color else color.copy(alpha = 0.12f),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedDifficulty = diff }
                        ) {
                            Text(
                                text = diff.label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (isSelected) Color.White else color,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (chapterName.isNotBlank()) {
                        onAdd(chapterName.trim(), selectedDifficulty)
                    }
                },
                enabled = chapterName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add Chapter", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}
