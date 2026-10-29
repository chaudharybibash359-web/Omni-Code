package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CodeBg
import com.example.ui.theme.CodeHeaderBg
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberOnSurface
import com.example.ui.theme.CyberOnSurfaceMuted
import com.example.ui.theme.CyberOutline
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberPrimaryBright
import com.example.ui.theme.CyberSecondary
import androidx.compose.foundation.clickable
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTertiary
import com.example.ui.theme.CyberWarning
import com.example.ui.viewmodel.MainViewModel

@Composable
fun CodeSandboxScreen(
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val code by mainViewModel.sandboxCode.collectAsState()
    val language by mainViewModel.sandboxLanguage.collectAsState()
    val output by mainViewModel.sandboxOutput.collectAsState()
    val isExecuting by mainViewModel.isExecuting.collectAsState()

    var showSaveDialog by remember { mutableStateOf(false) }
    var snippetTitle by remember { mutableStateOf("") }

    val languages = listOf("kotlin", "python", "javascript", "java", "sql", "rust", "cpp", "bash")

    Surface(
        modifier = modifier.fillMaxSize(),
        color = CyberBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Language selector chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                languages.forEach { lang ->
                    val isSelected = lang.equals(language, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) CyberPrimary.copy(alpha = 0.25f) else CyberSurfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) CyberSecondary else CyberOutline,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickableWithoutRipple { mainViewModel.setSandboxLanguage(lang) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = lang.uppercase(),
                            color = if (isSelected) CyberSecondary else CyberOnSurfaceMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sandbox Action Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Execute Button
                Button(
                    onClick = { mainViewModel.runSandboxCode() },
                    enabled = !isExecuting,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberTertiary,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.testTag("run_sandbox_code_button")
                ) {
                    if (isExecuting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Executing...",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Run",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Run Code",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Quick Template Button
                    OutlinedButton(
                        onClick = {
                            val template = getBoilerplateTemplate(language)
                            mainViewModel.updateSandboxCode(template)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberSecondary),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(CyberSecondary.copy(alpha = 0.5f))
                        )
                    ) {
                        Text(text = "Template", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }

                    // Save to Snippets
                    IconButton(
                        onClick = {
                            snippetTitle = "Snippet ${language.uppercase()} ${System.currentTimeMillis() % 1000}"
                            showSaveDialog = true
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(CyberSurfaceVariant, RoundedCornerShape(8.dp))
                            .border(1.dp, CyberOutline, RoundedCornerShape(8.dp))
                            .testTag("save_to_snippets_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkAdd,
                            contentDescription = "Save Snippet",
                            tint = CyberPrimaryBright,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Code Editor Box
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(CodeBg, RoundedCornerShape(10.dp))
                    .border(1.dp, CyberOutline, RoundedCornerShape(10.dp))
            ) {
                // Editor Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CodeHeaderBg, RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = CyberSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "main.${getFileExtension(language)}",
                            color = CyberOnSurface,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = "${code.lines().size} lines",
                        color = CyberOnSurfaceMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Editor Text Area
                OutlinedTextField(
                    value = code,
                    onValueChange = { mainViewModel.updateSandboxCode(it) },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("sandbox_code_input"),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = CyberOnSurface
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = CodeBg,
                        unfocusedContainerColor = CodeBg
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Terminal Output Console
            Column(
                modifier = Modifier
                    .height(170.dp)
                    .fillMaxWidth()
                    .background(CyberSurface, RoundedCornerShape(10.dp))
                    .border(1.dp, CyberTertiary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = CyberTertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TERMINAL STDOUT",
                            color = CyberTertiary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = if (isExecuting) "RUNNING" else "READY",
                        color = if (isExecuting) CyberWarning else CyberOnSurfaceMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFF06080C), RoundedCornerShape(6.dp))
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (output != null) {
                        Text(
                            text = output ?: "",
                            color = CyberTertiary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )
                    } else {
                        Text(
                            text = "[SYSTEM] Tap 'Run Code' above to compile & execute.",
                            color = CyberOnSurfaceMuted.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }

    // Save Snippet Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(
                    text = "Save Code to Vault",
                    color = CyberOnSurface,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter a title for this $language snippet:",
                        color = CyberOnSurfaceMuted,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = snippetTitle,
                        onValueChange = { snippetTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberPrimary,
                            unfocusedBorderColor = CyberOutline,
                            focusedTextColor = CyberOnSurface,
                            unfocusedTextColor = CyberOnSurface
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        mainViewModel.saveSnippet(snippetTitle, language, code)
                        showSaveDialog = false
                        Toast.makeText(context, "Snippet saved to Cloud Vault", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary)
                ) {
                    Text("Save", fontFamily = FontFamily.Monospace)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel", color = CyberOnSurfaceMuted)
                }
            },
            containerColor = CyberSurface,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

private fun getFileExtension(lang: String): String = when (lang.lowercase()) {
    "kotlin" -> "kt"
    "python" -> "py"
    "javascript" -> "js"
    "java" -> "java"
    "sql" -> "sql"
    "rust" -> "rs"
    "cpp" -> "cpp"
    "bash" -> "sh"
    else -> "txt"
}

private fun getBoilerplateTemplate(lang: String): String = when (lang.lowercase()) {
    "kotlin" -> """// Dark Claude Kotlin Algorithm
fun main() {
    val numbers = intArrayOf(2, 7, 11, 15)
    val target = 9
    val result = twoSum(numbers, target)
    println("Indices found: [${'$'}{result.joinToString(", ")}]")
}

fun twoSum(nums: IntArray, target: Int): IntArray {
    val map = mutableMapOf<Int, Int>()
    for (i in nums.indices) {
        val complement = target - nums[i]
        if (map.containsKey(complement)) {
            return intArrayOf(map[complement]!!, i)
        }
        map[nums[i]] = i
    }
    return intArrayOf()
}
""".trimIndent()

    "python" -> """# Dark Claude Python Algorithm
def binary_search(arr, target):
    low, high = 0, len(arr) - 1
    while low <= high:
        mid = (low + high) // 2
        if arr[mid] == target:
            return mid
        elif arr[mid] < target:
            low = mid + 1
        else:
            high = mid - 1
    return -1

items = [10, 20, 30, 40, 50, 60, 70]
idx = binary_search(items, 40)
print(f"Target 40 located at index: {idx}")
""".trimIndent()

    "javascript" -> """// Dark Claude JS Async Engine
async function simulateAsyncPipeline() {
    console.log("Initializing worker thread...");
    const data = [1, 2, 3, 4, 5];
    const squared = data.map(n => n * n);
    console.log("Processed pipeline data: " + squared.join(", "));
}

simulateAsyncPipeline();
""".trimIndent()

    else -> """// Dark Claude General Algorithm
fun main() {
    println("Algorithm execution ready.")
}
""".trimIndent()
}

// Clickable without ripple helper
private fun Modifier.clickableWithoutRipple(onClick: () -> Unit) = this.then(
    Modifier.clickable(
        interactionSource = null,
        indication = null,
        onClick = onClick
    )
)
