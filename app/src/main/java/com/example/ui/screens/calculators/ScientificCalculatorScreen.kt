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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import kotlin.math.PI
import kotlin.math.E
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

@Composable
fun ScientificCalculatorScreen(
    language: AppLanguage,
    prefs: PreferencesManager,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var isRad by remember { mutableStateOf(false) }

    fun evaluateSci(): String? {
        if (expression.isBlank()) return null
        return try {
            val res = evalScientific(expression, isRad)
            val df = DecimalFormat("#.########")
            df.format(res)
        } catch (_: Exception) {
            null
        }
    }

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
                    result = evaluateSci() ?: ""
                }
            }
            "DEG", "RAD" -> {
                isRad = !isRad
                result = evaluateSci() ?: ""
            }
            "=" -> {
                val eval = evaluateSci()
                if (eval != null) {
                    expression = eval
                    result = ""
                } else {
                    result = "Error"
                }
            }
            "x²" -> {
                expression += "^2"
                result = evaluateSci() ?: ""
            }
            "x^y" -> {
                expression += "^"
            }
            "sin", "cos", "tan", "ln", "log", "√" -> {
                expression += "$key("
            }
            "π" -> {
                expression += PI.toString()
                result = evaluateSci() ?: ""
            }
            "e" -> {
                expression += E.toString()
                result = evaluateSci() ?: ""
            }
            else -> {
                expression += key
                result = evaluateSci() ?: ""
            }
        }
    }

    Scaffold(
        topBar = {
            ToolBoxTopBar(
                title = if (language == AppLanguage.BANGLA) "সাইন্টিফিক ক্যালকুলেটর" else "Scientific Calculator",
                onBackClick = onBackClick,
                isFavorite = prefs.isFavorite("sci_calc"),
                onFavoriteToggle = { prefs.toggleFavorite("sci_calc") }
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
            // Display Screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = if (isRad) "RAD" else "DEG",
                        style = MaterialTheme.typography.labelSmall,
                        color = RedAccent
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (expression.isEmpty()) "0" else expression,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = if (expression.length > 12) 20.sp else 28.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End,
                        maxLines = 2
                    )
                    if (result.isNotEmpty() && result != "Error") {
                        Text(
                            text = "= $result",
                            style = MaterialTheme.typography.titleMedium.copy(color = RedAccent),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Keypad
            val sciKeys = listOf(
                listOf(if (isRad) "RAD" else "DEG", "sin", "cos", "tan", "ln"),
                listOf("log", "√", "x²", "x^y", "π"),
                listOf("C", "(", ")", "⌫", "÷"),
                listOf("7", "8", "9", "e", "×"),
                listOf("4", "5", "6", "%", "-"),
                listOf("1", "2", "3", ".", "+"),
                listOf("0", "00", "=", "=", "=")
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sciKeys.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (row.size == 5 && row[2] == "=" && row[3] == "=" && row[4] == "=") {
                            // Special last row: 0, 00, big =
                            val k0 = row[0]
                            val k1 = row[1]
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.2f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkSurface)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                                    .clickable { onKey(k0) }
                                    .testTag("sci_key_$k0"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(k0, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.2f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkSurface)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                                    .clickable { onKey(k1) }
                                    .testTag("sci_key_$k1"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(k1, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(3f)
                                    .aspectRatio(3.6f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(RedAccent)
                                    .clickable { onKey("=") }
                                    .testTag("sci_key_equals"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("=", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            }
                        } else {
                            row.forEach { key ->
                                val isOp = key in listOf("÷", "×", "-", "+", "=")
                                val isSci = key in listOf("sin", "cos", "tan", "ln", "log", "√", "x²", "x^y", "π", "e", "RAD", "DEG")
                                val isAction = key in listOf("C", "⌫", "(", ")", "%")

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1.2f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            when {
                                                isOp -> DarkSurfaceElevated
                                                isSci -> DarkSurfaceElevated.copy(alpha = 0.5f)
                                                isAction -> DarkSurfaceElevated.copy(alpha = 0.7f)
                                                else -> DarkSurface
                                            }
                                        )
                                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                                        .clickable { onKey(key) }
                                        .testTag("sci_key_$key"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = key,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = if (key.length > 2) 12.sp else 16.sp
                                        ),
                                        color = when {
                                            isOp -> RedAccent
                                            isSci -> MaterialTheme.colorScheme.secondary
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
}

fun evalScientific(expr: String, isRad: Boolean): Double {
    var s = expr.replace("×", "*").replace("÷", "/")

    return object : Any() {
        var pos = -1
        var ch = 0

        fun nextChar() {
            ch = if (++pos < s.length) s[pos].code else -1
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
            if (pos < s.length) throw RuntimeException("Unexpected: " + ch.toChar())
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
                    eat('/'.code) -> x /= parseFactor()
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
                x = s.substring(startPos, pos).toDouble()
            } else if (ch in 'a'.code..'z'.code || ch == '√'.code) {
                while (ch in 'a'.code..'z'.code || ch == '√'.code) nextChar()
                val func = s.substring(startPos, pos)
                x = parseFactor()
                x = when (func) {
                    "√" -> sqrt(x)
                    "sin" -> if (isRad) sin(x) else sin(Math.toRadians(x))
                    "cos" -> if (isRad) cos(x) else cos(Math.toRadians(x))
                    "tan" -> if (isRad) tan(x) else tan(Math.toRadians(x))
                    "ln" -> ln(x)
                    "log" -> log10(x)
                    else -> throw RuntimeException("Unknown function: $func")
                }
            } else {
                throw RuntimeException("Unexpected: " + ch.toChar())
            }

            if (eat('^'.code)) x = x.pow(parseFactor())
            return x
        }
    }.parse()
}
