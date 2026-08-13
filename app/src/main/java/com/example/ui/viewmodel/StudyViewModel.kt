package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ChapterDifficulty
import com.example.data.model.ChapterEntity
import com.example.data.model.ChapterStatus
import com.example.data.model.RevisionHistoryEntity
import com.example.data.model.StudyTaskEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.UserSettingsEntity
import com.example.data.repository.DefaultStudyData
import com.example.data.repository.StudyRepository
import com.example.engine.AiCoachService
import com.example.engine.ChatMessage
import com.example.engine.MessageSender
import com.example.engine.ScheduleGenerator
import com.example.engine.ScheduleProgressStatus
import com.example.engine.ScheduleStatusReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class StudyUiState(
    val subjects: List<SubjectEntity> = emptyList(),
    val chapters: List<ChapterEntity> = emptyList(),
    val allTasks: List<StudyTaskEntity> = emptyList(),
    val userSettings: UserSettingsEntity = DefaultStudyData.getDefaultSettings(),
    val revisionHistory: List<RevisionHistoryEntity> = emptyList(),
    val statusReport: ScheduleStatusReport = ScheduleStatusReport(
        status = ScheduleProgressStatus.ON_TRACK,
        expectedProgressPercent = 50,
        actualProgressPercent = 50,
        differencePercent = 0,
        summaryMessage = "Analyzing schedule...",
        daysRemaining = 42,
        unfinishedChaptersCount = 0,
        reviseChaptersCount = 0,
        completedChaptersCount = 0,
        totalChaptersCount = 0,
        weakChapters = emptyList()
    ),
    val selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val todayDateStr: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val tomorrowDateStr: String = getOffsetDateStr(1),
    val isGenerating: Boolean = false,
    val chatMessages: List<ChatMessage> = listOf(
        ChatMessage(
            sender = MessageSender.AI,
            text = "👋 Hello! I am your AI Study Coach. Ask me what to study today, how to tackle difficult chapters, or check if you're on track for your exam!"
        )
    ),
    val isAiThinking: Boolean = false
)

private fun getOffsetDateStr(days: Int): String {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, days)
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
}

class StudyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudyRepository
    private val aiCoachService = AiCoachService()

    private val _selectedDate = MutableStateFlow(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    )
    private val _isGenerating = MutableStateFlow(false)
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.AI,
                text = "👋 Hello! I am your AI Study Coach. Ask me what to study today, how to tackle difficult chapters, or check if you're on track for your exam!"
            )
        )
    )
    private val _isAiThinking = MutableStateFlow(false)

    val uiState: StateFlow<StudyUiState>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = StudyRepository(db.studyDao())

        viewModelScope.launch {
            repository.checkAndInitializeDefaults()
            // Auto generate initial tasks if none exist
            checkAndGenerateInitialSchedule()
        }

        uiState = combine(
            repository.allSubjects,
            repository.allChapters,
            repository.allTasks,
            repository.userSettings,
            repository.allRevisions,
            _selectedDate,
            _chatMessages,
            _isAiThinking,
            _isGenerating
        ) { args: Array<Any?> ->
            @Suppress("UNCHECKED_CAST")
            val subjects = args[0] as List<com.example.data.model.SubjectEntity>
            @Suppress("UNCHECKED_CAST")
            val chapters = args[1] as List<ChapterEntity>
            @Suppress("UNCHECKED_CAST")
            val tasks = args[2] as List<com.example.data.model.StudyTaskEntity>
            val settings = args[3] as com.example.data.model.UserSettingsEntity?
            @Suppress("UNCHECKED_CAST")
            val revisions = args[4] as List<com.example.data.model.RevisionHistoryEntity>
            val selectedDate = args[5] as String
            @Suppress("UNCHECKED_CAST")
            val messages = args[6] as List<ChatMessage>
            val isThinking = args[7] as Boolean
            val isGen = args[8] as Boolean

            val nonNullSettings = settings ?: DefaultStudyData.getDefaultSettings()
            val report = ScheduleGenerator.evaluateScheduleStatus(nonNullSettings.examDateMillis, chapters)

            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val tomorrowStr = getOffsetDateStr(1)

            StudyUiState(
                subjects = subjects,
                chapters = chapters,
                allTasks = tasks,
                userSettings = nonNullSettings,
                revisionHistory = revisions,
                statusReport = report,
                selectedDate = selectedDate,
                todayDateStr = todayStr,
                tomorrowDateStr = tomorrowStr,
                isGenerating = isGen,
                chatMessages = messages,
                isAiThinking = isThinking
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StudyUiState()
        )
    }

    private suspend fun checkAndGenerateInitialSchedule() {
        val subjects = repository.allSubjects
        val chapters = repository.allChapters
        // Initial schedule auto-builder
        viewModelScope.launch {
            repository.allTasks.collect { tasks ->
                if (tasks.isEmpty()) {
                    generateSchedule()
                }
            }
        }
    }

    fun setSelectedDate(dateStr: String) {
        _selectedDate.value = dateStr
    }

    fun toggleTask(taskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(taskId, isCompleted)
        }
    }

    fun updateChapterStatus(
        chapterId: String,
        newStatus: ChapterStatus,
        progressPercent: Int? = null,
        confidence: Int? = null
    ) {
        viewModelScope.launch {
            repository.updateChapterStatus(chapterId, newStatus, progressPercent, confidence)
        }
    }

    fun saveChapter(chapter: ChapterEntity) {
        viewModelScope.launch {
            repository.saveChapter(chapter)
        }
    }

    fun deleteChapter(chapterId: String) {
        viewModelScope.launch {
            repository.deleteChapter(chapterId)
        }
    }

    fun saveSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.saveSubject(subject)
        }
    }

    fun deleteSubject(subjectId: String) {
        viewModelScope.launch {
            repository.deleteSubject(subjectId)
        }
    }

    fun updateUserSettings(settings: UserSettingsEntity) {
        viewModelScope.launch {
            repository.updateUserSettings(settings)
        }
    }

    fun generateSchedule(days: Int = 7) {
        viewModelScope.launch {
            _isGenerating.value = true
            val currentState = uiState.value
            val todayStr = currentState.todayDateStr

            // Delete upcoming uncompleted tasks to regenerate fresh plan
            repository.deleteFutureUncompletedTasks(todayStr)

            val generated = ScheduleGenerator.generateWeeklySchedule(
                subjects = currentState.subjects,
                chapters = currentState.chapters,
                settings = currentState.userSettings,
                startDateCalendar = Calendar.getInstance(),
                daysToGenerate = days
            )

            repository.saveTasks(generated.dailyTasks)
            _isGenerating.value = false
        }
    }

    fun rescheduleMissedTasks() {
        viewModelScope.launch {
            val currentState = uiState.value
            val todayStr = currentState.todayDateStr
            val missedTasks = currentState.allTasks.filter {
                it.date < todayStr && !it.isCompleted
            }

            if (missedTasks.isNotEmpty()) {
                // Shift missed tasks to today/tomorrow
                val shiftedTasks = missedTasks.mapIndexed { index, task ->
                    task.copy(
                        date = todayStr,
                        priority = "HIGH",
                        reason = "Rescheduled missed task from ${task.date}.",
                        orderIndex = task.orderIndex + 10
                    )
                }
                repository.saveTasks(shiftedTasks)
            } else {
                generateSchedule(7)
            }
        }
    }

    fun sendAiMessage(userPrompt: String) {
        val trimmed = userPrompt.trim()
        if (trimmed.isBlank()) return

        val userMsg = ChatMessage(sender = MessageSender.USER, text = trimmed)
        _chatMessages.value = _chatMessages.value + userMsg
        _isAiThinking.value = true

        viewModelScope.launch {
            val state = uiState.value
            val todayTasks = state.allTasks.filter { it.date == state.todayDateStr }
            val replyText = aiCoachService.getAiResponse(
                userPrompt = trimmed,
                userSettings = state.userSettings,
                subjects = state.subjects,
                chapters = state.chapters,
                todayTasks = todayTasks
            )

            val aiMsg = ChatMessage(sender = MessageSender.AI, text = replyText)
            _chatMessages.value = _chatMessages.value + aiMsg
            _isAiThinking.value = false
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            repository.resetAllDataToDefaults()
            generateSchedule(7)
        }
    }
}
