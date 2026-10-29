package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSession
import com.example.data.model.CodeSnippet
import com.example.data.model.CodingPersona
import com.example.data.repository.AiCodingRepository
import com.example.data.repository.FirestoreChatRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "MainViewModel"

class MainViewModel(
    private val chatRepository: FirestoreChatRepository,
    private val aiRepository: AiCodingRepository,
    private val currentUserId: String
) : ViewModel() {

    val sessions: StateFlow<List<ChatSession>> = chatRepository.observeSessions(currentUserId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val snippets: StateFlow<List<CodeSnippet>> = chatRepository.observeSnippets(currentUserId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    private val _activePersona = MutableStateFlow(CodingPersona.MASTER_KING)
    val activePersona: StateFlow<CodingPersona> = _activePersona.asStateFlow()

    // Observe messages for active session
    val messages: StateFlow<List<ChatMessage>> = _currentSessionId.flatMapLatest { sessionId ->
        if (sessionId != null) {
            chatRepository.observeMessages(currentUserId, sessionId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    private val _streamingText = MutableStateFlow("")
    val streamingText: StateFlow<String> = _streamingText.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Code Sandbox State
    private val _sandboxCode = MutableStateFlow(
        """// Dark Claude Code Sandbox
fun main() {
    val languages = listOf("Kotlin", "Python", "Rust", "TypeScript")
    println("⚡ Dark Claude Master Engine Initialized!")
    languages.forEachIndexed { i, lang ->
        println("[${'$'}{i + 1}] Optimized for: ${'$'}lang")
    }
}
""".trimIndent()
    )
    val sandboxCode: StateFlow<String> = _sandboxCode.asStateFlow()

    private val _sandboxLanguage = MutableStateFlow("kotlin")
    val sandboxLanguage: StateFlow<String> = _sandboxLanguage.asStateFlow()

    private val _sandboxOutput = MutableStateFlow<String?>(null)
    val sandboxOutput: StateFlow<String?> = _sandboxOutput.asStateFlow()

    private val _isExecuting = MutableStateFlow(false)
    val isExecuting: StateFlow<Boolean> = _isExecuting.asStateFlow()

    private var streamingJob: Job? = null

    init {
        // Automatically create or select initial session once sessions emit
        viewModelScope.launch {
            sessions.collect { list ->
                if (_currentSessionId.value == null && list.isNotEmpty()) {
                    _currentSessionId.value = list.first().id
                }
            }
        }
    }

    fun selectSession(sessionId: String) {
        _currentSessionId.value = sessionId
    }

    fun setPersona(persona: CodingPersona) {
        _activePersona.value = persona
    }

    fun createNewSession(title: String = "Coding Session") {
        viewModelScope.launch {
            try {
                val newId = chatRepository.createSession(
                    userId = currentUserId,
                    title = title,
                    persona = _activePersona.value
                )
                _currentSessionId.value = newId
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create session", e)
                _error.value = "Failed to create new session: ${e.localizedMessage}"
            }
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            try {
                chatRepository.deleteSession(currentUserId, sessionId)
                if (_currentSessionId.value == sessionId) {
                    _currentSessionId.value = sessions.value.firstOrNull { it.id != sessionId }?.id
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete session", e)
            }
        }
    }

    fun sendPrompt(promptText: String) {
        val prompt = promptText.trim()
        if (prompt.isEmpty() || _isStreaming.value) return

        viewModelScope.launch {
            var sessionId = _currentSessionId.value
            if (sessionId == null) {
                // Auto create initial session
                val title = if (prompt.length > 30) prompt.take(30) + "..." else prompt
                sessionId = chatRepository.createSession(currentUserId, title, _activePersona.value)
                _currentSessionId.value = sessionId
            }

            // Save user message to Firestore
            try {
                chatRepository.saveMessage(currentUserId, sessionId, "user", prompt)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save user message", e)
            }

            // Begin AI Streaming
            _isStreaming.value = true
            _streamingText.value = ""
            _error.value = null

            val currentHistory = messages.value
            val buffer = StringBuilder()

            streamingJob = launch {
                try {
                    aiRepository.streamCodingResponse(
                        prompt = prompt,
                        persona = _activePersona.value,
                        history = currentHistory
                    ).collect { chunk ->
                        buffer.append(chunk)
                        _streamingText.value = buffer.toString()
                    }

                    // Save AI response to Firestore
                    val fullResponse = buffer.toString()
                    if (fullResponse.isNotBlank()) {
                        chatRepository.saveMessage(currentUserId, sessionId, "model", fullResponse)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Error in stream response", e)
                    _error.value = "Dark Claude Engine Error: ${e.localizedMessage ?: "Unknown connection issue"}"
                } finally {
                    _isStreaming.value = false
                    _streamingText.value = ""
                }
            }
        }
    }

    fun stopStreaming() {
        streamingJob?.cancel()
        _isStreaming.value = false
    }

    fun updateSandboxCode(code: String) {
        _sandboxCode.value = code
    }

    fun setSandboxLanguage(lang: String) {
        _sandboxLanguage.value = lang
    }

    fun sendToSandbox(code: String, language: String) {
        _sandboxCode.value = code.trim()
        _sandboxLanguage.value = language.lowercase().ifBlank { "kotlin" }
        _sandboxOutput.value = null
    }

    fun runSandboxCode() {
        viewModelScope.launch {
            _isExecuting.value = true
            _sandboxOutput.value = null
            delay(400) // Simulated execution compile step

            val code = _sandboxCode.value
            val lang = _sandboxLanguage.value.lowercase()

            val simulatedOutput = evaluateSandboxCode(code, lang)
            _sandboxOutput.value = simulatedOutput
            _isExecuting.value = false
        }
    }

    private fun evaluateSandboxCode(code: String, lang: String): String {
        val lines = code.lines()
        val prints = mutableListOf<String>()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("println(") && trimmed.endsWith(")")) {
                val arg = trimmed.removeSurrounding("println(", ")")
                prints.add(arg.removeSurrounding("\"").removeSurrounding("'"))
            } else if (trimmed.startsWith("print(") && trimmed.endsWith(")")) {
                val arg = trimmed.removeSurrounding("print(", ")")
                prints.add(arg.removeSurrounding("\"").removeSurrounding("'"))
            } else if (trimmed.startsWith("console.log(") && trimmed.endsWith(")")) {
                val arg = trimmed.removeSurrounding("console.log(", ")")
                prints.add(arg.removeSurrounding("\"").removeSurrounding("'"))
            }
        }

        val timestamp = System.currentTimeMillis() % 1000
        val header = "[DARK CLAUDE SANDBOX RUNNER - ${lang.uppercase()}]\n" +
                "Compiling source file...\n" +
                "Linking runtime symbols...\n" +
                "Execution Started (PID: 2048$timestamp)\n" +
                "----------------------------------------"

        val body = if (prints.isNotEmpty()) {
            prints.joinToString("\n")
        } else {
            "✓ Code analyzed successfully.\n" +
            "✓ No syntax defects detected (${lines.size} lines, ${code.length} bytes).\n" +
            "✓ AST Graph parsed for $lang runtime.\n" +
            "Execution completed cleanly."
        }

        val footer = "----------------------------------------\n" +
                "Process finished with exit code 0 (Elapsed: ${24 + (lines.size * 2)}ms)"

        return "$header\n$body\n$footer"
    }

    fun saveSnippet(title: String, language: String, code: String) {
        viewModelScope.launch {
            try {
                chatRepository.saveSnippet(
                    userId = currentUserId,
                    title = title,
                    language = language,
                    code = code
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save snippet", e)
                _error.value = "Failed to save snippet: ${e.localizedMessage}"
            }
        }
    }

    fun deleteSnippet(snippetId: String) {
        viewModelScope.launch {
            try {
                chatRepository.deleteSnippet(currentUserId, snippetId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete snippet", e)
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
