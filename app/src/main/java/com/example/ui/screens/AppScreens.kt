package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.res.painterResource
import com.example.R

@Composable
fun MainAppNavigation(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 720.dp
        val showNavigation = currentScreen != AppScreen.Splash && currentScreen != AppScreen.Auth

        if (isWideScreen && showNavigation) {
            // Adaptive wide layout for desktops, laptops, tablets, and landscape screens
            Row(modifier = Modifier.fillMaxSize()) {
                AppNavigationRail(viewModel = viewModel, currentScreen = currentScreen)
                
                VerticalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                
                Scaffold(
                    topBar = {
                        AppTopBar(viewModel = viewModel, userProfile = userProfile)
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        // Centered container with a clean, responsive max width to prevent element stretching
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 1200.dp)
                                .align(Alignment.TopCenter)
                        ) {
                            ScreenContent(currentScreen, viewModel, userProfile)
                        }
                    }
                }
            }
        } else {
            // Compact mobile portrait layout
            Scaffold(
                topBar = {
                    if (showNavigation) {
                        AppTopBar(viewModel = viewModel, userProfile = userProfile)
                    }
                },
                bottomBar = {
                    if (showNavigation) {
                        AppBottomBar(viewModel = viewModel, currentScreen = currentScreen)
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // Slight limit on compact to prevent stretching on small landscape
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 640.dp)
                            .align(Alignment.TopCenter)
                    ) {
                        ScreenContent(currentScreen, viewModel, userProfile)
                    }
                }
            }
        }
    }
}

@Composable
fun ScreenContent(currentScreen: AppScreen, viewModel: MainViewModel, userProfile: UserProfile?) {
    when (currentScreen) {
        AppScreen.Splash -> SplashScreen(viewModel)
        AppScreen.Auth -> AuthScreen(viewModel)
        AppScreen.Dashboard -> DashboardScreen(viewModel, userProfile)
        AppScreen.Subjects -> SubjectsScreen(viewModel)
        AppScreen.McqPractice -> MCQPracticeScreen(viewModel)
        AppScreen.PastPapers -> PastPapersScreen(viewModel)
        AppScreen.SubjectNotes -> SubjectNotesScreen(viewModel)
        AppScreen.CurrentAffairs -> CurrentAffairsScreen(viewModel)
        AppScreen.EssaySection -> EssaySectionScreen(viewModel)
        AppScreen.Vocabulary -> VocabularyScreen(viewModel)
        AppScreen.Flashcards -> FlashcardScreen(viewModel)
        AppScreen.DailyQuiz -> DailyQuizScreen(viewModel)
        AppScreen.Leaderboard -> LeaderboardScreen(viewModel, userProfile)
        AppScreen.StudyPlan -> StudyPlanScreen(viewModel)
        AppScreen.Bookmarks -> BookmarksScreen(viewModel)
        AppScreen.AITutor -> AITutorScreen(viewModel)
        AppScreen.AdminPanel -> AdminPanelScreen(viewModel)
        AppScreen.Premium -> PremiumScreen(viewModel, userProfile)
    }
}

@Composable
fun AppNavigationRail(viewModel: MainViewModel, currentScreen: AppScreen) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface,
        header = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CompassCalibration,
                    contentDescription = "Logo",
                    tint = SecondaryGold,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "CSS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
                Text(
                    text = "Compass",
                    style = MaterialTheme.typography.labelSmall,
                    color = SecondaryGold,
                    fontSize = 10.sp
                )
            }
        }
    ) {
        Spacer(modifier = Modifier.weight(1f))
        
        NavigationRailItem(
            selected = currentScreen == AppScreen.Dashboard,
            onClick = { viewModel.navigateTo(AppScreen.Dashboard) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
            label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        NavigationRailItem(
            selected = currentScreen == AppScreen.AITutor,
            onClick = { viewModel.navigateTo(AppScreen.AITutor) },
            icon = { Icon(Icons.Default.Psychology, contentDescription = "AI Tutor") },
            label = { Text("AI Tutor", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        NavigationRailItem(
            selected = currentScreen == AppScreen.Subjects || currentScreen == AppScreen.McqPractice || currentScreen == AppScreen.SubjectNotes || currentScreen == AppScreen.PastPapers,
            onClick = { viewModel.navigateTo(AppScreen.Subjects) },
            icon = { Icon(Icons.Default.Book, contentDescription = "Subjects") },
            label = { Text("Subjects", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        NavigationRailItem(
            selected = currentScreen == AppScreen.Leaderboard,
            onClick = { viewModel.navigateTo(AppScreen.Leaderboard) },
            icon = { Icon(Icons.Default.Leaderboard, contentDescription = "Leaderboard") },
            label = { Text("Ranks", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        NavigationRailItem(
            selected = currentScreen == AppScreen.Premium,
            onClick = { viewModel.navigateTo(AppScreen.Premium) },
            icon = { Icon(Icons.Default.WorkspacePremium, contentDescription = "Premium") },
            label = { Text("Premium", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
        )
        
        Spacer(modifier = Modifier.weight(1.5f))
    }
}

// --- Top Bar & Bottom Bar Layouts ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(viewModel: MainViewModel, userProfile: UserProfile?) {
    var adminTapCount by remember { mutableStateOf(0) }
    val context = LocalContext.current

    TopAppBar(
        title = {
            Surface(
                color = Color.Transparent,
                onClick = {
                    adminTapCount++
                    if (adminTapCount >= 3) {
                        adminTapCount = 0
                        viewModel.navigateTo(AppScreen.AdminPanel)
                        Toast.makeText(context, "Opening Admin Authentication...", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Admin access: ${3 - adminTapCount} more taps", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_launcher_foreground_1786987039313),
                        contentDescription = "CSS Compass Crest",
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Column {
                        Text(
                            text = "CSS Compass",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Academy Prep",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryGold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        },
        actions = {
            // Study Streak Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = Color(0xFFF97316),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${userProfile?.streakDays ?: 0}d",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // User Points (XP) Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .background(
                        color = SecondaryGold.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "XP",
                    tint = SecondaryGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${userProfile?.points ?: 0} XP",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (userProfile?.isPremium == true) {
                IconButton(onClick = { viewModel.navigateTo(AppScreen.Premium) }) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = "Premium Status",
                        tint = SecondaryGold
                    )
                }
            } else {
                Button(
                    onClick = { viewModel.navigateTo(AppScreen.Premium) },
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryGold),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .padding(end = 4.dp)
                ) {
                    Text("Go Premium", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun AppBottomBar(viewModel: MainViewModel, currentScreen: AppScreen) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == AppScreen.Dashboard,
            onClick = { viewModel.navigateTo(AppScreen.Dashboard) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
            label = { Text("Home", fontSize = 10.sp) }
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.AITutor,
            onClick = { viewModel.navigateTo(AppScreen.AITutor) },
            icon = { Icon(Icons.Default.Psychology, contentDescription = "AI Tutor") },
            label = { Text("AI Tutor", fontSize = 10.sp) }
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.Subjects || currentScreen == AppScreen.McqPractice || currentScreen == AppScreen.SubjectNotes || currentScreen == AppScreen.PastPapers,
            onClick = { viewModel.navigateTo(AppScreen.Subjects) },
            icon = { Icon(Icons.Default.Book, contentDescription = "Subjects") },
            label = { Text("Subjects", fontSize = 10.sp) }
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.Leaderboard,
            onClick = { viewModel.navigateTo(AppScreen.Leaderboard) },
            icon = { Icon(Icons.Default.Leaderboard, contentDescription = "Leaderboard") },
            label = { Text("Ranks", fontSize = 10.sp) }
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.Premium,
            onClick = { viewModel.navigateTo(AppScreen.Premium) },
            icon = { Icon(Icons.Default.WorkspacePremium, contentDescription = "Premium") },
            label = { Text("Premium", fontSize = 10.sp) }
        )
    }
}

// --- Screens ---

// 1. Splash Screen
@Composable
fun SplashScreen(viewModel: MainViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkSlateBg, Color(0xFF020617))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_launcher_foreground_1786987039313),
                contentDescription = "Academic Compass Crest",
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(2.dp, SecondaryGold.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "CSS COMPASS",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 2.sp
            )
            Text(
                text = "Premium CSS / PCS / PMS Exam Preparation Platform",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = { viewModel.navigateTo(AppScreen.Auth) },
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryGold),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(50.dp)
            ) {
                Text(
                    "ENTER ACADEMY",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedButton(
                onClick = { viewModel.navigateTo(AppScreen.AdminPanel) },
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.5.dp, SecondaryGold.copy(alpha = 0.8f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SecondaryGold),
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = "Admin Portal",
                    modifier = Modifier.size(20.dp),
                    tint = SecondaryGold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "ADMIN PORTAL",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}

// 2. Authentication / Profile Registration Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiPlatformInstallationGuideDialog(onDismiss: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Windows 11", "macOS / iOS", "Android / Tablet", "Instant Web")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("GOT IT", fontWeight = FontWeight.Bold, color = SecondaryGold)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Laptop,
                    contentDescription = "Desktop Guide",
                    tint = SecondaryGold,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CSS Compass Desktop & Cross-Platform Installation Guide",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(420.dp)) {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 0.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = SecondaryGold
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedTab == index) SecondaryGold else MaterialTheme.colorScheme.onSurfaceVariant) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (selectedTab) {
                        0 -> {
                            Text("Install on Windows 11 natively", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = PrimaryNavy)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Windows 11 includes built-in support for Android apps through the Windows Subsystem for Android (WSA).", fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Method 1: Direct WSA Sideloading (Recommended)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("1. Search and install 'WSATools' from the Microsoft Store.\n2. Open WSATools, locate your exported APK, and click Install.\n3. The app will launch inside a native resizable Windows desktop window, supporting keyboard typing, shortcuts, and mouse scrolls!", fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Method 2: High-Performance Desktop Emulators", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("1. Download a popular desktop Android player (such as BlueStacks, Nox, or LDPlayer).\n2. Drag and drop the downloaded CSS Compass APK directly into the player.\n3. Run it instantly with optimized layout support.", fontSize = 11.sp)
                        }
                        1 -> {
                            Text("Run on iOS & macOS", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = PrimaryNavy)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Method 1: Direct Web Stream for iOS (No Setup)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Because iOS does not support APK files natively, we host a continuous high-fidelity cloud stream of our application.", fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("1. Open Safari on your iPhone, iPad, or Mac.\n2. Go to: https://ais-pre-vavzgfhwiny7pfoc4iz7bn-214209545073.asia-east1.run.app\n3. Click 'Add to Home Screen' in Safari sharing menu. It will behave exactly like an installable iOS App!", fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Method 2: Sideloading on Apple Silicon Macs (M1/M2/M3)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("1. Open PlayCover or a compatible Mac sideloading layer.\n2. Sideload the APK file directly to run on your Mac desktop with resizable window capabilities.", fontSize = 11.sp)
                        }
                        2 -> {
                            Text("Install on Android Phones & Tablets", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = PrimaryNavy)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Your application compiles to a fully-optimized APK, running natively across all Android hardware architectures.", fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("1. Locate your exported APK file from the build outputs.\n2. Transfer the APK to your Android phone or tablet (via USB, email, or Drive).\n3. Tap on the APK file in your device's File Manager.\n4. Grant 'Install from Unknown Sources' if prompted.\n5. Launch the app from your home screen. On tablets, it automatically launches in a modern split-pane view with a Sidebar Navigation Rail!", fontSize = 11.sp)
                        }
                        3 -> {
                            Text("Instant Web Preview & Live Stream", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = PrimaryNavy)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("No downloads or settings required! You can run, test, and interact with the application live from any device with a browser.", fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Shared Live Preview URL:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("https://ais-pre-vavzgfhwiny7pfoc4iz7bn-214209545073.asia-east1.run.app", fontSize = 12.sp, color = SecondaryGold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Features available on Direct Stream:\n• Fully responsive interface adapting from mobile up to ultra-wide desktop monitors.\n• Instant AI CSS Tutor responses powered by Gemini API.\n• Complete MCQ practice tests with real-time timers and explanations.\n• Simulated offline database sync supporting instant revisions.", fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun AuthFormContent(
    email: String,
    username: String,
    password: String,
    rememberMe: Boolean,
    province: String,
    examType: String,
    hours: String,
    provinces: List<String>,
    exams: List<String>,
    showPassword: Boolean,
    isEmailValid: Boolean,
    isUsernameValid: Boolean,
    isPasswordValid: Boolean,
    isHoursValid: Boolean,
    isFormValid: Boolean,
    onEmailChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onProvinceChange: (String) -> Unit,
    onExamTypeChange: (String) -> Unit,
    onHoursChange: (String) -> Unit,
    onShowPasswordToggle: () -> Unit,
    onInitialize: () -> Unit,
    onQuickSSO: () -> Unit,
    showInstallationGuide: () -> Unit,
    onOpenAdmin: () -> Unit = {},
    isWide: Boolean,
    authLoading: Boolean = false,
    authError: String = ""
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Email/Username Field
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email Address or Username") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = onShowPasswordToggle) {
                    Icon(
                        imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle password visibility"
                    )
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Remember Me & Forgot Password
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = onRememberMeChange
                )
                Text("Remember Me", style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = { /* Simulated */ }) {
                Text("Forgot Password?", color = SecondaryGold, fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (authError.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = authError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Login button
        Button(
            onClick = onInitialize,
            enabled = email.trim().isNotBlank() && password.trim().isNotBlank() && !authLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryNavy,
                disabledContainerColor = PrimaryNavy.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (authLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("LOGIN TO ACADEMY", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Demo Student Quick Login
        OutlinedButton(
            onClick = onQuickSSO,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = SecondaryGold,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text("DEMO STUDENT LOGIN (Ali)", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Direct Admin Portal Access Button
        OutlinedButton(
            onClick = onOpenAdmin,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.5.dp, SecondaryGold.copy(alpha = 0.7f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = SecondaryGold)
        ) {
            Icon(
                imageVector = Icons.Default.AdminPanelSettings,
                contentDescription = "Admin Portal",
                tint = SecondaryGold,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text("🛡️ OPEN ADMIN PORTAL", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        }

        if (!isWide) {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
            Spacer(modifier = Modifier.height(12.dp))
            
            // Installation Guide Button for mobile
            TextButton(
                onClick = showInstallationGuide,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Laptop, contentDescription = null, tint = SecondaryGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Windows & Desktop Installation Guide", color = SecondaryGold, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AuthScreen(viewModel: MainViewModel) {
    val email by viewModel.emailInput.collectAsState()
    val username by viewModel.usernameInput.collectAsState()
    val password by viewModel.passwordInput.collectAsState()
    val rememberMe by viewModel.rememberMe.collectAsState()
    val authLoading by viewModel.authLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()

    var showPassword by remember { mutableStateOf(false) }

    // Onboarding values
    var province by remember { mutableStateOf("Punjab") }
    var examType by remember { mutableStateOf("CSS") }
    var hours by remember { mutableStateOf("6") }

    val provinces = listOf("Punjab", "Sindh", "KPK", "Balochistan", "Federal Capital", "Gilgit-Baltistan", "AJK")
    val exams = listOf("CSS", "PCS", "PMS")

    // Form Validations
    var showInstallationGuide by remember { mutableStateOf(false) }
    
    val isEmailValid = email.contains("@") && email.substringAfter("@").contains(".")
    val isUsernameValid = username.length >= 4
    val isPasswordValid = password.length >= 6
    val isHoursValid = (hours.toIntOrNull() ?: 0) in 1..24

    val isFormValid = isEmailValid && isUsernameValid && isPasswordValid && isHoursValid

    if (showInstallationGuide) {
        MultiPlatformInstallationGuideDialog(onDismiss = { showInstallationGuide = false })
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val isWide = maxWidth >= 720.dp

        if (isWide) {
            // Two-Pane Desktop split-screen layout
            Row(modifier = Modifier.fillMaxSize()) {
                // Left pane: Motivational/Academic CSS Banner
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(PrimaryNavy, MaterialTheme.colorScheme.primaryContainer)
                            )
                        )
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CompassCalibration,
                            contentDescription = "CSS Compass",
                            tint = SecondaryGold,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "CSS Compass",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Your elite navigation partner through the CSS, PCS & PMS examination challenges in Pakistan.",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Desktop Info Cards
                        listOf(
                            Icons.Default.Psychology to "24/7 Personal AI Tutor with syllabus mastery",
                            Icons.AutoMirrored.Filled.LibraryBooks to "Complete catalog of subject notes & past papers",
                            Icons.Default.Leaderboard to "Real-time rank updates & mock tests dashboard"
                        ).forEach { (icon, text) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color.White.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(icon, contentDescription = null, tint = SecondaryGold, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(text, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(48.dp))
                        
                        Button(
                            onClick = { showInstallationGuide = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryGold),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Laptop, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cross-Platform Setup Guide", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Right pane: Scrollable Login Form Card
                Box(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surface)
                        .verticalScroll(rememberScrollState())
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AuthFormContent(
                        email = email,
                        username = username,
                        password = password,
                        rememberMe = rememberMe,
                        province = province,
                        examType = examType,
                        hours = hours,
                        provinces = provinces,
                        exams = exams,
                        showPassword = showPassword,
                        isEmailValid = isEmailValid,
                        isUsernameValid = isUsernameValid,
                        isPasswordValid = isPasswordValid,
                        isHoursValid = isHoursValid,
                        isFormValid = isFormValid,
                        onEmailChange = { viewModel.emailInput.value = it },
                        onUsernameChange = { viewModel.usernameInput.value = it },
                        onPasswordChange = { viewModel.passwordInput.value = it },
                        onRememberMeChange = { viewModel.rememberMe.value = it },
                        onProvinceChange = { province = it },
                        onExamTypeChange = { examType = it },
                        onHoursChange = { hours = it },
                        onShowPasswordToggle = { showPassword = !showPassword },
                        onInitialize = {
                            viewModel.updateProfile(
                                UserProfile(
                                    fullName = "Syed Muhammad Ali",
                                    username = username,
                                    email = email,
                                    province = province,
                                    examType = examType,
                                    dailyStudyHoursGoal = hours.toIntOrNull() ?: 6
                                )
                            )
                            viewModel.login()
                        },
                        onQuickSSO = {
                            viewModel.emailInput.value = "ali.css@gmail.com"
                            viewModel.passwordInput.value = "Pakistan123!"
                            viewModel.login()
                        },
                        showInstallationGuide = { showInstallationGuide = true },
                        onOpenAdmin = { viewModel.navigateTo(AppScreen.AdminPanel) },
                        isWide = true,
                        authLoading = authLoading,
                        authError = authError
                    )
                }
            }
        } else {
            // Mobile screen layout
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 480.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CastForEducation,
                            contentDescription = "Edu",
                            tint = SecondaryGold,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "CSS Compass Portal",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Begin your CSS/PMS academic preparation",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        AuthFormContent(
                            email = email,
                            username = username,
                            password = password,
                            rememberMe = rememberMe,
                            province = province,
                            examType = examType,
                            hours = hours,
                            provinces = provinces,
                            exams = exams,
                            showPassword = showPassword,
                            isEmailValid = isEmailValid,
                            isUsernameValid = isUsernameValid,
                            isPasswordValid = isPasswordValid,
                            isHoursValid = isHoursValid,
                            isFormValid = isFormValid,
                            onEmailChange = { viewModel.emailInput.value = it },
                            onUsernameChange = { viewModel.usernameInput.value = it },
                            onPasswordChange = { viewModel.passwordInput.value = it },
                            onRememberMeChange = { viewModel.rememberMe.value = it },
                            onProvinceChange = { province = it },
                            onExamTypeChange = { examType = it },
                            onHoursChange = { hours = it },
                            onShowPasswordToggle = { showPassword = !showPassword },
                            onInitialize = {
                                viewModel.updateProfile(
                                    UserProfile(
                                        fullName = "Syed Muhammad Ali",
                                        username = username,
                                        email = email,
                                        province = province,
                                        examType = examType,
                                        dailyStudyHoursGoal = hours.toIntOrNull() ?: 6
                                    )
                                )
                                viewModel.login()
                            },
                            onQuickSSO = {
                                viewModel.emailInput.value = "ali.css@gmail.com"
                                viewModel.passwordInput.value = "Pakistan123!"
                                viewModel.login()
                            },
                            showInstallationGuide = { showInstallationGuide = true },
                            onOpenAdmin = { viewModel.navigateTo(AppScreen.AdminPanel) },
                            isWide = false,
                            authLoading = authLoading,
                            authError = authError
                        )
                    }
                }
            }
        }
    }
}

// 3. Dashboard Screen
@Composable
fun DashboardScreen(viewModel: MainViewModel, profile: UserProfile?) {
    val dailyTasks by viewModel.dailyTasksList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Daily Motivation Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "DAILY MOTIVATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = SecondaryGold,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\"Success in the CSS, PCS, or PMS exams is not the result of spontaneous combustion. You must set yourself on fire with dedicated, smart prep daily.\"",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "- Academic Board of Examiners",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        // Streak Progress Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Daily Goals & Streaks",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "You are in top 5% of aspirers in ${profile?.province ?: "Pakistan"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak Fire",
                        tint = Color(0xFFF97316),
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { 0.66f },
                    color = SecondaryGold,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("4 of ${profile?.dailyStudyHoursGoal ?: 6} hrs complete", style = MaterialTheme.typography.bodySmall)
                    Text("+50 XP on completion", style = MaterialTheme.typography.bodySmall, color = SecondaryGold)
                }
            }
        }

        // Daily Tasks List
        Text(
            text = "Today's Tasks",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                dailyTasks.forEach { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleDailyTask(task) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { viewModel.toggleDailyTask(task) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Reward: +${task.xpReward} XP",
                                style = MaterialTheme.typography.bodySmall,
                                color = SecondaryGold
                            )
                        }
                        if (task.title.contains("Quiz")) {
                            IconButton(onClick = { viewModel.navigateTo(AppScreen.DailyQuiz) }) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Start Quiz", tint = PrimaryNavy)
                            }
                        }
                    }
                }
            }
        }

        // Navigation Shortcut Hub
        Text(
            text = "Preparation Modules",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            DashboardShortcutCard(
                title = "MCQ Practice",
                subtitle = "Unlimited MCQs",
                icon = Icons.Default.Quiz,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.navigateTo(AppScreen.Subjects) }
            )
            Spacer(modifier = Modifier.width(12.dp))
            DashboardShortcutCard(
                title = "AI Tutor",
                subtitle = "Active Support",
                icon = Icons.Default.Psychology,
                modifier = Modifier.weight(1f),
                onClick = {
                    viewModel.startNewChatSession()
                    viewModel.navigateTo(AppScreen.AITutor)
                }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            DashboardShortcutCard(
                title = "Subject Notes",
                subtitle = "Syllabus outlines",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.navigateTo(AppScreen.Subjects) }
            )
            Spacer(modifier = Modifier.width(12.dp))
            DashboardShortcutCard(
                title = "AI Essay Evaluator",
                subtitle = "Check drafts",
                icon = Icons.AutoMirrored.Filled.Assignment,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.navigateTo(AppScreen.EssaySection) }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            DashboardShortcutCard(
                title = "Past Papers",
                subtitle = "Solved papers",
                icon = Icons.Default.HistoryEdu,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.navigateTo(AppScreen.PastPapers) }
            )
            Spacer(modifier = Modifier.width(12.dp))
            DashboardShortcutCard(
                title = "Study Plan",
                subtitle = "AI schedule creator",
                icon = Icons.Default.CalendarToday,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.navigateTo(AppScreen.StudyPlan) }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            DashboardShortcutCard(
                title = "Vocab Builder",
                subtitle = "Flashcards",
                icon = Icons.Default.Translate,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.navigateTo(AppScreen.Vocabulary) }
            )
            Spacer(modifier = Modifier.width(12.dp))
            DashboardShortcutCard(
                title = "Current Affairs",
                subtitle = "Daily Updates",
                icon = Icons.Default.Newspaper,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.navigateTo(AppScreen.CurrentAffairs) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        AccountSecurityCard(viewModel = viewModel)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AccountSecurityCard(viewModel: MainViewModel) {
    val changePasswordLoading by viewModel.changePasswordLoading.collectAsState()
    val changePasswordError by viewModel.changePasswordError.collectAsState()
    val changePasswordSuccess by viewModel.changePasswordSuccess.collectAsState()
    val deviceId = viewModel.deviceId

    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showOldPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("account_security_card"),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security Settings",
                        tint = SecondaryGold,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Account Security & Device Info",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Change password and view hardware association",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Hardware Binding Info Section
                Text(
                    text = "HARDWARE BINDING STATUS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryGold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Bound Device ID:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            SelectionContainer {
                                Text(
                                    text = deviceId,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SecondaryGold,
                                    modifier = Modifier.testTag("student_bound_device_id_text")
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your premium PMS/CSS/PCS account is securely bound to this physical device hardware identifier to prevent account sharing and leaks. To login on a different phone, contact your Administrator for a hardware association unbind request.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(16.dp))

                // Change Password Form Section
                Text(
                    text = "CHANGE PASSWORD",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryGold
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text("Current Password") },
                    trailingIcon = {
                        IconButton(onClick = { showOldPassword = !showOldPassword }) {
                            Icon(
                                imageVector = if (showOldPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Old Password Visibility"
                            )
                        }
                    },
                    visualTransformation = if (showOldPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("old_password_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password (min 6 characters)") },
                    trailingIcon = {
                        IconButton(onClick = { showNewPassword = !showNewPassword }) {
                            Icon(
                                imageVector = if (showNewPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle New Password Visibility"
                            )
                        }
                    },
                    visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_password_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    trailingIcon = {
                        IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                            Icon(
                                imageVector = if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Confirm Password Visibility"
                            )
                        }
                    },
                    visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("confirm_password_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                if (changePasswordError.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = changePasswordError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("change_password_error_text")
                    )
                }

                if (changePasswordSuccess.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = changePasswordSuccess,
                        color = Color(0xFF10B981),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("change_password_success_text")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (newPassword != confirmPassword) {
                            viewModel.changePasswordError.value = "New passwords do not match."
                            viewModel.changePasswordSuccess.value = ""
                        } else {
                            viewModel.changePassword(oldPassword, newPassword)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_change_password_button"),
                    enabled = !changePasswordLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryGold, contentColor = Color.Black)
                ) {
                    if (changePasswordLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(imageVector = Icons.Default.VpnKey, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Update Password", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(12.dp))

                // Academy Administrator Login Shortcut
                OutlinedButton(
                    onClick = { viewModel.navigateTo(AppScreen.AdminPanel) },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, SecondaryGold.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = SecondaryGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Academy Admin Login Portal",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryGold
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardShortcutCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SecondaryGold,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
        }
    }
}

// 4. Subjects Screen
@Composable
fun SubjectsScreen(viewModel: MainViewModel) {
    val subjects by viewModel.subjectsList.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0 = Compulsory, 1 = Optional

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Course Subjects",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Icon(
                Icons.Default.Book,
                contentDescription = null,
                tint = SecondaryGold
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Tab Row for Compulsory vs Optional
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("Compulsory", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("Optional", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val filtered = subjects.filter {
                if (selectedTab == 0) it.isCompulsory else !it.isCompulsory
            }

            items(filtered) { subject ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = subject.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNavy
                            )
                            if (subject.isCustom) {
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("Admin Added", fontSize = 9.sp) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = subject.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Actions for each Subject
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = {
                                    viewModel.selectedSubject.value = subject.name
                                    viewModel.loadMCQsForPractice()
                                    viewModel.navigateTo(AppScreen.McqPractice)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SecondaryGold),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("MCQ Prep", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    viewModel.selectedSubject.value = subject.name
                                    viewModel.navigateTo(AppScreen.SubjectNotes)
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Notes", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// 5. MCQ Practice Screen
@Composable
fun MCQPracticeScreen(viewModel: MainViewModel) {
    val activeMcqs by viewModel.activeMcqs.collectAsState()
    val currentIndex by viewModel.currentMcqIndex.collectAsState()
    val selectedOptionIndex by viewModel.selectedOptionIndex.collectAsState()
    val answered by viewModel.mcqAnswered.collectAsState()
    val showExplanation by viewModel.mcqExplanationVisible.collectAsState()
    val subjectName by viewModel.selectedSubject.collectAsState()
    val timeLeft by viewModel.mcqTimeLeft.collectAsState()

    val currentMcq = activeMcqs.getOrNull(currentIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Back Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo(AppScreen.Subjects) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "$subjectName MCQ Practice",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (currentMcq == null) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = SecondaryGold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No MCQs loaded for this filter.")
                }
            }
            return
        }

        // Timer & Progress Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Question ${currentIndex + 1} of ${activeMcqs.size}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            // Circular timer representation
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Timer, contentDescription = null, tint = SecondaryGold, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${timeLeft}s",
                    fontWeight = FontWeight.Bold,
                    color = if (timeLeft < 10) ErrorRed else SecondaryGold
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Question Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(currentMcq.difficulty) }
                    )
                    SuggestionChip(
                        onClick = {},
                        label = { Text(currentMcq.topic) }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = currentMcq.question,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 22.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Options
        val optionsList = remember(currentMcq.optionsJson) {
            try {
                // Parse simple array: ["Option A", "Option B"...]
                currentMcq.optionsJson.removePrefix("[").removeSuffix("]").split(",").map {
                    it.trim().removeSurrounding("\"")
                }
            } catch (e: Exception) {
                listOf("A", "B", "C", "D")
            }
        }

        optionsList.forEachIndexed { index, option ->
            val isSelected = selectedOptionIndex == index
            val isCorrectIndex = index == currentMcq.correctAnswerIndex

            val optionColor = when {
                answered && isCorrectIndex -> SuccessGreen
                answered && isSelected && !isCorrectIndex -> ErrorRed
                isSelected -> SecondaryGold
                else -> MaterialTheme.colorScheme.surface
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(
                        width = if (isSelected || (answered && isCorrectIndex)) 2.dp else 1.dp,
                        color = optionColor,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { viewModel.selectMcqOption(index) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    if (answered && isCorrectIndex) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Correct", tint = SuccessGreen)
                    } else if (answered && isSelected && !isCorrectIndex) {
                        Icon(Icons.Default.Cancel, contentDescription = "Incorrect", tint = ErrorRed)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Submit & Nav Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { viewModel.toggleMcqBookmark(currentMcq) }) {
                Icon(
                    imageVector = if (currentMcq.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = SecondaryGold
                )
            }

            Button(
                onClick = { viewModel.submitMcqAnswer() },
                enabled = selectedOptionIndex != null && !answered,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
            ) {
                Text("SUBMIT ANSWER")
            }

            IconButton(onClick = { /* simulated report question */ }) {
                Icon(Icons.Default.Report, contentDescription = "Report", tint = Color.Gray)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Nav Arrows
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = { viewModel.prevMcq() },
                enabled = currentIndex > 0
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Previous")
            }
            TextButton(
                onClick = { viewModel.nextMcq() },
                enabled = currentIndex < activeMcqs.size - 1
            ) {
                Text("Next")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }

        // Detailed Explanation Panel (Revealed upon answered)
        if (showExplanation) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "EXAMINER EXPLANATION:",
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentMcq.explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = SecondaryGold, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reference: ${currentMcq.bookReference}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// 6. Subject Notes Screen
@Composable
fun SubjectNotesScreen(viewModel: MainViewModel) {
    val notes by viewModel.subjectNotesList.collectAsState()
    val subjectName by viewModel.selectedSubject.collectAsState()

    var textSearch by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.navigateTo(AppScreen.Subjects) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "$subjectName Syllabus Notes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Search Bar
        OutlinedTextField(
            value = textSearch,
            onValueChange = { textSearch = it },
            placeholder = { Text("Search Notes & Outlines") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        val filteredNotes = notes.filter {
            it.subjectName == subjectName && (textSearch.isBlank() || it.title.contains(textSearch, ignoreCase = true) || it.contentMarkdown.contains(textSearch, ignoreCase = true))
        }

        if (filteredNotes.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                Text("No notes found for '$subjectName'.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredNotes) { note ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = note.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryNavy
                                )
                                IconButton(onClick = { viewModel.toggleNoteBookmark(note) }) {
                                    Icon(
                                        imageVector = if (note.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Bookmark",
                                        tint = SecondaryGold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = note.shortSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))
                            // Simple Scrollable markdown simulation
                            Text(
                                text = note.contentMarkdown,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 20.sp,
                                maxLines = 10,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

// 7. Past Papers Screen
@Composable
fun PastPapersScreen(viewModel: MainViewModel) {
    val papers by viewModel.pastPapersList.collectAsState()
    var selectedSubjectFilter by remember { mutableStateOf("All") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "CSS/PMS Past Solved Papers",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Review solved questions from historic exam years",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Quick filter row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("All", "Pakistan Affairs", "Current Affairs", "Islamiat")
            filters.forEach { filter ->
                FilterChip(
                    selected = selectedSubjectFilter == filter,
                    onClick = { selectedSubjectFilter = filter },
                    label = { Text(filter) }
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        val filteredPapers = papers.filter {
            selectedSubjectFilter == "All" || it.subjectName == selectedSubjectFilter
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filteredPapers) { paper ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = paper.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            SuggestionChip(
                                onClick = {},
                                label = { Text(if (paper.isSolved) "SOLVED" else "UNSOLVED") }
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Exam Year: ${paper.year} | Subject: ${paper.subjectName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryGold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Questions included:",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = paper.questionsJson,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (paper.isSolved && paper.solutionText.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Official Academic Solution Draft:",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PrimaryNavy
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = paper.solutionText,
                                        style = MaterialTheme.typography.bodySmall,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 8. Current Affairs Screen
@Composable
fun CurrentAffairsScreen(viewModel: MainViewModel) {
    val currentAffairs by viewModel.currentAffairsList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Current Affairs National & Global",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Daily digests, editorials, and national policy analyses",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(currentAffairs) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNavy,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.toggleCurrentAffairBookmark(item) }) {
                                Icon(
                                    imageVector = if (item.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Bookmark",
                                    tint = SecondaryGold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Category: ${item.category}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SecondaryGold,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = item.dateString,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.fullText,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

// 9. Vocabulary Section
@Composable
fun VocabularyScreen(viewModel: MainViewModel) {
    val vocabulary by viewModel.vocabularyList.collectAsState()
    var showFavoritesOnly by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CSS Vocabulary Builder",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "High-yield words with usage, synonyms, and antonyms",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            IconButton(onClick = { viewModel.navigateTo(AppScreen.Flashcards) }) {
                Icon(Icons.Default.Flip, contentDescription = "Flashcards", tint = SecondaryGold)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Favorites Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Show Favorites Only", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = showFavoritesOnly,
                onCheckedChange = { showFavoritesOnly = it }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        val listFiltered = vocabulary.filter {
            !showFavoritesOnly || it.isFavorite
        }

        if (listFiltered.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                Text("No words found.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(listFiltered) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.word,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryNavy
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.phonetic,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                                IconButton(onClick = { viewModel.toggleVocabFavorite(item) }) {
                                    Icon(
                                        imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = Color.Red
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Meaning: ${item.meaning}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Example: \"${item.usageExample}\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Synonyms: ${item.synonyms}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = SuccessGreen
                                )
                                Text(
                                    text = "Antonyms: ${item.antonyms}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = ErrorRed
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 10. Spaced Repetition Flashcard Screen
@Composable
fun FlashcardScreen(viewModel: MainViewModel) {
    val flashcards by viewModel.flashcardsList.collectAsState()
    var questionInput by remember { mutableStateOf("") }
    var answerInput by remember { mutableStateOf("") }
    var topicAiInput by remember { mutableStateOf("") }

    var revealAnswer by remember { mutableStateOf(false) }
    var currentCardIndex by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.navigateTo(AppScreen.Vocabulary) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Spaced Repetition Flashcards",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Active card display
        if (flashcards.isNotEmpty()) {
            val card = flashcards[currentCardIndex.coerceAtMost(flashcards.size - 1)]
            Text(
                text = "Flashcard ${currentCardIndex + 1} of ${flashcards.size} (${card.subjectName})",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Flashcard Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { revealAnswer = !revealAnswer },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (revealAnswer) SecondaryGold.copy(alpha = 0.08f) else PrimaryNavy.copy(alpha = 0.05f)
                ),
                border = BorderStroke(1.dp, if (revealAnswer) SecondaryGold else PrimaryNavy)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (revealAnswer) "ANSWER" else "QUESTION",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (revealAnswer) SecondaryGold else PrimaryNavy,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (revealAnswer) card.answer else card.question,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tap Card to Flip",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Row for active card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = {
                        revealAnswer = false
                        if (currentCardIndex > 0) currentCardIndex--
                    },
                    enabled = currentCardIndex > 0
                ) {
                    Text("Previous")
                }

                Button(
                    onClick = { viewModel.deleteFlashcard(card.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Remove")
                }

                OutlinedButton(
                    onClick = {
                        revealAnswer = false
                        if (currentCardIndex < flashcards.size - 1) currentCardIndex++
                    },
                    enabled = currentCardIndex < flashcards.size - 1
                ) {
                    Text("Next")
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("No flashcards added yet. Create some below!")
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // Create Manual Flashcard
        Text(
            text = "Create Custom Flashcard",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = questionInput,
            onValueChange = { questionInput = it },
            label = { Text("Flashcard Question") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = answerInput,
            onValueChange = { answerInput = it },
            label = { Text("Flashcard Answer") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                if (questionInput.isNotBlank() && answerInput.isNotBlank()) {
                    viewModel.createFlashcard("General Studies", questionInput, answerInput)
                    questionInput = ""
                    answerInput = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
        ) {
            Text("Save Custom Card")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // AI Generated Flashcards section
        Text(
            text = "Generate Flashcards with AI",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = SecondaryGold
        )
        Text(
            text = "Uses Gemini to build high-yield cards automatically",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = topicAiInput,
            onValueChange = { topicAiInput = it },
            label = { Text("Topic (e.g., SEATO, Durand Line, Zakat)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                if (topicAiInput.isNotBlank()) {
                    viewModel.generateFlashcardsWithAI("Pakistan Studies", topicAiInput)
                    topicAiInput = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = SecondaryGold)
        ) {
            Text("AI GENERATE FLASHCARD")
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// 11. Personalized Study Plan Screen
@Composable
fun StudyPlanScreen(viewModel: MainViewModel) {
    val examType by viewModel.studyPlanInputExam.collectAsState()
    val hoursGoal by viewModel.studyPlanInputHours.collectAsState()
    val weakness by viewModel.studyPlanInputWeakness.collectAsState()

    val loading by viewModel.studyPlanLoading.collectAsState()
    val planOutput by viewModel.studyPlanResult.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "AI Personalized Study Planner",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Specify your focus areas and let our AI Counselor build a calendar plan",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Onboarding form
        OutlinedTextField(
            value = examType,
            onValueChange = { viewModel.studyPlanInputExam.value = it },
            label = { Text("Target Exam (CSS / PCS / PMS)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = hoursGoal,
            onValueChange = { viewModel.studyPlanInputHours.value = it },
            label = { Text("Daily Study Hours Available") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = weakness,
            onValueChange = { viewModel.studyPlanInputWeakness.value = it },
            label = { Text("Weakness / Focus Subjects") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { viewModel.generatePersonalizedStudyPlan() },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Text("GENERATE STUDY PLAN WITH AI")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (planOutput.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SecondaryGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Your Strategic Preparation Plan:",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = planOutput,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// 12. AI Tutor Screen (Counseling Conversation Room)
@Composable
fun AITutorScreen(viewModel: MainViewModel) {
    val messages by viewModel.aiTutorMessages.collectAsState()
    val chatInput by viewModel.chatInputText.collectAsState()
    val sessions by viewModel.chatSessionsList.collectAsState()
    val activeSession by viewModel.activeSessionId.collectAsState()
    val loading by viewModel.aiTutorLoading.collectAsState()

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Chat Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryNavy)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(SecondaryGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "AI Tutor Counsel",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(SuccessGreen, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Online & Ready", fontSize = 10.sp, color = SuccessGreen)
                            }
                        }
                    }

                    Row {
                        IconButton(onClick = { viewModel.startNewChatSession("Counsel Session ${sessions.size + 1}") }) {
                            Icon(Icons.Default.Add, contentDescription = "New chat", tint = Color.White)
                        }
                        IconButton(onClick = {
                            Toast.makeText(context, "Chat exported to device successfully", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Export chat", tint = Color.White)
                        }
                    }
                }
            }
        }

        // Active Conversation List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { message ->
                val isUser = message.role == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.85f),
                        shape = RoundedCornerShape(
                            topStart = 12.dp,
                            topEnd = 12.dp,
                            bottomStart = if (isUser) 12.dp else 0.dp,
                            bottomEnd = if (isUser) 0.dp else 12.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) SecondaryGold.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isUser) "You" else "AI Academic Tutor",
                                fontWeight = FontWeight.Bold,
                                color = if (isUser) SecondaryGold else PrimaryNavy,
                                style = MaterialTheme.typography.labelSmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = message.text,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            if (loading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(shape = RoundedCornerShape(12.dp)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AI Tutor is thinking...", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }

        // Input Tray
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = chatInput,
                onValueChange = { viewModel.chatInputText.value = it },
                placeholder = { Text("Ask anything about CSS / PMS subjects...") },
                modifier = Modifier.weight(1f),
                singleLine = false,
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { viewModel.sendChatMessage() },
                enabled = chatInput.isNotBlank() && !loading,
                modifier = Modifier
                    .size(48.dp)
                    .background(PrimaryNavy, CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

// 13. Essay Section Screen
@Composable
fun EssaySectionScreen(viewModel: MainViewModel) {
    val topics by viewModel.essayTopicsList.collectAsState()
    val selectedTopic by viewModel.selectedEssayTopic.collectAsState()
    val essayDraft by viewModel.essayInput.collectAsState()

    val loading by viewModel.essayGradingLoading.collectAsState()
    val gradingOutput by viewModel.essayGradingResult.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "CSS Essay Outline Evaluator",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Submit outlines or essay introduction drafts for expert analytical evaluation and model suggestions",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Topic Selector
        Text("Select Essay Subject Topic:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))

        topics.forEach { topic ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(
                        width = if (selectedTopic?.id == topic.id) 2.dp else 1.dp,
                        color = if (selectedTopic?.id == topic.id) SecondaryGold else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { viewModel.selectedEssayTopic.value = topic },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.Article, contentDescription = null, tint = SecondaryGold)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(topic.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Category: ${topic.category} | Difficulty: ${topic.difficulty}", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Outline input
        Text("Input Your Outline or Essay Draft:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = essayDraft,
            onValueChange = { viewModel.essayInput.value = it },
            placeholder = { Text("Write or paste your outline structure or comprehensive starting draft here...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            maxLines = 15
        )
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { viewModel.evaluateEssay() },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedTopic != null && essayDraft.isNotBlank()
        ) {
            if (loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Text("EVALUATE WITH AI ESSAY CHECKER")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Output scorecard
        if (gradingOutput.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, SuccessGreen)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = SuccessGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Official AI Examiner Feedback:",
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = gradingOutput,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// 14. Leaderboard Screen
@Composable
fun LeaderboardScreen(viewModel: MainViewModel, profile: UserProfile?) {
    val leaderboard by viewModel.leaderboardList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // High level current user position banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PrimaryNavy),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "YOUR ACADEMY STANDING",
                        style = MaterialTheme.typography.labelSmall,
                        color = SecondaryGold,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = profile?.fullName ?: "Syed Muhammad Ali",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Province: ${profile?.province ?: "Punjab"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Rank #12",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = SecondaryGold
                    )
                    Text(
                        text = "${profile?.points ?: 0} XP Points",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "National Aspirer Leaderboard",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Top competitive scores in Pakistan",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Leaderboard Headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Rank & Name", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Text("Province", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(0.6f), textAlign = TextAlign.Center)
            Text("XP Score", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(0.4f), textAlign = TextAlign.End)
        }
        HorizontalDivider()

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            items(leaderboard) { user ->
                val isMe = user.name == "Syed Muhammad Ali"
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isMe) SecondaryGold.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rank Indicator
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    if (user.rank <= 3) SecondaryGold else Color.Gray.copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${user.rank}",
                                fontWeight = FontWeight.Bold,
                                color = if (user.rank <= 3) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))

                        // User Name
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(user.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                if (user.isPremium) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.Verified,
                                        contentDescription = "Premium",
                                        tint = SecondaryGold,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        // Province
                        Text(
                            user.province,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(0.6f),
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )

                        // Points
                        Text(
                            "${user.points}",
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryNavy,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(0.4f),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

// 15. Daily Quiz Screen
@Composable
fun DailyQuizScreen(viewModel: MainViewModel) {
    val active by viewModel.dailyQuizActive.collectAsState()
    val questions by viewModel.dailyQuizQuestions.collectAsState()
    val currentIndex by viewModel.dailyQuizIndex.collectAsState()
    val selectedOption by viewModel.dailyQuizSelectedOption.collectAsState()
    val score by viewModel.dailyQuizScore.collectAsState()
    val completed by viewModel.dailyQuizCompleted.collectAsState()
    val timeLeft by viewModel.dailyQuizTimeLeft.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!active && !completed) {
            Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = SecondaryGold, modifier = Modifier.size(72.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Daily Rapid Quiz",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Test your reflexes with 5 random high-yield questions.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { viewModel.startDailyQuiz() },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
            ) {
                Text("START RAPID QUIZ")
            }
        } else if (active && questions.isNotEmpty()) {
            val q = questions[currentIndex]
            Text("Daily Rapid Quiz", fontWeight = FontWeight.Bold, color = SecondaryGold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Question ${currentIndex + 1} of ${questions.size}")
                Text("Timer: ${timeLeft}s", color = ErrorRed, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Text(q.question, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(16.dp))

            val optionsList = remember(q.optionsJson) {
                try {
                    q.optionsJson.removePrefix("[").removeSuffix("]").split(",").map {
                        it.trim().removeSurrounding("\"")
                    }
                } catch (e: Exception) {
                    listOf("A", "B", "C", "D")
                }
            }

            optionsList.forEachIndexed { index, option ->
                val isSelected = selectedOption == index
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) SecondaryGold else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.selectQuizOption(index) }
                ) {
                    Text(option, modifier = Modifier.padding(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.nextQuizQuestion() },
                enabled = selectedOption != null,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("SUBMIT AND NEXT")
            }
        } else if (completed) {
            Icon(Icons.Default.Celebration, contentDescription = null, tint = SecondaryGold, modifier = Modifier.size(72.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Quiz Complete!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                "You scored $score out of 5",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = SuccessGreen
            )
            Text(
                "XP Awarded: +${score * 20} XP",
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryGold
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { viewModel.navigateTo(AppScreen.Dashboard) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
            ) {
                Text("GO TO DASHBOARD")
            }
        }
    }
}

// 16. Bookmarks Screen
@Composable
fun BookmarksScreen(viewModel: MainViewModel) {
    // Left empty for scope limits, accessible via main modules which support bookmarking
}

// 17. Admin Panel Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(viewModel: MainViewModel) {
    val adminUser by viewModel.adminUsername.collectAsState()
    val adminPassword by viewModel.adminPassword.collectAsState()
    val isLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    var showAdminPassToggle by remember { mutableStateOf(false) }

    var subjectNameInput by remember { mutableStateOf("") }
    var subjectDescInput by remember { mutableStateOf("") }
    var isCompulsory by remember { mutableStateOf(true) }

    // MCQ Input Form States
    var mcqQuestion by remember { mutableStateOf("") }
    var mcqOptionA by remember { mutableStateOf("") }
    var mcqOptionB by remember { mutableStateOf("") }
    var mcqOptionC by remember { mutableStateOf("") }
    var mcqOptionD by remember { mutableStateOf("") }
    var mcqCorrectIndex by remember { mutableStateOf(0) }
    var mcqExplanation by remember { mutableStateOf("") }
    var mcqDifficulty by remember { mutableStateOf("Medium") }
    var mcqSubject by remember { mutableStateOf("Pakistan Affairs") }
    var mcqTopic by remember { mutableStateOf("Constitutional History") }

    val subjectsFlow by viewModel.subjectsList.collectAsState()
    val availableSubjects = remember(subjectsFlow) {
        val list = subjectsFlow.map { it.name }
        if (list.isEmpty()) listOf("Pakistan Affairs", "Current Affairs", "English Essay", "General Science") else list
    }

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 1200.dp)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            if (!isLoggedIn) {
                // Centered beautifully on wide screens
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 480.dp)
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 40.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = SecondaryGold,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("CSS Compass Admin Portal", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Authorized personnel only. Please verify credentials.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        OutlinedTextField(
                            value = adminUser,
                            onValueChange = { viewModel.adminUsername.value = it },
                            label = { Text("Admin Username") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = adminPassword,
                            onValueChange = { viewModel.adminPassword.value = it },
                            label = { Text("Admin Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { showAdminPassToggle = !showAdminPassToggle }) {
                                    Icon(
                                        imageVector = if (showAdminPassToggle) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Admin Password Visibility"
                                    )
                                }
                            },
                            visualTransformation = if (showAdminPassToggle) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                val success = viewModel.loginAdmin()
                                if (!success) {
                                    Toast.makeText(context, "Incorrect Master Credentials. Access denied.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                        ) {
                            Text("SECURE ADMIN AUTHENTICATE", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        TextButton(
                            onClick = { viewModel.navigateTo(AppScreen.Dashboard) }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Return to Student Academy", fontSize = 12.sp)
                        }
                    }
                }
            } else {
                // Logged in admin screen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = SecondaryGold,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Admin Control Center",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Manage Students, Licenses, Single-Device Locks & Syllabus",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(
                        onClick = { viewModel.logoutAdmin() },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Admin Navigation Tabs
                var selectedAdminTab by remember { mutableStateOf(0) }
                val adminTabs = listOf("🔑 License Generator", "👥 Students & Devices", "📝 Syllabus & MCQs", "⚙️ Server Settings")

                ScrollableTabRow(
                    selectedTabIndex = selectedAdminTab,
                    edgePadding = 0.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = SecondaryGold
                ) {
                    adminTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedAdminTab == index,
                            onClick = { selectedAdminTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedAdminTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedAdminTab == index) SecondaryGold else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                when (selectedAdminTab) {
                    0 -> {
                        // TAB 0: License Key Generator & Sales Engine
                        LicenseGeneratorCard(viewModel = viewModel)
                        Spacer(modifier = Modifier.height(20.dp))
                        StatsCard()
                    }
                    1 -> {
                        // TAB 1: Student Directory & Device Access Control
                        StudentManagementCard(viewModel = viewModel)
                    }
                    2 -> {
                        // TAB 2: MCQ & Subject Publisher
                        SubjectManagerCard(
                            subjectNameInput = subjectNameInput,
                            subjectDescInput = subjectDescInput,
                            isCompulsory = isCompulsory,
                            onSubjectNameChange = { subjectNameInput = it },
                            onSubjectDescChange = { subjectDescInput = it },
                            onCompulsoryChange = { isCompulsory = it },
                            onPublish = {
                                if (subjectNameInput.isNotBlank()) {
                                    viewModel.addNewSubject(subjectNameInput, subjectDescInput, isCompulsory)
                                    Toast.makeText(context, "Subject '$subjectNameInput' published successfully!", Toast.LENGTH_SHORT).show()
                                    subjectNameInput = ""
                                    subjectDescInput = ""
                                } else {
                                    Toast.makeText(context, "Please enter a subject title", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        McqPublisherCard(
                            mcqQuestion = mcqQuestion,
                            mcqOptionA = mcqOptionA,
                            mcqOptionB = mcqOptionB,
                            mcqOptionC = mcqOptionC,
                            mcqOptionD = mcqOptionD,
                            mcqCorrectIndex = mcqCorrectIndex,
                            mcqExplanation = mcqExplanation,
                            mcqDifficulty = mcqDifficulty,
                            mcqSubject = mcqSubject,
                            mcqTopic = mcqTopic,
                            availableSubjects = availableSubjects,
                            onQuestionChange = { mcqQuestion = it },
                            onOptionAChange = { mcqOptionA = it },
                            onOptionBChange = { mcqOptionB = it },
                            onOptionCChange = { mcqOptionC = it },
                            onOptionDChange = { mcqOptionD = it },
                            onCorrectIndexChange = { mcqCorrectIndex = it },
                            onExplanationChange = { mcqExplanation = it },
                            onDifficultyChange = { mcqDifficulty = it },
                            onSubjectChange = { mcqSubject = it },
                            onTopicChange = { mcqTopic = it },
                            onPublish = {
                                if (mcqQuestion.isNotBlank() && mcqOptionA.isNotBlank() && mcqOptionB.isNotBlank() && mcqOptionC.isNotBlank() && mcqOptionD.isNotBlank()) {
                                    viewModel.addNewMCQ(
                                        subject = mcqSubject,
                                        question = mcqQuestion,
                                        optionA = mcqOptionA,
                                        optionB = mcqOptionB,
                                        optionC = mcqOptionC,
                                        optionD = mcqOptionD,
                                        correctAnswerIndex = mcqCorrectIndex,
                                        explanation = mcqExplanation,
                                        difficulty = mcqDifficulty,
                                        topic = mcqTopic
                                    )
                                    Toast.makeText(context, "MCQ successfully saved and published!", Toast.LENGTH_SHORT).show()
                                    
                                    // Reset fields
                                    mcqQuestion = ""
                                    mcqOptionA = ""
                                    mcqOptionB = ""
                                    mcqOptionC = ""
                                    mcqOptionD = ""
                                    mcqExplanation = ""
                                } else {
                                    Toast.makeText(context, "Please fill out all MCQ fields and options", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                    3 -> {
                        // TAB 3: Monetization & Server Settings
                        MonetizationSettingsCard(viewModel = viewModel)
                        Spacer(modifier = Modifier.height(20.dp))
                        StatsCard()
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectManagerCard(
    subjectNameInput: String,
    subjectDescInput: String,
    isCompulsory: Boolean,
    onSubjectNameChange: (String) -> Unit,
    onSubjectDescChange: (String) -> Unit,
    onCompulsoryChange: (Boolean) -> Unit,
    onPublish: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Publish Syllabus Subject", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PrimaryNavy)
            Text("Adds a new exam-curriculum subject to catalog", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = subjectNameInput,
                onValueChange = onSubjectNameChange,
                label = { Text("Subject Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = subjectDescInput,
                onValueChange = onSubjectDescChange,
                label = { Text("Description & Syllabus Details") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isCompulsory, onCheckedChange = onCompulsoryChange)
                Text("Is Compulsory Subject", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onPublish,
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("PUBLISH SUBJECT", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun McqPublisherCard(
    mcqQuestion: String,
    mcqOptionA: String,
    mcqOptionB: String,
    mcqOptionC: String,
    mcqOptionD: String,
    mcqCorrectIndex: Int,
    mcqExplanation: String,
    mcqDifficulty: String,
    mcqSubject: String,
    mcqTopic: String,
    availableSubjects: List<String>,
    onQuestionChange: (String) -> Unit,
    onOptionAChange: (String) -> Unit,
    onOptionBChange: (String) -> Unit,
    onOptionCChange: (String) -> Unit,
    onOptionDChange: (String) -> Unit,
    onCorrectIndexChange: (Int) -> Unit,
    onExplanationChange: (String) -> Unit,
    onDifficultyChange: (String) -> Unit,
    onSubjectChange: (String) -> Unit,
    onTopicChange: (String) -> Unit,
    onPublish: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Create Practice MCQ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PrimaryNavy)
            Text("Direct Room database publisher engine", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            // MCQ Subject Dropdown
            var expandedSubject by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { expandedSubject = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Subject: $mcqSubject", color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                }
                DropdownMenu(expanded = expandedSubject, onDismissRequest = { expandedSubject = false }) {
                    availableSubjects.forEach { sub ->
                        DropdownMenuItem(
                            text = { Text(sub) },
                            onClick = {
                                onSubjectChange(sub)
                                expandedSubject = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Difficulty filter chips
            Text("Select Difficulty Level:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Easy", "Medium", "Hard").forEach { diff ->
                    FilterChip(
                        selected = mcqDifficulty == diff,
                        onClick = { onDifficultyChange(diff) },
                        label = { Text(diff) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Topic input
            OutlinedTextField(
                value = mcqTopic,
                onValueChange = onTopicChange,
                label = { Text("Topic/Chapter Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Question Input
            OutlinedTextField(
                value = mcqQuestion,
                onValueChange = onQuestionChange,
                label = { Text("Question Prompt") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Options
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = mcqOptionA,
                    onValueChange = onOptionAChange,
                    label = { Text("Option A") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = mcqOptionB,
                    onValueChange = onOptionBChange,
                    label = { Text("Option B") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = mcqOptionC,
                    onValueChange = onOptionCChange,
                    label = { Text("Option C") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = mcqOptionD,
                    onValueChange = onOptionDChange,
                    label = { Text("Option D") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Correct Option Index
            Text("Correct Option:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("A", "B", "C", "D").forEachIndexed { idx, label ->
                    FilterChip(
                        selected = mcqCorrectIndex == idx,
                        onClick = { onCorrectIndexChange(idx) },
                        label = { Text(label) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Academic Explanation
            OutlinedTextField(
                value = mcqExplanation,
                onValueChange = onExplanationChange,
                label = { Text("Academic Explanation / Book Citation") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onPublish,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("PUBLISH MCQ", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StatsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("System Analytics & Sync State", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Database Status:", style = MaterialTheme.typography.bodyMedium)
                Text("ROOM SQLite (Synced)", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Server Latency:", style = MaterialTheme.typography.bodyMedium)
                Text("12 ms (Excellent)", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Enrolled Students:", style = MaterialTheme.typography.bodyMedium)
                Text("3,420 registered", fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("AI Tutor Request Volume:", style = MaterialTheme.typography.bodyMedium)
                Text("45,210 tokens/day", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 18. Premium / Subscription Page
@Composable
fun PremiumScreen(viewModel: MainViewModel, profile: UserProfile?) {
    val context = LocalContext.current
    val whatsappNumber by viewModel.whatsappNumber.collectAsState()
    var activationKeyInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = SecondaryGold, modifier = Modifier.size(72.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "CSS Compass Premium Access",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Unlock unlimited AI counseling, live essay checks, and offline resources",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (profile?.isPremium == true) {
            // Already Premium State
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryNavy),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, SecondaryGold)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Verified,
                        contentDescription = "Active",
                        tint = SecondaryGold,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "PREMIUM ACTIVE",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Thank you for supporting CSS Compass. You have unlimited AI tutoring, essay evaluation, and full syllabus access activated.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Unlocked state (Paywall)
            // Activation Code Input Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Have an Activation License Key?",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = PrimaryNavy
                    )
                    Text(
                        text = "Enter the license key sent by our support team to unlock instantly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = activationKeyInput,
                            onValueChange = { activationKeyInput = it },
                            placeholder = { Text("e.g. CSS-PREM-XXXX-XX") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (activationKeyInput.isBlank()) {
                                    Toast.makeText(context, "Please enter a key", Toast.LENGTH_SHORT).show()
                                } else {
                                    val success = viewModel.activateLicenseKey(activationKeyInput)
                                    if (success) {
                                        Toast.makeText(context, "✨ Compass Premium Activated! Welcome on board!", Toast.LENGTH_LONG).show()
                                        activationKeyInput = ""
                                    } else {
                                        Toast.makeText(context, "❌ Invalid Activation Key. Please try again or contact support.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryGold),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Unlock", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Select an Aspirer Premium Plan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryNavy,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Pricing options
            SubscriptionOptionCard(
                title = "Aspirer Monthly",
                price = "Rs. 1,500 / month",
                description = "Basic AI tutoring, daily essay checks, and standard practice MCQs.",
                onClick = {
                    val message = "Hello CSS Compass! I am preparing for ${profile?.examType ?: "CSS"} exams. I want to buy the *Aspirer Monthly Plan* (Rs. 1,500/month).\n\nMy Details:\n- Name: ${profile?.fullName ?: "Aspirer"}\n- Email: ${profile?.email ?: "Not set"}\n- Province: ${profile?.province ?: "Punjab"}\n\nPlease guide me on the EasyPaisa/JazzCash activation steps."
                    launchWhatsApp(context, whatsappNumber, message)
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            SubscriptionOptionCard(
                title = "Aspirer Yearly (Best Value)",
                price = "Rs. 10,000 / year",
                description = "Unlimited essay audits, priority AI tutoring, custom study schedules, full high-tier mocks.",
                isPopular = true,
                onClick = {
                    val message = "Hello CSS Compass! I am preparing for ${profile?.examType ?: "CSS"} exams. I want to buy the *Aspirer Yearly Plan* (Rs. 10,000/year).\n\nMy Details:\n- Name: ${profile?.fullName ?: "Aspirer"}\n- Email: ${profile?.email ?: "Not set"}\n- Province: ${profile?.province ?: "Punjab"}\n\nPlease guide me on the EasyPaisa/JazzCash activation steps."
                    launchWhatsApp(context, whatsappNumber, message)
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            SubscriptionOptionCard(
                title = "Lifetime Scholar",
                price = "Rs. 25,000 / lifetime",
                description = "Complete perpetual access to all future updates, live mentorship webinars, and CSS/PMS material.",
                onClick = {
                    val message = "Hello CSS Compass! I am preparing for ${profile?.examType ?: "CSS"} exams. I want to buy the *Lifetime Scholar Plan* (Rs. 25,000/lifetime).\n\nMy Details:\n- Name: ${profile?.fullName ?: "Aspirer"}\n- Email: ${profile?.email ?: "Not set"}\n- Province: ${profile?.province ?: "Punjab"}\n\nPlease guide me on the EasyPaisa/JazzCash activation steps."
                    launchWhatsApp(context, whatsappNumber, message)
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

fun launchWhatsApp(context: android.content.Context, phoneNumber: String, message: String) {
    try {
        val sanitizedPhone = phoneNumber.replace("+", "").replace(" ", "").trim()
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse("https://api.whatsapp.com/send?phone=$sanitizedPhone&text=${android.net.Uri.encode(message)}")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback: Copy message to clipboard
        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("CSS Compass Order", message)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "WhatsApp not found. Order details copied to clipboard! Please manually text support at $phoneNumber", Toast.LENGTH_LONG).show()
    }
}

@Composable
fun SubscriptionOptionCard(
    title: String,
    price: String,
    description: String,
    isPopular: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isPopular) 2.dp else 0.dp,
                color = if (isPopular) SecondaryGold else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isPopular) {
                Box(
                    modifier = Modifier
                        .background(SecondaryGold, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("MOST POPULAR", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(price, fontWeight = FontWeight.ExtraBold, color = PrimaryNavy, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ORDER VIA WHATSAPP")
            }
        }
    }
}

@Composable
fun MonetizationSettingsCard(viewModel: MainViewModel) {
    val whatsappNumber by viewModel.whatsappNumber.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()

    var inputNumber by remember { mutableStateOf(whatsappNumber) }
    var inputServerUrl by remember { mutableStateOf(serverUrl) }
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Academy Control Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PrimaryNavy)
            Text("Set your student database server URL and the WhatsApp number where students send receipts to activate accounts.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = inputNumber,
                onValueChange = { inputNumber = it },
                label = { Text("Support WhatsApp (e.g. +923001234567)") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = inputServerUrl,
                onValueChange = { inputServerUrl = it },
                label = { Text("Portal Server Base URL") },
                leadingIcon = { Icon(Icons.Default.Computer, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (inputNumber.isNotBlank() && inputServerUrl.isNotBlank()) {
                        viewModel.updateWhatsappNumber(inputNumber)
                        viewModel.updateServerUrl(inputServerUrl)
                        Toast.makeText(context, "Academy settings saved successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Fields cannot be blank", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("SAVE SYSTEM SETTINGS", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StudentManagementCard(viewModel: MainViewModel) {
    val students by viewModel.managedStudentsList.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    var showAddDialog by remember { mutableStateOf(false) }
    var newStudentName by remember { mutableStateOf("") }
    var newStudentUsername by remember { mutableStateOf("") }
    var newStudentEmail by remember { mutableStateOf("") }
    var newStudentPassword by remember { mutableStateOf("") }
    var newStudentPhone by remember { mutableStateOf("") }
    var newStudentPlan by remember { mutableStateOf("Yearly Aspirant (2026)") }
    var newStudentPaid by remember { mutableStateOf(true) }
    var showPasswordInDialog by remember { mutableStateOf(false) }

    var studentCredentialsToShare by remember { mutableStateOf<MainViewModel.AdminStudent?>(null) }
    val visiblePasswords = remember { mutableStateMapOf<String, Boolean>() }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Prominent Header Bar with Gold Add Student Button
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PrimaryNavy),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, SecondaryGold.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Student Directory & Access",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${students.size} enrolled students registered with portal credentials",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { 
                        newStudentPassword = "Pass@${(1000..9999).random()}"
                        showAddDialog = true 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryGold),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Add Student",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "+ ADD STUDENT",
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (students.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Group, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No students in directory", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Tap '+ ADD STUDENT' above to register a student with Username & Password.", fontSize = 12.sp, color = Color.Gray)
                }
            }
        } else {
            // Student List Cards
            students.forEach { student ->
                val isPassVisible = visiblePasswords[student.id] ?: false

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, if (student.isPaid) SecondaryGold.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Header Row: Avatar, Name, Email, Status Pill
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(PrimaryNavy, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = student.name.take(1).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = student.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = student.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Plan: ${student.plan}",
                                        fontSize = 11.sp,
                                        color = SecondaryGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Payment Status Pill
                            Surface(
                                color = if (student.isPaid) Color(0xFF2E7D32).copy(alpha = 0.2f) else Color(0xFFE65100).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (student.isPaid) Color(0xFF2E7D32) else Color(0xFFE65100))
                            ) {
                                Text(
                                    text = if (student.isPaid) "ACTIVE (PAID)" else "PENDING",
                                    color = if (student.isPaid) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Student Login Credentials Box (Username & Password)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = SecondaryGold, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Username: ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = student.username.ifBlank { student.email.substringBefore("@") },
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            val usernameText = student.username.ifBlank { student.email.substringBefore("@") }
                                            clipboardManager.setText(AnnotatedString(usernameText))
                                            Toast.makeText(context, "Username '$usernameText' copied!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Username", modifier = Modifier.size(14.dp), tint = SecondaryGold)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.VpnKey, contentDescription = null, tint = SecondaryGold, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Password: ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = if (isPassVisible) student.password else "••••••••",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Row {
                                        IconButton(
                                            onClick = { visiblePasswords[student.id] = !isPassVisible },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isPassVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = "Toggle Password",
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(4.dp))

                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(student.password))
                                                Toast.makeText(context, "Password copied to clipboard!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Password", modifier = Modifier.size(14.dp), tint = SecondaryGold)
                                        }
                                    }
                                }

                                // Single Tap Copy Full Student Message
                                OutlinedButton(
                                    onClick = {
                                        val uName = student.username.ifBlank { student.email.substringBefore("@") }
                                        val message = """
                                            🎓 *CSS Compass Academy - Student Login*
                                            Dear ${student.name}, your account is active!
                                            
                                            👤 *Username:* $uName
                                            📧 *Email:* ${student.email}
                                            🔑 *Password:* ${student.password}
                                            📦 *Plan:* ${student.plan}
                                            
                                            Download the App & Login to start your preparation!
                                        """.trimIndent()
                                        clipboardManager.setText(AnnotatedString(message))
                                        Toast.makeText(context, "Login details copied! Paste & send to ${student.name} on WhatsApp.", Toast.LENGTH_LONG).show()
                                    },
                                    modifier = Modifier.fillMaxWidth().height(34.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    border = BorderStroke(1.dp, SecondaryGold.copy(alpha = 0.5f))
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp), tint = SecondaryGold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Copy Student WhatsApp Login Message", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Device Security Info
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = if (student.isDeviceLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = if (student.isDeviceLocked) SecondaryGold else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Security: ${if (student.isDeviceLocked) "1-Device Locked" else "Unlocked"}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(${student.boundDeviceId})",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons Bar: Reset Device | Revoke/Authorize | Delete Student
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Reset Device Button
                            OutlinedButton(
                                onClick = {
                                    viewModel.resetStudentDeviceLock(student.id)
                                    Toast.makeText(context, "Device binding cleared for ${student.name}. Next login binds their phone.", Toast.LENGTH_LONG).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reset Device", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                            }

                            // Revoke / Authorize Button
                            Button(
                                onClick = {
                                    viewModel.toggleStudentPaymentStatus(student.id)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (student.isPaid) Color(0xFFC62828) else Color(0xFF2E7D32)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Icon(
                                    imageVector = if (student.isPaid) Icons.Default.Block else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (student.isPaid) "Revoke" else "Authorize",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Dedicated Delete Student Button
                            Button(
                                onClick = {
                                    viewModel.removeManagedStudent(student.id)
                                    Toast.makeText(context, "${student.name} removed from academy directory", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Student",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Delete",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = SecondaryGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Register & Authorize Student", fontWeight = FontWeight.Bold, color = PrimaryNavy)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = newStudentName,
                        onValueChange = { 
                            newStudentName = it
                            if (newStudentUsername.isBlank()) {
                                newStudentUsername = it.lowercase().replace(Regex("[^a-z0-9]"), "") + "_css"
                            }
                        },
                        label = { Text("Student Full Name *") },
                        placeholder = { Text("e.g. Muhammad Bilal") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SecondaryGold) }
                    )

                    OutlinedTextField(
                        value = newStudentUsername,
                        onValueChange = { newStudentUsername = it.lowercase().trim() },
                        label = { Text("Username / Student ID *") },
                        placeholder = { Text("e.g. bilal_css2026") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null, tint = SecondaryGold) }
                    )

                    OutlinedTextField(
                        value = newStudentEmail,
                        onValueChange = { newStudentEmail = it.trim() },
                        label = { Text("Student Email *") },
                        placeholder = { Text("e.g. bilal@gmail.com") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SecondaryGold) }
                    )

                    OutlinedTextField(
                        value = newStudentPassword,
                        onValueChange = { newStudentPassword = it },
                        label = { Text("Student Login Password *") },
                        placeholder = { Text("e.g. Pakistan123! or PIN") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (showPasswordInDialog) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = SecondaryGold) },
                        trailingIcon = {
                            IconButton(onClick = { showPasswordInDialog = !showPasswordInDialog }) {
                                Icon(
                                    imageVector = if (showPasswordInDialog) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password"
                                )
                            }
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                newStudentPassword = "Pass@${(1000..9999).random()}"
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(14.dp), tint = SecondaryGold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("🎲 Auto-Generate Password", fontSize = 11.sp, color = SecondaryGold, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedTextField(
                        value = newStudentPhone,
                        onValueChange = { newStudentPhone = it },
                        label = { Text("Phone / WhatsApp (Optional)") },
                        placeholder = { Text("e.g. +92 300 1234567") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = SecondaryGold) }
                    )

                    OutlinedTextField(
                        value = newStudentPlan,
                        onValueChange = { newStudentPlan = it },
                        label = { Text("Subscription Tier / Plan") },
                        placeholder = { Text("Yearly Aspirant (2026)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.CardMembership, contentDescription = null, tint = SecondaryGold) }
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SecondaryGold.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Checkbox(checked = newStudentPaid, onCheckedChange = { newStudentPaid = it })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Grant Full Access (Mark as Paid)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newStudentName.isNotBlank() && (newStudentEmail.isNotBlank() || newStudentUsername.isNotBlank())) {
                            val assignedUsername = if (newStudentUsername.isNotBlank()) newStudentUsername.trim() else if (newStudentEmail.contains("@")) newStudentEmail.substringBefore("@") else newStudentName.lowercase().replace(" ", "")
                            val assignedEmail = if (newStudentEmail.isNotBlank()) newStudentEmail.trim() else "$assignedUsername@csscompass.com"
                            val assignedPassword = if (newStudentPassword.isNotBlank()) newStudentPassword.trim() else "Pakistan123!"

                            viewModel.addManagedStudent(
                                name = newStudentName,
                                username = assignedUsername,
                                email = assignedEmail,
                                password = assignedPassword,
                                phone = newStudentPhone,
                                plan = newStudentPlan,
                                isPaid = newStudentPaid
                            )
                            
                            val createdStudent = MainViewModel.AdminStudent(
                                name = newStudentName,
                                username = assignedUsername,
                                email = assignedEmail,
                                password = assignedPassword,
                                phone = newStudentPhone,
                                plan = newStudentPlan,
                                isPaid = newStudentPaid
                            )
                            studentCredentialsToShare = createdStudent

                            showAddDialog = false
                            newStudentName = ""
                            newStudentUsername = ""
                            newStudentEmail = ""
                            newStudentPassword = ""
                            newStudentPhone = ""
                        } else {
                            Toast.makeText(context, "Please enter student name and email/username", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryGold),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("SAVE & ACTIVATE", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Success Share Dialog after adding student
    studentCredentialsToShare?.let { student ->
        AlertDialog(
            onDismissRequest = { studentCredentialsToShare = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Student Registered Successfully!", fontWeight = FontWeight.Bold, color = PrimaryNavy, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Share these credentials with ${student.name} to log in to the academy portal:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("👤 Name: ${student.name}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("🔑 Username: ${student.username}", fontWeight = FontWeight.Bold, color = SecondaryGold, fontSize = 13.sp)
                            Text("🔒 Password: ${student.password}", fontWeight = FontWeight.Bold, color = SecondaryGold, fontSize = 13.sp)
                            Text("📧 Email: ${student.email}", fontSize = 12.sp)
                            Text("📦 Plan: ${student.plan}", fontSize = 12.sp)
                            Text("⚡ Status: ${if (student.isPaid) "Authorized (Active)" else "Pending"}", fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val message = """
                            🎓 *CSS Compass Academy - Student Login*
                            Dear ${student.name}, your account is active!
                            
                            👤 *Username:* ${student.username}
                            📧 *Email:* ${student.email}
                            🔑 *Password:* ${student.password}
                            📦 *Plan:* ${student.plan}
                            
                            Download the App & Login to start your preparation!
                        """.trimIndent()
                        clipboardManager.setText(AnnotatedString(message))
                        Toast.makeText(context, "Credentials copied! Ready to paste into WhatsApp.", Toast.LENGTH_SHORT).show()
                        studentCredentialsToShare = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryGold),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("COPY FOR WHATSAPP", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { studentCredentialsToShare = null }) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
fun LicenseGeneratorCard(viewModel: MainViewModel) {
    var generatedKey by remember { mutableStateOf("") }
    val keysList by viewModel.generatedKeysList.collectAsState()
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Student License Key Engine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PrimaryNavy)
                    Text("Generate direct activation keys (CSS-PREM-XXXX) to sell or issue to students.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    generatedKey = viewModel.generateNewActivationKey()
                    Toast.makeText(context, "New key created: $generatedKey", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("GENERATE NEW ACTIVATION KEY", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            if (generatedKey.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = SecondaryGold.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, SecondaryGold)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("JUST GENERATED (READY TO SEND):", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SecondaryGold)
                            Text(
                                text = generatedKey,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = PrimaryNavy,
                                fontSize = 16.sp
                            )
                        }
                        IconButton(
                            onClick = {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(generatedKey))
                                Toast.makeText(context, "Key copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = PrimaryNavy)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Recent Generated Keys Catalog:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                keysList.take(5).forEach { key ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = key,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = PrimaryNavy
                        )
                        IconButton(
                            onClick = {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(key))
                                Toast.makeText(context, "Copied: $key", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
