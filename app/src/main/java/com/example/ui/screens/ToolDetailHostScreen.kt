package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import com.example.data.NotesRepository
import com.example.data.PreferencesManager
import com.example.data.ShoppingRepository
import com.example.data.TodoRepository
import com.example.localization.AppLanguage
import com.example.ui.screens.bangladesh.BanglaNumberConverterScreen
import com.example.ui.screens.bangladesh.BdtCashCalculatorScreen
import com.example.ui.screens.bangladesh.BdUnitsScreen
import com.example.ui.screens.bangladesh.GpaCalculatorScreen
import com.example.ui.screens.bangladesh.LandConverterScreen
import com.example.ui.screens.calculators.BasicCalculatorScreen
import com.example.ui.screens.calculators.FinancialCalculatorsScreen
import com.example.ui.screens.calculators.MathCalculatorsScreen
import com.example.ui.screens.calculators.ScientificCalculatorScreen
import com.example.ui.screens.converters.UnitConverterScreen
import com.example.ui.screens.datetime.DateToolsScreen
import com.example.ui.screens.datetime.StopwatchScreen
import com.example.ui.screens.datetime.TimerScreen
import com.example.ui.screens.datetime.WorldClockScreen
import com.example.ui.screens.everyday.ChecklistScreen
import com.example.ui.screens.everyday.ColorPickerScreen
import com.example.ui.screens.everyday.NotesScreen
import com.example.ui.screens.everyday.PasswordGeneratorScreen
import com.example.ui.screens.everyday.RandomToolsScreen
import com.example.ui.screens.everyday.ShoppingListScreen
import com.example.ui.screens.everyday.SimpleMemoScreen
import com.example.ui.screens.everyday.TodoScreen
import com.example.ui.screens.files.FileToolsScreen
import com.example.ui.screens.files.PdfToolsScreen
import com.example.ui.screens.qr.QrGeneratorScreen
import com.example.ui.screens.qr.QrScannerScreen
import com.example.ui.screens.text.TextToolsScreen

@Composable
fun ToolDetailHostScreen(
    toolId: String,
    language: AppLanguage,
    prefs: PreferencesManager,
    notesRepository: NotesRepository,
    todoRepository: TodoRepository,
    shoppingRepository: ShoppingRepository,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    when (toolId) {
        "basic_calc" -> BasicCalculatorScreen(language, prefs, onBack)
        "sci_calc" -> ScientificCalculatorScreen(language, prefs, onBack)

        "pct_calc", "discount_calc", "profit_loss_calc", "gst_vat_calc", "tip_calc" ->
            FinancialCalculatorsScreen(toolId, language, prefs, onBack)

        "avg_calc", "fraction_calc", "ratio_calc", "age_calc", "date_diff_calc" ->
            MathCalculatorsScreen(toolId, language, prefs, onBack)

        "unit_length", "unit_weight", "unit_temp", "unit_area", "unit_volume",
        "unit_speed", "unit_time", "unit_data", "unit_pressure", "unit_energy" ->
            UnitConverterScreen(toolId, language, prefs, onBack)

        "stopwatch" -> StopwatchScreen(language, prefs, onBack)
        "countdown_timer" -> TimerScreen(language, prefs, onBack)
        "add_days", "subtract_days", "days_until" -> DateToolsScreen(toolId, language, prefs, onBack)
        "world_clock" -> WorldClockScreen(language, prefs, onBack)

        "qr_generator" -> QrGeneratorScreen(language, prefs, onBack)
        "qr_scanner" -> QrScannerScreen(language, prefs, onBack)

        "text_counter", "case_converter", "remove_spaces", "remove_duplicates",
        "text_sorter", "text_cleaner" ->
            TextToolsScreen(toolId, language, prefs, onBack)

        "notes" -> NotesScreen(language, prefs, notesRepository, onBack)
        "todo_list" -> TodoScreen(language, prefs, todoRepository, onBack)
        "shopping_list" -> ShoppingListScreen(language, prefs, shoppingRepository, onBack)
        "password_gen" -> PasswordGeneratorScreen(language, prefs, onBack)
        "random_number", "random_picker" -> RandomToolsScreen(toolId, language, prefs, onBack)
        "color_picker" -> ColorPickerScreen(language, prefs, onBack)
        "simple_memo" -> SimpleMemoScreen(language, prefs, onBack)
        "checklist" -> ChecklistScreen(language, prefs, onBack)

        "bdt_cash" -> BdtCashCalculatorScreen(language, prefs, onBack)
        "bangla_number" -> BanglaNumberConverterScreen(language, prefs, onBack)
        "ssc_gpa", "hsc_gpa" -> GpaCalculatorScreen(toolId, language, prefs, onBack)
        "land_converter" -> LandConverterScreen(language, prefs, onBack)
        "bd_units" -> BdUnitsScreen(language, prefs, onBack)

        "text_to_pdf", "image_to_pdf", "multi_image_to_pdf" ->
            PdfToolsScreen(toolId, language, prefs, onBack)

        "file_name_gen", "file_info" ->
            FileToolsScreen(toolId, language, prefs, onBack)

        else -> BasicCalculatorScreen(language, prefs, onBack)
    }
}
