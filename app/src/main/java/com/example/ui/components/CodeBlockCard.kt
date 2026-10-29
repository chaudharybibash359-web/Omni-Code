package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CodeBg
import com.example.ui.theme.CodeHeaderBg
import com.example.ui.theme.CyberOnSurfaceMuted
import com.example.ui.theme.CyberOutline
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberSecondary
import com.example.ui.theme.CyberTertiary
import com.example.ui.theme.SyntaxComment
import com.example.ui.theme.SyntaxFunction
import com.example.ui.theme.SyntaxKeyword
import com.example.ui.theme.SyntaxNumber
import com.example.ui.theme.SyntaxString
import com.example.ui.theme.SyntaxType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CodeBlockCard(
    code: String,
    language: String,
    onOpenInSandbox: ((code: String, language: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isCopied by remember { mutableStateOf(false) }

    val cleanLang = language.trim().lowercase().ifBlank { "code" }
    val lines = remember(code) { code.lines() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(1.dp, CyberOutline, RoundedCornerShape(10.dp)),
        color = CodeBg,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CodeHeaderBg)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(CyberPrimary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = cleanLang.uppercase(),
                            color = CyberSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${lines.size} lines",
                        color = CyberOnSurfaceMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Open in Sandbox button
                    if (onOpenInSandbox != null) {
                        OutlinedButton(
                            onClick = { onOpenInSandbox(code, cleanLang) },
                            modifier = Modifier
                                .testTag("sandbox_open_button")
                                .padding(end = 4.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = CyberTertiary
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(CyberTertiary.copy(alpha = 0.5f))
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Run in Sandbox",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Sandbox", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    // Copy Button
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("code", code)
                            clipboard.setPrimaryClip(clip)
                            isCopied = true
                            Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                            coroutineScope.launch {
                                delay(2000)
                                isCopied = false
                            }
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("copy_code_button")
                    ) {
                        Icon(
                            imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = if (isCopied) "Copied" else "Copy Code",
                            tint = if (isCopied) CyberTertiary else CyberOnSurfaceMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Code Content with Line Numbers & Syntax Highlighting
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                // Line Numbers
                Column(
                    modifier = Modifier.padding(end = 12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    lines.forEachIndexed { index, _ ->
                        Text(
                            text = "${index + 1}",
                            color = CyberOnSurfaceMuted.copy(alpha = 0.4f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Highlighted Code Lines
                Column {
                    lines.forEach { line ->
                        Text(
                            text = highlightSyntax(line, cleanLang),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Lightweight syntax highlighter for code keywords, strings, types, and comments
 */
fun highlightSyntax(line: String, language: String): AnnotatedString {
    return buildAnnotatedString {
        val trimmed = line.trimStart()

        // Comments
        if (trimmed.startsWith("//") || trimmed.startsWith("#") || trimmed.startsWith("--")) {
            append(line)
            addStyle(SpanStyle(color = SyntaxComment), 0, line.length)
            return@buildAnnotatedString
        }

        append(line)

        val keywords = when (language) {
            "kotlin", "java" -> listOf("fun", "val", "var", "class", "interface", "object", "return", "if", "else", "when", "for", "while", "import", "package", "public", "private", "suspend", "override", "data", "sealed")
            "python" -> listOf("def", "class", "return", "import", "from", "if", "elif", "else", "for", "while", "in", "as", "with", "try", "except", "async", "await", "lambda")
            "javascript", "typescript", "js", "ts" -> listOf("function", "const", "let", "var", "return", "if", "else", "for", "while", "import", "export", "class", "async", "await", "new", "try", "catch")
            "sql" -> listOf("SELECT", "FROM", "WHERE", "INSERT", "INTO", "UPDATE", "DELETE", "JOIN", "ORDER", "BY", "GROUP", "LIMIT", "CREATE", "TABLE")
            else -> listOf("fun", "def", "function", "return", "class", "val", "var", "const", "let", "if", "else")
        }

        // Highlight Keywords
        for (kw in keywords) {
            var index = line.indexOf(kw)
            while (index != -1) {
                val beforeChar = if (index > 0) line[index - 1] else ' '
                val afterIndex = index + kw.length
                val afterChar = if (afterIndex < line.length) line[afterIndex] else ' '
                if (!beforeChar.isLetterOrDigit() && !afterChar.isLetterOrDigit()) {
                    addStyle(SpanStyle(color = SyntaxKeyword, fontWeight = FontWeight.Bold), index, afterIndex)
                }
                index = line.indexOf(kw, index + kw.length)
            }
        }

        // Highlight Strings (simple quotes)
        var inQuote = false
        var quoteStart = -1
        line.forEachIndexed { i, c ->
            if (c == '"' || c == '\'') {
                if (!inQuote) {
                    inQuote = true
                    quoteStart = i
                } else {
                    inQuote = false
                    addStyle(SpanStyle(color = SyntaxString), quoteStart, i + 1)
                }
            }
        }

        // Highlight Numbers
        val numberRegex = "\\b\\d+\\b".toRegex()
        numberRegex.findAll(line).forEach { match ->
            addStyle(SpanStyle(color = SyntaxNumber), match.range.first, match.range.last + 1)
        }
    }
}
