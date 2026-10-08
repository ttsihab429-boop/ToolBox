package com.example.ui.screens.calculators

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import com.example.localization.AppLanguage
import com.example.ui.components.ToolActions
import com.example.ui.components.ToolBoxTopBar
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.RedAccent
import java.text.DecimalFormat

@Composable
fun BasicCalculatorScreen(
    language: AppLanguage,
    prefs: PreferencesManager,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var history by remember { mutableStateOf(listOf<String>()) }
    var showHistory by remember { mutableStateOf(false) }

    fun onKey(key: String) {
        ToolActions.triggerHaptic(context, prefs.hapticEnabled.value)
        when (key) {
            "C" -> {
                expression = ""
                result = ""
            }
            "⌫" -> {
                if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                    if (expression.isEmpty()) result = ""
                    else calculateSimple(expression)?.let { result = it }
                }
            }
            "=" -> {
                if (expression.isNotEmpty()) {
                    val calc = calculateSimple(expression)
                    if (calc != null) {
                        history = listOf("$expression = $calc") + history.take(9)
                        expression = calc
                        result = ""
                    } else {
                        result = "Error"
                    }
                }
            }
            else -> {
                expression += key
                calculateSimple(expression)?.let { result = it }
            }
        }
    }

    Scaffold(
        topBar = {
            ToolBoxTopBar(
                title = if (language == AppLanguage.BANGLA) "সাধারণ ক্যালকুলেটর" else "Basic Calculator",
                onBackClick = onBackClick,
                isFavorite = prefs.isFavorite("basic_calc"),
                onFavoriteToggle = { prefs.toggleFavorite("basic_calc") }
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Display Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                if (showHistory && history.isNotEmpty()) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Text(
                                text = if (language == AppLanguage.BANGLA) "হিসাবের ইতিহাস" else "Calculation History",
                                style = MaterialTheme.typography.labelSmall,
                                color = RedAccent,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(history) { entry ->
                            Text(
                                text = entry,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        val parts = entry.split(" = ")
                                        if (parts.size == 2) {
                                            expression = parts[1]
                                            showHistory = false
                                        }
                                    }
                            )
                            HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.End
                    ) {
                        if (history.isNotEmpty()) {
                            Text(
                                text = if (showHistory) "Close" else "History (${history.size})",
                                style = MaterialTheme.typography.labelSmall,
                                color = RedAccent,
                                modifier = Modifier
                                    .clickable { showHistory = !showHistory }
                                    .padding(bottom = 8.dp)
                            )
                        }
                        Text(
                            text = if (expression.isEmpty()) "0" else expression,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = if (expression.length > 14) 24.sp else 36.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.End,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (result.isNotEmpty() && result != "Error") {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "= $result",
                                style = MaterialTheme.typography.titleLarge.copy(color = RedAccent),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Keypad Grid
            val keys = listOf(
                listOf("C", "(", ")", "÷"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("%", "0", ".", "=")
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                keys.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { key ->
                            val isOp = key in listOf("÷", "×", "-", "+", "=")
                            val isAction = key in listOf("C", "⌫", "(", ")", "%")
                            val isEquals = key == "="

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.25f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        when {
                                            isEquals -> RedAccent
                                            isOp -> DarkSurfaceElevated
                                            isAction -> DarkSurfaceElevated.copy(alpha = 0.6f)
                                            else -> DarkSurface
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        if (isEquals) RedAccent else DarkBorder,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable { onKey(key) }
                                    .testTag("calc_key_$key"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = key,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    ),
                                    color = when {
                                        isEquals -> Color.White
                                        isOp -> RedAccent
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun calculateSimple(expr: String): String? {
    return try {
        val sanitized = expr.replace("×", "*").replace("÷", "/")
        val result = evalMath(sanitized)
        val df = DecimalFormat("#.########")
        df.format(result)
    } catch (_: Exception) {
        null
    }
}

fun evalMath(str: String): Double {
    return object : Any() {
        var pos = -1
        var ch = 0

        fun nextChar() {
            ch = if (++pos < str.length) str[pos].code else -1
        }

        fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
            return x
        }

        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+'.code) -> x += parseTerm()
                    eat('-'.code) -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*'.code) -> x *= parseFactor()
                    eat('/'.code) -> {
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Div by zero")
                        x /= divisor
                    }
                    eat('%'.code) -> x %= parseFactor()
                    else -> return x
                }
            }
        }

        fun parseFactor(): Double {
            if (eat('+'.code)) return +parseFactor()
            if (eat('-'.code)) return -parseFactor()

            var x: Double
            val startPos = pos
            if (eat('('.code)) {
                x = parseExpression()
                eat(')'.code)
            } else if ((ch in '0'.code..'9'.code) || ch == '.'.code) {
                while ((ch in '0'.code..'9'.code) || ch == '.'.code) nextChar()
                x = str.substring(startPos, pos).toDouble()
            } else {
                throw RuntimeException("Unexpected: " + ch.toChar())
            }
            return x
        }
    }.parse()
}
