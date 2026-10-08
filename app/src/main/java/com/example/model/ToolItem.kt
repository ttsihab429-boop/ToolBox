package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.CleanHands
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.localization.AppLanguage

enum class ToolCategory(val id: String) {
    CALCULATORS("calculators"),
    UNIT_CONVERTER("unit_converter"),
    DATE_TIME("date_time"),
    QR_SCANNER("qr_scanner"),
    TEXT_TOOLS("text_tools"),
    EVERYDAY_TOOLS("everyday_tools"),
    BANGLADESH_TOOLS("bangladesh_tools"),
    FILE_TOOLS("file_tools")
}

data class ToolItem(
    val id: String,
    val category: ToolCategory,
    val icon: ImageVector,
    val titleEn: String,
    val titleBn: String,
    val descEn: String,
    val descBn: String,
    val isPopular: Boolean = false,
    val keywords: List<String> = emptyList()
) {
    fun title(lang: AppLanguage): String =
        if (lang == AppLanguage.BANGLA) titleBn else titleEn

    fun desc(lang: AppLanguage): String =
        if (lang == AppLanguage.BANGLA) descBn else descEn
}

object ToolRegistry {
    val allTools: List<ToolItem> = listOf(
        // CATEGORY 1: CALCULATORS
        ToolItem(
            id = "basic_calc",
            category = ToolCategory.CALCULATORS,
            icon = Icons.Default.Functions,
            titleEn = "Basic Calculator",
            titleBn = "সাধারণ ক্যালকুলেটর",
            descEn = "Everyday arithmetic with history and brackets",
            descBn = "বন্ধন ও ইতিহাসসহ দৈনন্দিন হিসাব",
            isPopular = true,
            keywords = listOf("math", "add", "sub", "multiply", "divide", "hishab")
        ),
        ToolItem(
            id = "sci_calc",
            category = ToolCategory.CALCULATORS,
            icon = Icons.Default.Science,
            titleEn = "Scientific Calculator",
            titleBn = "সাইন্টিফিক ক্যালকুলেটর",
            descEn = "Trigonometry, logarithms, powers, and constants",
            descBn = "ত্রিকোণমিতি, লগ, ঘাত ও কনস্ট্যান্ট",
            isPopular = true,
            keywords = listOf("sin", "cos", "tan", "log", "sqrt", "power", "pi")
        ),
        ToolItem(
            id = "pct_calc",
            category = ToolCategory.CALCULATORS,
            icon = Icons.Default.Percent,
            titleEn = "Percentage Calculator",
            titleBn = "শতকরা ক্যালকুলেটর",
            descEn = "Find percentage, ratio, increase, and decrease",
            descBn = "শতকরা হার, অনুপাত ও বৃদ্ধি/হ্রাস নির্ণয়",
            isPopular = true,
            keywords = listOf("percent", "portion", "shokora")
        ),
        ToolItem(
            id = "discount_calc",
            category = ToolCategory.CALCULATORS,
            icon = Icons.Default.PriceCheck,
            titleEn = "Discount Calculator",
            titleBn = "ছাড় / ডিসকাউন্ট",
            descEn = "Calculate savings, final price, and coupon rates",
            descBn = "ছাড়ের পর চূড়ান্ত মূল্য ও মোট সাশ্রয়",
            keywords = listOf("sale", "coupon", "offer", "discount")
        ),
        ToolItem(
            id = "profit_loss_calc",
            category = ToolCategory.CALCULATORS,
            icon = Icons.Default.TrendingUp,
            titleEn = "Profit & Loss Calculator",
            titleBn = "লাভ ও ক্ষতি",
            descEn = "Analyze margin, markup, and net profit percentage",
            descBn = "ব্যবসার লাভ-ক্ষতির পরিমাণ ও শতকরা হার",
            keywords = listOf("business", "margin", "markup", "cost")
        ),
        ToolItem(
            id = "avg_calc",
            category = ToolCategory.CALCULATORS,
            icon = Icons.Default.Assessment,
            titleEn = "Average Calculator",
            titleBn = "গড় ক্যালকুলেটর",
            descEn = "Mean, median, minimum, maximum, and sum",
            descBn = "গড়, মধ্যক, মোট যোগফল ও রেঞ্জ",
            keywords = listOf("mean", "median", "statistics", "gor")
        ),
        ToolItem(
            id = "fraction_calc",
            category = ToolCategory.CALCULATORS,
            icon = Icons.Default.Tune,
            titleEn = "Fraction Calculator",
            titleBn = "ভগ্নাংশ ক্যালকুলেটর",
            descEn = "Add, subtract, multiply fractions with step simplification",
            descBn = "ভগ্নাংশের যোগ, বিয়োগ ও সরলীকরণ",
            keywords = listOf("fraction", "numerator", "denominator")
        ),
        ToolItem(
            id = "ratio_calc",
            category = ToolCategory.CALCULATORS,
            icon = Icons.AutoMirrored.Filled.CompareArrows,
            titleEn = "Ratio Calculator",
            titleBn = "অনুপাত ক্যালকুলেটর",
            descEn = "Solve A:B = C:D proportions and simplify ratios",
            descBn = "অনুপাত সমাধান ও সরল রূপ",
            keywords = listOf("ratio", "proportion", "scale")
        ),
        ToolItem(
            id = "gst_vat_calc",
            category = ToolCategory.CALCULATORS,
            icon = Icons.Default.ReceiptLong,
            titleEn = "GST / VAT Calculator",
            titleBn = "ভ্যাট / ট্যাক্স",
            descEn = "Add or reverse VAT/tax amount with custom rates",
            descBn = "ভ্যাট যুক্ত অথবা পৃথক করার হিসাব",
            keywords = listOf("vat", "tax", "gst", "revenue")
        ),
        ToolItem(
            id = "tip_calc",
            category = ToolCategory.CALCULATORS,
            icon = Icons.Default.CreditCard,
            titleEn = "Tip & Bill Split",
            titleBn = "টিপ ও বিল ভাগ",
            descEn = "Calculate tip percentage and split bills among friends",
            descBn = "টিপ হিসাব ও বন্ধুদের মাঝে বিল ভাগাভাগি",
            keywords = listOf("tip", "bill", "split", "restaurant")
        ),
        ToolItem(
            id = "age_calc",
            category = ToolCategory.CALCULATORS,
            icon = Icons.Default.CalendarToday,
            titleEn = "Age Calculator",
            titleBn = "বয়স ক্যালকুলেটর",
            descEn = "Exact years, months, days, and next birthday countdown",
            descBn = "বছর, মাস ও দিনে সঠিক বয়স এবং পরবর্তী জন্মদিন",
            isPopular = true,
            keywords = listOf("birthday", "years", "dob", "boyos")
        ),
        ToolItem(
            id = "date_diff_calc",
            category = ToolCategory.CALCULATORS,
            icon = Icons.Default.DateRange,
            titleEn = "Date Difference",
            titleBn = "তারিখের ব্যবধান",
            descEn = "Count exact days, weeks, and months between two dates",
            descBn = "দুইটি তারিখের মধ্যবর্তী দিন ও সময়ের হিসাব",
            keywords = listOf("duration", "between", "calendar")
        ),

        // CATEGORY 2: UNIT CONVERTER
        ToolItem(
            id = "unit_length",
            category = ToolCategory.UNIT_CONVERTER,
            icon = Icons.Default.Straighten,
            titleEn = "Length Converter",
            titleBn = "দৈর্ঘ্য রূপান্তর",
            descEn = "mm, cm, meter, km, inch, feet, yard, mile",
            descBn = "মিমি, সেমি, মিটার, কিমি, ইঞ্চি, ফুট, গজ, মাইল",
            isPopular = true,
            keywords = listOf("distance", "meter", "inch", "foot", "km")
        ),
        ToolItem(
            id = "unit_weight",
            category = ToolCategory.UNIT_CONVERTER,
            icon = Icons.Default.Scale,
            titleEn = "Weight Converter",
            titleBn = "ওজন রূপান্তর",
            descEn = "mg, gram, kg, ounce, pound, ton",
            descBn = "মিলিগ্রাম, গ্রাম, কেজি, আউন্স, পাউন্ড, টন",
            isPopular = true,
            keywords = listOf("mass", "kg", "gram", "pound", "lbs")
        ),
        ToolItem(
            id = "unit_temp",
            category = ToolCategory.UNIT_CONVERTER,
            icon = Icons.Default.Thermostat,
            titleEn = "Temperature Converter",
            titleBn = "তাপমাত্রা রূপান্তর",
            descEn = "Celsius, Fahrenheit, and Kelvin",
            descBn = "সেলসিয়াস, ফারেনহাইট ও কেলভিন",
            keywords = listOf("celsius", "fahrenheit", "kelvin", "heat")
        ),
        ToolItem(
            id = "unit_area",
            category = ToolCategory.UNIT_CONVERTER,
            icon = Icons.Default.GridOn,
            titleEn = "Area Converter",
            titleBn = "ক্ষেত্রফল রূপান্তর",
            descEn = "sq meter, sq feet, acre, hectare, sq km",
            descBn = "বর্গমিটার, বর্গফুট, একর, হেক্টর, বর্গকিমি",
            keywords = listOf("area", "sqft", "acre", "hectare")
        ),
        ToolItem(
            id = "unit_volume",
            category = ToolCategory.UNIT_CONVERTER,
            icon = Icons.Default.WaterDrop,
            titleEn = "Volume Converter",
            titleBn = "আয়তন রূপান্তর",
            descEn = "ml, liter, gallon, cup, fl oz, cubic meter",
            descBn = "মিলি, লিটার, গ্যালন, কাপ, তরল আউন্স",
            keywords = listOf("liter", "liquid", "gallon", "ml")
        ),
        ToolItem(
            id = "unit_speed",
            category = ToolCategory.UNIT_CONVERTER,
            icon = Icons.Default.Speed,
            titleEn = "Speed Converter",
            titleBn = "গতিবেগ রূপান্তর",
            descEn = "km/h, mph, m/s, knot, ft/s",
            descBn = "কিমি/ঘণ্টা, মাইল/ঘণ্টা, মি/সেকেন্ড, নট",
            keywords = listOf("velocity", "kmh", "mph", "fast")
        ),
        ToolItem(
            id = "unit_time",
            category = ToolCategory.UNIT_CONVERTER,
            icon = Icons.Default.AvTimer,
            titleEn = "Time Converter",
            titleBn = "সময় রূপান্তর",
            descEn = "ms, seconds, minutes, hours, days, weeks",
            descBn = "মিলিসেকেন্ড, সেকেন্ড, মিনিট, ঘণ্টা, দিন",
            keywords = listOf("hour", "minute", "second", "day")
        ),
        ToolItem(
            id = "unit_data",
            category = ToolCategory.UNIT_CONVERTER,
            icon = Icons.Default.DataUsage,
            titleEn = "Digital Data Converter",
            titleBn = "ডিজিটাল ডাটা রূপান্তর",
            descEn = "bit, byte, KB, MB, GB, TB, PB",
            descBn = "বিট, বাইট, কিলোবাইট, মেগাবাইট, গিগাবাইট, টেরাবাইট",
            keywords = listOf("storage", "mb", "gb", "tb", "byte")
        ),
        ToolItem(
            id = "unit_pressure",
            category = ToolCategory.UNIT_CONVERTER,
            icon = Icons.Default.Compress,
            titleEn = "Pressure Converter",
            titleBn = "চাপ রূপান্তর",
            descEn = "Pascal, bar, psi, atmosphere (atm), mmHg",
            descBn = "প্যাসকেল, বার, পিএসআই, বায়ুমণ্ডলীয় চাপ",
            keywords = listOf("psi", "bar", "pascal", "atm")
        ),
        ToolItem(
            id = "unit_energy",
            category = ToolCategory.UNIT_CONVERTER,
            icon = Icons.Default.ElectricBolt,
            titleEn = "Energy Converter",
            titleBn = "শক্তি রূপান্তর",
            descEn = "Joule, kilojoule, calorie, kcal, kWh, BTU",
            descBn = "জুল, ক্যালরি, কিলোওয়াট-আওয়ার, বিটিইউ",
            keywords = listOf("joule", "calorie", "kwh", "power")
        ),

        // CATEGORY 3: DATE & TIME
        ToolItem(
            id = "stopwatch",
            category = ToolCategory.DATE_TIME,
            icon = Icons.Default.Timer,
            titleEn = "Stopwatch",
            titleBn = "স্টপওয়াচ",
            descEn = "Millisecond-precise stopwatch with lap recorder",
            descBn = "ল্যাপ রেকর্ডসহ সুনির্দিষ্ট স্টপওয়াচ",
            isPopular = true,
            keywords = listOf("lap", "clock", "seconds", "timing")
        ),
        ToolItem(
            id = "countdown_timer",
            category = ToolCategory.DATE_TIME,
            icon = Icons.Default.HourglassEmpty,
            titleEn = "Countdown Timer",
            titleBn = "কাউন্টডাউন টাইমার",
            descEn = "Custom timer with circular progress and alerts",
            descBn = "প্রোগ্রেস বার ও অ্যালার্টসহ টাইমার",
            isPopular = true,
            keywords = listOf("timer", "alarm", "countdown", "interval")
        ),
        ToolItem(
            id = "add_days",
            category = ToolCategory.DATE_TIME,
            icon = Icons.Default.CalendarMonth,
            titleEn = "Add Days to Date",
            titleBn = "তারিখ যোগ করুন",
            descEn = "Calculate the target date after adding days or weeks",
            descBn = "কোনো তারিখের সাথে নির্দিষ্ট দিন যোগ করার ফলাফল",
            keywords = listOf("plus", "future", "schedule")
        ),
        ToolItem(
            id = "subtract_days",
            category = ToolCategory.DATE_TIME,
            icon = Icons.Default.CalendarToday,
            titleEn = "Subtract Days from Date",
            titleBn = "তারিখ বিয়োগ করুন",
            descEn = "Find the past date by subtracting days or weeks",
            descBn = "কোনো তারিখ থেকে নির্দিষ্ট দিন বাদ দেওয়ার ফলাফল",
            keywords = listOf("minus", "past", "history")
        ),
        ToolItem(
            id = "world_clock",
            category = ToolCategory.DATE_TIME,
            icon = Icons.Default.Public,
            titleEn = "World Clock",
            titleBn = "বিশ্ব ঘড়ি",
            descEn = "Live timezones for Dhaka, London, New York, Tokyo, and more",
            descBn = "ঢাকা, লন্ডন, নিউইয়র্ক, টোকিওসহ আন্তর্জাতিক সময়",
            keywords = listOf("timezone", "utc", "gmt", "dhaka")
        ),
        ToolItem(
            id = "days_until",
            category = ToolCategory.DATE_TIME,
            icon = Icons.Default.CalendarMonth,
            titleEn = "Days Until Date",
            titleBn = "দিন গণনা (কাউন্টডাউন)",
            descEn = "Track upcoming exams, Eid, New Year, or birthdays",
            descBn = "পরীক্ষা, ঈদ বা যেকোনো বিশেষ দিনের দিন গণনা",
            keywords = listOf("countdown", "event", "exam", "eid")
        ),

        // CATEGORY 4: QR & SCANNER
        ToolItem(
            id = "qr_generator",
            category = ToolCategory.QR_SCANNER,
            icon = Icons.Default.QrCode,
            titleEn = "QR Code Generator",
            titleBn = "কিউআর কোড তৈরি",
            descEn = "Create QR codes for Text, URL, Phone, Email, and Wi-Fi",
            descBn = "টেক্সট, লিংক, ফোন, ইমেইল ও ওয়াইফাই এর কিউআর বানান",
            isPopular = true,
            keywords = listOf("qr", "wifi", "barcode", "create")
        ),
        ToolItem(
            id = "qr_scanner",
            category = ToolCategory.QR_SCANNER,
            icon = Icons.Default.QrCodeScanner,
            titleEn = "QR & Barcode Scanner",
            titleBn = "কিউআর ও বারকোড স্ক্যানার",
            descEn = "Scan QR codes and barcodes from camera or gallery",
            descBn = "ক্যামেরা বা গ্যালারি থেকে কিউআর ও বারকোড স্ক্যান",
            isPopular = true,
            keywords = listOf("scan", "read", "detect", "reader")
        ),

        // CATEGORY 5: TEXT TOOLS
        ToolItem(
            id = "text_counter",
            category = ToolCategory.TEXT_TOOLS,
            icon = Icons.Default.TextFields,
            titleEn = "Text & Word Counter",
            titleBn = "শব্দ ও বর্ণ গণক",
            descEn = "Count characters, words, sentences, lines, and reading time",
            descBn = "বর্ণ, শব্দ, বাক্য, লাইন এবং পড়ার আনুমানিক সময়",
            isPopular = true,
            keywords = listOf("words", "characters", "length", "reading")
        ),
        ToolItem(
            id = "case_converter",
            category = ToolCategory.TEXT_TOOLS,
            icon = Icons.Default.Title,
            titleEn = "Case Converter",
            titleBn = "কেস কনভার্টার",
            descEn = "UPPERCASE, lowercase, Title Case, camelCase, snake_case",
            descBn = "বড় হাতের, ছোট হাতের, টাইটেল ও প্রোগ্রামিং কেস",
            keywords = listOf("upper", "lower", "capital", "case")
        ),
        ToolItem(
            id = "remove_spaces",
            category = ToolCategory.TEXT_TOOLS,
            icon = Icons.Default.CleaningServices,
            titleEn = "Remove Extra Spaces",
            titleBn = "অতিরিক্ত স্পেস মোছা",
            descEn = "Trim duplicate spaces, blank lines, and trailing tabs",
            descBn = "অপ্রয়োজনীয় অতিরিক্ত ফাঁকা স্থান ও ফাঁকা লাইন পরিষ্কার",
            keywords = listOf("trim", "whitespace", "format")
        ),
        ToolItem(
            id = "remove_duplicates",
            category = ToolCategory.TEXT_TOOLS,
            icon = Icons.Default.DeleteSweep,
            titleEn = "Duplicate Line Remover",
            titleBn = "ডুপ্লিকেট লাইন রিমুভার",
            descEn = "Remove repeated lines while preserving text order",
            descBn = "পুনরাবৃত্তি হওয়া লাইনগুলো স্বয়ংক্রিয়ভাবে বাদ দিন",
            keywords = listOf("unique", "filter", "lines")
        ),
        ToolItem(
            id = "text_sorter",
            category = ToolCategory.TEXT_TOOLS,
            icon = Icons.AutoMirrored.Filled.Sort,
            titleEn = "Text Sorter",
            titleBn = "টেক্সট সর্টার",
            descEn = "Sort lines alphabetically A-Z, Z-A, or by line length",
            descBn = "বর্ণানুক্রমে (A-Z, Z-A) অথবা দৈর্ঘ্যের ভিত্তিতে সাজান",
            keywords = listOf("sort", "alphabetical", "order")
        ),
        ToolItem(
            id = "text_cleaner",
            category = ToolCategory.TEXT_TOOLS,
            icon = Icons.Default.CleanHands,
            titleEn = "Text Cleaner",
            titleBn = "টেক্সট ক্লিনার",
            descEn = "Strip HTML tags, numbers, punctuation, or empty lines",
            descBn = "এইচটিএমএল ট্যাগ, সংখ্যা ও বিরামচিহ্ন অপসারণ",
            keywords = listOf("strip", "html", "sanitize")
        ),

        // CATEGORY 6: EVERYDAY TOOLS
        ToolItem(
            id = "notes",
            category = ToolCategory.EVERYDAY_TOOLS,
            icon = Icons.Default.EditNote,
            titleEn = "Quick Notes",
            titleBn = "কুইক নোটস",
            descEn = "Offline notes with search, color tags, and auto-save",
            descBn = "অফলাইন নোটপ্যাড, স্বয়ংক্রিয় সেভ ও সার্চ সুবিধা",
            isPopular = true,
            keywords = listOf("note", "pad", "write", "memo")
        ),
        ToolItem(
            id = "todo_list",
            category = ToolCategory.EVERYDAY_TOOLS,
            icon = Icons.Default.Checklist,
            titleEn = "To-Do List",
            titleBn = "টু-ডু লিস্ট",
            descEn = "Manage tasks, check off completed items, and set priorities",
            descBn = "দৈনন্দিন কাজের তালিকা ও অগ্রাধিকার নির্ধারণ",
            isPopular = true,
            keywords = listOf("task", "todo", "done", "work")
        ),
        ToolItem(
            id = "shopping_list",
            category = ToolCategory.EVERYDAY_TOOLS,
            icon = Icons.Default.ShoppingCart,
            titleEn = "Shopping List",
            titleBn = "বাজারের তালিকা",
            descEn = "Item checklist with quantities, price calculator, and totals",
            descBn = "পরিমাণ ও মোট খরচ হিসাবসহ কেনাকাটার তালিকা",
            isPopular = true,
            keywords = listOf("grocery", "market", "items", "bajar")
        ),
        ToolItem(
            id = "password_gen",
            category = ToolCategory.EVERYDAY_TOOLS,
            icon = Icons.Default.Lock,
            titleEn = "Password Generator",
            titleBn = "পাসওয়ার্ড জেনারেটর",
            descEn = "Secure random passwords with strength meter",
            descBn = "নিরাপদ ও শক্তিশালী পাসওয়ার্ড তৈরির টুল",
            keywords = listOf("security", "random", "protect")
        ),
        ToolItem(
            id = "random_number",
            category = ToolCategory.EVERYDAY_TOOLS,
            icon = Icons.Default.Pin,
            titleEn = "Random Number Generator",
            titleBn = "র‍্যান্ডম নম্বর",
            descEn = "Roll dice, generate lucky numbers, and random ranges",
            descBn = "নির্দিষ্ট রেঞ্জের মধ্যে এলোমেলো সংখ্যা নির্ণয়",
            keywords = listOf("dice", "rng", "chance", "lottery")
        ),
        ToolItem(
            id = "random_picker",
            category = ToolCategory.EVERYDAY_TOOLS,
            icon = Icons.Default.VolunteerActivism,
            titleEn = "Random Picker (Decision)",
            titleBn = "র‍্যান্ডম সিলেক্টর",
            descEn = "Enter choices and pick a winner randomly with celebration",
            descBn = "অপশনগুলোর মধ্যে থেকে একটি লটারির মতো বেছে নিন",
            keywords = listOf("choose", "decision", "picker", "spin")
        ),
        ToolItem(
            id = "color_picker",
            category = ToolCategory.EVERYDAY_TOOLS,
            icon = Icons.Default.ColorLens,
            titleEn = "Color Picker & Palette",
            titleBn = "কালার পিকার ও প্যালেট",
            descEn = "HEX, RGB, HSL viewer with copyable color codes",
            descBn = "হেক্স, আরজিবি কালার কোড দেখা ও কপি করা",
            keywords = listOf("hex", "rgb", "design", "palette")
        ),
        ToolItem(
            id = "simple_memo",
            category = ToolCategory.EVERYDAY_TOOLS,
            icon = Icons.AutoMirrored.Filled.TextSnippet,
            titleEn = "Instant Scratchpad",
            titleBn = "স্ক্র্যাচপ্যাড / মেমো",
            descEn = "Fast persistent single-board scratchpad for quick paste",
            descBn = "ঝটপট লেখা বা পেস্ট করে রাখার জন্য দ্রুত মেমো",
            keywords = listOf("scratchpad", "paste", "clipboard", "quick")
        ),
        ToolItem(
            id = "checklist",
            category = ToolCategory.EVERYDAY_TOOLS,
            icon = Icons.AutoMirrored.Filled.List,
            titleEn = "Routine Checklist",
            titleBn = "রুটিন চেকলিস্ট",
            descEn = "Reusable checklists for travel, camping, and daily habits",
            descBn = "ভ্রমণ বা কাজের জন্য পুনর্ব্যবহারযোগ্য চেকলিস্ট",
            keywords = listOf("pack", "travel", "routine", "habit")
        ),

        // CATEGORY 7: BANGLADESH TOOLS
        ToolItem(
            id = "bdt_cash",
            category = ToolCategory.BANGLADESH_TOOLS,
            icon = Icons.Default.Money,
            titleEn = "BDT Cash Note Counter",
            titleBn = "টাকা নোট গণক",
            descEn = "Calculate total taka by count of 1000, 500, 200, 100 notes",
            descBn = "১০০০, ৫০০, ২০০, ১০০ টাকার নোট সংখ্যা থেকে মোট টাকার হিসাব",
            isPopular = true,
            keywords = listOf("taka", "bdt", "notes", "cash", "bank")
        ),
        ToolItem(
            id = "bangla_number",
            category = ToolCategory.BANGLADESH_TOOLS,
            icon = Icons.Default.TextFields,
            titleEn = "Bangla ↔ English Numbers",
            titleBn = "বাংলা ↔ ইংরেজি সংখ্যা",
            descEn = "Convert digits and numbers to spoken Bangla words (লাখ, কোটি)",
            descBn = "সংখ্যা রূপান্তর এবং বাংলায় কথায় রূপান্তর (লাখ, কোটি)",
            isPopular = true,
            keywords = listOf("digit", "kotha", "lakh", "crore", "shongkha")
        ),
        ToolItem(
            id = "ssc_gpa",
            category = ToolCategory.BANGLADESH_TOOLS,
            icon = Icons.Default.School,
            titleEn = "SSC GPA Calculator",
            titleBn = "এসএসসি জিপিএ ক্যালকুলেটর",
            descEn = "Bangladesh Education Board grading scale with 4th subject logic",
            descBn = "৪র্থ বিষয়ের নিয়মসমেত বাংলাদেশ বোর্ডের এসএসসি জিপিএ",
            keywords = listOf("ssc", "gpa", "grade", "board", "result")
        ),
        ToolItem(
            id = "hsc_gpa",
            category = ToolCategory.BANGLADESH_TOOLS,
            icon = Icons.Default.School,
            titleEn = "HSC GPA Calculator",
            titleBn = "এইচএসসি জিপিএ ক্যালকুলেটর",
            descEn = "Grade points and overall GPA calculation for HSC students",
            descBn = "এইচএসসি বিষয়ের গ্রেড পয়েন্ট ও চূড়ান্ত জিপিএ",
            keywords = listOf("hsc", "gpa", "grade", "college", "result")
        ),
        ToolItem(
            id = "land_converter",
            category = ToolCategory.BANGLADESH_TOOLS,
            icon = Icons.Default.Landscape,
            titleEn = "BD Land Measurement",
            titleBn = "জমি পরিমাপ কনভার্টার",
            descEn = "Shatak / Decimal, Katha, Bigha, Kani, Gonda, Sq Ft, Acre",
            descBn = "শতাংশ, কাঠা, বিঘা, কানি, গণ্ডা, বর্গফুট ও একর রূপান্তর",
            isPopular = true,
            keywords = listOf("shatak", "katha", "bigha", "kani", "jomi")
        ),
        ToolItem(
            id = "bd_units",
            category = ToolCategory.BANGLADESH_TOOLS,
            icon = Icons.Default.AccountBalance,
            titleEn = "BD Traditional Weight & Price",
            titleBn = "মণ, সের, তোলা ও স্বর্ণের দর",
            descEn = "Mon, Ser, Chhatak, and Tola for gold/silver with price rates",
            descBn = "মণ, সের, ছটাক ও স্বর্ণ-রুপার তোলা হিসাব এবং কেজি মূল্য",
            keywords = listOf("mon", "ser", "tola", "gold", "chhatak")
        ),

        // CATEGORY 8: FILE & DOCUMENT UTILITIES
        ToolItem(
            id = "text_to_pdf",
            category = ToolCategory.FILE_TOOLS,
            icon = Icons.Default.Description,
            titleEn = "Text to PDF",
            titleBn = "টেক্সট থেকে পিডিএফ",
            descEn = "Type or paste text and export a clean PDF document",
            descBn = "যেকোনো লেখা বা নোট সুন্দর পিডিএফ ফাইলে রূপান্তর",
            isPopular = true,
            keywords = listOf("pdf", "export", "doc", "text")
        ),
        ToolItem(
            id = "image_to_pdf",
            category = ToolCategory.FILE_TOOLS,
            icon = Icons.Default.Image,
            titleEn = "Image to PDF",
            titleBn = "ছবি থেকে পিডিএফ",
            descEn = "Convert a single photo into a high-quality PDF page",
            descBn = "গ্যালারির ছবি থেকে সরাসরি পিডিএফ ফাইল তৈরি",
            isPopular = true,
            keywords = listOf("photo", "picture", "pdf", "scan")
        ),
        ToolItem(
            id = "multi_image_to_pdf",
            category = ToolCategory.FILE_TOOLS,
            icon = Icons.Default.PhotoLibrary,
            titleEn = "Multiple Images to PDF",
            titleBn = "একাধিক ছবি থেকে পিডিএফ",
            descEn = "Combine multiple pictures into a single multi-page PDF",
            descBn = "একের অধিক ছবি একসাথে করে একটি পিডিএফ বুক তৈরি",
            keywords = listOf("album", "combine", "pages", "merge")
        ),
        ToolItem(
            id = "file_name_gen",
            category = ToolCategory.FILE_TOOLS,
            icon = Icons.Default.Title,
            titleEn = "File Name Generator",
            titleBn = "ফাইল নাম জেনারেটর",
            descEn = "Sanitized timestamped, slugified, and structured filenames",
            descBn = "প্রফেশনাল ও নিরাপদ ফাইল নামকরণ ফরম্যাট",
            keywords = listOf("slug", "sanitize", "rename", "timestamp")
        ),
        ToolItem(
            id = "file_info",
            category = ToolCategory.FILE_TOOLS,
            icon = Icons.Default.Info,
            titleEn = "File Inspector",
            titleBn = "ফাইল তথ্য ও বিবরণী",
            descEn = "Inspect size, MIME type, modified time, and path of any file",
            descBn = "যেকোনো ফাইলের সঠিক সাইজ, এক্সটেনশন ও বিবরণী",
            keywords = listOf("details", "mime", "size", "bytes")
        )
    )

    fun getTool(id: String): ToolItem? = allTools.find { it.id == id }

    fun getByCategory(category: ToolCategory): List<ToolItem> =
        allTools.filter { it.category == category }
}
