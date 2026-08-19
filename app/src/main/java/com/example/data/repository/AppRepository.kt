package com.example.data.repository

import com.example.data.dao.AppDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class AppRepository(private val appDao: AppDao) {

    // --- User Profile ---
    val userProfile: Flow<UserProfile?> = appDao.getUserProfile()

    suspend fun getUserProfileOneShot(): UserProfile? = appDao.getUserProfileOneShot()

    suspend fun insertUserProfile(profile: UserProfile) = appDao.insertUserProfile(profile)

    suspend fun updateUserProfile(profile: UserProfile) = appDao.updateUserProfile(profile)

    // --- Subjects ---
    val subjects: Flow<List<Subject>> = appDao.getSubjects()

    suspend fun insertSubject(subject: Subject) = appDao.insertSubject(subject)

    // --- MCQs ---
    val allMCQs: Flow<List<MCQ>> = appDao.getAllMCQs()

    fun getMCQsBySubject(subject: String): Flow<List<MCQ>> = appDao.getMCQsBySubject(subject)

    val bookmarkedMCQs: Flow<List<MCQ>> = appDao.getBookmarkedMCQs()

    suspend fun getMCQById(id: Int): MCQ? = appDao.getMCQById(id)

    suspend fun updateMCQ(mcq: MCQ) = appDao.updateMCQ(mcq)

    suspend fun insertMCQs(list: List<MCQ>) = appDao.insertMCQs(list)

    // --- Past Papers ---
    val pastPapers: Flow<List<PastPaper>> = appDao.getPastPapers()

    suspend fun insertPastPapers(list: List<PastPaper>) = appDao.insertPastPapers(list)

    // --- Subject Notes ---
    val subjectNotes: Flow<List<SubjectNote>> = appDao.getSubjectNotes()

    fun getNotesBySubject(subject: String): Flow<List<SubjectNote>> = appDao.getNotesBySubject(subject)

    suspend fun updateSubjectNote(note: SubjectNote) = appDao.updateSubjectNote(note)

    suspend fun insertSubjectNotes(list: List<SubjectNote>) = appDao.insertSubjectNotes(list)

    // --- Current Affairs ---
    val currentAffairs: Flow<List<CurrentAffair>> = appDao.getCurrentAffairs()

    suspend fun updateCurrentAffair(affair: CurrentAffair) = appDao.updateCurrentAffair(affair)

    suspend fun insertCurrentAffairs(list: List<CurrentAffair>) = appDao.insertCurrentAffairs(list)

    // --- Essay Topics ---
    val essayTopics: Flow<List<EssayTopic>> = appDao.getEssayTopics()

    suspend fun insertEssayTopics(list: List<EssayTopic>) = appDao.insertEssayTopics(list)

    // --- Vocabulary ---
    val vocabulary: Flow<List<VocabularyWord>> = appDao.getVocabulary()

    suspend fun updateVocabularyWord(word: VocabularyWord) = appDao.updateVocabularyWord(word)

    suspend fun insertVocabulary(list: List<VocabularyWord>) = appDao.insertVocabulary(list)

    // --- Flashcards ---
    val flashcards: Flow<List<Flashcard>> = appDao.getFlashcards()

    suspend fun insertFlashcard(flashcard: Flashcard) = appDao.insertFlashcard(flashcard)

    suspend fun deleteFlashcard(id: Int) = appDao.deleteFlashcard(id)

    // --- Chat Session & Messages ---
    val chatSessions: Flow<List<ChatSession>> = appDao.getChatSessions()

    suspend fun insertChatSession(session: ChatSession) = appDao.insertChatSession(session)

    fun getChatMessages(sessionId: String): Flow<List<ChatMessage>> = appDao.getChatMessages(sessionId)

    suspend fun insertChatMessage(message: ChatMessage) = appDao.insertChatMessage(message)

    // --- Leaderboard ---
    val leaderboard: Flow<List<LeaderboardUser>> = appDao.getLeaderboard()

    suspend fun insertLeaderboardUsers(list: List<LeaderboardUser>) = appDao.insertLeaderboardUsers(list)

    // --- Daily Tasks ---
    val dailyTasks: Flow<List<DailyTask>> = appDao.getDailyTasks()

    suspend fun updateDailyTask(task: DailyTask) = appDao.updateDailyTask(task)

    suspend fun insertDailyTasks(list: List<DailyTask>) = appDao.insertDailyTasks(list)
}
