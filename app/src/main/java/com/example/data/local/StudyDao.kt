package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ChapterEntity
import com.example.data.model.RevisionHistoryEntity
import com.example.data.model.StudyTaskEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {

    // --- Subjects ---
    @Query("SELECT * FROM subjects ORDER BY orderIndex ASC, name ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :subjectId LIMIT 1")
    suspend fun getSubjectById(subjectId: String): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Query("DELETE FROM subjects WHERE id = :subjectId")
    suspend fun deleteSubjectById(subjectId: String)

    // --- Chapters ---
    @Query("SELECT * FROM chapters ORDER BY subjectId, chapterNumber ASC")
    fun getAllChapters(): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE subjectId = :subjectId ORDER BY chapterNumber ASC")
    fun getChaptersBySubject(subjectId: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE id = :chapterId LIMIT 1")
    suspend fun getChapterById(chapterId: String): ChapterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Update
    suspend fun updateChapter(chapter: ChapterEntity)

    @Query("DELETE FROM chapters WHERE id = :chapterId")
    suspend fun deleteChapterById(chapterId: String)

    @Query("DELETE FROM chapters WHERE subjectId = :subjectId")
    suspend fun deleteChaptersBySubjectId(subjectId: String)

    // --- Study Tasks ---
    @Query("SELECT * FROM study_tasks ORDER BY date ASC, orderIndex ASC")
    fun getAllTasks(): Flow<List<StudyTaskEntity>>

    @Query("SELECT * FROM study_tasks WHERE date = :date ORDER BY orderIndex ASC")
    fun getTasksForDate(date: String): Flow<List<StudyTaskEntity>>

    @Query("SELECT * FROM study_tasks WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC, orderIndex ASC")
    fun getTasksInRange(startDate: String, endDate: String): Flow<List<StudyTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: StudyTaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<StudyTaskEntity>)

    @Update
    suspend fun updateTask(task: StudyTaskEntity)

    @Query("DELETE FROM study_tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)

    @Query("DELETE FROM study_tasks WHERE date >= :startDate AND isCompleted = 0")
    suspend fun deleteUncompletedTasksFrom(startDate: String)

    @Query("DELETE FROM study_tasks WHERE date = :date")
    suspend fun deleteTasksForDate(date: String)

    // --- User Settings ---
    @Query("SELECT * FROM user_settings WHERE id = 'user_settings_singleton' LIMIT 1")
    fun getUserSettingsFlow(): Flow<UserSettingsEntity?>

    @Query("SELECT * FROM user_settings WHERE id = 'user_settings_singleton' LIMIT 1")
    suspend fun getUserSettings(): UserSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSettings(settings: UserSettingsEntity)

    // --- Revision History ---
    @Query("SELECT * FROM revision_history ORDER BY completedTimestamp DESC")
    fun getAllRevisions(): Flow<List<RevisionHistoryEntity>>

    @Query("SELECT * FROM revision_history WHERE chapterId = :chapterId ORDER BY completedTimestamp DESC")
    fun getRevisionsForChapter(chapterId: String): Flow<List<RevisionHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRevision(revision: RevisionHistoryEntity)

    @Query("DELETE FROM revision_history")
    suspend fun clearRevisionHistory()

    @Query("DELETE FROM study_tasks")
    suspend fun clearAllTasks()

    @Query("DELETE FROM chapters")
    suspend fun clearAllChapters()

    @Query("DELETE FROM subjects")
    suspend fun clearAllSubjects()
}
