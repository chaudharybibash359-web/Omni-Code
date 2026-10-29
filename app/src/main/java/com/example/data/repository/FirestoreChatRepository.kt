package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSession
import com.example.data.model.CodeSnippet
import com.example.data.model.CodingPersona
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreChatRepo"

class FirestoreChatRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    fun observeSessions(userId: String): Flow<List<ChatSession>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val sessionsRef = db.collection("users")
            .document(userId)
            .collection("sessions")
            .orderBy("updatedAt", Query.Direction.DESCENDING)

        val listener = sessionsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error observing sessions", error)
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val sessions = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ChatSession::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        ?.copy(id = doc.id)
                }
                trySend(sessions)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun createSession(userId: String, title: String, persona: CodingPersona): String {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val sessionsRef = db.collection("users").document(userId).collection("sessions")
        val docRef = sessionsRef.document()
        val data = mapOf(
            "id" to docRef.id,
            "userId" to userId,
            "title" to title,
            "modelName" to "Gemini 3.8 Flash (Claude Persona)",
            "persona" to persona.name,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
            "messageCount" to 0
        )
        docRef.set(data).await()
        return docRef.id
    }

    suspend fun updateSessionTitle(userId: String, sessionId: String, newTitle: String) {
        db.collection("users").document(userId)
            .collection("sessions").document(sessionId)
            .update(
                mapOf(
                    "title" to newTitle,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
    }

    suspend fun deleteSession(userId: String, sessionId: String) {
        val sessionRef = db.collection("users").document(userId)
            .collection("sessions").document(sessionId)

        // Delete subcollection messages first
        val messages = sessionRef.collection("messages").get().await()
        for (doc in messages.documents) {
            doc.reference.delete().await()
        }
        sessionRef.delete().await()
    }

    fun observeMessages(userId: String, sessionId: String): Flow<List<ChatMessage>> = callbackFlow {
        if (userId.isBlank() || sessionId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val messagesRef = db.collection("users").document(userId)
            .collection("sessions").document(sessionId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)

        val listener = messagesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error observing messages", error)
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val messages = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ChatMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        ?.copy(id = doc.id)
                }
                trySend(messages)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveMessage(userId: String, sessionId: String, sender: String, content: String): String {
        require(userId.isNotBlank()) { "User ID required" }
        require(sessionId.isNotBlank()) { "Session ID required" }

        val sessionRef = db.collection("users").document(userId)
            .collection("sessions").document(sessionId)

        val msgRef = sessionRef.collection("messages").document()
        val msgData = mapOf(
            "id" to msgRef.id,
            "sessionId" to sessionId,
            "sender" to sender,
            "content" to content,
            "timestamp" to FieldValue.serverTimestamp()
        )
        msgRef.set(msgData).await()

        // Update session meta
        sessionRef.update(
            mapOf(
                "updatedAt" to FieldValue.serverTimestamp(),
                "messageCount" to FieldValue.increment(1)
            )
        ).await()

        return msgRef.id
    }

    fun observeSnippets(userId: String): Flow<List<CodeSnippet>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val snippetsRef = db.collection("users").document(userId)
            .collection("snippets")
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val listener = snippetsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error observing snippets", error)
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val snippets = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(CodeSnippet::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        ?.copy(id = doc.id)
                }
                trySend(snippets)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveSnippet(userId: String, title: String, language: String, code: String): String {
        require(userId.isNotBlank()) { "User ID required" }
        val docRef = db.collection("users").document(userId).collection("snippets").document()
        val data = mapOf(
            "id" to docRef.id,
            "userId" to userId,
            "title" to title.ifBlank { "Untitled Code" },
            "language" to language.lowercase(),
            "code" to code,
            "timestamp" to FieldValue.serverTimestamp()
        )
        docRef.set(data).await()
        return docRef.id
    }

    suspend fun deleteSnippet(userId: String, snippetId: String) {
        db.collection("users").document(userId)
            .collection("snippets").document(snippetId)
            .delete().await()
    }
}
