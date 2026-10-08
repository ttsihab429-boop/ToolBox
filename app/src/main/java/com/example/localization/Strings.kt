package com.example.localization

object Strings {
    fun appName(lang: AppLanguage) = "ToolBox"

    fun tagline(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "All Your Everyday Tools, In One Place."
        AppLanguage.BANGLA -> "আপনার প্রতিদিনের প্রয়োজনীয় সব টুলস, এক জায়গায়।"
    }

    fun devCredit(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Developed by Maybe Sihab"
        AppLanguage.BANGLA -> "ডেভেলপার: Maybe Sihab"
    }

    fun devContact(lang: AppLanguage) = "+8801646864645"

    // Navigation
    fun navHome(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Home"
        AppLanguage.BANGLA -> "হোম"
    }
    fun navTools(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Tools"
        AppLanguage.BANGLA -> "টুলস"
    }
    fun navFavorites(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Favorites"
        AppLanguage.BANGLA -> "পছন্দের"
    }
    fun navSettings(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Settings"
        AppLanguage.BANGLA -> "সেটিংস"
    }

    // Common
    fun searchPlaceholder(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Search tools…"
        AppLanguage.BANGLA -> "টুলস খুঁজুন…"
    }
    fun recentlyUsed(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Recently Used"
        AppLanguage.BANGLA -> "সম্প্রতি ব্যবহৃত"
    }
    fun popularTools(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Popular Tools"
        AppLanguage.BANGLA -> "জনপ্রিয় টুলস"
    }
    fun categories(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Categories"
        AppLanguage.BANGLA -> "ক্যাটাগরি"
    }
    fun clear(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Clear"
        AppLanguage.BANGLA -> "মুছুন"
    }
    fun copy(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Copy"
        AppLanguage.BANGLA -> "কপি"
    }
    fun copied(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Copied to clipboard"
        AppLanguage.BANGLA -> "ক্লিপবোর্ডে কপি হয়েছে"
    }
    fun share(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Share"
        AppLanguage.BANGLA -> "শেয়ার"
    }
    fun calculate(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Calculate"
        AppLanguage.BANGLA -> "হিসাব করুন"
    }
    fun convert(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Convert"
        AppLanguage.BANGLA -> "রূপান্তর"
    }
    fun result(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Result"
        AppLanguage.BANGLA -> "ফলাফল"
    }
    fun swap(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Swap"
        AppLanguage.BANGLA -> "অদলবদল"
    }
    fun save(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Save"
        AppLanguage.BANGLA -> "সংরক্ষণ"
    }
    fun delete(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Delete"
        AppLanguage.BANGLA -> "মুছুন"
    }
    fun edit(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Edit"
        AppLanguage.BANGLA -> "সম্পাদনা"
    }
    fun add(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Add"
        AppLanguage.BANGLA -> "যোগ করুন"
    }
    fun cancel(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Cancel"
        AppLanguage.BANGLA -> "বাতিল"
    }
    fun confirm(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Confirm"
        AppLanguage.BANGLA -> "নিশ্চিত"
    }
    fun reset(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Reset"
        AppLanguage.BANGLA -> "রিসেট"
    }
    fun noFavoritesTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "No Favorites Yet"
        AppLanguage.BANGLA -> "এখনো পছন্দের কোনো টুল নেই"
    }
    fun noFavoritesSubtitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Tap the heart icon on any tool card to add it to your quick favorites."
        AppLanguage.BANGLA -> "যেকোনো টুলের কার্ডে হার্ট আইকনে ট্যাপ করে পছন্দের তালিকায় যুক্ত করুন।"
    }
    fun noResults(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "No matching tools found"
        AppLanguage.BANGLA -> "কোনো টুল খুঁজে পাওয়া যায়নি"
    }

    // Categories
    fun catCalculators(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Calculators"
        AppLanguage.BANGLA -> "ক্যালকুলেটর"
    }
    fun catConverters(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Unit Converter"
        AppLanguage.BANGLA -> "ইউনিট কনভার্টার"
    }
    fun catDateTime(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Date & Time"
        AppLanguage.BANGLA -> "তারিখ ও সময়"
    }
    fun catQrScanner(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "QR & Scanner"
        AppLanguage.BANGLA -> "কিউআর ও স্ক্যানার"
    }
    fun catTextTools(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Text Tools"
        AppLanguage.BANGLA -> "টেক্সট টুলস"
    }
    fun catEveryday(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Everyday Tools"
        AppLanguage.BANGLA -> "দৈনন্দিন টুলস"
    }
    fun catBangladesh(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Bangladesh Tools"
        AppLanguage.BANGLA -> "বাংলাদেশ টুলস"
    }
    fun catFiles(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "File & Document"
        AppLanguage.BANGLA -> "ফাইল ও ডকুমেন্ট"
    }

    // Settings
    fun appearance(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Appearance"
        AppLanguage.BANGLA -> "থিম ও ডিসপ্লে"
    }
    fun themeDark(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Dark"
        AppLanguage.BANGLA -> "ডার্ক"
    }
    fun themeLight(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Light"
        AppLanguage.BANGLA -> "লাইট"
    }
    fun themeSystem(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "System Default"
        AppLanguage.BANGLA -> "সিস্টেম ডিফল্ট"
    }
    fun language(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Language"
        AppLanguage.BANGLA -> "ভাষা"
    }
    fun preferences(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Preferences"
        AppLanguage.BANGLA -> "পছন্দসমূহ"
    }
    fun hapticFeedback(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Haptic Feedback"
        AppLanguage.BANGLA -> "হ্যাপটিক ভাইব্রেশন"
    }
    fun enableAnimations(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Smooth Animations"
        AppLanguage.BANGLA -> "মসৃণ অ্যানিমেশন"
    }
    fun trackRecents(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Track Recently Used"
        AppLanguage.BANGLA -> "সম্প্রতি ব্যবহৃত ট্র্যাক করুন"
    }
    fun dataManagement(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Data & Storage"
        AppLanguage.BANGLA -> "ডাটা ও স্টোরেজ"
    }
    fun clearRecents(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Clear Recent Tools"
        AppLanguage.BANGLA -> "সম্প্রতি ব্যবহৃত টুল ক্লিয়ার"
    }
    fun clearFavorites(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Clear Favorites"
        AppLanguage.BANGLA -> "পছন্দের তালিকা ক্লিয়ার"
    }
    fun clearNotes(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Clear Notes & Tasks"
        AppLanguage.BANGLA -> "নোট ও টাস্ক ক্লিয়ার"
    }
    fun clearAllData(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Reset All App Data"
        AppLanguage.BANGLA -> "অ্যাপের সব ডাটা রিসেট"
    }
    fun aboutApp(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "About ToolBox"
        AppLanguage.BANGLA -> "টুলবক্স পরিচিতি"
    }
    fun privacyInfo(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Privacy & Security"
        AppLanguage.BANGLA -> "গোপনীয়তা ও নিরাপত্তা"
    }
    fun privacyDesc(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "ToolBox is 100% offline-first. Your notes, calculations, files, and settings stay private on your device. No analytics, no ads, no trackers, and no internet account required."
        AppLanguage.BANGLA -> "টুলবক্স সম্পূর্ণ অফলাইন-ফার্স্ট। আপনার নোট, হিসাব, ফাইল ও সেটিংস শুধুমাত্র আপনার ডিভাইসেই সংরক্ষিত থাকে। কোনো ট্র্যাকিং বা ইন্টারনেট অ্যাকাউন্টের প্রয়োজন নেই।"
    }
}
