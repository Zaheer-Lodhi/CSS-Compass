package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AppDao
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfile::class,
        Subject::class,
        MCQ::class,
        PastPaper::class,
        SubjectNote::class,
        CurrentAffair::class,
        EssayTopic::class,
        VocabularyWord::class,
        Flashcard::class,
        ChatSession::class,
        ChatMessage::class,
        LeaderboardUser::class,
        DailyTask::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "css_compass_database"
                )
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.appDao())
                }
            }
        }

        private suspend fun populateDatabase(dao: AppDao) {
            // 1. User Profile
            dao.insertUserProfile(UserProfile())

            // 2. Prepopulate Compulsory & Optional Subjects
            val standardSubjects = listOf(
                Subject(name = "Pakistan Affairs", description = "Compulsory - Post-partition politics, history, and constitutional evolution.", isCompulsory = true),
                Subject(name = "Current Affairs", description = "Compulsory - National and international developments and foreign policy.", isCompulsory = true),
                Subject(name = "Islamiat", description = "Compulsory - Islamic beliefs, history, governance, and comparative religion.", isCompulsory = true),
                Subject(name = "English (Précis & Composition)", description = "Compulsory - Advanced grammar, vocabulary, précis writing, and compression.", isCompulsory = true),
                Subject(name = "Essay", description = "Compulsory - High-impact argumentative essay outlines and comprehensive writing.", isCompulsory = true),
                Subject(name = "General Science & Ability", description = "Compulsory - Everyday science, basic math, logical reasoning.", isCompulsory = true),
                Subject(name = "Political Science", description = "Optional - Western and Islamic political thought, state systems.", isCompulsory = false),
                Subject(name = "International Relations", description = "Optional - Modern state systems, global wars, strategic balance.", isCompulsory = false)
            )
            standardSubjects.forEach { dao.insertSubject(it) }

            // 3. Prepopulate Practice MCQs
            val initialMCQs = listOf(
                MCQ(
                    question = "Which amendment to the 1973 Constitution of Pakistan abolished the Concurrent List, enhancing provincial autonomy?",
                    optionsJson = "[\"17th Amendment\", \"18th Amendment\", \"19th Amendment\", \"21st Amendment\"]",
                    correctAnswerIndex = 1,
                    explanation = "The 18th Amendment, passed in 2010, abolished the Concurrent Legislative List, delegating greater legislative and financial power directly to the provinces.",
                    difficulty = "Medium",
                    subjectName = "Pakistan Affairs",
                    topic = "Constitutional Development",
                    bookReference = "Constitutional and Political History of Pakistan by Hamid Khan"
                ),
                MCQ(
                    question = "The objective resolution of Pakistan was passed on which of the following dates?",
                    optionsJson = "[\"March 12, 1949\", \"August 14, 1948\", \"March 23, 1940\", \"September 11, 1948\"]",
                    correctAnswerIndex = 0,
                    explanation = "The Objective Resolution was moved by Liaquat Ali Khan, the first Prime Minister of Pakistan, and approved on March 12, 1949, serving as the foundation of future constitutions.",
                    difficulty = "Easy",
                    subjectName = "Pakistan Affairs",
                    topic = "Pre-Republic Era",
                    bookReference = "Pakistan: A New History by Ian Talbot"
                ),
                MCQ(
                    question = "Select the word closest in meaning to 'Pernicious':",
                    optionsJson = "[\"Beneficial\", \"Harmful\", \"Anomalous\", \"Incongruous\"]",
                    correctAnswerIndex = 1,
                    explanation = "Pernicious means having a harmful, highly damaging, or destructive effect, especially in a gradual or subtle way.",
                    difficulty = "Hard",
                    subjectName = "English (Précis & Composition)",
                    topic = "Vocabulary Builder",
                    bookReference = "GRE Barron's Guide & TOEFL Prep"
                ),
                MCQ(
                    question = "In Islamic Jurisprudence, 'Ijma' stands for which of the following?",
                    optionsJson = "[\"Individual reasoning\", \"Consensus of opinions\", \"Analogy of texts\", \"Traditional customs\"]",
                    correctAnswerIndex = 1,
                    explanation = "Ijma refers to the consensus of Islamic jurists on a particular legal issue where direct rulings are not explicit in the Quran and Sunnah.",
                    difficulty = "Easy",
                    subjectName = "Islamiat",
                    topic = "Islamic Jurisprudence (Fiqh)",
                    bookReference = "Principles of Islamic Jurisprudence by Mohammad Hashim Kamali"
                ),
                MCQ(
                    question = "Under which article of the Constitution of Pakistan are fundamental human rights guaranteed?",
                    optionsJson = "[\"Articles 8-28\", \"Articles 1-5\", \"Articles 45-50\", \"Articles 100-112\"]",
                    correctAnswerIndex = 0,
                    explanation = "Part II, Chapter 1 (Articles 8 to 28) of the Constitution of Pakistan guarantees fundamental rights to all Pakistani citizens, including freedom of speech, movement, assembly, and fair trial.",
                    difficulty = "Medium",
                    subjectName = "Pakistan Affairs",
                    topic = "Pakistan Constitution",
                    bookReference = "Constitution of Pakistan 1973"
                )
            )
            dao.insertMCQs(initialMCQs)

            // 4. Prepopulate Past Papers
            val papers = listOf(
                PastPaper(
                    year = 2025,
                    subjectName = "Pakistan Affairs",
                    title = "CSS Pakistan Affairs Solved 2025",
                    isSolved = true,
                    questionsJson = "[\"1. Highlight the strategic importance of CPEC Phase II.\", \"2. Discuss the federal structure of Pakistan under the 18th amendment.\", \"3. Analyze the factors contributing to Pakistan's energy crisis.\"]",
                    solutionText = "Detailed Solutions:\n\nQ1: CPEC Phase II expands beyond infrastructure into agriculture, science & technology, and Special Economic Zones (SEZs).\n\nQ2: The 18th amendment reinforced federalism, decentralized major departments to provincial cabinets, and enhanced provincial fiscal capacities via the NFC Award."
                ),
                PastPaper(
                    year = 2024,
                    subjectName = "Current Affairs",
                    title = "CSS Current Affairs Solved 2024",
                    isSolved = true,
                    questionsJson = "[\"1. Analyze Pakistan's position on US-China geopolitical competition.\", \"2. What are the key hurdles to Pakistan's climate finance acquisition?\"]",
                    solutionText = "Detailed Solutions:\n\nQ1: Pakistan adopts a balanced posture, avoiding bloc politics, safeguarding CPEC investments, and maintaining dynamic trade relationships with both Western partners and China."
                ),
                PastPaper(
                    year = 2023,
                    subjectName = "Islamiat",
                    title = "CSS Islamiat Unsolved 2023",
                    isSolved = false,
                    questionsJson = "[\"1. Discuss the Prophet's treaty of Hudaibiyah as a model of diplomatic strategy.\", \"2. Describe the collection and codification of the Holy Quran during the Khulafa-e-Rashideen era.\"]"
                )
            )
            dao.insertPastPapers(papers)

            // 5. Prepopulate Subject Notes
            val notes = listOf(
                SubjectNote(
                    subjectName = "Pakistan Affairs",
                    title = "The Ideology of Pakistan & Two-Nation Theory",
                    shortSummary = "Understanding the philosophical and political basis of Pakistan's creation.",
                    contentMarkdown = "# The Two-Nation Theory\n\nThe Two-Nation Theory is the foundational ideology of Pakistan. It states that Muslims and Hindus are two distinct nations, with separate religions, cultures, histories, social values, and political philosophies.\n\n### Key Milestones\n1. **Sir Syed Ahmed Khan:** Initiated the modernization of Muslim education through the Aligarh Movement. Post-Hindi-Urdu controversy (1867), he recognized that joint coexistence would be challenging.\n2. **Allama Iqbal's Allahabad Address (1930):** Iqbal provided a solid philosophical framework, declaring that a separate Muslim homeland in North-Western India was the destiny of Indian Muslims.\n3. **Quaid-e-Azam's Address (1940):** In his historic Lahore Resolution speech, Quaid-e-Azam declared: \"Hindus and Muslims belong to two different religious philosophies, social customs, and literatures... They neither intermarry nor interdine...\""
                ),
                SubjectNote(
                    subjectName = "Current Affairs",
                    title = "Pakistan's IMF Program & Economic Stabilization",
                    shortSummary = "An analysis of structural reforms, fiscal deficits, and economic outlook.",
                    contentMarkdown = "# Pakistan and the IMF\n\nPakistan's economic challenges stem from structural imbalances, including fiscal deficits, a low tax-to-GDP ratio, and circular debt in the power sector.\n\n### Key Reforms Mandated by IMF\n* **Tax Reforms:** Expanding the tax net, raising direct taxes, and digitalizing the Federal Board of Revenue (FBR).\n* **Energy Tariffs:** Cost-reflective energy tariffs to curb circular debt.\n* **Monetary Policy:** Maintaining a tight monetary stance to anchor inflation expectations.\n* **Privatization:** Restructuring State-Owned Enterprises (SOEs) such as PIA and steel mills."
                )
            )
            dao.insertSubjectNotes(notes)

            // 6. Prepopulate Current Affairs
            val currentAffairsList = listOf(
                CurrentAffair(
                    dateString = "July 6, 2026",
                    title = "Pakistan expands Green Transition framework with Global Partners",
                    category = "Pakistan",
                    summary = "Pakistan launches a $1.2B green energy project targeting solar and wind capacity expansion in Sindh.",
                    fullText = "The Ministry of Energy, in collaboration with international climate funds, has launched Phase 3 of the Renewable Energy Transition program. This project aims to bring 1,200 MW of wind and solar grids online by 2028, reducing dependence on imported fuel and enhancing climate resilience as part of international agreements."
                ),
                CurrentAffair(
                    dateString = "July 3, 2026",
                    title = "The Shift in Global Superpower Alliances",
                    category = "International",
                    summary = "Analysis of regional security arrangements and trade diversification in Central Asia.",
                    fullText = "A recent summit in Tashkent highlights growing economic ties between Central Asian states and South Asia. Discussions focused on transit trade, energy corridors, and regional connectivity projects, reflecting a pivot towards multi-alignment in modern international diplomacy."
                )
            )
            dao.insertCurrentAffairs(currentAffairsList)

            // 7. Prepopulate Essay Topics
            val essayTopicsList = listOf(
                EssayTopic(
                    title = "Polarized Politics: Threat to Democratic Stability in Pakistan",
                    category = "Political",
                    outlineGuideline = "I. Introduction & Thesis Statement\nII. Historical Context of Political Polarization in Pakistan\nIII. Root Causes (Governance deficits, institutional imbalances, social media bubbles)\nIV. Impact (Policy paralysis, economic uncertainty, social fragmentation)\nV. Pragmatic Solutions & Way Forward\nVI. Conclusion",
                    sampleEssay = "Political polarization refers to the division of society and leadership into ideological extremes... In Pakistan, this has historically disrupted democratic continuity, delayed economic reforms, and created societal friction. Real stability can only be restored through institutional restraint, electoral transparency, and constructive public dialogue."
                ),
                EssayTopic(
                    title = "Socio-Economic Impacts of Artificial Intelligence in Developing Nations",
                    category = "Economic",
                    outlineGuideline = "I. Introduction\nII. The AI Revolution: A Paradigm Shift\nIII. Advantages (Increased productivity, data-driven agriculture, digital governance)\nIV. Challenges (Digital divide, job displacement, infrastructure deficits)\nV. Policy Recommendations for Pakistan\nVI. Conclusion",
                    sampleEssay = "Artificial Intelligence represents a double-edged sword for developing economies. While it offers unparalleled leaps in healthcare diagnostics and precision farming, it risks broadening the inequality gap if local workforces are not upskilled..."
                )
            )
            dao.insertEssayTopics(essayTopicsList)

            // 8. Prepopulate Vocabulary
            val vocabWords = listOf(
                VocabularyWord(
                    word = "Ebullient",
                    phonetic = "/ɪˈbʊl.i.ənt/",
                    meaning = "Cheerful and full of energy; exuberant.",
                    usageExample = "The candidate was in an ebullient mood after scoring top marks in the CSS mock exams.",
                    synonyms = "Exuberant, buoyant, animated, cheerful",
                    antonyms = "Depressed, gloomy, somber"
                ),
                VocabularyWord(
                    word = "Pernicious",
                    phonetic = "/pəˈnɪʃ.əs/",
                    meaning = "Having a harmful effect, especially in a gradual or subtle way.",
                    usageExample = "Fake news on social media is a pernicious influence on democratic governance.",
                    synonyms = "Harmful, damaging, destructive, insidious",
                    antonyms = "Beneficial, healthy, harmless"
                ),
                VocabularyWord(
                    word = "Anachronism",
                    phonetic = "/əˈnæk.rə.nɪ.zəm/",
                    meaning = "A thing belonging or appropriate to a period other than that in which it exists, especially a thing that is old-fashioned.",
                    usageExample = "Evaluating modern analytical civil service papers with outdated rote-learning models is an anachronism.",
                    synonyms = "Misplacement, historical inconsistency, throwback",
                    antonyms = "Synchronism"
                )
            )
            dao.insertVocabulary(vocabWords)

            // 9. Prepopulate Leaderboard
            val leaders = listOf(
                LeaderboardUser(name = "Zainab Malik", province = "Punjab", points = 1250, rank = 1, isPremium = true),
                LeaderboardUser(name = "Hamza Yousaf", province = "KPK", points = 1120, rank = 2, isPremium = false),
                LeaderboardUser(name = "Danyal Baloch", province = "Balochistan", points = 980, rank = 3, isPremium = false),
                LeaderboardUser(name = "Ayesha Khan", province = "Sindh", points = 850, rank = 4, isPremium = true),
                LeaderboardUser(name = "Sardar Ali", province = "Punjab", points = 720, rank = 5, isPremium = false)
            )
            dao.insertLeaderboardUsers(leaders)

            // 10. Prepopulate Daily Tasks
            val tasks = listOf(
                DailyTask(title = "Complete Daily 10-MCQ Quiz", xpReward = 50),
                DailyTask(title = "Read today's Editorial Note", xpReward = 30),
                DailyTask(title = "Review 5 Flashcards", xpReward = 20)
            )
            dao.insertDailyTasks(tasks)
        }
    }
}
