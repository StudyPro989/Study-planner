package com.example.engine

import com.example.BuildConfig
import com.example.data.model.ChapterEntity
import com.example.data.model.ChapterStatus
import com.example.data.model.StudyTaskEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.UserSettingsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageSender {
    USER, AI
}

class AiCoachService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun getAiResponse(
        userPrompt: String,
        userSettings: UserSettingsEntity,
        subjects: List<SubjectEntity>,
        chapters: List<ChapterEntity>,
        todayTasks: List<StudyTaskEntity>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        // Evaluate context stats
        val totalChapters = chapters.size
        val completedChapters = chapters.count { it.status == ChapterStatus.COMPLETED.name }
        val continueChapters = chapters.count { it.status == ChapterStatus.CONTINUE.name }
        val reviseChapters = chapters.count { it.status == ChapterStatus.REVISE.name }
        val daysRemaining = ScheduleGenerator.calculateDaysRemaining(userSettings.examDateMillis)
        val weakChapters = ScheduleGenerator.detectWeakChapters(chapters).take(4)

        val weakNames = weakChapters.joinToString(", ") { "${it.name} (${it.difficulty})" }
        val todayPlanSummary = todayTasks.joinToString("; ") {
            "${it.startTime}-${it.endTime}: ${it.subjectName} - ${it.chapterName} [${it.taskType}]"
        }

        val systemPrompt = """
            You are a warm, encouraging, structured, and expert AI Study Coach powered by Google Gemini 3.5.
            You are mentoring a student named ${userSettings.userName}.
            
            Student Academic Context:
            - Target Exam: ${userSettings.examName} in $daysRemaining days
            - Total Chapters: $totalChapters (Completed: $completedChapters, In Progress: $continueChapters, Priority Revision: $reviseChapters)
            - Identified Weak / High-Priority Areas: ${if (weakNames.isNotEmpty()) weakNames else "None flagged"}
            - Today's Schedule: ${if (todayPlanSummary.isNotEmpty()) todayPlanSummary else "No tasks generated yet for today"}
            - Daily Capacity: ${userSettings.dailyStudyHours} hours/day (${userSettings.sessionDurationMinutes}m sessions / ${userSettings.breakDurationMinutes}m breaks)
            
            Instructions:
            - Provide clear, direct, actionable advice formatted with bullet points and bold headers.
            - Answer questions directly using the student's real subject & chapter data.
            - Keep responses structured, concise, and highly practical.
        """.trimIndent()

        if (!apiKey.isNullOrBlank() && !apiKey.contains("MY_GEMINI_API_KEY")) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

                val requestJson = JSONObject().apply {
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
                    })
                    put("contents", JSONArray().put(JSONObject().apply {
                        put("parts", JSONArray().put(JSONObject().put("text", userPrompt)))
                    }))
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.7)
                    })
                }

                val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder().url(url).post(body).build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val respStr = response.body?.string() ?: ""
                    val root = JSONObject(respStr)
                    val candidates = root.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val text = candidates.getJSONObject(0)
                            .optJSONObject("content")
                            ?.optJSONArray("parts")
                            ?.optJSONObject(0)
                            ?.optString("text")
                        if (!text.isNullOrBlank()) {
                            return@withContext text
                        }
                    }
                }
            } catch (_: Exception) {
                // Fallback to local intelligent assistant engine
            }
        }

        // Fallback local intelligent study assistant engine
        generateLocalCoachResponse(
            userPrompt,
            userSettings.userName,
            daysRemaining,
            completedChapters,
            continueChapters,
            reviseChapters,
            weakChapters,
            todayTasks
        )
    }

    private fun generateLocalCoachResponse(
        prompt: String,
        userName: String,
        daysRemaining: Int,
        completed: Int,
        continueCount: Int,
        reviseCount: Int,
        weakChapters: List<ChapterEntity>,
        todayTasks: List<StudyTaskEntity>
    ): String {
        val lower = prompt.lowercase()

        return when {
            lower.contains("what should i study") || lower.contains("today") -> {
                if (todayTasks.isNotEmpty()) {
                    val taskList = todayTasks.joinToString("\n") {
                        "• **${it.startTime} - ${it.endTime}**: ${it.subjectName} — *${it.chapterName}* (${it.taskType})"
                    }
                    "Here is your customized study plan for today, $userName:\n\n$taskList\n\n💡 **Tip:** Focus on finishing one chapter block before taking your scheduled 10-minute break!"
                } else {
                    "You have no tasks scheduled yet for today. Tap **'Generate Plan'** on the Schedule tab to build an optimized timetable based on your $daysRemaining days remaining!"
                }
            }

            lower.contains("revise") || lower.contains("revision") -> {
                if (weakChapters.isNotEmpty()) {
                    val weakList = weakChapters.take(3).joinToString(", ") { it.name }
                    "Based on your spaced repetition curve, prioritize revising:\n\n1. **$weakList** (identifies as high-impact weak area)\n2. Run a 25-minute flashcard recall test before sleeping.\n3. Mark chapters as 'Revise' once you complete sample questions."
                } else {
                    "You are doing well! For revision today, review the formula sheets and summary notes for completed chapters. Keep a 1:1 ratio between active practice and theory review."
                }
            }

            lower.contains("track") || lower.contains("exam") || lower.contains("ready") -> {
                val paceMsg = if (daysRemaining > 30) "You have a comfortable buffer" else if (daysRemaining > 14) "You are in the vital consolidation phase" else "You are in the final countdown sprint"
                "📊 **Exam Readiness Report ($daysRemaining Days Left)**:\n\n• **Completed:** $completed chapters\n• **In Progress:** $continueCount chapters\n• **Pending Revision:** $reviseCount chapters\n\n$paceMsg! Stick to your ${if (todayTasks.isNotEmpty()) todayTasks.size else 3} daily sessions and you will comfortably complete the syllabus."
            }

            lower.contains("missed") || lower.contains("yesterday") || lower.contains("behind") -> {
                "Don't worry about missed sessions! Our scheduler automatically catches you up:\n\n1. Go to the **Schedule** tab and tap **'Reschedule Missed Tasks'**.\n2. The algorithm will rebalance tomorrow's sessions without overwhelming you.\n3. Take a quick 5-minute breather and tackle the first task on today's list."
            }

            lower.contains("weak") || lower.contains("difficult") || lower.contains("hard") -> {
                if (weakChapters.isNotEmpty()) {
                    val weakList = weakChapters.joinToString("\n") { "• **${it.name}** (${it.difficulty} difficulty)" }
                    "Here are your detected priority weak areas:\n\n$weakList\n\n🎯 **Strategy:** Break each hard topic into 30-minute micro-sessions: 15 mins concept breakdown + 15 mins solved examples."
                } else {
                    "No critical weak spots detected right now! Keep marking confidence ratings after each study session so I can calibrate your revision schedule."
                }
            }

            else -> {
                "Hello $userName! With **$daysRemaining days** until your exam, consistency is your superpower.\n\n• You have **$completed chapters** mastered and **$continueCount** underway.\n• Tap any task in **Today's Plan** to check it off as you study.\n• Ask me anytime: *'What should I study today?'*, *'Which chapters should I revise?'*, or *'Am I on track?'*"
            }
        }
    }
}
