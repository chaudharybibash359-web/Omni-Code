# Dark Claude AI: Dark Coding Master King

A premium, subscription-free AI coding environment featuring a dark cyberpunk IDE aesthetic, multi-turn coding chats powered by high-performance Gemini models configured with deep Claude-style coding architecture personas, an integrated syntax-highlighting code viewer with instant execution sandbox, and secure Google Sign-In with encrypted Firebase Firestore cloud history sync.

## User Review & Critical Decisions

> [!IMPORTANT]
> The architectural direction and key user choices have been aligned through Phase 1 requirements clarification:

- **AI Model Engine & Persona**: Configured with `gemini-3.8-flash` (and Pro capability) utilizing a comprehensive "Dark Claude Coding Master" system prompt—delivering advanced software architecture, step-by-step reasoning, clean modular code generation, and instant refactoring without paywalls or usage subscriptions.
- **Visual Design & IDE Layout**: Cyberpunk obsidian dark theme (`#0D0F14` void black with `#6C5CE7` violet, `#00F0FF` electric cyan, and `#00E676` emerald accents), featuring tabbed workspaces (Chat Console, Live Code Sandbox, Snippet Library), monospaced syntax-highlighted code cards, one-tap clipboard copy, and simulated output runner.
- **Authentication & Cloud Persistence**: Integrated Google Sign-In via Android Credential Manager and Firebase Authentication, backing persistent session storage in Firebase Firestore with user-isolated security rules so coding chats and code snippets sync seamlessly across devices.

---

## 1. Overview & Core Concept

- **What It Does**: Dark Claude AI provides an elite mobile software engineering hub where developers can prompt, debug, architect, and run code. Users can switch between coding modes (Full-stack Architect, Bug Hunter, Code Optimizer, Explainer), inspect code in formatted syntax-highlighted cards, run interactive test scripts in a sandbox terminal, and securely maintain all conversation threads across sessions.
- **Target Audience / Persona**: Mobile developers, software engineering students, and power users who want an unrestricted, fast, and aesthetically polished AI coding companion on Android.
- **Key Value**: Delivers zero-paywall, unrestricted AI coding intelligence with persistent cloud backup, production-grade security, and a dedicated code-first mobile IDE experience.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Secure Onboarding & Auth Gate**:
   - First launch displays the Cyberpunk Dark Claude splash with Google Sign-In via Credential Manager.
   - Upon signing in, the user's profile, saved project threads, and custom snippets are synchronized from Firestore.
2. **Coding Chat & Streaming Workspace**:
   - The user opens an existing session or taps "+ New Session" with customizable persona directives.
   - User types a coding request (or picks quick starters: "Debug NullPointer", "Design Clean REST API", "Optimize Compose Recomposition").
   - AI streams responses in real-time, automatically isolating code blocks into specialized syntax-highlighted containers.
3. **Interactive Code Sandbox & Execution**:
   - Code blocks inside the chat include action headers: Language badge (Kotlin, Python, JS, SQL, Rust), "Copy", and "Send to Sandbox".
   - The integrated Code Sandbox tab lets users view formatted line numbers, edit code, and trigger an interactive runtime executor/evaluator with simulated terminal output.
4. **Session Management & Cloud Vault**:
   - Sidebar drawer displays all past coding chats sorted by date, with search, pinning, and deletion.
   - Profile sheet shows authenticated Google user status, token statistics, active AI model parameters, and sign-out control.

### Visual Identity & Theme

- **Aesthetic Direction**: High-tech Cyberpunk Obsidian IDE. Monochromatic deep obsidian backgrounds accented by glowing neon cyan, royal purple, and terminal green.
- **Color Palette**:
  - `Background`: `#0A0C10` (Ultra Dark Obsidian)
  - `Surface / Card`: `#12161F` (Elevated Panel)
  - `Surface Variant`: `#1A202C` (Input fields & Code containers)
  - `Primary Accent`: `#7C4DFF` (Dark Claude Violet)
  - `Secondary Accent`: `#00E5FF` (Electric Cyan)
  - `Terminal Green`: `#00E676` (Console output & execution success)
  - `Error / Warning`: `#FF5252` / `#FFD600`
  - `Text Primary`: `#F1F5F9`, `Text Muted`: `#94A3B8`
- **Typography**:
  - Clean display font for headers and system typography.
  - Monospace font (`FontFamily.Monospace`) strictly applied for all code blocks, diff views, and sandbox terminal outputs.
- **Component Styling & Layout**:
  - M3 NavigationBar or Top Tabs (`Chat`, `Code Sandbox`, `Saved Snippets`).
  - Edge-to-edge safe area handling with proper system gesture padding.
  - Touch targets sized to 48dp with subtle glow and ripple feedback.

---

## 3. Key Product Decisions & Trade-Offs

### Decision 1: AI Engine & Personas
- **Chosen Approach**: Use Firebase AI Logic with `gemini-3.8-flash` initialized lazily, bound to a detailed "Dark Claude Coding Master King" system instruction.
- **Why**: `gemini-3.8-flash` delivers ultra-fast streaming (TTFT < 1s), large context window, deep coding comprehension, and free tier reliability without requiring paid user subscriptions.
- **Persona Directives**: Expert code design, clean architecture, DRY principles, defensive error handling, and structured markdown code fences.

### Decision 2: Google Sign-In & Zero-Trust Cloud Storage
- **Chosen Approach**: Firebase Authentication with Google Sign-In via Jetpack `CredentialManager`, paired with Firebase Firestore storing user sessions at `/users/{userId}/sessions/{sessionId}`.
- **Why**: Meets the user's explicit requirement for secure login, OAuth, and encrypted cloud storage, while conforming strictly to the platform's authentication invariants.

### Decision 3: Integrated Code Viewer & Execution Sandbox
- **Chosen Approach**: Native Compose syntax parser with keyword highlighting (Kotlin, Python, Java, JavaScript, C++, Dart, SQL) and an interactive sandbox runner with virtual standard input/output simulation and template snippets.
- **Why**: Allows users to interact with generated code immediately without leaving the app.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        MainActivity (Edge-to-Edge)                     │
│  - configureAppCheck(context, intent)                                  │
│  - AuthGate (CredentialManager Google Sign-In)                         │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
       ┌────────────────────────────┴─────────────────────────────┐
       ▼                                                           ▼
┌──────────────────────────────────────┐     ┌────────────────────────────────────┐
│          AuthViewModel               │     │           ChatViewModel            │
│  - Google Sign-In state              │     │  - Active session & messages       │
│  - Current User (Firebase User)      │     │  - Streaming inference (Firebase AI│
│  - Sign-out & profile data           │     │  - Coding persona switcher         │
└──────────────────┬───────────────────┘     └─────────────────┬──────────────────┘
                   │                                           │
                   ▼                                           ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           Firestore Repository Layer                            │
│  - UserSessionsRepository: /users/{userId}/sessions/{sessionId}                 │
│  - ChatMessagesRepository: /users/{userId}/sessions/{sessionId}/messages/{msgId}│
│  - CodeSnippetsRepository: /users/{userId}/snippets/{snippetId}                 │
│  - Firestore rules: request.auth != null && request.auth.uid == userId          │
└───────────────────────────────────┬─────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                                 UI Layer                                        │
│  ┌───────────────────────┐ ┌──────────────────────────┐ ┌────────────────────┐  │
│  │      Chat Screen      │ │       Code Sandbox       │ │   Snippets Vault   │  │
│  │ - Streaming bubbles   │ │ - Monospace editor       │ │ - Saved algorithms │  │
│  │ - Syntax code cards   │ │ - Output console runner  │ │ - Language filters │  │
│  │ - Quick prompt chips  │ │ - Copy / Share / Format  │ │ - Cloud sync badge │  │
│  └───────────────────────┘ └──────────────────────────┘ └────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### Data Schema (Firestore & Local Models)

1. **`Session`**:
   - `id`: String (UUID)
   - `userId`: String (Firebase UID)
   - `title`: String
   - `modelName`: String ("Dark Claude 3.8 Coding Engine")
   - `createdAt`: Long
   - `updatedAt`: Long
   - `messageCount`: Int

2. **`ChatMessage`**:
   - `id`: String
   - `sessionId`: String
   - `sender`: String ("user" | "model")
   - `content`: String (Markdown with code blocks)
   - `timestamp`: Long
   - `codeSnippetExtracted`: Boolean

3. **`CodeSnippet`**:
   - `id`: String
   - `userId`: String
   - `title`: String
   - `language`: String
   - `code`: String
   - `timestamp`: Long

---

## 5. Phased Implementation Roadmap

1. **Backend Provisioning**:
   - Call `set_up_firebase` with `platform: "web"`, `requestedProducts: ["firestore", "ai_logic"]` via the interactive tool.
   - Configure dependencies in `app/build.gradle.kts` (Firebase Auth, Credential Manager, GoogleId, Firestore, App Check).
2. **Security Rules & Testing**:
   - Draft `firebase-blueprint.json` and strict user-isolated `firestore.rules`.
   - Author rules tests and deploy rules using `deploy_firebase`.
3. **Core AI & Auth Architecture**:
   - Implement `configureAppCheck` in `MainActivity`.
   - Setup `AuthRepository` with Credential Manager Google Sign-In.
   - Setup `AiCodingRepository` using `Firebase.ai.generativeModel` with streaming and Dark Claude coding master system directives.
   - Setup `FirestoreRepository` for persistent session history.
4. **Cyberpunk Dark IDE UI**:
   - Build `Theme.kt` with dark obsidian color scheme.
   - Build `CodeBlockCard` with syntax coloring and copy/sandbox buttons.
   - Build `ChatScreen` with quick-prompt chips, model badge, and real-time streaming indicator.
   - Build `CodeSandboxScreen` with line numbers, code editor, and console output simulator.
   - Build `SessionDrawer` and `UserProfileDialog`.
5. **Verification & Polish**:
   - Verify compilation with `compile_applet`.
   - Ensure edge-to-edge safe insets, accessible touch targets, and resilient error handling.
