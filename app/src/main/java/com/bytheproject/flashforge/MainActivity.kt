// MainActivity.kt
package com.bytheproject.FlashForge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ============================================================================
// DATA CLASSES
// ============================================================================

data class NavItem(val label: String, val icon: ImageVector)

data class Stats(
    val cardsMastered: Int,
    val accuracy: Int,
    val sessions: Int,
    val level: Int
)

data class FlashCard(
    val question: String,
    val answer: String,
    val difficulty: String,
    val current: Int,
    val total: Int
)

data class FlashForgeUiState(
    val isNavRailCollapsed: Boolean = false,
    val isMobile: Boolean = false,
    val selectedNavIndex: Int = 0,
    val selectedActionIndex: Int = 1,
    val isDarkTheme: Boolean = false,
    val isAvatarMorphed: Boolean = false,
    val streakDays: Int = 7,
    val xpPoints: Int = 2450,
    val dailyProgress: Float = 0.68f,
    val stats: Stats = Stats(156, 89, 23, 12),
    val currentFlashCard: FlashCard = FlashCard(
        question = "What is the primary purpose of Kotlin coroutines and how do they differ from traditional threading?",
        answer = "Kotlin coroutines provide lightweight concurrency that allows you to write asynchronous code in a sequential manner. Unlike traditional threads, coroutines are much more efficient and can be suspended and resumed without blocking the underlying thread, making them ideal for handling thousands of concurrent operations.",
        difficulty = "Medium",
        current = 15,
        total = 45
    ),
    val isAnswerRevealed: Boolean = false,
    val selectedResponse: String? = null,
    val isWaveformActive: Boolean = false
)

// ============================================================================
// VIEWMODEL
// ============================================================================

class FlashForgeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FlashForgeUiState())
    val uiState: StateFlow<FlashForgeUiState> = _uiState.asStateFlow()

    fun onNavItemClick(index: Int) {
        _uiState.value = _uiState.value.copy(selectedNavIndex = index)
    }

    fun toggleNavRail() {
        _uiState.value = _uiState.value.copy(
            isNavRailCollapsed = !_uiState.value.isNavRailCollapsed
        )
    }

    fun toggleTheme() {
        _uiState.value = _uiState.value.copy(
            isDarkTheme = !_uiState.value.isDarkTheme
        )
    }

    fun onActionClick(index: Int) {
        _uiState.value = _uiState.value.copy(selectedActionIndex = index)
    }

    fun onAvatarClick() {
        _uiState.value = _uiState.value.copy(isAvatarMorphed = true)
        viewModelScope.launch {
            delay(800)
            _uiState.value = _uiState.value.copy(isAvatarMorphed = false)
        }
    }

    fun triggerWaveform() {
        _uiState.value = _uiState.value.copy(isWaveformActive = true)
        viewModelScope.launch {
            delay(1500)
            _uiState.value = _uiState.value.copy(isWaveformActive = false)
        }
    }

    fun revealAnswer() {
        _uiState.value = _uiState.value.copy(
            isAnswerRevealed = !_uiState.value.isAnswerRevealed,
            selectedResponse = null
        )
    }

    fun markResponse(response: String) {
        _uiState.value = _uiState.value.copy(selectedResponse = response)
        viewModelScope.launch {
            delay(800)
            nextCard()
        }
    }

    fun nextCard() {
        val currentCard = _uiState.value.currentFlashCard
        val newCurrent = if (currentCard.current < currentCard.total) {
            currentCard.current + 1
        } else {
            1
        }

        _uiState.value = _uiState.value.copy(
            currentFlashCard = currentCard.copy(current = newCurrent),
            isAnswerRevealed = false,
            selectedResponse = null
        )
    }

    fun previousCard() {
        val currentCard = _uiState.value.currentFlashCard
        val newCurrent = if (currentCard.current > 1) {
            currentCard.current - 1
        } else {
            currentCard.total
        }

        _uiState.value = _uiState.value.copy(
            currentFlashCard = currentCard.copy(current = newCurrent),
            isAnswerRevealed = false,
            selectedResponse = null
        )
    }

    fun createNewDeck() {
        // TODO: Implement deck creation
    }

    fun openMyDecks() {
        // TODO: Implement deck browsing
    }

    fun aiGenerate() {
        // TODO: Implement AI generation
    }
}

// ============================================================================
// MAIN ACTIVITY
// ============================================================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                FlashForgeApp()
            }
        }
    }
}

// ============================================================================
// MAIN APP COMPOSABLE
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashForgeApp(viewModel: FlashForgeViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Navigation Rail
            AnimatedVisibility(
                visible = !uiState.isNavRailCollapsed || !uiState.isMobile,
                enter = slideInHorizontally() + fadeIn(),
                exit = slideOutHorizontally() + fadeOut()
            ) {
                NavigationRailComponent(
                    isCollapsed = uiState.isNavRailCollapsed,
                    selectedIndex = uiState.selectedNavIndex,
                    isDarkTheme = uiState.isDarkTheme,
                    onNavItemClick = viewModel::onNavItemClick,
                    onToggleRail = viewModel::toggleNavRail,
                    onThemeToggle = viewModel::toggleTheme
                )
            }

            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                // Top Navigation Bar
                TopNavigationBar(
                    selectedActionIndex = uiState.selectedActionIndex,
                    onActionClick = viewModel::onActionClick
                )

                // User Profile Branding
                UserProfileSection(
                    userName = "Tristan Smith",
                    userTitle = "The Imposter Arisen",
                    questGoal = "Kotlin Savage",
                    onAvatarClick = viewModel::onAvatarClick,
                    isAvatarMorphed = uiState.isAvatarMorphed
                )

                // Main Content Area
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = PaddingValues(vertical = 24.dp)
                ) {
                    item {
                        ProgressSection(
                            streakDays = uiState.streakDays,
                            xpPoints = uiState.xpPoints,
                            dailyProgress = uiState.dailyProgress,
                            stats = uiState.stats,
                            onProgressClick = viewModel::triggerWaveform,
                            isWaveformActive = uiState.isWaveformActive
                        )
                    }

                    item {
                        QuickActionsSection(
                            onCreateDeck = viewModel::createNewDeck,
                            onOpenDecks = viewModel::openMyDecks,
                            onAiGenerate = viewModel::aiGenerate
                        )
                    }

                    item {
                        FlashCardSection(
                            flashCard = uiState.currentFlashCard,
                            isAnswerRevealed = uiState.isAnswerRevealed,
                            selectedResponse = uiState.selectedResponse,
                            onRevealAnswer = viewModel::revealAnswer,
                            onPreviousCard = viewModel::previousCard,
                            onNextCard = viewModel::nextCard,
                            onMarkResponse = viewModel::markResponse
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// NAVIGATION RAIL COMPONENTS
// ============================================================================

@Composable
fun NavigationRailComponent(
    isCollapsed: Boolean,
    selectedIndex: Int,
    isDarkTheme: Boolean,
    onNavItemClick: (Int) -> Unit,
    onToggleRail: () -> Unit,
    onThemeToggle: () -> Unit
) {
    val navItems = listOf(
        NavItem("Study", Icons.Filled.Home),
        NavItem("My Decks", Icons.Filled.LibraryBooks),
        NavItem("Create", Icons.Filled.AddCircle),
        NavItem("Browse", Icons.Filled.Search),
        NavItem("Analytics", Icons.Filled.Analytics),
        NavItem("Groups", Icons.Filled.Group),
        NavItem("AI Tutor", Icons.Filled.Psychology)
    )

    val railWidth by animateDpAsState(
        targetValue = if (isCollapsed) 80.dp else 280.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Surface(
        modifier = Modifier
            .width(railWidth)
            .fillMaxHeight()
            .zIndex(100f),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 12.dp,
        shape = RectangleShape
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with Flame Toggle
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FlameToggleButton(onClick = onToggleRail)
                        AnimatedVisibility(
                            visible = !isCollapsed,
                            enter = fadeIn() + slideInHorizontally(),
                            exit = fadeOut() + slideOutHorizontally()
                        ) {
                            Text(
                                text = "FlashForge",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Items
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                navItems.forEachIndexed { index, item ->
                    NavigationRailItem(
                        item = item,
                        isSelected = index == selectedIndex,
                        isCollapsed = isCollapsed,
                        onClick = { onNavItemClick(index) }
                    )
                }
            }

            // Theme Toggle
            ThemeToggleButton(
                isDarkTheme = isDarkTheme,
                isCollapsed = isCollapsed,
                onClick = onThemeToggle
            )
        }
    }
}

@Composable
fun FlameToggleButton(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()
    val flameRotation by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val hapticFeedback = LocalHapticFeedback.current

    FilledIconButton(
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = Modifier
            .size(48.dp)
            .graphicsLayer {
                rotationZ = flameRotation
                scaleX = flameScale
                scaleY = flameScale
            },
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(
            imageVector = Icons.Filled.Whatshot,
            contentDescription = "Toggle Navigation",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(24.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationRailItem(
    item: NavItem,
    isSelected: Boolean,
    isCollapsed: Boolean,
    onClick: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            Color.Transparent
        },
        shape = RoundedCornerShape(20.dp),
        shadowElevation = if (isSelected) 4.dp else 0.dp,
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(24.dp)
            )

            AnimatedVisibility(
                visible = !isCollapsed,
                enter = fadeIn() + slideInHorizontally(),
                exit = fadeOut() + slideOutHorizontally()
            ) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ThemeToggleButton(
    isDarkTheme: Boolean,
    isCollapsed: Boolean,
    onClick: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current

    Surface(
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = if (isDarkTheme) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                contentDescription = "Toggle Theme",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )

            AnimatedVisibility(
                visible = !isCollapsed,
                enter = fadeIn() + slideInHorizontally(),
                exit = fadeOut() + slideOutHorizontally()
            ) {
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ============================================================================
// TOP NAVIGATION BAR
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationBar(
    selectedActionIndex: Int,
    onActionClick: (Int) -> Unit
) {
    val topActions = listOf(
        Icons.Filled.Search,
        Icons.Filled.Notifications,
        Icons.Filled.Settings,
        Icons.Filled.AccountCircle
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            topActions.forEachIndexed { index, icon ->
                val hapticFeedback = LocalHapticFeedback.current

                IconButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onActionClick(index)
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (index == selectedActionIndex) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            Color.Transparent
                        }
                    )
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (index == selectedActionIndex) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

// ============================================================================
// USER PROFILE SECTION
// ============================================================================

@Composable
fun UserProfileSection(
    userName: String,
    userTitle: String,
    questGoal: String,
    onAvatarClick: () -> Unit,
    isAvatarMorphed: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Profile Container
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(28.dp),
                shadowElevation = 12.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Avatar
                    MorphingAvatar(
                        initials = "TS",
                        isMorphed = isAvatarMorphed,
                        onClick = onAvatarClick
                    )

                    // User Info
                    Column(
                        modifier = Modifier.weight(1f).padding(start = 20.dp)
                    ) {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = userTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.alpha(0.9f)
                        )
                    }

                    // Quest Section
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "IN THE FORGE:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.alpha(0.8f)
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.tertiary,
                            shape = RoundedCornerShape(20.dp),
                            shadowElevation = 8.dp
                        ) {
                            Text(
                                text = questGoal,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onTertiary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(12.dp, 8.dp)
                            )
                        }
                    }
                }
            }

            // Current Study Session
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp, 12.dp)
                ) {
                    Text(
                        text = "Current Study Session",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Kotlin • Advanced Concepts • Coroutines",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.alpha(0.9f)
                    )
                }
            }
        }
    }
}

@Composable
fun MorphingAvatar(
    initials: String,
    isMorphed: Boolean,
    onClick: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        targetValue = if (isMorphed) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    val rotation by animateFloatAsState(
        targetValue = if (isMorphed) 40f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    Surface(
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = Modifier
            .size(64.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = rotation
            },
        color = MaterialTheme.colorScheme.primary,
        shape = if (isMorphed) {
            // 9-sided polygon approximation using RoundedCornerShape
            RoundedCornerShape(8.dp)
        } else {
            CircleShape
        },
        shadowElevation = 8.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ============================================================================
// PROGRESS SECTION
// ============================================================================

@Composable
fun ProgressSection(
    streakDays: Int,
    xpPoints: Int,
    dailyProgress: Float,
    stats: Stats,
    onProgressClick: () -> Unit,
    isWaveformActive: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(32.dp),
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp, 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Whatshot,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "$streakDays Day Streak",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(28.dp),
                    shadowElevation = 12.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp, 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "$xpPoints XP",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Progress Bar
            WaveformProgressBar(
                progress = dailyProgress,
                isWaveformActive = isWaveformActive,
                onClick = onProgressClick
            )

            // Stats Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    StatCard("${stats.cardsMastered}", "Cards Mastered")
                }
                item {
                    StatCard("${stats.accuracy}%", "Accuracy")
                }
                item {
                    StatCard("${stats.sessions}", "Sessions")
                }
                item {
                    StatCard("Level ${stats.level}", "Current")
                }
            }
        }
    }
}

@Composable
fun WaveformProgressBar(
    progress: Float,
    isWaveformActive: Boolean,
    onClick: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val waveScale by animateFloatAsState(
        targetValue = if (isWaveformActive) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    val waveOffset by animateFloatAsState(
        targetValue = if (isWaveformActive) 5f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Daily Goal Progress: ${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Medium
        )

        Surface(
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .graphicsLayer {
                    scaleY = waveScale
                    translationX = waveOffset
                },
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 4.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight(),
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                ) {}
            }
        }
    }
}

@Composable
fun StatCard(value: String, label: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ============================================================================
// QUICK ACTIONS SECTION
// ============================================================================

@Composable
fun QuickActionsSection(
    onCreateDeck: () -> Unit,
    onOpenDecks: () -> Unit,
    onAiGenerate: () -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 280.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            QuickActionCard(
                title = "Create New Deck",
                subtitle = "Build custom Kotlin flash cards",
                icon = Icons.Filled.AddBox,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = onCreateDeck
            )
        }
        item {
            QuickActionCard(
                title = "My Decks",
                subtitle = "12 decks available",
                icon = Icons.Filled.FolderOpen,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = onOpenDecks
            )
        }
        item {
            QuickActionCard(
                title = "AI Generate",
                subtitle = "Create Kotlin cards with AI",
                icon = Icons.Filled.AutoAwesome,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = onAiGenerate
            )
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current

    Surface(
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                color = containerColor,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 8.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ============================================================================
// FLASH CARD SECTION
// ============================================================================

@Composable
fun FlashCardSection(
    flashCard: FlashCard,
    isAnswerRevealed: Boolean,
    selectedResponse: String?,
    onRevealAnswer: () -> Unit,
    onPreviousCard: () -> Unit,
    onNextCard: () -> Unit,
    onMarkResponse: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(32.dp),
        shadowElevation = 16.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Difficulty Badge
                Surface(
                    color = when (flashCard.difficulty.lowercase()) {
                        "easy" -> Color(0xFF4CAF50)
                        "medium" -> Color(0xFFFF9800)
                        "hard" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
                    },
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = 8.dp
                ) {
                    Text(
                        text = flashCard.difficulty.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp, 6.dp)
                    )
                }

                // Card Counter
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 8.dp
                ) {
                    Text(
                        text = "${flashCard.current} / ${flashCard.total}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(12.dp, 8.dp)
                    )
                }
            }

            // Question
            Text(
                text = flashCard.question,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 1.4.em
            )

            // Answer (Animated)
            AnimatedVisibility(
                visible = isAnswerRevealed,
                enter = fadeIn() + slideInVertically { it / 2 } + scaleIn(),
                exit = fadeOut() + slideOutVertically { it / 2 } + scaleOut()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 8.dp
                ) {
                    Text(
                        text = flashCard.answer,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(28.dp),
                        lineHeight = 1.5.em
                    )
                }
            }

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingActionButton(
                    onClick = onPreviousCard,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = "Previous Card",
                        modifier = Modifier.size(28.dp)
                    )
                }

                FloatingActionButton(
                    onClick = onRevealAnswer,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        imageVector = if (isAnswerRevealed) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (isAnswerRevealed) "Hide Answer" else "Reveal Answer",
                        modifier = Modifier.size(32.dp)
                    )
                }

                FloatingActionButton(
                    onClick = onNextCard,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Next Card",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Response Buttons
            AnimatedVisibility(
                visible = isAnswerRevealed,
                enter = fadeIn() + slideInVertically() + scaleIn(),
                exit = fadeOut() + slideOutVertically() + scaleOut()
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        ResponseButton(
                            text = "Incorrect",
                            icon = Icons.Filled.ThumbDown,
                            responseType = "incorrect",
                            isSelected = selectedResponse == "incorrect",
                            onClick = { onMarkResponse("incorrect") }
                        )
                    }
                    item {
                        ResponseButton(
                            text = "Review",
                            icon = Icons.Filled.Refresh,
                            responseType = "needs-review",
                            isSelected = selectedResponse == "needs-review",
                            onClick = { onMarkResponse("needs-review") }
                        )
                    }
                    item {
                        ResponseButton(
                            text = "Correct",
                            icon = Icons.Filled.ThumbUp,
                            responseType = "correct",
                            isSelected = selectedResponse == "correct",
                            onClick = { onMarkResponse("correct") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ResponseButton(
    text: String,
    icon: ImageVector,
    responseType: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    val backgroundColor = when {
        isSelected && responseType == "correct" -> Color(0xFF4CAF50)
        isSelected && responseType == "incorrect" -> MaterialTheme.colorScheme.error
        isSelected && responseType == "needs-review" -> Color(0xFFFF9800)
        else -> Color.Transparent
    }

    val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
    val borderColor = if (isSelected) backgroundColor else MaterialTheme.colorScheme.outline

    Surface(
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = Modifier.fillMaxWidth(),
        color = backgroundColor,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(2.dp, borderColor),
        shadowElevation = if (isSelected) 8.dp else 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
} 