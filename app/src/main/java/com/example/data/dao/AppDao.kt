package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- User Profile ---
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getUserProfileOneShot(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)

    @Update
    suspend fun updateUserProfile(profile: UserProfile)

    // --- Subjects ---
    @Query("SELECT * FROM subjects")
    fun getSubjects(): Flow<List<Subject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject)

    // --- MCQs ---
    @Query("SELECT * FROM mcqs")
    fun getAllMCQs(): Flow<List<MCQ>>

    @Query("SELECT * FROM mcqs WHERE subjectName = :subject")
    fun getMCQsBySubject(subject: String): Flow<List<MCQ>>

    @Query("SELECT * FROM mcqs WHERE isBookmarked = 1")
    fun getBookmarkedMCQs(): Flow<List<MCQ>>

    @Query("SELECT * FROM mcqs WHERE id = :id")
    suspend fun getMCQById(id: Int): MCQ?

    @Update
    suspend fun updateMCQ(mcq: MCQ)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMCQs(list: List<MCQ>)

    // --- Past Papers ---
    @Query("SELECT * FROM past_papers ORDER BY year DESC")
    fun getPastPapers(): Flow<List<PastPaper>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPastPapers(list: List<PastPaper>)

    // --- Subject Notes ---
    @Query("SELECT * FROM subject_notes")
    fun getSubjectNotes(): Flow<List<SubjectNote>>

    @Query("SELECT * FROM subject_notes WHERE subjectName = :subject")
    fun getNotesBySubject(subject: String): Flow<List<SubjectNote>>

    @Update
    suspend fun updateSubjectNote(note: SubjectNote)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjectNotes(list: List<SubjectNote>)

    // --- Current Affairs ---
    @Query("SELECT * FROM current_affairs ORDER BY id DESC")
    fun getCurrentAffairs(): Flow<List<CurrentAffair>>

    @Update
    suspend fun updateCurrentAffair(affair: CurrentAffair)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrentAffairs(list: List<CurrentAffair>)

    // --- Essay Topics ---
    @Query("SELECT * FROM essay_topics")
    fun getEssayTopics(): Flow<List<EssayTopic>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEssayTopics(list: List<EssayTopic>)

    // --- Vocabulary ---
    @Query("SELECT * FROM vocabulary")
    fun getVocabulary(): Flow<List<VocabularyWord>>

    @Update
    suspend fun updateVocabularyWord(word: VocabularyWord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVocabulary(list: List<VocabularyWord>)

    // --- Flashcards ---
    @Query("SELECT * FROM flashcards")
    fun getFlashcards(): Flow<List<Flashcard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: Flashcard)

    @Query("DELETE FROM flashcards WHERE id = :id")
    suspend fun deleteFlashcard(id: Int)

    // --- AI Chat ---
    @Query("SELECT * FROM chat_sessions ORDER BY lastUpdated DESC")
    fun getChatSessions(): Flow<List<ChatSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatSession(session: ChatSession)

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getChatMessages(sessionId: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage)

    // --- Leaderboard ---
    @Query("SELECT * FROM leaderboard_users ORDER BY points DESC")
    fun getLeaderboard(): Flow<List<LeaderboardUser>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboardUsers(list: List<LeaderboardUser>)

    // --- Daily Tasks ---
    @Query("SELECT * FROM daily_tasks")
    fun getDailyTasks(): Flow<List<DailyTask>>

    @Update
    suspend fun updateDailyTask(task: DailyTask)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyTasks(list: List<DailyTask>)
}
