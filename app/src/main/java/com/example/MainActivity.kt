package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.AiCodingRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.FirestoreChatRepository
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.MainScreen
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.DarkClaudeTheme
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.util.configureAppCheck
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Mandatory App Check configuration with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        val auth = FirebaseAuth.getInstance()
        val databaseId = getString(R.string.firestore_database_id)
        val firestore = FirebaseFirestore.getInstance(databaseId)

        val authRepository = AuthRepository(auth, firestore)
        val authViewModel = AuthViewModel(authRepository)

        setContent {
            DarkClaudeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CyberBackground
                ) {
                    AppRoot(
                        authViewModel = authViewModel,
                        firestore = firestore
                    )
                }
            }
        }
    }
}

@Composable
fun AppRoot(
    authViewModel: AuthViewModel,
    firestore: FirebaseFirestore
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val user = currentUser

    if (user == null) {
        // Unauthenticated gate: render Sign In screen only
        AuthScreen(authViewModel = authViewModel)
    } else {
        // Authenticated session: pass authenticated UID to scoped ViewModel
        val chatRepository = remember(user.uid) { FirestoreChatRepository(firestore) }
        val aiRepository = remember { AiCodingRepository() }

        val mainViewModel: MainViewModel = viewModel(
            key = user.uid,
            factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return MainViewModel(chatRepository, aiRepository, user.uid) as T
                }
            }
        )

        MainScreen(
            user = user,
            authViewModel = authViewModel,
            mainViewModel = mainViewModel
        )
    }
}
