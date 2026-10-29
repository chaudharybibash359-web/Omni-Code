package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.CodingPersona
import com.example.ui.components.CodeBlockCard
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
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ChatScreen(
    mainViewModel: MainViewModel,
    onOpenInSandbox: (code: String, language: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val messages by mainViewModel.messages.collectAsState()
    val isStreaming by mainViewModel.isStreaming.collectAsState()
    val streamingText by mainViewModel.streamingText.collectAsState()
    val error by mainViewModel.error.collectAsState()
    val activePersona by mainViewModel.activePersona.collectAsState()

    var inputPrompt by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto scroll to bottom when messages change or streaming updates
    LaunchedEffect(messages.size, streamingText) {
        if (messages.isNotEmpty() || streamingText.isNotEmpty()) {
            listState.animateScrollToItem(index = (messages.size + if (isStreaming) 1 else 0))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
    ) {
        // Persona Selector Chips
        PersonaSelectorRow(
            selectedPersona = activePersona,
            onSelectPersona = { mainViewModel.setPersona(it) }
        )

        // Quick Starter Prompt Chips
        QuickPromptChipsRow(
            onSelectStarter = { starter ->
                inputPrompt = starter
                mainViewModel.sendPrompt(starter)
                inputPrompt = ""
            }
        )

        // Error message banner
        if (error != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .background(CyberError.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .border(1.dp, CyberError.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Error",
                    tint = CyberError,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = error ?: "",
                    color = CyberError,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { mainViewModel.clearError() },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Dismiss",
                        tint = CyberError,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Messages List or Empty State
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty() && !isStreaming) {
                EmptyChatState(
                    persona = activePersona,
                    onStarterClick = { starter ->
                        mainViewModel.sendPrompt(starter)
                    }
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { message ->
                        MessageBubble(
                            message = message,
                            onOpenInSandbox = onOpenInSandbox
                        )
                    }

                    if (isStreaming) {
                        item {
                            StreamingMessageBubble(
                                persona = activePersona,
                                streamingText = streamingText,
                                onOpenInSandbox = onOpenInSandbox
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(12.dp)) }
                }
            }
        }

        // Input Bar
        ChatInputBar(
            value = inputPrompt,
            onValueChange = { inputPrompt = it },
            onSend = {
                if (inputPrompt.isNotBlank()) {
                    mainViewModel.sendPrompt(inputPrompt)
                    inputPrompt = ""
                }
            },
            isStreaming = isStreaming,
            onStop = { mainViewModel.stopStreaming() }
        )
    }
}

@Composable
private fun PersonaSelectorRow(
    selectedPersona: CodingPersona,
    onSelectPersona: (CodingPersona) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CodingPersona.values().forEach { persona ->
            val isSelected = persona == selectedPersona
            val borderColor = if (isSelected) CyberPrimary else CyberOutline
            val bg = if (isSelected) CyberPrimary.copy(alpha = 0.2f) else CyberSurface

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bg)
                    .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                    .clickable { onSelectPersona(persona) }
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = persona.icon, fontSize = 13.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = persona.title,
                    color = if (isSelected) CyberSecondary else CyberOnSurfaceMuted,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun QuickPromptChipsRow(
    onSelectStarter: (String) -> Unit
) {
    val starters = listOf(
        "⚡ Debug NPE in Kotlin",
        "🏗️ Clean Architecture REST API",
        "⚡ Coroutine Flow vs StateFlow",
        "🧪 Write Unit Tests with Mock",
        "🚀 Jetpack Compose Optimization",
        "🔒 SQL Injection Defense Query"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        starters.forEach { starter ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberSurfaceVariant)
                    .border(1.dp, CyberOutline, RoundedCornerShape(12.dp))
                    .clickable { onSelectStarter(starter) }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = starter,
                    color = CyberOnSurfaceMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun EmptyChatState(
    persona: CodingPersona,
    onStarterClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(CyberPrimary.copy(alpha = 0.15f), CircleShape)
                .border(1.dp, CyberPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = persona.icon, fontSize = 32.sp)
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "DARK CLAUDE: ${persona.title.uppercase()}",
            color = CyberSecondary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = persona.description,
            color = CyberOnSurfaceMuted,
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Tap a prompt to initiate code generation:",
            color = CyberOnSurfaceMuted.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(10.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            val templates = listOf(
                "Write an ultra-fast LRU Cache in Kotlin with O(1) ops",
                "Explain how to structure Android M3 MVVM architecture",
                "Create a Python script for async WebSockets with retry"
            )
            templates.forEach { t ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberSurface)
                        .border(1.dp, CyberOutline, RoundedCornerShape(8.dp))
                        .clickable { onStarterClick(t) }
                        .padding(10.dp)
                ) {
                    Text(
                        text = "▶ $t",
                        color = CyberOnSurface,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    onOpenInSandbox: (code: String, language: String) -> Unit
) {
    val isUser = message.sender == "user"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Sender Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 2.dp, start = 4.dp, end = 4.dp)
        ) {
            Text(
                text = if (isUser) "DEVELOPER" else "⚡ DARK CLAUDE",
                color = if (isUser) CyberSecondary else CyberPrimaryBright,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        if (isUser) {
            Surface(
                color = CyberSurfaceVariant,
                shape = RoundedCornerShape(14.dp, 14.dp, 2.dp, 14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberPrimary.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Text(
                    text = message.content,
                    color = CyberOnSurface,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        } else {
            // Model Response: Parse Markdown segments
            val segments = remember(message.content) { parseMarkdownSegments(message.content) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberSurfaceContainer, RoundedCornerShape(12.dp))
                    .border(1.dp, CyberOutline, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                segments.forEach { segment ->
                    if (segment.isCode) {
                        CodeBlockCard(
                            code = segment.content,
                            language = segment.language,
                            onOpenInSandbox = onOpenInSandbox
                        )
                    } else {
                        if (segment.content.isNotBlank()) {
                            Text(
                                text = segment.content.trim(),
                                color = CyberOnSurface,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StreamingMessageBubble(
    persona: CodingPersona,
    streamingText: String,
    onOpenInSandbox: (code: String, language: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(10.dp),
                color = CyberTertiary,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "⚡ DARK CLAUDE (${persona.title.uppercase()}) STREAMING...",
                color = CyberTertiary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        val segments = remember(streamingText) { parseMarkdownSegments(streamingText) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurfaceContainer, RoundedCornerShape(12.dp))
                .border(1.dp, CyberPrimary.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            segments.forEach { segment ->
                if (segment.isCode) {
                    CodeBlockCard(
                        code = segment.content,
                        language = segment.language,
                        onOpenInSandbox = onOpenInSandbox
                    )
                } else {
                    if (segment.content.isNotBlank()) {
                        Text(
                            text = segment.content.trim(),
                            color = CyberOnSurface,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }

            // Blinking Cyber Cursor
            Text(
                text = "▋",
                color = CyberSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isStreaming: Boolean,
    onStop: () -> Unit
) {
    Surface(
        color = CyberSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberOutline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(
                        text = "Enter code query or prompt...",
                        color = CyberOnSurfaceMuted,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("prompt_input_field"),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberPrimary,
                    unfocusedBorderColor = CyberOutline,
                    focusedTextColor = CyberOnSurface,
                    unfocusedTextColor = CyberOnSurface,
                    focusedContainerColor = CyberSurfaceVariant,
                    unfocusedContainerColor = CyberSurfaceVariant
                ),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() })
            )

            Spacer(modifier = Modifier.width(6.dp))

            if (isStreaming) {
                IconButton(
                    onClick = onStop,
                    modifier = Modifier
                        .size(44.dp)
                        .background(CyberError, CircleShape)
                        .testTag("stop_streaming_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop Generation",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = onSend,
                    enabled = value.isNotBlank(),
                    modifier = Modifier
                        .size(44.dp)
                        .background(if (value.isNotBlank()) CyberPrimary else CyberSurfaceVariant, CircleShape)
                        .testTag("send_prompt_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Prompt",
                        tint = if (value.isNotBlank()) Color.White else CyberOnSurfaceMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

data class MessageSegment(
    val isCode: Boolean,
    val content: String,
    val language: String = ""
)

fun parseMarkdownSegments(rawText: String): List<MessageSegment> {
    val segments = mutableListOf<MessageSegment>()
    val fenceRegex = "```([a-zA-Z0-9_-]*)\\s*\\n([\\s\\S]*?)(```|\$)".toRegex()

    var lastIndex = 0
    fenceRegex.findAll(rawText).forEach { match ->
        val range = match.range
        if (range.first > lastIndex) {
            val textBefore = rawText.substring(lastIndex, range.first)
            if (textBefore.isNotBlank()) {
                segments.add(MessageSegment(isCode = false, content = textBefore))
            }
        }

        val lang = match.groupValues[1].ifBlank { "code" }
        val codeContent = match.groupValues[2]
        segments.add(MessageSegment(isCode = true, content = codeContent, language = lang))

        lastIndex = range.last + 1
    }

    if (lastIndex < rawText.length) {
        val remaining = rawText.substring(lastIndex)
        if (remaining.isNotBlank()) {
            segments.add(MessageSegment(isCode = false, content = remaining))
        }
    }

    if (segments.isEmpty() && rawText.isNotEmpty()) {
        segments.add(MessageSegment(isCode = false, content = rawText))
    }

    return segments
}
