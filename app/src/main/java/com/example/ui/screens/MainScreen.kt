package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.UserProfileDialog
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberError
import com.example.ui.theme.CyberOnSurface
import com.example.ui.theme.CyberOnSurfaceMuted
import com.example.ui.theme.CyberOutline
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberPrimaryBright
import com.example.ui.theme.CyberSecondary
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceContainer
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTertiary
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    user: FirebaseUser,
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var currentTab by remember { mutableIntStateOf(0) } // 0: Chat, 1: Sandbox, 2: Snippets
    var showProfileDialog by remember { mutableStateOf(false) }

    val sessions by mainViewModel.sessions.collectAsState()
    val currentSessionId by mainViewModel.currentSessionId.collectAsState()
    val activePersona by mainViewModel.activePersona.collectAsState()
    val snippets by mainViewModel.snippets.collectAsState()

    val currentSession = sessions.find { it.id == currentSessionId }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = CyberSurface,
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Drawer Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        Text(text = "👑", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "DARK CLAUDE",
                                color = CyberOnSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "CODING MASTER KING",
                                color = CyberSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // New Session Button
                    OutlinedButton(
                        onClick = {
                            mainViewModel.createNewSession("Coding Session ${sessions.size + 1}")
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_session_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            contentColor = CyberPrimaryBright
                        ),
                        border = androidx.compose.material3.ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(CyberPrimary)
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "New Coding Session", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "SAVED SESSIONS (${sessions.size})",
                        color = CyberOnSurfaceMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Sessions List
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(sessions, key = { it.id }) { session ->
                            val isSelected = session.id == currentSessionId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) CyberPrimary.copy(alpha = 0.2f) else CyberSurfaceVariant)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) CyberPrimary else CyberOutline,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        mainViewModel.selectSession(session.id)
                                        coroutineScope.launch { drawerState.close() }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = session.title,
                                        color = if (isSelected) CyberSecondary else CyberOnSurface,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${session.messageCount} messages",
                                        color = CyberOnSurfaceMuted,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                IconButton(
                                    onClick = { mainViewModel.deleteSession(session.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Session",
                                        tint = CyberError.copy(alpha = 0.7f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Bottom User Tile in Drawer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberSurfaceContainer)
                            .border(1.dp, CyberOutline, RoundedCornerShape(8.dp))
                            .clickable {
                                showProfileDialog = true
                                coroutineScope.launch { drawerState.close() }
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val initial = user.displayName?.firstOrNull()?.toString() ?: "D"
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(CyberPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = initial, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user.displayName ?: "Dark Coder",
                                color = CyberOnSurface,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Account & Sync Settings",
                                color = CyberSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = when (currentTab) {
                                    0 -> currentSession?.title ?: "Dark Claude Coding"
                                    1 -> "Code Sandbox & Runner"
                                    else -> "Encrypted Snippets Vault"
                                },
                                color = CyberOnSurface,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${activePersona.icon} ${activePersona.title}",
                                    color = CyberSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• Gemini 3.8",
                                    color = CyberTertiary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { coroutineScope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("drawer_toggle_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Drawer",
                                tint = CyberSecondary
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showProfileDialog = true },
                            modifier = Modifier.testTag("profile_button")
                        ) {
                            val initial = user.displayName?.firstOrNull()?.toString() ?: "D"
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(CyberPrimary, CircleShape)
                                    .border(1.dp, CyberSecondary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initial,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CyberSurface,
                        titleContentColor = CyberOnSurface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = CyberSurface,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "Chat Console"
                            )
                        },
                        label = {
                            Text(
                                text = "Console",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberSecondary,
                            selectedTextColor = CyberSecondary,
                            indicatorColor = CyberPrimary.copy(alpha = 0.25f),
                            unselectedIconColor = CyberOnSurfaceMuted,
                            unselectedTextColor = CyberOnSurfaceMuted
                        ),
                        modifier = Modifier.testTag("nav_chat_console")
                    )

                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = "Code Sandbox"
                            )
                        },
                        label = {
                            Text(
                                text = "Sandbox",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberSecondary,
                            selectedTextColor = CyberSecondary,
                            indicatorColor = CyberPrimary.copy(alpha = 0.25f),
                            unselectedIconColor = CyberOnSurfaceMuted,
                            unselectedTextColor = CyberOnSurfaceMuted
                        ),
                        modifier = Modifier.testTag("nav_code_sandbox")
                    )

                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = { currentTab = 2 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "Snippets Vault"
                            )
                        },
                        label = {
                            Text(
                                text = "Vault",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberSecondary,
                            selectedTextColor = CyberSecondary,
                            indicatorColor = CyberPrimary.copy(alpha = 0.25f),
                            unselectedIconColor = CyberOnSurfaceMuted,
                            unselectedTextColor = CyberOnSurfaceMuted
                        ),
                        modifier = Modifier.testTag("nav_snippets_vault")
                    )
                }
            },
            containerColor = CyberBackground
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentTab) {
                    0 -> ChatScreen(
                        mainViewModel = mainViewModel,
                        onOpenInSandbox = { code, lang ->
                            mainViewModel.sendToSandbox(code, lang)
                            currentTab = 1
                        }
                    )
                    1 -> CodeSandboxScreen(
                        mainViewModel = mainViewModel
                    )
                    2 -> SnippetsVaultScreen(
                        mainViewModel = mainViewModel,
                        onOpenInSandbox = { code, lang ->
                            mainViewModel.sendToSandbox(code, lang)
                            currentTab = 1
                        }
                    )
                }
            }
        }
    }

    if (showProfileDialog) {
        UserProfileDialog(
            user = user,
            sessionCount = sessions.size,
            snippetCount = snippets.size,
            onDismiss = { showProfileDialog = false },
            onSignOut = {
                showProfileDialog = false
                authViewModel.signOut()
            }
        )
    }
}
