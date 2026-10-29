package com.example.data.repository

import android.util.Log
import com.example.data.model.ChatMessage
import com.example.data.model.CodingPersona
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

private const val TAG = "AiCodingRepository"

class AiCodingRepository {

    private fun buildSystemPrompt(persona: CodingPersona): String {
        return """
            You are Dark Claude AI, the Dark Coding Master King. You are a legendary software engineering titan, elite systems architect, and principal hacker.
            You possess unmatched mastery in software architecture, algorithms, mobile and web engineering, low-level optimization, and clean code principles.
            
            Key Directives:
            1. Uncompromising Quality: Always generate production-grade, bug-free, robust, self-contained, and modern code.
            2. Dark Cyber Tone: Speak with confidence, precision, razor-sharp technical insight, and elite engineering authority.
            3. Code Formatting: Wrap all code in markdown code fences with explicit language tags (e.g. ```kotlin, ```python, ```javascript, ```sql, ```rust, ```cpp, ```bash).
            4. Explanations: Provide crisp architectural rationale, time/space complexity analysis (Big-O), and call out potential edge cases.
            5. Role Focus: ${persona.systemPromptAddition}
        """.trimIndent()
    }

    private fun getGenerativeModel(persona: CodingPersona) = Firebase.ai.generativeModel(
        modelName = "gemini-3.8-flash",
        systemInstruction = content {
            text(buildSystemPrompt(persona))
        }
    )

    fun streamCodingResponse(
        prompt: String,
        persona: CodingPersona = CodingPersona.MASTER_KING,
        history: List<ChatMessage> = emptyList()
    ): Flow<String> = flow {
        val model = getGenerativeModel(persona)

        // Build conversation history contents
        val conversationHistory = history.takeLast(10).map { msg ->
            if (msg.sender == "user") {
                content("user") { text(msg.content) }
            } else {
                content("model") { text(msg.content) }
            }
        }

        try {
            withTimeout(60_000L) {
                if (conversationHistory.isNotEmpty()) {
                    val chat = model.startChat(conversationHistory)
                    chat.sendMessageStream(prompt).collect { chunk ->
                        val text = chunk.text.orEmpty()
                        if (text.isNotEmpty()) {
                            emit(text)
                        }
                    }
                } else {
                    model.generateContentStream(prompt).collect { chunk ->
                        val text = chunk.text.orEmpty()
                        if (text.isNotEmpty()) {
                            emit(text)
                        }
                    }
                }
            }
        } catch (e: CancellationException) {
            // Rethrow cooperative cancellation
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Streaming generation failed", e)
            throw e
        }
    }
}
