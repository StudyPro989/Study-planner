package com.example.data.repository

import com.example.data.local.StudyDao
import com.example.data.model.ChapterEntity
import com.example.data.model.ChapterStatus
import com.example.data.model.RevisionHistoryEntity
import com.example.data.model.StudyTaskEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.UserSettingsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class StudyRepository(private val dao: StudyDao) {

    val allSubjects: Flow<List<SubjectEntity>> = dao.getAllSubjects()
    val allChapters: Flow<List<ChapterEntity>> = dao.getAllChapters()
    val allTasks: Flow<List<StudyTaskEntity>> = dao.getAllTasks()
    val userSettings: Flow<UserSettingsEntity?> = dao.getUserSettingsFlow()
    val allRevisions: Flow<List<RevisionHistoryEntity>> = dao.getAllRevisions()

    fun getChaptersForSubject(subjectId: String): Flow<List<ChapterEntity>> =
        dao.getChaptersBySubject(subjectId)

    fun getTasksForDate(date: String): Flow<List<StudyTaskEntity>> =
        dao.getTasksForDate(date)

    fun getTasksInRange(startDate: String, endDate: String): Flow<List<StudyTaskEntity>> =
        dao.getTasksInRange(startDate, endDate)

    suspend fun checkAndInitializeDefaults() = withContext(Dispatchers.IO) {
        val existingSettings = dao.getUserSettings()
        if (existingSettings == null) {
            dao.insertUserSettings(DefaultStudyData.getDefaultSettings())
        }

        val existingSubjects = dao.getAllSubjects().firstOrNull()
        if (existingSubjects.isNullOrEmpty()) {
            val (subjects, chapters) = DefaultStudyData.getDefaultSubjectsAndChapters()
            dao.insertSubjects(subjects)
            dao.insertChapters(chapters)
        }
    }

    suspend fun updateUserSettings(settings: UserSettingsEntity) = withContext(Dispatchers.IO) {
        dao.insertUserSettings(settings)
    }

    suspend fun saveSubject(subject: SubjectEntity) = withContext(Dispatchers.IO) {
        dao.insertSubject(subject)
    }

    suspend fun deleteSubject(subjectId: String) = withContext(Dispatchers.IO) {
        dao.deleteChaptersBySubjectId(subjectId)
        dao.deleteSubjectById(subjectId)
    }

    suspend fun saveChapter(chapter: ChapterEntity) = withContext(Dispatchers.IO) {
        dao.insertChapter(chapter)
    }

    suspend fun deleteChapter(chapterId: String) = withContext(Dispatchers.IO) {
        dao.deleteChapterById(chapterId)
    }

    suspend fun updateChapterStatus(
        chapterId: String,
        newStatus: ChapterStatus,
        progressPercent: Int? = null,
        confidence: Int? = null
    ) = withContext(Dispatchers.IO) {
        val chapter = dao.getChapterById(chapterId) ?: return@withContext
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L

        val updatedProgress = progressPercent ?: when (newStatus) {
            ChapterStatus.COMPLETED -> 100
            ChapterStatus.REVISE -> 100
            ChapterStatus.CONTINUE -> if (chapter.progressPercent == 0) 25 else chapter.progressPercent
            ChapterStatus.NOT_STARTED -> 0
        }

        val nextRev = if (newStatus == ChapterStatus.COMPLETED || newStatus == ChapterStatus.REVISE) {
            now + (1 * oneDayMillis)
        } else {
            null
        }

        val updated = chapter.copy(
            status = newStatus.name,
            progressPercent = updatedProgress,
            confidenceRating = confidence ?: chapter.confidenceRating,
            lastStudiedDate = now,
            firstStudiedDate = chapter.firstStudiedDate ?: now,
            lastRevisionDate = if (newStatus == ChapterStatus.REVISE || newStatus == ChapterStatus.COMPLETED) now else chapter.lastRevisionDate,
            nextRevisionDate = nextRev ?: chapter.nextRevisionDate,
            revisionCount = if (newStatus == ChapterStatus.COMPLETED || newStatus == ChapterStatus.REVISE) chapter.revisionCount + 1 else chapter.revisionCount
        )
        dao.updateChapter(updated)
    }

    suspend fun toggleTaskCompletion(taskId: String, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        val tasks = dao.getAllTasks().firstOrNull() ?: emptyList()
        val task = tasks.find { it.id == taskId } ?: return@withContext
        val now = System.currentTimeMillis()

        val updatedTask = task.copy(
            isCompleted = isCompleted,
            completedAt = if (isCompleted) now else null
        )
        dao.updateTask(updatedTask)

        // If task was completed, award XP & update chapter stats
        val currentSettings = dao.getUserSettings() ?: DefaultStudyData.getDefaultSettings()
        if (isCompleted) {
            val updatedSettings = currentSettings.copy(
                xpPoints = currentSettings.xpPoints + 25,
                lastActiveDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now))
            )
            dao.insertUserSettings(updatedSettings)

            // Update Chapter Progress if associated
            val chapter = dao.getChapterById(task.chapterId)
            if (chapter != null) {
                val newSessions = chapter.completedSessions + 1
                val isDone = newSessions >= chapter.totalSessionsEstimated
                val newStatus = if (isDone) ChapterStatus.COMPLETED else ChapterStatus.CONTINUE
                val newProgress = if (isDone) 100 else minOf(90, ((newSessions.toFloat() / chapter.totalSessionsEstimated) * 100).toInt())

                val updatedChapter = chapter.copy(
                    completedSessions = newSessions,
                    status = newStatus.name,
                    progressPercent = newProgress,
                    lastStudiedDate = now,
                    firstStudiedDate = chapter.firstStudiedDate ?: now,
                    consecutiveMissedTasks = 0
                )
                dao.updateChapter(updatedChapter)

                // Log revision history if it was a revision or completed task
                if (task.taskType == "REVISE" || task.taskType == "QUICK_REVISION" || isDone) {
                    dao.insertRevision(
                        RevisionHistoryEntity(
                            id = UUID.randomUUID().toString(),
                            chapterId = chapter.id,
                            chapterName = chapter.name,
                            subjectName = task.subjectName,
                            revisionNumber = chapter.revisionCount + 1,
                            completedTimestamp = now,
                            confidenceRating = chapter.confidenceRating,
                            notes = "Completed scheduled task: ${task.startTime} - ${task.endTime}"
                        )
                    )
                }
            }
        }
    }

    suspend fun saveTasks(tasks: List<StudyTaskEntity>) = withContext(Dispatchers.IO) {
        dao.insertTasks(tasks)
    }

    suspend fun deleteFutureUncompletedTasks(fromFormattedDate: String) = withContext(Dispatchers.IO) {
        dao.deleteUncompletedTasksFrom(fromFormattedDate)
    }

    suspend fun resetAllDataToDefaults() = withContext(Dispatchers.IO) {
        dao.clearRevisionHistory()
        dao.clearAllTasks()
        dao.clearAllChapters()
        dao.clearAllSubjects()
        val (subjects, chapters) = DefaultStudyData.getDefaultSubjectsAndChapters()
        dao.insertSubjects(subjects)
        dao.insertChapters(chapters)
        dao.insertUserSettings(DefaultStudyData.getDefaultSettings())
    }
}
