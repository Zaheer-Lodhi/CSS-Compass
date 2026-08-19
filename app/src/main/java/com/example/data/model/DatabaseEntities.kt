package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: String = "current_user",
    val fullName: String = "Syed Muhammad Ali",
    val username: String = "ali_css2026",
    val email: String = "ali.css@gmail.com",
    val phoneNumber: String = "+92 300 1234567",
    val province: String = "Punjab",
    val examType: String = "CSS", // CSS, PCS, PMS
    val targetYear: Int = 2026,
    val preferredSubjects: String = "Pakistan Affairs, Current Affairs, English, Essay",
    val dailyStudyHoursGoal: Int = 6,
    val points: Int = 340,
    val streakDays: Int = 5,
    val isPremium: Boolean = false,
    val badges: String = "First Step, Syllabus Explorer, Daily Streak v5" // Comma-separated
)

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val isCompulsory: Boolean = true,
    val isCustom: Boolean = false
)

@Entity(tableName = "mcqs")
data class MCQ(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val question: String,
    val optionsJson: String, // Serialized list of strings (e.g. ["Option A", "Option B", "Option C", "Option D"])
    val correctAnswerIndex: Int,
    val explanation: String,
    val difficulty: String, // Easy, Medium, Hard
    val subjectName: String,
    val topic: String,
    val bookReference: String = "CSS Companion guide",
    val isBookmarked: Boolean = false,
    val isReviewedLater: Boolean = false,
    val isAttempted: Boolean = false,
    val wasCorrect: Boolean = false
)

@Entity(tableName = "past_papers")
data class PastPaper(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val year: Int,
    val subjectName: String,
    val examType: String = "CSS",
    val isSolved: Boolean,
    val title: String,
    val questionsJson: String, // JSON questions or plain description
    val solutionText: String = ""
)

@Entity(tableName = "subject_notes")
data class SubjectNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectName: String,
    val title: String,
    val shortSummary: String,
    val contentMarkdown: String,
    val isBookmarked: Boolean = false,
    val highlightedPhrases: String = "" // Comma-separated highlighted text
)

@Entity(tableName = "current_affairs")
data class CurrentAffair(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateString: String, // e.g., "July 6, 2026"
    val title: String,
    val category: String, // Pakistan, International, Editorial
    val summary: String,
    val fullText: String,
    val isBookmarked: Boolean = false
)

@Entity(tableName = "essay_topics")
data class EssayTopic(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // Political, Economic, Social, Literary
    val outlineGuideline: String,
    val sampleEssay: String,
    val difficulty: String = "Medium"
)

@Entity(tableName = "vocabulary")
data class VocabularyWord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val word: String,
    val phonetic: String = "",
    val meaning: String,
    val usageExample: String,
    val synonyms: String = "", // Comma-separated
    val antonyms: String = "", // Comma-separated
    val isFavorite: Boolean = false
)

@Entity(tableName = "flashcards")
data class Flashcard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectName: String,
    val question: String,
    val answer: String,
    val isBookmarked: Boolean = false,
    val reviewIntervalDays: Int = 1,
    val nextReviewTimestamp: Long = 0L
)

@Entity(tableName = "chat_sessions")
data class ChatSession(
    @PrimaryKey val id: String, // unique session UUID or id
    val title: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: String,
    val role: String, // "user" or "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "leaderboard_users")
data class LeaderboardUser(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val province: String,
    val points: Int,
    val rank: Int,
    val isPremium: Boolean = false
)

@Entity(tableName = "daily_tasks")
data class DailyTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val xpReward: Int = 50,
    val isCompleted: Boolean = false
)
