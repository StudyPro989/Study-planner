package com.example.data.repository

import com.example.data.model.ChapterDifficulty
import com.example.data.model.ChapterEntity
import com.example.data.model.ChapterStatus
import com.example.data.model.SubjectEntity
import com.example.data.model.UserSettingsEntity
import java.util.UUID

object DefaultStudyData {

    fun getDefaultSettings(): UserSettingsEntity {
        // 42 days from now as requested in the design spec
        val examDateMillis = System.currentTimeMillis() + (42L * 24 * 60 * 60 * 1000)
        return UserSettingsEntity(
            id = "user_settings_singleton",
            userName = "Alex",
            examName = "Final Exam",
            examDateMillis = examDateMillis,
            dailyStudyHours = 3.0f,
            sessionDurationMinutes = 45,
            breakDurationMinutes = 10,
            preferredStartTime = "17:00", // 5:00 PM
            weekdayScheduleJson = """{"MON":3.0,"TUE":3.0,"WED":3.0,"THU":3.0,"FRI":3.0,"SAT":4.5,"SUN":4.5}""",
            studyStreak = 5,
            lastActiveDate = "",
            xpPoints = 840,
            notificationsEnabled = true,
            darkMode = false
        )
    }

    fun getDefaultSubjectsAndChapters(): Pair<List<SubjectEntity>, List<ChapterEntity>> {
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L

        val mathId = "sub_math_" + UUID.randomUUID().toString().take(8)
        val scienceId = "sub_sci_" + UUID.randomUUID().toString().take(8)
        val sstId = "sub_sst_" + UUID.randomUUID().toString().take(8)
        val englishId = "sub_eng_" + UUID.randomUUID().toString().take(8)
        val hindiId = "sub_hin_" + UUID.randomUUID().toString().take(8)
        val sanskritId = "sub_san_" + UUID.randomUUID().toString().take(8)
        val itId = "sub_it_" + UUID.randomUUID().toString().take(8)

        val subjects = listOf(
            SubjectEntity(id = mathId, name = "Mathematics", iconName = "calculate", colorHex = 0xFF3B82F6, orderIndex = 0),
            SubjectEntity(id = scienceId, name = "Science", iconName = "science", colorHex = 0xFF10B981, orderIndex = 1),
            SubjectEntity(id = sstId, name = "Social Science", iconName = "public", colorHex = 0xFFF97316, orderIndex = 2),
            SubjectEntity(id = englishId, name = "English", iconName = "menu_book", colorHex = 0xFF8B5CF6, orderIndex = 3),
            SubjectEntity(id = hindiId, name = "Hindi", iconName = "translate", colorHex = 0xFFEC4899, orderIndex = 4),
            SubjectEntity(id = sanskritId, name = "Sanskrit", iconName = "auto_stories", colorHex = 0xFF14B8A6, orderIndex = 5),
            SubjectEntity(id = itId, name = "Information Technology", iconName = "terminal", colorHex = 0xFF6366F1, orderIndex = 6)
        )

        val chapters = mutableListOf<ChapterEntity>()

        // 1. Mathematics Chapters
        val mathList = listOf(
            Triple("Real Numbers", ChapterDifficulty.EASY, ChapterStatus.COMPLETED to 100),
            Triple("Polynomials", ChapterDifficulty.MEDIUM, ChapterStatus.COMPLETED to 100),
            Triple("Linear Equations", ChapterDifficulty.MEDIUM, ChapterStatus.COMPLETED to 100),
            Triple("Quadratic Equations", ChapterDifficulty.HARD, ChapterStatus.CONTINUE to 55),
            Triple("Arithmetic Progressions", ChapterDifficulty.MEDIUM, ChapterStatus.CONTINUE to 40),
            Triple("Triangles", ChapterDifficulty.HARD, ChapterStatus.REVISE to 100),
            Triple("Coordinate Geometry", ChapterDifficulty.EASY, ChapterStatus.COMPLETED to 100),
            Triple("Trigonometry", ChapterDifficulty.HARD, ChapterStatus.CONTINUE to 20),
            Triple("Applications of Trigonometry", ChapterDifficulty.HARD, ChapterStatus.NOT_STARTED to 0),
            Triple("Circles", ChapterDifficulty.MEDIUM, ChapterStatus.REVISE to 100),
            Triple("Surface Areas & Volumes", ChapterDifficulty.HARD, ChapterStatus.NOT_STARTED to 0),
            Triple("Statistics & Probability", ChapterDifficulty.EASY, ChapterStatus.COMPLETED to 100)
        )
        mathList.forEachIndexed { index, (name, diff, statusProgress) ->
            val status = statusProgress.first
            val progress = statusProgress.second
            chapters.add(
                ChapterEntity(
                    subjectId = mathId,
                    chapterNumber = index + 1,
                    name = name,
                    difficulty = diff.name,
                    status = status.name,
                    progressPercent = progress,
                    confidenceRating = if (diff == ChapterDifficulty.HARD && status != ChapterStatus.COMPLETED) 2 else if (status == ChapterStatus.COMPLETED) 5 else 3,
                    firstStudiedDate = if (status != ChapterStatus.NOT_STARTED) now - (12 * oneDayMillis) else null,
                    lastStudiedDate = if (status != ChapterStatus.NOT_STARTED) now - (2 * oneDayMillis) else null,
                    lastRevisionDate = if (status == ChapterStatus.REVISE) now - (4 * oneDayMillis) else null,
                    nextRevisionDate = if (status == ChapterStatus.REVISE) now + (1 * oneDayMillis) else null,
                    revisionCount = if (status == ChapterStatus.REVISE || status == ChapterStatus.COMPLETED) 2 else 0,
                    completedSessions = if (status == ChapterStatus.COMPLETED) 3 else if (status == ChapterStatus.CONTINUE) 2 else 0,
                    totalSessionsEstimated = if (diff == ChapterDifficulty.HARD) 4 else 3,
                    isImportant = diff == ChapterDifficulty.HARD,
                    consecutiveMissedTasks = if (name == "Trigonometry") 2 else 0
                )
            )
        }

        // 2. Science Chapters
        val scienceList = listOf(
            Triple("Chemical Reactions & Equations", ChapterDifficulty.MEDIUM, ChapterStatus.COMPLETED to 100),
            Triple("Acids, Bases & Salts", ChapterDifficulty.MEDIUM, ChapterStatus.COMPLETED to 100),
            Triple("Metals and Non-metals", ChapterDifficulty.HARD, ChapterStatus.REVISE to 100),
            Triple("Carbon and its Compounds", ChapterDifficulty.HARD, ChapterStatus.CONTINUE to 45),
            Triple("Life Processes", ChapterDifficulty.HARD, ChapterStatus.REVISE to 100),
            Triple("Control and Coordination", ChapterDifficulty.HARD, ChapterStatus.NOT_STARTED to 0),
            Triple("How do Organisms Reproduce?", ChapterDifficulty.MEDIUM, ChapterStatus.CONTINUE to 50),
            Triple("Heredity and Evolution", ChapterDifficulty.HARD, ChapterStatus.NOT_STARTED to 0),
            Triple("Light: Reflection and Refraction", ChapterDifficulty.HARD, ChapterStatus.COMPLETED to 100),
            Triple("Human Eye and Colourful World", ChapterDifficulty.MEDIUM, ChapterStatus.COMPLETED to 100),
            Triple("Electricity", ChapterDifficulty.HARD, ChapterStatus.CONTINUE to 30),
            Triple("Our Environment", ChapterDifficulty.EASY, ChapterStatus.COMPLETED to 100)
        )
        scienceList.forEachIndexed { index, (name, diff, statusProgress) ->
            val status = statusProgress.first
            val progress = statusProgress.second
            chapters.add(
                ChapterEntity(
                    subjectId = scienceId,
                    chapterNumber = index + 1,
                    name = name,
                    difficulty = diff.name,
                    status = status.name,
                    progressPercent = progress,
                    confidenceRating = if (diff == ChapterDifficulty.HARD) 2 else 4,
                    firstStudiedDate = if (status != ChapterStatus.NOT_STARTED) now - (15 * oneDayMillis) else null,
                    lastStudiedDate = if (status != ChapterStatus.NOT_STARTED) now - (1 * oneDayMillis) else null,
                    lastRevisionDate = if (status == ChapterStatus.REVISE) now - (3 * oneDayMillis) else null,
                    nextRevisionDate = if (status == ChapterStatus.REVISE) now + (2 * oneDayMillis) else null,
                    revisionCount = if (status == ChapterStatus.REVISE || status == ChapterStatus.COMPLETED) 2 else 0,
                    completedSessions = if (status == ChapterStatus.COMPLETED) 3 else 1,
                    totalSessionsEstimated = if (diff == ChapterDifficulty.HARD) 4 else 3,
                    isImportant = diff == ChapterDifficulty.HARD
                )
            )
        }

        // 3. Social Science Chapters
        val sstList = listOf(
            Triple("The Rise of Nationalism in Europe", ChapterDifficulty.HARD, ChapterStatus.COMPLETED to 100),
            Triple("Nationalism in India", ChapterDifficulty.HARD, ChapterStatus.CONTINUE to 60),
            Triple("The Making of a Global World", ChapterDifficulty.MEDIUM, ChapterStatus.COMPLETED to 100),
            Triple("Resources and Development", ChapterDifficulty.EASY, ChapterStatus.COMPLETED to 100),
            Triple("Forest and Wildlife Resources", ChapterDifficulty.EASY, ChapterStatus.COMPLETED to 100),
            Triple("Water Resources", ChapterDifficulty.EASY, ChapterStatus.COMPLETED to 100),
            Triple("Agriculture", ChapterDifficulty.MEDIUM, ChapterStatus.REVISE to 100),
            Triple("Federalism", ChapterDifficulty.MEDIUM, ChapterStatus.CONTINUE to 30),
            Triple("Gender, Religion and Caste", ChapterDifficulty.MEDIUM, ChapterStatus.NOT_STARTED to 0),
            Triple("Political Parties", ChapterDifficulty.HARD, ChapterStatus.NOT_STARTED to 0),
            Triple("Money and Credit", ChapterDifficulty.MEDIUM, ChapterStatus.COMPLETED to 100),
            Triple("Globalisation and the Indian Economy", ChapterDifficulty.MEDIUM, ChapterStatus.NOT_STARTED to 0)
        )
        sstList.forEachIndexed { index, (name, diff, statusProgress) ->
            val status = statusProgress.first
            val progress = statusProgress.second
            chapters.add(
                ChapterEntity(
                    subjectId = sstId,
                    chapterNumber = index + 1,
                    name = name,
                    difficulty = diff.name,
                    status = status.name,
                    progressPercent = progress,
                    confidenceRating = if (diff == ChapterDifficulty.HARD) 3 else 4,
                    firstStudiedDate = if (status != ChapterStatus.NOT_STARTED) now - (10 * oneDayMillis) else null,
                    lastStudiedDate = if (status != ChapterStatus.NOT_STARTED) now - (2 * oneDayMillis) else null,
                    lastRevisionDate = if (status == ChapterStatus.REVISE) now - (5 * oneDayMillis) else null,
                    revisionCount = if (status == ChapterStatus.COMPLETED) 1 else 0,
                    completedSessions = if (status == ChapterStatus.COMPLETED) 2 else 1,
                    totalSessionsEstimated = 3
                )
            )
        }

        // 4. English Chapters
        val engList = listOf(
            Triple("A Letter to God", ChapterDifficulty.EASY, ChapterStatus.COMPLETED to 100),
            Triple("Nelson Mandela: Long Walk to Freedom", ChapterDifficulty.MEDIUM, ChapterStatus.COMPLETED to 100),
            Triple("Two Stories about Flying", ChapterDifficulty.EASY, ChapterStatus.COMPLETED to 100),
            Triple("From the Diary of Anne Frank", ChapterDifficulty.MEDIUM, ChapterStatus.COMPLETED to 100),
            Triple("Glimpses of India", ChapterDifficulty.EASY, ChapterStatus.CONTINUE to 50),
            Triple("Madam Rides the Bus", ChapterDifficulty.MEDIUM, ChapterStatus.NOT_STARTED to 0),
            Triple("The Sermon at Benares", ChapterDifficulty.MEDIUM, ChapterStatus.NOT_STARTED to 0),
            Triple("The Proposal (Play)", ChapterDifficulty.HARD, ChapterStatus.NOT_STARTED to 0),
            Triple("A Triumph of Surgery", ChapterDifficulty.EASY, ChapterStatus.COMPLETED to 100),
            Triple("The Thief's Story", ChapterDifficulty.EASY, ChapterStatus.REVISE to 100),
            Triple("Footprints without Feet", ChapterDifficulty.MEDIUM, ChapterStatus.NOT_STARTED to 0)
        )
        engList.forEachIndexed { index, (name, diff, statusProgress) ->
            val status = statusProgress.first
            val progress = statusProgress.second
            chapters.add(
                ChapterEntity(
                    subjectId = englishId,
                    chapterNumber = index + 1,
                    name = name,
                    difficulty = diff.name,
                    status = status.name,
                    progressPercent = progress,
                    confidenceRating = 4,
                    firstStudiedDate = if (status != ChapterStatus.NOT_STARTED) now - (8 * oneDayMillis) else null,
                    lastStudiedDate = if (status != ChapterStatus.NOT_STARTED) now - (3 * oneDayMillis) else null,
                    revisionCount = if (status == ChapterStatus.COMPLETED) 1 else 0,
                    completedSessions = if (status == ChapterStatus.COMPLETED) 2 else 0,
                    totalSessionsEstimated = 2
                )
            )
        }

        // 5. Information Technology
        val itList = listOf(
            Triple("Communication Skills", ChapterDifficulty.EASY, ChapterStatus.COMPLETED to 100),
            Triple("Self-Management Skills", ChapterDifficulty.EASY, ChapterStatus.COMPLETED to 100),
            Triple("ICT Skills & Cybersecurity", ChapterDifficulty.MEDIUM, ChapterStatus.COMPLETED to 100),
            Triple("Digital Documentation (Advanced)", ChapterDifficulty.MEDIUM, ChapterStatus.CONTINUE to 60),
            Triple("Electronic Spreadsheet (Advanced)", ChapterDifficulty.HARD, ChapterStatus.CONTINUE to 30),
            Triple("Database Management System (DBMS)", ChapterDifficulty.HARD, ChapterStatus.NOT_STARTED to 0),
            Triple("Web Applications and Security", ChapterDifficulty.HARD, ChapterStatus.NOT_STARTED to 0)
        )
        itList.forEachIndexed { index, (name, diff, statusProgress) ->
            val status = statusProgress.first
            val progress = statusProgress.second
            chapters.add(
                ChapterEntity(
                    subjectId = itId,
                    chapterNumber = index + 1,
                    name = name,
                    difficulty = diff.name,
                    status = status.name,
                    progressPercent = progress,
                    confidenceRating = if (diff == ChapterDifficulty.HARD) 2 else 4,
                    firstStudiedDate = if (status != ChapterStatus.NOT_STARTED) now - (6 * oneDayMillis) else null,
                    lastStudiedDate = if (status != ChapterStatus.NOT_STARTED) now - (1 * oneDayMillis) else null,
                    revisionCount = if (status == ChapterStatus.COMPLETED) 1 else 0,
                    completedSessions = if (status == ChapterStatus.COMPLETED) 2 else 0,
                    totalSessionsEstimated = 3
                )
            )
        }

        // 6. Hindi
        val hindiList = listOf(
            Triple("Surdas ke Pad", ChapterDifficulty.MEDIUM, ChapterStatus.COMPLETED to 100),
            Triple("Ram-Lakshman-Parshuram Samvad", ChapterDifficulty.HARD, ChapterStatus.REVISE to 100),
            Triple("Netaji ka Chashma", ChapterDifficulty.EASY, ChapterStatus.COMPLETED to 100),
            Triple("Balgobin Bhagat", ChapterDifficulty.MEDIUM, ChapterStatus.CONTINUE to 40),
            Triple("Mata ka Anchal", ChapterDifficulty.EASY, ChapterStatus.COMPLETED to 100),
            Triple("Sana Sana Hath Jodi", ChapterDifficulty.MEDIUM, ChapterStatus.NOT_STARTED to 0)
        )
        hindiList.forEachIndexed { index, (name, diff, statusProgress) ->
            val status = statusProgress.first
            val progress = statusProgress.second
            chapters.add(
                ChapterEntity(
                    subjectId = hindiId,
                    chapterNumber = index + 1,
                    name = name,
                    difficulty = diff.name,
                    status = status.name,
                    progressPercent = progress,
                    confidenceRating = 4,
                    firstStudiedDate = if (status != ChapterStatus.NOT_STARTED) now - (7 * oneDayMillis) else null,
                    lastStudiedDate = if (status != ChapterStatus.NOT_STARTED) now - (2 * oneDayMillis) else null,
                    revisionCount = if (status == ChapterStatus.COMPLETED) 1 else 0,
                    completedSessions = if (status == ChapterStatus.COMPLETED) 2 else 0,
                    totalSessionsEstimated = 2
                )
            )
        }

        // 7. Sanskrit
        val sanList = listOf(
            Triple("Shuchi Paryavaranam", ChapterDifficulty.MEDIUM, ChapterStatus.COMPLETED to 100),
            Triple("Buddhirbalavati Sada", ChapterDifficulty.MEDIUM, ChapterStatus.COMPLETED to 100),
            Triple("Shishulalanam", ChapterDifficulty.HARD, ChapterStatus.CONTINUE to 40),
            Triple("Janani Tulyavatsala", ChapterDifficulty.MEDIUM, ChapterStatus.NOT_STARTED to 0),
            Triple("Subhashitani", ChapterDifficulty.EASY, ChapterStatus.REVISE to 100)
        )
        sanList.forEachIndexed { index, (name, diff, statusProgress) ->
            val status = statusProgress.first
            val progress = statusProgress.second
            chapters.add(
                ChapterEntity(
                    subjectId = sanskritId,
                    chapterNumber = index + 1,
                    name = name,
                    difficulty = diff.name,
                    status = status.name,
                    progressPercent = progress,
                    confidenceRating = 3,
                    firstStudiedDate = if (status != ChapterStatus.NOT_STARTED) now - (5 * oneDayMillis) else null,
                    lastStudiedDate = if (status != ChapterStatus.NOT_STARTED) now - (1 * oneDayMillis) else null,
                    revisionCount = if (status == ChapterStatus.COMPLETED) 1 else 0,
                    completedSessions = if (status == ChapterStatus.COMPLETED) 2 else 0,
                    totalSessionsEstimated = 2
                )
            )
        }

        return Pair(subjects, chapters)
    }
}
