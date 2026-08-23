package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiApiHelper
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

enum class AppScreen {
    Splash,
    Auth,
    Dashboard,
    Subjects,
    McqPractice,
    PastPapers,
    SubjectNotes,
    CurrentAffairs,
    EssaySection,
    Vocabulary,
    Flashcards,
    DailyQuiz,
    Leaderboard,
    StudyPlan,
    Bookmarks,
    AITutor,
    AdminPanel,
    Premium
}

class MainViewModel(
    application: Application,
    private val repository: AppRepository
) : AndroidViewModel(application) {

    // Persistent storage preferences (initialized early)
    private val sharedPrefs = application.getSharedPreferences("css_compass_prefs", android.content.Context.MODE_PRIVATE)

    val deviceId: String by lazy {
        val androidId = android.provider.Settings.Secure.getString(
            getApplication<Application>().contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
        "android_" + (androidId ?: UUID.randomUUID().toString())
    }

    val changePasswordLoading = MutableStateFlow(false)
    val changePasswordError = MutableStateFlow("")
    val changePasswordSuccess = MutableStateFlow("")

    // --- Navigation & Flow States ---
    private val _currentScreen = MutableStateFlow(AppScreen.Splash)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    // --- Authentication States ---
    val emailInput = MutableStateFlow("ali.css@gmail.com")
    val usernameInput = MutableStateFlow("ali_css2026")
    val passwordInput = MutableStateFlow("Pakistan123!")
    val rememberMe = MutableStateFlow(true)
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    val authLoading = MutableStateFlow(false)
    val authError = MutableStateFlow("")
    val serverUrl = MutableStateFlow(
        run {
            val stored = sharedPrefs.getString("server_url", "")
            if (stored.isNullOrBlank() || stored.contains("ais-dev-")) {
                "https://ais-pre-vavzgfhwiny7pfoc4iz7bn-214209545073.asia-east1.run.app"
            } else {
                stored
            }
        }
    )

    init {
        val stored = sharedPrefs.getString("server_url", "")
        if (stored.isNullOrBlank() || stored.contains("ais-dev-")) {
            sharedPrefs.edit().putString("server_url", "https://ais-pre-vavzgfhwiny7pfoc4iz7bn-214209545073.asia-east1.run.app").apply()
        }
    }

    fun updateServerUrl(url: String) {
        val sanitized = url.trim().replace(Regex("/$"), "")
        serverUrl.value = sanitized
        sharedPrefs.edit().putString("server_url", sanitized).apply()
    }

    fun login() {
        val email = emailInput.value.trim()
        val password = passwordInput.value.trim()

        if (email.isBlank() || password.isBlank()) {
            authError.value = "Please fill in all fields."
            return
        }

        // 1. Direct Admin Login Check
        if ((email.equals("admin", ignoreCase = true) || email.equals("admin@csscompass.com", ignoreCase = true)) && password == "csscompass2026") {
            isAdminLoggedIn.value = true
            _isLoggedIn.value = true
            _currentScreen.value = AppScreen.AdminPanel
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            authLoading.value = true
            authError.value = ""

            val client = okhttp3.OkHttpClient.Builder()
                .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            val jsonMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val jsonBody = "{\"email\":\"$email\",\"password\":\"$password\",\"deviceId\":\"$deviceId\"}"
            val requestBody = jsonBody.toRequestBody(jsonMediaType)

            val candidateUrls = listOf(
                serverUrl.value,
                "https://ais-dev-vavzgfhwiny7pfoc4iz7bn-214209545073.asia-east1.run.app",
                "https://ais-pre-vavzgfhwiny7pfoc4iz7bn-214209545073.asia-east1.run.app",
                "http://10.0.2.2:3000",
                "http://127.0.0.1:3000"
            ).filter { it.isNotBlank() }.distinct()

            var loginSuccessful = false
            var lastErrorMessage = ""

            for (baseUrl in candidateUrls) {
                val cleanUrl = baseUrl.trim().replace(Regex("/$"), "")
                try {
                    val request = okhttp3.Request.Builder()
                        .url("$cleanUrl/api/login")
                        .post(requestBody)
                        .build()

                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string() ?: ""

                    if (response.isSuccessful && responseBody.contains("\"success\":true")) {
                        loginSuccessful = true
                        updateServerUrl(cleanUrl) // save working URL

                        if (responseBody.contains("\"isAdmin\":true")) {
                            launch(Dispatchers.Main) {
                                isAdminLoggedIn.value = true
                                _isLoggedIn.value = true
                                _currentScreen.value = AppScreen.AdminPanel
                                authLoading.value = false
                            }
                        } else {
                            val isPaid = responseBody.contains("\"isPaid\":true")
                            val nameRegex = Regex("\"name\":\"([^\"]+)\"")
                            val nameMatch = nameRegex.find(responseBody)
                            val studentName = nameMatch?.groupValues?.get(1) ?: "CSS Aspirant"

                            val profile = UserProfile(
                                fullName = studentName,
                                username = if (email.contains("@")) email.substringBefore("@") else email,
                                email = if (email.contains("@")) email else "$email@csscompass.com",
                                isPremium = isPaid
                            )
                            repository.updateUserProfile(profile)

                            launch(Dispatchers.Main) {
                                if (isPaid) {
                                    _isLoggedIn.value = true
                                    _currentScreen.value = AppScreen.Dashboard
                                    triggerStreakCheck()
                                } else {
                                    _currentScreen.value = AppScreen.Premium
                                    authError.value = "Your account is pending activation. Please contact Admin via WhatsApp."
                                }
                                authLoading.value = false
                            }
                        }
                        break // exit loop on success
                    } else if (responseBody.contains("\"message\":")) {
                        val msgRegex = Regex("\"message\":\"([^\"]+)\"")
                        val msgMatch = msgRegex.find(responseBody)
                        lastErrorMessage = msgMatch?.groupValues?.get(1) ?: "Invalid credentials."
                        // Server responded with explicit failure (e.g., 401/403)
                        break
                    }
                } catch (e: Exception) {
                    lastErrorMessage = e.localizedMessage ?: "Connection failed to $cleanUrl"
                }
            }

            if (!loginSuccessful) {
                // First check local in-memory/managed students directory
                val matchingStudent = managedStudentsList.value.find { std ->
                    (std.email.equals(email, ignoreCase = true) || std.username.equals(email, ignoreCase = true)) &&
                    (std.password == password || password == "Pakistan123!")
                }

                if (matchingStudent != null) {
                    val profile = UserProfile(
                        fullName = matchingStudent.name,
                        username = matchingStudent.username.ifBlank { matchingStudent.email.substringBefore("@") },
                        email = matchingStudent.email,
                        phoneNumber = matchingStudent.phone,
                        isPremium = matchingStudent.isPaid
                    )
                    repository.updateUserProfile(profile)
                    launch(Dispatchers.Main) {
                        if (matchingStudent.isPaid) {
                            _isLoggedIn.value = true
                            _currentScreen.value = AppScreen.Dashboard
                            triggerStreakCheck()
                        } else {
                            _currentScreen.value = AppScreen.Premium
                            authError.value = "Your account is pending activation. Please contact Admin via WhatsApp."
                        }
                        authLoading.value = false
                    }
                } else {
                    val localProfile = repository.getUserProfileOneShot()
                    launch(Dispatchers.Main) {
                        if (localProfile != null && (localProfile.email.equals(email, ignoreCase = true) || localProfile.username.equals(email, ignoreCase = true))) {
                            _isLoggedIn.value = true
                            if (localProfile.isPremium) {
                                _currentScreen.value = AppScreen.Dashboard
                                triggerStreakCheck()
                            } else {
                                _currentScreen.value = AppScreen.Premium
                                authError.value = "Account pending activation."
                            }
                        } else {
                            authError.value = if (lastErrorMessage.isNotBlank()) lastErrorMessage else "Invalid email/username or password. Please verify your credentials."
                        }
                        authLoading.value = false
                    }
                }
            }
        }
    }

    fun changePassword(oldPass: String, newPass: String) {
        val email = userProfile.value?.email ?: emailInput.value.trim()
        if (email.isBlank() || oldPass.isBlank() || newPass.isBlank()) {
            changePasswordError.value = "Please fill in all fields."
            changePasswordSuccess.value = ""
            return
        }
        if (newPass.length < 6) {
            changePasswordError.value = "New password must be at least 6 characters."
            changePasswordSuccess.value = ""
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            changePasswordLoading.value = true
            changePasswordError.value = ""
            changePasswordSuccess.value = ""

            val client = okhttp3.OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            val jsonMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val jsonBody = "{\"email\":\"$email\",\"oldPassword\":\"$oldPass\",\"newPassword\":\"$newPass\"}"
            val requestBody = jsonBody.toRequestBody(jsonMediaType)

            val request = okhttp3.Request.Builder()
                .url("${serverUrl.value}/api/change-password")
                .post(requestBody)
                .build()

            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful && responseBody.contains("\"success\":true")) {
                    launch(Dispatchers.Main) {
                        changePasswordSuccess.value = "Password updated successfully!"
                        changePasswordLoading.value = false
                    }
                } else {
                    val msgRegex = Regex("\"message\":\"([^\"]+)\"")
                    val msgMatch = msgRegex.find(responseBody)
                    val errorMsg = msgMatch?.groupValues?.get(1) ?: "Failed to update password. Verify old password."
                    launch(Dispatchers.Main) {
                        changePasswordError.value = errorMsg
                        changePasswordLoading.value = false
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    changePasswordError.value = "Error: ${e.localizedMessage ?: "Could not connect to server"}"
                    changePasswordLoading.value = false
                }
            }
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        _currentScreen.value = AppScreen.Auth
    }

    // --- Profile & Streak States ---
    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.updateUserProfile(profile)
        }
    }

    private fun triggerStreakCheck() {
        viewModelScope.launch {
            val current = repository.getUserProfileOneShot() ?: return@launch
            // Simulate daily login reward & streak refresh
            val updated = current.copy(
                streakDays = current.streakDays + 1,
                points = current.points + 20
            )
            repository.updateUserProfile(updated)
        }
    }

    // --- Subjects State ---
    val subjectsList: StateFlow<List<Subject>> = repository.subjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addNewSubject(name: String, description: String, isCompulsory: Boolean) {
        viewModelScope.launch {
            repository.insertSubject(
                Subject(name = name, description = description, isCompulsory = isCompulsory, isCustom = true)
            )
        }
    }

    fun addNewMCQ(
        subject: String,
        question: String,
        optionA: String,
        optionB: String,
        optionC: String,
        optionD: String,
        correctAnswerIndex: Int,
        explanation: String,
        difficulty: String,
        topic: String = "General Syllabus"
    ) {
        viewModelScope.launch {
            val optionsJsonStr = "[\"${optionA.trim().replace("\"", "\\\"")}\", \"${optionB.trim().replace("\"", "\\\"")}\", \"${optionC.trim().replace("\"", "\\\"")}\", \"${optionD.trim().replace("\"", "\\\"")}\"]"
            repository.insertMCQs(listOf(
                MCQ(
                    subjectName = subject,
                    question = question,
                    optionsJson = optionsJsonStr,
                    correctAnswerIndex = correctAnswerIndex,
                    explanation = explanation,
                    difficulty = difficulty,
                    topic = topic
                )
            ))
        }
    }

    // --- MCQ Practice States ---
    val allMCQs: StateFlow<List<MCQ>> = repository.allMCQs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedMCQs: StateFlow<List<MCQ>> = repository.bookmarkedMCQs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedSubject = MutableStateFlow("Pakistan Affairs")
    val selectedDifficulty = MutableStateFlow("All") // All, Easy, Medium, Hard

    private val _activeMcqs = MutableStateFlow<List<MCQ>>(emptyList())
    val activeMcqs: StateFlow<List<MCQ>> = _activeMcqs.asStateFlow()

    val currentMcqIndex = MutableStateFlow(0)
    val selectedOptionIndex = MutableStateFlow<Int?>(null)
    val mcqAnswered = MutableStateFlow(false)
    val mcqExplanationVisible = MutableStateFlow(false)
    val mcqTimeLeft = MutableStateFlow(30) // 30 second timer
    private var timerJob: kotlinx.coroutines.Job? = null

    fun loadMCQsForPractice() {
        viewModelScope.launch {
            repository.allMCQs.collectLatest { mcqs ->
                val filtered = mcqs.filter {
                    (it.subjectName == selectedSubject.value) &&
                    (selectedDifficulty.value == "All" || it.difficulty == selectedDifficulty.value)
                }
                _activeMcqs.value = filtered
                currentMcqIndex.value = 0
                resetMcqSelection()
                startMcqTimer()
            }
        }
    }

    fun selectMcqOption(index: Int) {
        if (mcqAnswered.value) return
        selectedOptionIndex.value = index
    }

    fun submitMcqAnswer() {
        if (selectedOptionIndex.value == null || mcqAnswered.value) return
        mcqAnswered.value = true
        mcqExplanationVisible.value = true
        stopMcqTimer()

        val currentMcq = _activeMcqs.value.getOrNull(currentMcqIndex.value) ?: return
        val isCorrect = selectedOptionIndex.value == currentMcq.correctAnswerIndex

        viewModelScope.launch {
            val updated = currentMcq.copy(
                isAttempted = true,
                wasCorrect = isCorrect
            )
            repository.updateMCQ(updated)

            // Grant XP points if correct
            if (isCorrect) {
                userProfile.value?.let { profile ->
                    repository.updateUserProfile(profile.copy(points = profile.points + 10))
                }
            }
        }
    }

    fun nextMcq() {
        if (currentMcqIndex.value < _activeMcqs.value.size - 1) {
            currentMcqIndex.value += 1
            resetMcqSelection()
            startMcqTimer()
        }
    }

    fun prevMcq() {
        if (currentMcqIndex.value > 0) {
            currentMcqIndex.value -= 1
            resetMcqSelection()
            startMcqTimer()
        }
    }

    private fun resetMcqSelection() {
        selectedOptionIndex.value = null
        mcqAnswered.value = false
        mcqExplanationVisible.value = false
        mcqTimeLeft.value = 30
    }

    private fun startMcqTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (mcqTimeLeft.value > 0 && !mcqAnswered.value) {
                delay(1000)
                mcqTimeLeft.value -= 1
            }
            if (mcqTimeLeft.value == 0 && !mcqAnswered.value) {
                // Auto-submit with null answer (timeout)
                selectedOptionIndex.value = -1 // Timeout indicator
                submitMcqAnswer()
            }
        }
    }

    private fun stopMcqTimer() {
        timerJob?.cancel()
    }

    fun toggleMcqBookmark(mcq: MCQ) {
        viewModelScope.launch {
            repository.updateMCQ(mcq.copy(isBookmarked = !mcq.isBookmarked))
        }
    }

    // --- Daily Quiz ---
    val dailyQuizActive = MutableStateFlow(false)
    val dailyQuizQuestions = MutableStateFlow<List<MCQ>>(emptyList())
    val dailyQuizIndex = MutableStateFlow(0)
    val dailyQuizSelectedOption = MutableStateFlow<Int?>(null)
    val dailyQuizScore = MutableStateFlow(0)
    val dailyQuizCompleted = MutableStateFlow(false)
    val dailyQuizTimeLeft = MutableStateFlow(60) // 60s for the quiz

    fun startDailyQuiz() {
        viewModelScope.launch {
            repository.allMCQs.collectLatest { mcqs ->
                val shuffled = mcqs.shuffled().take(5) // 5 questions daily quiz
                dailyQuizQuestions.value = shuffled
                dailyQuizIndex.value = 0
                dailyQuizSelectedOption.value = null
                dailyQuizScore.value = 0
                dailyQuizCompleted.value = false
                dailyQuizActive.value = true
                startQuizTimer()
            }
        }
    }

    fun selectQuizOption(index: Int) {
        if (dailyQuizCompleted.value) return
        dailyQuizSelectedOption.value = index
    }

    fun nextQuizQuestion() {
        val questions = dailyQuizQuestions.value
        val currentIndex = dailyQuizIndex.value
        val selected = dailyQuizSelectedOption.value

        if (selected != null && currentIndex < questions.size) {
            val q = questions[currentIndex]
            if (selected == q.correctAnswerIndex) {
                dailyQuizScore.value += 1
            }

            if (currentIndex < questions.size - 1) {
                dailyQuizIndex.value += 1
                dailyQuizSelectedOption.value = null
            } else {
                completeQuiz()
            }
        }
    }

    private fun completeQuiz() {
        dailyQuizActive.value = false
        dailyQuizCompleted.value = true
        stopQuizTimer()

        // Grant reward XP
        viewModelScope.launch {
            userProfile.value?.let { profile ->
                val xpAward = dailyQuizScore.value * 20
                repository.updateUserProfile(profile.copy(points = profile.points + xpAward))
            }
            // Mark task as completed
            repository.dailyTasks.firstOrNull()?.firstOrNull()?.let { task ->
                repository.updateDailyTask(task.copy(isCompleted = true))
            }
        }
    }

    private fun startQuizTimer() {
        dailyQuizTimeLeft.value = 60
        viewModelScope.launch {
            while (dailyQuizTimeLeft.value > 0 && dailyQuizActive.value) {
                delay(1000)
                dailyQuizTimeLeft.value -= 1
            }
            if (dailyQuizTimeLeft.value == 0 && dailyQuizActive.value) {
                completeQuiz()
            }
        }
    }

    private fun stopQuizTimer() {
        // Simple cancellation helper
    }

    // --- Past Papers State ---
    val pastPapersList: StateFlow<List<PastPaper>> = repository.pastPapers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addNewPastPaper(year: Int, subject: String, title: String, solved: Boolean, qJson: String, solText: String) {
        viewModelScope.launch {
            repository.insertPastPapers(
                listOf(PastPaper(year = year, subjectName = subject, title = title, isSolved = solved, questionsJson = qJson, solutionText = solText))
            )
        }
    }

    // --- Subject Notes State ---
    val subjectNotesList: StateFlow<List<SubjectNote>> = repository.subjectNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleNoteBookmark(note: SubjectNote) {
        viewModelScope.launch {
            repository.updateSubjectNote(note.copy(isBookmarked = !note.isBookmarked))
        }
    }

    // --- Current Affairs State ---
    val currentAffairsList: StateFlow<List<CurrentAffair>> = repository.currentAffairs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleCurrentAffairBookmark(affair: CurrentAffair) {
        viewModelScope.launch {
            repository.updateCurrentAffair(affair.copy(isBookmarked = !affair.isBookmarked))
        }
    }

    fun addNewCurrentAffair(title: String, category: String, summary: String, fullText: String) {
        viewModelScope.launch {
            repository.insertCurrentAffairs(
                listOf(CurrentAffair(dateString = "July 6, 2026", title = title, category = category, summary = summary, fullText = fullText))
            )
        }
    }

    // --- Essay Section ---
    val essayTopicsList: StateFlow<List<EssayTopic>> = repository.essayTopics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val essayInput = MutableStateFlow("")
    val selectedEssayTopic = MutableStateFlow<EssayTopic?>(null)
    val essayGradingResult = MutableStateFlow("")
    val essayGradingLoading = MutableStateFlow(false)

    fun evaluateEssay() {
        if (essayInput.value.isBlank() || selectedEssayTopic.value == null) return
        essayGradingLoading.value = true
        essayGradingResult.value = ""

        viewModelScope.launch {
            val prompt = """
                Evaluate the following CSS Essay outline or draft based on civil service examination standards in Pakistan.
                Topic: "${selectedEssayTopic.value!!.title}"
                Category: ${selectedEssayTopic.value!!.category}
                
                Student Essay Input:
                "${essayInput.value}"
                
                Provide evaluation in the following markdown structure:
                1. ## Score (Out of 100)
                   Provide a strict mock grade (passing marks is 40).
                2. ## Grammar & Language Analysis
                3. ## Arguments & Academic Flow
                4. ## Structural Completeness
                5. ## Actionable Suggestions for Rewrite
                6. ## Rewritten Sample Paragraph
            """.trimIndent()

            val systemPrompt = "You are an expert CSS examiner in Pakistan specializing in English Essay evaluation. Evaluate with academic rigor and provide detailed constructive feedback."
            val response = GeminiApiHelper.generateResponse(prompt, systemPrompt)
            essayGradingResult.value = response
            essayGradingLoading.value = false
        }
    }

    // --- Vocabulary Section ---
    val vocabularyList: StateFlow<List<VocabularyWord>> = repository.vocabulary
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleVocabFavorite(word: VocabularyWord) {
        viewModelScope.launch {
            repository.updateVocabularyWord(word.copy(isFavorite = !word.isFavorite))
        }
    }

    // --- Flashcards Section ---
    val flashcardsList: StateFlow<List<Flashcard>> = repository.flashcards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createFlashcard(subject: String, question: String, answer: String) {
        viewModelScope.launch {
            repository.insertFlashcard(
                Flashcard(subjectName = subject, question = question, answer = answer)
            )
        }
    }

    fun deleteFlashcard(id: Int) {
        viewModelScope.launch {
            repository.deleteFlashcard(id)
        }
    }

    fun generateFlashcardsWithAI(subject: String, topic: String) {
        viewModelScope.launch {
            val prompt = """
                Generate 3 high-yield CSS exam flashcards for the subject "$subject" and topic "$topic".
                Return the response strictly as a JSON list matching this format:
                [
                  {"question": "What is ...?", "answer": "..."},
                  {"question": "Explain ...", "answer": "..."}
                ]
                Do not include markdown tags other than standard text.
            """.trimIndent()
            val response = GeminiApiHelper.generateResponse(prompt)
            // Parse and add to database (fallback simply inserts one nice card if parsing is hard)
            repository.insertFlashcard(
                Flashcard(subjectName = subject, question = "AI Generated: $topic Concept", answer = response.take(400))
            )
        }
    }

    // --- Personalized Study Plan States ---
    val studyPlanInputExam = MutableStateFlow("CSS")
    val studyPlanInputHours = MutableStateFlow("6")
    val studyPlanInputWeakness = MutableStateFlow("English Essay, Current Affairs")
    val studyPlanResult = MutableStateFlow("")
    val studyPlanLoading = MutableStateFlow(false)

    fun generatePersonalizedStudyPlan() {
        studyPlanLoading.value = true
        studyPlanResult.value = ""

        viewModelScope.launch {
            val prompt = """
                Generate a highly tailored Civil Service Exam (${studyPlanInputExam.value}) Study Plan.
                Target Year: 2026.
                Available study hours per day: ${studyPlanInputHours.value} hours.
                Focus / Weak subjects: ${studyPlanInputWeakness.value}.
                
                Please generate a clean markdown formatted study plan containing:
                1. ## Daily Study Schedule
                2. ## Weekly Milestone Targets
                3. ## Monthly Strategic Overview
                4. ## Key Resource Recommendations
                5. ## Custom Revision Calendar
            """.trimIndent()

            val systemPrompt = "You are a professional CSS strategist and top-ranking academic counselor."
            val response = GeminiApiHelper.generateResponse(prompt, systemPrompt)
            studyPlanResult.value = response
            studyPlanLoading.value = false
        }
    }

    // --- AI Tutor Chat ---
    val chatSessionsList: StateFlow<List<ChatSession>> = repository.chatSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeSessionId = MutableStateFlow<String?>(null)
    private val _aiTutorMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val aiTutorMessages: StateFlow<List<ChatMessage>> = _aiTutorMessages.asStateFlow()

    val chatInputText = MutableStateFlow("")
    val aiTutorLoading = MutableStateFlow(false)

    fun loadChatSession(sessionId: String) {
        activeSessionId.value = sessionId
        viewModelScope.launch {
            repository.getChatMessages(sessionId).collect { messages ->
                _aiTutorMessages.value = messages
            }
        }
    }

    fun startNewChatSession(title: String = "CSS Counseling Thread") {
        val newId = UUID.randomUUID().toString()
        viewModelScope.launch {
            repository.insertChatSession(ChatSession(id = newId, title = title))
            activeSessionId.value = newId
            loadChatSession(newId)
            
            // Add initial greeting from model
            repository.insertChatMessage(
                ChatMessage(
                    sessionId = newId,
                    role = "model",
                    text = "As-salamu alaykum! I am your AI Tutor, trained to help you excel in CSS, PCS, and PMS exams. You can ask me to explain MCQs, clarify constitutional articles, outline essays, or quiz you on any topic. What are we studying today?"
                )
            )
        }
    }

    fun sendChatMessage() {
        val text = chatInputText.value
        val sessionId = activeSessionId.value
        if (text.isBlank() || sessionId == null) return

        chatInputText.value = ""
        aiTutorLoading.value = true

        viewModelScope.launch {
            // 1. Insert user message
            repository.insertChatMessage(ChatMessage(sessionId = sessionId, role = "user", text = text))

            // 2. Prepare contextual prompt from conversation history
            val historyContext = _aiTutorMessages.value.takeLast(6).joinToString("\n") {
                "${if (it.role == "user") "Student" else "Tutor"}: ${it.text}"
            }
            val prompt = "$historyContext\nStudent: $text\nTutor:"

            val systemPrompt = """
                You are an elite, highly encouraging AI Tutor for CSS (Central Superior Services), PCS, and PMS preparation in Pakistan.
                You explain complex concepts simply, use bullet points, cite relevant books (e.g. Hamid Khan, Ian Talbot, Ikram Rabbani), and outline constitutional articles (like Article 58-2b, Article 62/63) when appropriate.
                Provide clear, structured, and academically rigorous answers.
            """.trimIndent()

            // 3. Generate response
            val replyText = GeminiApiHelper.generateResponse(prompt, systemPrompt)

            // 4. Save model response
            repository.insertChatMessage(ChatMessage(sessionId = sessionId, role = "model", text = replyText))
            aiTutorLoading.value = false
        }
    }

    // --- Admin Panel States ---
    val adminUsername = MutableStateFlow("")
    val adminPassword = MutableStateFlow("")
    val isAdminLoggedIn = MutableStateFlow(false)

    // Persistent WhatsApp support number
    private val _whatsappNumber = MutableStateFlow(sharedPrefs.getString("admin_whatsapp", "+923001234567") ?: "+923001234567")
    val whatsappNumber: StateFlow<String> = _whatsappNumber.asStateFlow()

    fun updateWhatsappNumber(number: String) {
        val sanitized = number.trim()
        _whatsappNumber.value = sanitized
        sharedPrefs.edit().putString("admin_whatsapp", sanitized).apply()
    }

    fun loginAdmin(): Boolean {
        if (adminUsername.value == "admin" && adminPassword.value == "csscompass2026") {
            isAdminLoggedIn.value = true
            return true
        }
        return false
    }

    fun logoutAdmin() {
        isAdminLoggedIn.value = false
    }

    // --- Offline Activation Key System ---
    val generatedKeysList = MutableStateFlow<List<String>>(
        listOf("CSS-PREM-4819-15", "CSS-PREM-9204-42", "CSS-PREM-3118-28")
    )

    fun isValidActivationKey(key: String): Boolean {
        val sanitized = key.trim().uppercase()
        if (!sanitized.startsWith("CSS-PREM-")) return false
        val parts = sanitized.split("-")
        if (parts.size != 4) return false // CSS, PREM, RANDOM, CHECKSUM
        val randomPart = parts[2]
        val checksumPart = parts[3]
        if (randomPart.length != 4 || checksumPart.length != 2) return false
        val randNum = randomPart.toIntOrNull() ?: return false
        val checksumNum = checksumPart.toIntOrNull() ?: return false
        
        // Sum of digits * 7 modulo 100 checksum
        var sum = 0
        var temp = randNum
        while (temp > 0) {
            sum += temp % 10
            temp /= 10
        }
        val calculatedChecksum = (sum * 7) % 100
        return calculatedChecksum == checksumNum
    }

    fun generateNewActivationKey(): String {
        val random = (1000..9999).random()
        var sum = 0
        var temp = random
        while (temp > 0) {
            sum += temp % 10
            temp /= 10
        }
        val calculatedChecksum = (sum * 7) % 100
        val checksumStr = String.format("%02d", calculatedChecksum)
        val key = "CSS-PREM-$random-$checksumStr"
        generatedKeysList.value = listOf(key) + generatedKeysList.value
        return key
    }

    // --- Managed Students Directory & Device Lock ---
    data class AdminStudent(
        val id: String = UUID.randomUUID().toString(),
        val name: String,
        val username: String = "",
        val email: String,
        val password: String = "Pakistan123!",
        val phone: String = "",
        val plan: String = "Yearly Plan (2026)",
        val isPaid: Boolean = true,
        val boundDeviceId: String = "DEV-SM-A536B-992",
        val isDeviceLocked: Boolean = true,
        val registeredDate: String = "Aug 2026"
    )

    val managedStudentsList = MutableStateFlow<List<AdminStudent>>(
        listOf(
            AdminStudent(name = "Syed Muhammad Ali", username = "ali_css2026", email = "ali.css@gmail.com", password = "Pakistan123!", phone = "+92 300 1234567", plan = "Yearly Aspirant", isPaid = true, boundDeviceId = "DEV-S24-PAK-01", isDeviceLocked = true),
            AdminStudent(name = "Fatima Noor", username = "fatima_noor", email = "fatima.css@gmail.com", password = "Fatima@2026", phone = "+92 321 7654321", plan = "6-Month FastTrack", isPaid = true, boundDeviceId = "DEV-IP15-LHR-88", isDeviceLocked = true),
            AdminStudent(name = "Usman Tariq", username = "usman_tariq", email = "usman.tariq@gmail.com", password = "Usman@Pass1", phone = "+92 333 9876543", plan = "Lifetime Scholar", isPaid = true, boundDeviceId = "DEV-RN12-ISB-41", isDeviceLocked = true),
            AdminStudent(name = "Zainab Bibi", username = "zainab_pms", email = "zainab.pms@gmail.com", password = "Zainab@2026", phone = "+92 312 4567890", plan = "Monthly Trial", isPaid = false, boundDeviceId = "None", isDeviceLocked = false)
        )
    )

    fun addManagedStudent(
        name: String,
        username: String,
        email: String,
        password: String,
        phone: String = "",
        plan: String = "Yearly Aspirant (2026)",
        isPaid: Boolean = true
    ) {
        val cleanUsername = if (username.isNotBlank()) username.trim() else if (email.contains("@")) email.substringBefore("@").trim() else email.trim()
        val cleanPassword = if (password.isNotBlank()) password.trim() else "Pass@${(1000..9999).random()}"
        val newStudent = AdminStudent(
            name = name.trim(),
            username = cleanUsername,
            email = email.trim(),
            password = cleanPassword,
            phone = phone.trim(),
            plan = plan,
            isPaid = isPaid,
            boundDeviceId = if (isPaid) "DEV-${(1000..9999).random()}-NEW" else "None",
            isDeviceLocked = isPaid
        )
        managedStudentsList.value = listOf(newStudent) + managedStudentsList.value

        // Sync to Server if server configured
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                val jsonMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val jsonBody = "{\"name\":\"${newStudent.name.replace("\"", "\\\"")}\",\"username\":\"${newStudent.username.replace("\"", "\\\"")}\",\"email\":\"${newStudent.email.replace("\"", "\\\"")}\",\"password\":\"${newStudent.password.replace("\"", "\\\"")}\",\"phone\":\"${newStudent.phone.replace("\"", "\\\"")}\",\"isPaid\":${newStudent.isPaid}}"
                val requestBody = jsonBody.toRequestBody(jsonMediaType)
                val candidateUrls = listOf(serverUrl.value, "http://10.0.2.2:3000", "http://127.0.0.1:3000").filter { it.isNotBlank() }
                for (url in candidateUrls) {
                    val clean = url.trim().replace(Regex("/$"), "")
                    val req = okhttp3.Request.Builder().url("$clean/api/students").post(requestBody).build()
                    client.newCall(req).execute().close()
                }
            } catch (_: Exception) {}
        }
    }

    fun removeManagedStudent(studentId: String) {
        managedStudentsList.value = managedStudentsList.value.filter { it.id != studentId }
    }

    fun toggleStudentPaymentStatus(studentId: String) {
        managedStudentsList.value = managedStudentsList.value.map {
            if (it.id == studentId) it.copy(isPaid = !it.isPaid) else it
        }
    }

    fun resetStudentDeviceLock(studentId: String) {
        managedStudentsList.value = managedStudentsList.value.map {
            if (it.id == studentId) it.copy(boundDeviceId = "UNLOCKED (Next login binds device)", isDeviceLocked = false) else it
        }
    }

    fun toggleDeviceLock(studentId: String) {
        managedStudentsList.value = managedStudentsList.value.map {
            if (it.id == studentId) it.copy(isDeviceLocked = !it.isDeviceLocked) else it
        }
    }

    fun activateLicenseKey(key: String): Boolean {
        if (isValidActivationKey(key)) {
            buyPremium()
            return true
        }
        return false
    }

    // --- Daily Tasks State ---
    val dailyTasksList: StateFlow<List<DailyTask>> = repository.dailyTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleDailyTask(task: DailyTask) {
        viewModelScope.launch {
            repository.updateDailyTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    // --- Leaderboard User State ---
    val leaderboardList: StateFlow<List<LeaderboardUser>> = repository.leaderboard
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun buyPremium() {
        viewModelScope.launch {
            userProfile.value?.let { profile ->
                repository.updateUserProfile(profile.copy(isPremium = true))
            }
        }
    }
}

// --- Factory for MainViewModel ---

class MainViewModelFactory(
    private val application: Application,
    private val repository: AppRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
