package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val iconName: String = "book",
    val colorHex: Long = 0xFF4F46E5,
    val orderIndex: Int = 0,
    val targetExamWeight: Int = 100 // % importance
)

enum class ChapterDifficulty(val label: String, val weight: Float) {
    EASY("Easy", 0.75f),
    MEDIUM("Medium", 1.0f),
    HARD("Hard", 1.5f);

    companion object {
        fun fromString(value: String): ChapterDifficulty =
            entries.find { it.name.equals(value, ignoreCase = true) || it.label.equals(value, ignoreCase = true) } ?: MEDIUM
    }
}

enum class ChapterStatus(val label: String, val icon: String) {
    NOT_STARTED("Not Started", "⬜"),
    CONTINUE("Continue", "🟡"),
    REVISE("Revise", "🔵"),
    COMPLETED("Completed", "🟢");

    companion object {
        fun fromString(value: String): ChapterStatus =
            entries.find { it.name.equals(value, ignoreCase = true) || it.label.equals(value, ignoreCase = true) } ?: NOT_STARTED
    }
}

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val subjectId: String,
    val chapterNumber: Int,
    val name: String,
    val difficulty: String = ChapterDifficulty.MEDIUM.name,
    val status: String = ChapterStatus.NOT_STARTED.name,
    val progressPercent: Int = 0, // 0 to 100
    val confidenceRating: Int = 3, // 1 to 5 stars
    val firstStudiedDate: Long? = null,
    val lastStudiedDate: Long? = null,
    val lastRevisionDate: Long? = null,
    val nextRevisionDate: Long? = null,
    val revisionCount: Int = 0,
    val completedSessions: Int = 0,
    val totalSessionsEstimated: Int = 3, // Estimated sessions based on difficulty
    val personalNotes: String = "",
    val isImportant: Boolean = false,
    val consecutiveMissedTasks: Int = 0
)

enum class TaskType(val label: String, val code: String) {
    CONTINUE("Continue", "CONTINUE"),
    REVISE("Revise", "REVISE"),
    PRACTICE("Practice", "PRACTICE"),
    WEAK_AREA("Weak Area", "WEAK_AREA"),
    MOCK_TEST("Mock Test", "MOCK_TEST"),
    QUICK_REVISION("Quick Revision", "QUICK_REVISION");

    companion object {
        fun fromCode(code: String): TaskType =
            entries.find { it.code.equals(code, ignoreCase = true) || it.name.equals(code, ignoreCase = true) } ?: CONTINUE
    }
}

enum class TaskPriority(val label: String) {
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low");

    companion object {
        fun fromString(value: String): TaskPriority =
            entries.find { it.name.equals(value, ignoreCase = true) || it.label.equals(value, ignoreCase = true) } ?: MEDIUM
    }
}

@Entity(tableName = "study_tasks")
data class StudyTaskEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val date: String, // Format: YYYY-MM-DD
    val subjectId: String,
    val subjectName: String,
    val chapterId: String,
    val chapterName: String,
    val chapterNumber: Int,
    val taskType: String = TaskType.CONTINUE.code,
    val startTime: String, // e.g. "05:00 PM"
    val endTime: String,   // e.g. "05:45 PM"
    val durationMinutes: Int = 45,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val priority: String = TaskPriority.MEDIUM.name,
    val reason: String = "",
    val notes: String = "",
    val orderIndex: Int = 0
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val id: String = "user_settings_singleton",
    val userName: String = "Alex Rivera",
    val examName: String = "Final Board Exams",
    val examDateMillis: Long = System.currentTimeMillis() + (42L * 24 * 60 * 60 * 1000), // Default 42 days from now
    val dailyStudyHours: Float = 3.0f,
    val sessionDurationMinutes: Int = 45,
    val breakDurationMinutes: Int = 10,
    val preferredStartTime: String = "17:00", // 5:00 PM
    val weekdayScheduleJson: String = """{"MON":3.0,"TUE":3.0,"WED":3.0,"THU":3.0,"FRI":3.0,"SAT":4.5,"SUN":4.5}""",
    val studyStreak: Int = 5,
    val lastActiveDate: String = "",
    val xpPoints: Int = 620,
    val notificationsEnabled: Boolean = true,
    val darkMode: Boolean = false
)

@Entity(tableName = "revision_history")
data class RevisionHistoryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val chapterId: String,
    val chapterName: String,
    val subjectName: String,
    val revisionNumber: Int,
    val completedTimestamp: Long = System.currentTimeMillis(),
    val confidenceRating: Int = 4,
    val notes: String = ""
)
