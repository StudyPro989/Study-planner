package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudyTaskEntity
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandBlueContainer
import com.example.ui.theme.EnglishColor
import com.example.ui.theme.HindiColor
import com.example.ui.theme.ITColor
import com.example.ui.theme.MathColor
import com.example.ui.theme.OnBrandBlueContainer
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityHighContainer
import com.example.ui.theme.SanskritColor
import com.example.ui.theme.ScienceColor
import com.example.ui.theme.SocialScienceColor
import com.example.ui.theme.StatusContinueBg
import com.example.ui.theme.StatusContinueText
import com.example.ui.theme.StatusNotStartedBg
import com.example.ui.theme.StatusNotStartedText
import com.example.ui.theme.StatusReviseBg
import com.example.ui.theme.StatusReviseText

fun getSubjectBrandColor(subjectName: String): Color {
    val lower = subjectName.lowercase()
    return when {
        lower.contains("math") -> MathColor
        lower.contains("sci") -> ScienceColor
        lower.contains("soc") || lower.contains("sst") || lower.contains("hist") -> SocialScienceColor
        lower.contains("eng") -> EnglishColor
        lower.contains("hin") -> HindiColor
        lower.contains("san") -> SanskritColor
        lower.contains("it") || lower.contains("comp") -> ITColor
        else -> BrandBlue
    }
}

@Composable
fun TaskCard(
    task: StudyTaskEntity,
    onToggleCompleted: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onTaskClick: (() -> Unit)? = null
) {
    var isExpanded by remember { mutableStateOf(false) }

    val (pillBg, pillText, pillLabel) = when (task.taskType) {
        "REVISE", "QUICK_REVISION" -> Triple(StatusReviseBg, StatusReviseText, "REVISE")
        "CONTINUE" -> Triple(StatusContinueBg, StatusContinueText, "CONTINUE")
        "WEAK_AREA" -> Triple(PriorityHighContainer, PriorityHigh, "WEAK AREA")
        "MOCK_TEST" -> Triple(BrandBlueContainer, OnBrandBlueContainer, "MOCK TEST")
        else -> Triple(StatusContinueBg, StatusContinueText, "STUDY")
    }

    val subjectColor = getSubjectBrandColor(task.subjectName)

    val cardBg by animateColorAsState(
        targetValue = if (task.isCompleted) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "task_card_bg"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(
            1.dp,
            if (task.isCompleted) Color.Transparent else MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable {
                if (onTaskClick != null) onTaskClick() else isExpanded = !isExpanded
            }
            .testTag("task_card_${task.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left section: Time column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.width(54.dp)
                ) {
                    Text(
                        text = task.startTime,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = task.endTime,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 10.sp
                        )
                    )
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Middle section: Subject + Pill + Title
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = task.subjectName.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else subjectColor,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                fontSize = 11.sp
                            )
                        )

                        // Status pill badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (task.isCompleted) StatusNotStartedBg else pillBg
                        ) {
                            Text(
                                text = if (task.isCompleted) "DONE" else pillLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (task.isCompleted) StatusNotStartedText else pillText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.4.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = task.chapterName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Right section: Professional square-rounded checkbox
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (task.isCompleted) BrandBlue else Color.Transparent
                        )
                        .border(
                            width = 2.dp,
                            color = if (task.isCompleted) BrandBlue else MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onToggleCompleted(!task.isCompleted) }
                        .testTag("task_checkbox_${task.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (task.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Reasoning & AI rationale when expanded or populated
            if (isExpanded || (task.reason.isNotBlank() && isExpanded)) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Reason",
                            tint = BrandBlue,
                            modifier = Modifier.size(16.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = task.reason.ifBlank { "Intelligently prioritized for optimal retention." },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

