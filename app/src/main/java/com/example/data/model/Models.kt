package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class UserProfile(
    @DocumentId val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val lastLoginAt: Timestamp? = null
)

enum class CodingPersona(
    val title: String,
    val icon: String,
    val description: String,
    val systemPromptAddition: String
) {
    MASTER_KING(
        title = "Master King",
        icon = "👑",
        description = "Supreme engineering architect. Deep reasoning, optimal patterns, clean code.",
        systemPromptAddition = "Act as the Dark Claude Coding Master King: provide masterful software engineering solutions, pristine code, defensive architectural design, and clear performance rationale."
    ),
    ARCHITECT(
        title = "Architect",
        icon = "🏗️",
        description = "System designer & structural lead. Scalability, data flow, modular boundaries.",
        systemPromptAddition = "Act as a Principal Systems Architect: emphasize clean architecture, high scalability, boundary separation, DRY principles, and system blueprints."
    ),
    BUG_HUNTER(
        title = "Bug Hunter",
        icon = "🐛",
        description = "Deep root-cause diagnostics, concurrency race detection, and edge cases.",
        systemPromptAddition = "Act as an elite Bug Hunter: aggressively diagnose defects, memory leaks, race conditions, edge cases, and provide definitive bulletproof patches."
    ),
    OPTIMIZER(
        title = "Optimizer",
        icon = "⚡",
        description = "Algorithmic speed, memory footprint reduction, and runtime profiling.",
        systemPromptAddition = "Act as an extreme Performance Optimizer: focus on asymptotic complexity (O(N)), caching, memory reduction, lazy allocations, and zero-overhead execution."
    )
}

data class ChatSession(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "New Coding Session",
    val modelName: String = "Gemini 3.8 Flash (Claude Persona)",
    val persona: String = CodingPersona.MASTER_KING.name,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
    val messageCount: Int = 0
)

data class ChatMessage(
    @DocumentId val id: String = "",
    val sessionId: String = "",
    val sender: String = "user", // "user" or "model"
    val content: String = "",
    @ServerTimestamp val timestamp: Timestamp? = null
)

data class CodeSnippet(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val language: String = "kotlin",
    val code: String = "",
    @ServerTimestamp val timestamp: Timestamp? = null
)
