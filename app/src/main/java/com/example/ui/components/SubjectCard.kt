package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChapterEntity
import com.example.data.model.ChapterStatus
import com.example.data.model.SubjectEntity
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusContinue
import com.example.ui.theme.StatusRevise

fun getSubjectIcon(name: String): ImageVector {
    val lower = name.lowercase()
    return when {
        lower.contains("math") -> Icons.Default.Calculate
        lower.contains("sci") -> Icons.Default.Science
        lower.contains("soc") || lower.contains("sst") || lower.contains("history") -> Icons.Default.Public
        lower.contains("eng") -> Icons.Default.MenuBook
        lower.contains("hin") || lower.contains("trans") -> Icons.Default.Translate
        lower.contains("san") -> Icons.Default.AutoStories
        lower.contains("it") || lower.contains("comp") || lower.contains("tech") -> Icons.Default.Computer
        else -> Icons.Default.MenuBook
    }
}

@Composable
fun SubjectCard(
    subject: SubjectEntity,
    chapters: List<ChapterEntity>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalChapters = chapters.size
    val completedCount = chapters.count { it.status == ChapterStatus.COMPLETED.name }
    val continueCount = chapters.count { it.status == ChapterStatus.CONTINUE.name }
    val reviseCount = chapters.count { it.status == ChapterStatus.REVISE.name }

    val progress = if (totalChapters > 0) (completedCount.toFloat() / totalChapters) else 0f
    val progressPercent = (progress * 100).toInt()
    val subjectColor = Color(subject.colorHex)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .testTag("subject_card_${subject.id}")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = subjectColor.copy(alpha = 0.12f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = getSubjectIcon(subject.name),
                                contentDescription = subject.name,
                                tint = subjectColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = subject.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "$totalChapters Chapters • $completedCount Mastered",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                // Circular Progress Indicator with text in center
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(48.dp),
                        color = subjectColor,
                        trackColor = subjectColor.copy(alpha = 0.12f),
                        strokeWidth = 4.dp,
                        strokeCap = StrokeCap.Round
                    )
                    Text(
                        text = "$progressPercent%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sub-status breakdown pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusCounterChip(
                    label = "$completedCount Done",
                    color = StatusCompleted,
                    bgColor = StatusCompleted.copy(alpha = 0.12f)
                )
                StatusCounterChip(
                    label = "$continueCount In Progress",
                    color = StatusContinue,
                    bgColor = StatusContinue.copy(alpha = 0.12f)
                )
                StatusCounterChip(
                    label = "$reviseCount Needs Review",
                    color = StatusRevise,
                    bgColor = StatusRevise.copy(alpha = 0.12f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = subjectColor,
                trackColor = subjectColor.copy(alpha = 0.12f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun StatusCounterChip(
    label: String,
    color: Color,
    bgColor: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

