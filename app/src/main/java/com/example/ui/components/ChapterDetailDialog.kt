package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.ChapterDifficulty
import com.example.data.model.ChapterEntity
import com.example.data.model.ChapterStatus
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusContinue
import com.example.ui.theme.StatusNotStarted
import com.example.ui.theme.StatusRevise
import com.example.ui.theme.StudyIndigo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChapterDetailDialog(
    chapter: ChapterEntity,
    onDismiss: () -> Unit,
    onSave: (ChapterEntity) -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    var status by remember { mutableStateOf(ChapterStatus.fromString(chapter.status)) }
    var difficulty by remember { mutableStateOf(ChapterDifficulty.fromString(chapter.difficulty)) }
    var progress by remember { mutableFloatStateOf(chapter.progressPercent.toFloat()) }
    var confidence by remember { mutableIntStateOf(chapter.confidenceRating) }
    var notes by remember { mutableStateOf(chapter.personalNotes) }

    val firstStudiedStr = chapter.firstStudiedDate?.let { dateFormat.format(Date(it)) } ?: "Not yet studied"
    val lastStudiedStr = chapter.lastStudiedDate?.let { dateFormat.format(Date(it)) } ?: "Never"
    val lastRevisionStr = chapter.lastRevisionDate?.let { dateFormat.format(Date(it)) } ?: "None"
    val nextRevisionStr = chapter.nextRevisionDate?.let { dateFormat.format(Date(it)) } ?: "Recommended in 3 days"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("chapter_detail_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Chapter ${chapter.chapterNumber}",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = StudyIndigo,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = chapter.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Status Selector Row
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ChapterStatus.entries.forEach { st ->
                        val isSelected = status == st
                        val chipColor = when (st) {
                            ChapterStatus.COMPLETED -> StatusCompleted
                            ChapterStatus.CONTINUE -> StatusContinue
                            ChapterStatus.REVISE -> StatusRevise
                            ChapterStatus.NOT_STARTED -> StatusNotStarted
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) chipColor else chipColor.copy(alpha = 0.1f),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    status = st
                                    if (st == ChapterStatus.COMPLETED) progress = 100f
                                    if (st == ChapterStatus.NOT_STARTED) progress = 0f
                                }
                        ) {
                            Text(
                                text = st.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) Color.White else chipColor,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Completion Progress",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        text = "${progress.toInt()}%",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = StudyIndigo
                        )
                    )
                }
                Slider(
                    value = progress,
                    onValueChange = { progress = it },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = StudyIndigo,
                        activeTrackColor = StudyIndigo
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Difficulty and Confidence Ratings
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Difficulty
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Difficulty",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            ChapterDifficulty.entries.forEach { diff ->
                                val isSelected = difficulty == diff
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) StudyIndigo else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { difficulty = diff }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = diff.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Confidence Stars
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Confidence Level",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row {
                            for (i in 1..5) {
                                IconButton(
                                    onClick = { confidence = i },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (i <= confidence) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                        contentDescription = "Star $i",
                                        tint = if (i <= confidence) Color(0xFFFBBF24) else MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Study Info Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        DetailInfoRow(label = "First studied", value = firstStudiedStr)
                        DetailInfoRow(label = "Last studied", value = lastStudiedStr)
                        DetailInfoRow(label = "Revision count", value = "${chapter.revisionCount} times")
                        DetailInfoRow(label = "Last revision", value = lastRevisionStr)
                        DetailInfoRow(label = "Next recommended revision", value = nextRevisionStr)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Personal Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Personal Chapter Notes") },
                    placeholder = { Text("Key formulas, doubts, important questions...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            status = ChapterStatus.COMPLETED
                            progress = 100f
                            onSave(
                                chapter.copy(
                                    status = status.name,
                                    difficulty = difficulty.name,
                                    progressPercent = 100,
                                    confidenceRating = confidence,
                                    personalNotes = notes,
                                    lastStudiedDate = System.currentTimeMillis()
                                )
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusCompleted),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Completed", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            status = ChapterStatus.REVISE
                            onSave(
                                chapter.copy(
                                    status = status.name,
                                    difficulty = difficulty.name,
                                    progressPercent = progress.toInt(),
                                    confidenceRating = confidence,
                                    personalNotes = notes,
                                    lastStudiedDate = System.currentTimeMillis()
                                )
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusRevise),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Revise", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Save / Delete / Reset Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            onDelete()
                            onDismiss()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Chapter",
                            tint = PriorityHigh
                        )
                    }

                    Button(
                        onClick = {
                            onSave(
                                chapter.copy(
                                    status = status.name,
                                    difficulty = difficulty.name,
                                    progressPercent = progress.toInt(),
                                    confidenceRating = confidence,
                                    personalNotes = notes
                                )
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudyIndigo),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}
