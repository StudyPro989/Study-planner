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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChapterDifficulty
import com.example.data.model.ChapterEntity
import com.example.data.model.ChapterStatus
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandBlueContainer
import com.example.ui.theme.OnBrandBlueContainer
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityHighContainer
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusCompletedContainer
import com.example.ui.theme.StatusContinue
import com.example.ui.theme.StatusContinueContainer
import com.example.ui.theme.StatusNotStarted
import com.example.ui.theme.StatusNotStartedContainer
import com.example.ui.theme.StatusRevise
import com.example.ui.theme.StatusReviseContainer

@Composable
fun ChapterRow(
    chapter: ChapterEntity,
    onStatusChange: (ChapterStatus) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isStatusMenuOpen by remember { mutableStateOf(false) }

    val statusEnum = ChapterStatus.fromString(chapter.status)
    val (statusColor, statusBg) = when (statusEnum) {
        ChapterStatus.COMPLETED -> StatusCompleted to StatusCompletedContainer
        ChapterStatus.CONTINUE -> StatusContinue to StatusContinueContainer
        ChapterStatus.REVISE -> StatusRevise to StatusReviseContainer
        ChapterStatus.NOT_STARTED -> StatusNotStarted to StatusNotStartedContainer
    }

    val difficultyEnum = ChapterDifficulty.fromString(chapter.difficulty)
    val (diffColor, diffBg) = when (difficultyEnum) {
        ChapterDifficulty.HARD -> PriorityHigh to PriorityHighContainer
        ChapterDifficulty.MEDIUM -> StatusContinue to StatusContinueContainer
        ChapterDifficulty.EASY -> StatusCompleted to StatusCompletedContainer
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("chapter_row_${chapter.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chapter index pill
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BrandBlueContainer,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${chapter.chapterNumber}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = OnBrandBlueContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = chapter.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        // Difficulty badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = diffBg
                        ) {
                            Text(
                                text = chapter.difficulty,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = diffColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Confidence stars
                        Row {
                            for (i in 1..5) {
                                Icon(
                                    imageVector = if (i <= chapter.confidenceRating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                    contentDescription = null,
                                    tint = if (i <= chapter.confidenceRating) Color(0xFFEAB308) else MaterialTheme.colorScheme.outlineVariant,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Interactive Status Pill with Menu
                Box {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = statusBg,
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { isStatusMenuOpen = true }
                            .testTag("chapter_status_pill_${chapter.id}")
                    ) {
                        Text(
                            text = "${statusEnum.icon} ${statusEnum.label}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = statusColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = isStatusMenuOpen,
                        onDismissRequest = { isStatusMenuOpen = false }
                    ) {
                        ChapterStatus.entries.forEach { st ->
                            DropdownMenuItem(
                                text = { Text("${st.icon}  ${st.label}", style = MaterialTheme.typography.bodyMedium) },
                                onClick = {
                                    onStatusChange(st)
                                    isStatusMenuOpen = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { chapter.progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

