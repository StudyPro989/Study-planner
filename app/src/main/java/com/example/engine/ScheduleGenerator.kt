package com.example.engine

import com.example.data.model.ChapterDifficulty
import com.example.data.model.ChapterEntity
import com.example.data.model.ChapterStatus
import com.example.data.model.StudyTaskEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.TaskPriority
import com.example.data.model.TaskType
import com.example.data.model.UserSettingsEntity
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.max

data class ScheduleStatusReport(
    val status: ScheduleProgressStatus,
    val expectedProgressPercent: Int,
    val actualProgressPercent: Int,
    val differencePercent: Int,
    val summaryMessage: String,
    val daysRemaining: Int,
    val unfinishedChaptersCount: Int,
    val reviseChaptersCount: Int,
    val completedChaptersCount: Int,
    val totalChaptersCount: Int,
    val weakChapters: List<ChapterEntity>
)

enum class ScheduleProgressStatus(val label: String, val emoji: String) {
    ON_TRACK("On Track", "🟢"),
    SLIGHTLY_BEHIND("Slightly Behind", "🟡"),
    BEHIND_SCHEDULE("Behind Schedule", "🔴")
}

data class GeneratedScheduleResult(
    val dailyTasks: List<StudyTaskEntity>,
    val statusReport: ScheduleStatusReport
)

object ScheduleGenerator {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    fun calculateDaysRemaining(examDateMillis: Long, currentDateMillis: Long = System.currentTimeMillis()): Int {
        val diffMillis = examDateMillis - currentDateMillis
        return max(0, (diffMillis / (24 * 60 * 60 * 1000L)).toInt())
    }

    fun detectWeakChapters(chapters: List<ChapterEntity>): List<ChapterEntity> {
        return chapters.filter { chapter ->
            val isLowConfidence = chapter.confidenceRating <= 2
            val isHardAndIncomplete = chapter.difficulty == ChapterDifficulty.HARD.name &&
                    (chapter.status == ChapterStatus.CONTINUE.name || chapter.status == ChapterStatus.NOT_STARTED.name)
            val hasMissedTasks = chapter.consecutiveMissedTasks >= 1
            val isOverdueRevision = chapter.status == ChapterStatus.REVISE.name &&
                    chapter.nextRevisionDate != null &&
                    chapter.nextRevisionDate <= System.currentTimeMillis()

            isLowConfidence || isHardAndIncomplete || hasMissedTasks || isOverdueRevision
        }.sortedWith(
            compareByDescending<ChapterEntity> { it.consecutiveMissedTasks }
                .thenBy { it.confidenceRating }
                .thenByDescending { it.difficulty == ChapterDifficulty.HARD.name }
        )
    }

    fun evaluateScheduleStatus(
        examDateMillis: Long,
        chapters: List<ChapterEntity>,
        currentDateMillis: Long = System.currentTimeMillis()
    ): ScheduleStatusReport {
        val total = chapters.size
        val completed = chapters.count { it.status == ChapterStatus.COMPLETED.name }
        val toContinue = chapters.count { it.status == ChapterStatus.CONTINUE.name }
        val toRevise = chapters.count { it.status == ChapterStatus.REVISE.name }
        val unfinished = total - completed

        val actualProgress = if (total > 0) ((completed.toFloat() / total) * 100).toInt() else 100
        val daysRemaining = calculateDaysRemaining(examDateMillis, currentDateMillis)

        // Target progress calculation: assuming an ideal 60-day prep window
        val idealTotalPrepDays = 60
        val daysPassed = max(0, idealTotalPrepDays - daysRemaining)
        val expectedProgress = if (daysRemaining <= 0) {
            100
        } else {
            minOf(95, ((daysPassed.toFloat() / idealTotalPrepDays) * 100).toInt())
        }

        val diff = actualProgress - expectedProgress
        val weakList = detectWeakChapters(chapters)

        val status = when {
            diff >= -3 -> ScheduleProgressStatus.ON_TRACK
            diff >= -12 -> ScheduleProgressStatus.SLIGHTLY_BEHIND
            else -> ScheduleProgressStatus.BEHIND_SCHEDULE
        }

        val message = when (status) {
            ScheduleProgressStatus.ON_TRACK ->
                "Great job! You are on pace with your exam preparation ($actualProgress% completed). Keep up the rhythm!"
            ScheduleProgressStatus.SLIGHTLY_BEHIND ->
                "You are ${-diff}% behind your planned progress. Today's schedule prioritizes critical continuation chapters to get you back on track."
            ScheduleProgressStatus.BEHIND_SCHEDULE ->
                "You are ${-diff}% behind schedule. The planner has increased daily review density and shifted non-critical chapters."
        }

        return ScheduleStatusReport(
            status = status,
            expectedProgressPercent = expectedProgress,
            actualProgressPercent = actualProgress,
            differencePercent = diff,
            summaryMessage = message,
            daysRemaining = daysRemaining,
            unfinishedChaptersCount = toContinue + chapters.count { it.status == ChapterStatus.NOT_STARTED.name },
            reviseChaptersCount = toRevise,
            completedChaptersCount = completed,
            totalChaptersCount = total,
            weakChapters = weakList
        )
    }

    fun generateWeeklySchedule(
        subjects: List<SubjectEntity>,
        chapters: List<ChapterEntity>,
        settings: UserSettingsEntity,
        startDateCalendar: Calendar = Calendar.getInstance(),
        daysToGenerate: Int = 7
    ): GeneratedScheduleResult {
        val subjectMap = subjects.associateBy { it.id }
        val allGeneratedTasks = mutableListOf<StudyTaskEntity>()
        val report = evaluateScheduleStatus(settings.examDateMillis, chapters)

        val daysRemaining = report.daysRemaining
        val sessionDuration = max(25, settings.sessionDurationMinutes)
        val breakDuration = max(5, settings.breakDurationMinutes)

        // Parse weekday hours if configured
        val weekdayHoursMap = parseWeekdayHours(settings.weekdayScheduleJson, settings.dailyStudyHours)

        // Clone chapters to simulate progress throughout the week
        val simulatedChapters = chapters.map { it.copy() }.toMutableList()

        val cal = startDateCalendar.clone() as Calendar

        for (dayOffset in 0 until daysToGenerate) {
            val dateStr = dateFormat.format(cal.time)
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val dayKey = when (dayOfWeek) {
                Calendar.MONDAY -> "MON"
                Calendar.TUESDAY -> "TUE"
                Calendar.WEDNESDAY -> "WED"
                Calendar.THURSDAY -> "THU"
                Calendar.FRIDAY -> "FRI"
                Calendar.SATURDAY -> "SAT"
                Calendar.SUNDAY -> "SUN"
                else -> "MON"
            }

            val availableHours = weekdayHoursMap[dayKey] ?: settings.dailyStudyHours
            val totalAvailableMinutes = (availableHours * 60).toInt()
            val totalSlotDuration = sessionDuration + breakDuration
            val numSessions = max(1, totalAvailableMinutes / totalSlotDuration)

            // Determine focus ratios based on exam proximity
            // >30 days: 60% Continue, 30% Revise, 10% Practice
            // 15-30 days: 40% Continue, 40% Revise, 20% Practice
            // <15 days: 20% Continue, 50% Revise, 30% Practice
            // <7 days: 10% Continue, 60% Revise, 30% Practice
            val effectiveDaysRemaining = max(0, daysRemaining - dayOffset)

            val (continueRatio, reviseRatio, practiceRatio) = when {
                effectiveDaysRemaining > 30 -> Triple(0.60f, 0.30f, 0.10f)
                effectiveDaysRemaining in 15..30 -> Triple(0.40f, 0.40f, 0.20f)
                effectiveDaysRemaining in 7..14 -> Triple(0.20f, 0.50f, 0.30f)
                else -> Triple(0.10f, 0.60f, 0.30f)
            }

            val dailyTasksForDay = generateTasksForSingleDay(
                dateStr = dateStr,
                numSessions = numSessions,
                sessionDuration = sessionDuration,
                breakDuration = breakDuration,
                preferredStartTime = settings.preferredStartTime,
                continueRatio = continueRatio,
                reviseRatio = reviseRatio,
                practiceRatio = practiceRatio,
                daysRemaining = effectiveDaysRemaining,
                subjects = subjects,
                subjectMap = subjectMap,
                chapters = simulatedChapters,
                dayOffset = dayOffset
            )

            allGeneratedTasks.addAll(dailyTasksForDay)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        return GeneratedScheduleResult(
            dailyTasks = allGeneratedTasks,
            statusReport = report
        )
    }

    private fun generateTasksForSingleDay(
        dateStr: String,
        numSessions: Int,
        sessionDuration: Int,
        breakDuration: Int,
        preferredStartTime: String,
        continueRatio: Float,
        reviseRatio: Float,
        practiceRatio: Float,
        daysRemaining: Int,
        subjects: List<SubjectEntity>,
        subjectMap: Map<String, SubjectEntity>,
        chapters: MutableList<ChapterEntity>,
        dayOffset: Int
    ): List<StudyTaskEntity> {
        val tasks = mutableListOf<StudyTaskEntity>()
        val startCalendar = parseTimeOrDefault(preferredStartTime)

        // Separate candidate chapters by status
        val continueCandidates = chapters.filter {
            it.status == ChapterStatus.CONTINUE.name || it.status == ChapterStatus.NOT_STARTED.name
        }.toMutableList()

        val reviseCandidates = chapters.filter {
            it.status == ChapterStatus.REVISE.name ||
                    (it.status == ChapterStatus.COMPLETED.name && (it.confidenceRating <= 3 || it.revisionCount < 3))
        }.toMutableList()

        val weakCandidates = detectWeakChapters(chapters).toMutableList()

        // Prioritization sort
        continueCandidates.sortWith(
            compareByDescending<ChapterEntity> { it.consecutiveMissedTasks }
                .thenByDescending { it.difficulty == ChapterDifficulty.HARD.name }
                .thenBy { it.progressPercent }
        )

        reviseCandidates.sortWith(
            compareBy<ChapterEntity> { it.confidenceRating }
                .thenBy { it.lastRevisionDate ?: 0L }
        )

        // Subject rotation tracker to avoid repetition within the same day
        val usedSubjectIdsToday = mutableSetOf<String>()

        for (sessionIndex in 0 until numSessions) {
            val startTimeStr = timeFormat.format(startCalendar.time)
            startCalendar.add(Calendar.MINUTE, sessionDuration)
            val endTimeStr = timeFormat.format(startCalendar.time)

            // Select task category based on index & ratios
            val fraction = (sessionIndex.toFloat() + 0.5f) / numSessions
            val taskType: TaskType
            val selectedChapter: ChapterEntity?
            val priority: TaskPriority
            val reason: String

            when {
                // First slot on close exam / weak chapter available -> Weak Area / High Priority
                (sessionIndex == 0 && weakCandidates.isNotEmpty() && daysRemaining <= 20) -> {
                    taskType = TaskType.WEAK_AREA
                    selectedChapter = pickDiverseChapter(weakCandidates, usedSubjectIdsToday) ?: weakCandidates.firstOrNull()
                    priority = TaskPriority.HIGH
                    reason = "High priority weak area detected with exam in $daysRemaining days."
                }
                fraction <= continueRatio && continueCandidates.isNotEmpty() -> {
                    taskType = TaskType.CONTINUE
                    selectedChapter = pickDiverseChapter(continueCandidates, usedSubjectIdsToday) ?: continueCandidates.firstOrNull()
                    priority = if (selectedChapter?.difficulty == ChapterDifficulty.HARD.name || daysRemaining <= 15) TaskPriority.HIGH else TaskPriority.MEDIUM
                    reason = "Scheduled to complete syllabus progression (${selectedChapter?.progressPercent ?: 0}% done)."
                }
                fraction <= (continueRatio + reviseRatio) && reviseCandidates.isNotEmpty() -> {
                    taskType = TaskType.REVISE
                    selectedChapter = pickDiverseChapter(reviseCandidates, usedSubjectIdsToday) ?: reviseCandidates.firstOrNull()
                    priority = if (selectedChapter?.confidenceRating ?: 3 <= 2) TaskPriority.HIGH else TaskPriority.MEDIUM
                    reason = "Spaced repetition revision to cement retention before the exam."
                }
                else -> {
                    // Practice / Mock / Quick revision
                    if (reviseCandidates.isNotEmpty()) {
                        taskType = if (daysRemaining <= 7) TaskType.MOCK_TEST else TaskType.PRACTICE
                        selectedChapter = pickDiverseChapter(reviseCandidates, usedSubjectIdsToday) ?: reviseCandidates.firstOrNull()
                        priority = TaskPriority.MEDIUM
                        reason = "Exam practice session to test speed, formulas, and concept clarity."
                    } else if (continueCandidates.isNotEmpty()) {
                        taskType = TaskType.CONTINUE
                        selectedChapter = pickDiverseChapter(continueCandidates, usedSubjectIdsToday) ?: continueCandidates.firstOrNull()
                        priority = TaskPriority.MEDIUM
                        reason = "Scheduled continuation to maintain study pace."
                    } else {
                        // All done! Quick revision of any subject
                        selectedChapter = chapters.firstOrNull()
                        taskType = TaskType.QUICK_REVISION
                        priority = TaskPriority.LOW
                        reason = "General recap and problem solving."
                    }
                }
            }

            if (selectedChapter != null) {
                usedSubjectIdsToday.add(selectedChapter.subjectId)
                val subjectName = subjectMap[selectedChapter.subjectId]?.name ?: "Subject"

                tasks.add(
                    StudyTaskEntity(
                        id = UUID.randomUUID().toString(),
                        date = dateStr,
                        subjectId = selectedChapter.subjectId,
                        subjectName = subjectName,
                        chapterId = selectedChapter.id,
                        chapterName = selectedChapter.name,
                        chapterNumber = selectedChapter.chapterNumber,
                        taskType = taskType.code,
                        startTime = startTimeStr,
                        endTime = endTimeStr,
                        durationMinutes = sessionDuration,
                        isCompleted = false,
                        completedAt = null,
                        priority = priority.name,
                        reason = reason,
                        notes = "Target: Study key topics, solve practice questions, take concise notes.",
                        orderIndex = sessionIndex
                    )
                )
            }

            // Add break time before next session
            startCalendar.add(Calendar.MINUTE, breakDuration)
        }

        return tasks
    }

    private fun pickDiverseChapter(
        candidates: MutableList<ChapterEntity>,
        usedSubjectIds: Set<String>
    ): ChapterEntity? {
        val preferred = candidates.find { !usedSubjectIds.contains(it.subjectId) }
        val chosen = preferred ?: candidates.firstOrNull()
        if (chosen != null) {
            candidates.remove(chosen)
        }
        return chosen
    }

    private fun parseWeekdayHours(json: String, defaultHours: Float): Map<String, Float> {
        val result = mutableMapOf<String, Float>()
        try {
            val obj = JSONObject(json)
            val keys = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
            for (key in keys) {
                if (obj.has(key)) {
                    result[key] = obj.getDouble(key).toFloat()
                } else {
                    result[key] = defaultHours
                }
            }
        } catch (_: Exception) {
            listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN").forEach {
                result[it] = defaultHours
            }
        }
        return result
    }

    private fun parseTimeOrDefault(timeStr: String): Calendar {
        val cal = Calendar.getInstance()
        try {
            val parts = timeStr.split(":")
            if (parts.size == 2) {
                val hour = parts[0].trim().toInt()
                val min = parts[1].trim().toInt()
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, min)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                return cal
            }
        } catch (_: Exception) {
            // fallback
        }
        cal.set(Calendar.HOUR_OF_DAY, 17)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal
    }
}
