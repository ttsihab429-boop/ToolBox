package com.example.ui.screens.bangladesh

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.PreferencesManager
import com.example.localization.AppLanguage
import com.example.ui.components.ToolBoxTopBar
import com.example.ui.components.ToolResultCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.RedAccent
import java.text.DecimalFormat

data class SubjectGrade(val name: String, var gradePoint: Double = 5.0, var gradeLetter: String = "A+")

@Composable
fun GpaCalculatorScreen(
    initialToolId: String,
    language: AppLanguage,
    prefs: PreferencesManager,
    onBackClick: () -> Unit
) {
    val isBn = language == AppLanguage.BANGLA
    var isHsc by remember { mutableStateOf(initialToolId == "hsc_gpa") }
    val df = remember { DecimalFormat("0.00") }

    val gradeScale = listOf(
        "A+" to 5.0,
        "A" to 4.0,
        "A-" to 3.5,
        "B" to 3.0,
        "C" to 2.0,
        "D" to 1.0,
        "F" to 0.0
    )

    val sscSubjects = remember {
        mutableStateListOf(
            SubjectGrade("Bangla"),
            SubjectGrade("English"),
            SubjectGrade("Mathematics"),
            SubjectGrade("Religion"),
            SubjectGrade("Science / Physics"),
            SubjectGrade("Social Science / Chemistry"),
            SubjectGrade("ICT"),
            SubjectGrade("Biology / Higher Math (Optional 4th)")
        )
    }

    val hscSubjects = remember {
        mutableStateListOf(
            SubjectGrade("Bangla"),
            SubjectGrade("English"),
            SubjectGrade("ICT"),
            SubjectGrade("Physics / Accounting"),
            SubjectGrade("Chemistry / Management"),
            SubjectGrade("Higher Math / Finance"),
            SubjectGrade("Biology / Marketing (Optional 4th)")
        )
    }

    val activeList = if (isHsc) hscSubjects else sscSubjects

    val gpaResult by remember(activeList) {
        derivedStateOf {
            val mainSubjects = activeList.dropLast(1)
            val fourthSubject = activeList.last()

            val hasFail = mainSubjects.any { it.gradePoint == 0.0 }
            if (hasFail) {
                return@derivedStateOf Pair("0.00 (F)", if (isBn) "আবশ্যিক বিষয়ে অনুত্তীর্ণ (Fail)" else "Failed in mandatory subject")
            }

            val mainSum = mainSubjects.sumOf { it.gradePoint }
            val extraFourth = (fourthSubject.gradePoint - 2.0).coerceAtLeast(0.0)
            val rawGpa = (mainSum + extraFourth) / mainSubjects.size
            val finalGpa = rawGpa.coerceAtMost(5.0)

            val letter = when {
                finalGpa >= 5.0 -> "A+"
                finalGpa >= 4.0 -> "A"
                finalGpa >= 3.5 -> "A-"
                finalGpa >= 3.0 -> "B"
                finalGpa >= 2.0 -> "C"
                finalGpa >= 1.0 -> "D"
                else -> "F"
            }

            Pair("${df.format(finalGpa)} ($letter)", "4th Subject Extra Bonus: +${df.format(extraFourth)} point")
        }
    }

    Scaffold(
        topBar = {
            ToolBoxTopBar(
                title = if (isHsc) (if (isBn) "এইচএসসি জিপিএ ক্যালকুলেটর" else "HSC GPA Calculator")
                else (if (isBn) "এসএসসি জিপিএ ক্যালকুলেটর" else "SSC GPA Calculator"),
                onBackClick = onBackClick,
                isFavorite = prefs.isFavorite(if (isHsc) "hsc_gpa" else "ssc_gpa"),
                onFavoriteToggle = { prefs.toggleFavorite(if (isHsc) "hsc_gpa" else "ssc_gpa") }
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !isHsc,
                    onClick = { isHsc = false },
                    label = { Text("SSC Board Scale") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RedAccent, containerColor = DarkSurfaceElevated)
                )
                FilterChip(
                    selected = isHsc,
                    onClick = { isHsc = true },
                    label = { Text("HSC Board Scale") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RedAccent, containerColor = DarkSurfaceElevated)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (isBn) "বাংলাদেশ শিক্ষাবোর্ড গ্রেডিং স্কেল ও ৪র্থ বিষয়ের নিয়ম অনুযায়ী হিসাবকৃত।"
                else "Calculated per Bangladesh Education Board grading rules (including 4th subject point addition > 2.00).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            ToolResultCard(
                resultValue = gpaResult.first,
                title = if (isBn) "চূড়ান্ত জিপিএ (GPA)" else "Final GPA Result",
                subtitle = gpaResult.second
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (isBn) "প্রতিটি বিষয়ের গ্রেড নির্বাচন করুন" else "Select Grade for Each Subject",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            activeList.forEachIndexed { index, subject ->
                val isFourth = index == activeList.lastIndex
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isFourth) RedAccent.copy(alpha = 0.5f) else DarkBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = subject.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isFourth) {
                                Text(
                                    text = if (isBn) "৪র্থ বিষয় (২.০ এর উপরের পয়েন্ট যোগ হবে)" else "4th Subject (Points > 2.0 added)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RedAccent
                                )
                            }
                        }

                        GradeDropdownSelector(
                            selectedGrade = subject.gradeLetter,
                            gradeScale = gradeScale,
                            onGradeSelected = { letter, point ->
                                activeList[index] = subject.copy(gradeLetter = letter, gradePoint = point)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GradeDropdownSelector(
    selectedGrade: String,
    gradeScale: List<Pair<String, Double>>,
    onGradeSelected: (String, Double) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = { expanded = true },
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(selectedGrade, color = RedAccent, fontWeight = FontWeight.Bold)
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            gradeScale.forEach { (letter, point) ->
                DropdownMenuItem(
                    text = { Text("$letter ($point)") },
                    onClick = {
                        onGradeSelected(letter, point)
                        expanded = false
                    }
                )
            }
        }
    }
}
