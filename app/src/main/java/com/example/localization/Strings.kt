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
    fun navBusiness(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Business"
        AppLanguage.BANGLA -> "ব্যবসা"
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
    fun ok(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "OK"
        AppLanguage.BANGLA -> "ঠিক আছে"
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
    fun catQuickScan(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Quick Scan"
        AppLanguage.BANGLA -> "কুইক স্ক্যান"
    }
    fun catDeviceTools(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Device Tools"
        AppLanguage.BANGLA -> "ডিভাইস তথ্য"
    }
    fun catHardwareCheckup(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Hardware Checkup"
        AppLanguage.BANGLA -> "হার্ডওয়্যার পরীক্ষা"
    }
    fun catNetworkTools(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Network Tools"
        AppLanguage.BANGLA -> "নেটওয়ার্ক টুলস"
    }
    fun catPrivacyCenter(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Privacy Center"
        AppLanguage.BANGLA -> "প্রাইভেসি সেন্টার"
    }
    fun catAppInspector(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "App Inspector"
        AppLanguage.BANGLA -> "অ্যাপ বিশ্লেষক"
    }
    fun catPerformanceMonitor(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Performance Monitor"
        AppLanguage.BANGLA -> "পারফরম্যান্স মনিটর"
    }
    fun catWifiQr(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Wi-Fi QR Tools"
        AppLanguage.BANGLA -> "ওয়াইফাই কিউআর"
    }
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

    // Business Manager Strings
    fun bizDashboard(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Dashboard"
        AppLanguage.BANGLA -> "ড্যাশবোর্ড"
    }
    fun bizCustomers(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Customers"
        AppLanguage.BANGLA -> "কাস্টমার"
    }
    fun bizSuppliers(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Suppliers"
        AppLanguage.BANGLA -> "মহাজন / সাপ্লায়ার"
    }
    fun bizInventory(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Inventory"
        AppLanguage.BANGLA -> "স্টক ও পণ্য"
    }
    fun bizAddCustomer(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Add Customer"
        AppLanguage.BANGLA -> "নতুন কাস্টমার যোগ"
    }
    fun bizAddSupplier(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Add Supplier"
        AppLanguage.BANGLA -> "নতুন সাপ্লায়ার যোগ"
    }
    fun bizAddProduct(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Add Product"
        AppLanguage.BANGLA -> "নতুন পণ্য যোগ"
    }
    fun bizRecordPayment(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Record Payment"
        AppLanguage.BANGLA -> "পেমেন্ট গ্রহণ / প্রদান"
    }
    fun bizTotalDue(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Total Due"
        AppLanguage.BANGLA -> "মোট বাকি"
    }
    fun bizTotalPayable(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Total Payable"
        AppLanguage.BANGLA -> "মোট পাওনা"
    }
    fun bizStockQuantity(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Stock Quantity"
        AppLanguage.BANGLA -> "মজুদ পরিমাণ"
    }
    fun bizLowStockAlert(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Low Stock"
        AppLanguage.BANGLA -> "কম স্টক"
    }
    fun bizOutOfStock(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Out of Stock"
        AppLanguage.BANGLA -> "স্টক শেষ"
    }
    fun bizAdjustStock(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Adjust Stock"
        AppLanguage.BANGLA -> "স্টক সমন্বয়"
    }
    fun bizCloudSync(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Cloud Backup & Sync"
        AppLanguage.BANGLA -> "ক্লাউড ব্যাকআপ ও সিঙ্ক"
    }
    fun bizSyncNow(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Sync Now"
        AppLanguage.BANGLA -> "এখনই সিঙ্ক করুন"
    }
    fun bizLastSynced(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Last Synced:"
        AppLanguage.BANGLA -> "সর্বশেষ সিঙ্ক:"
    }
    fun bizNeverSynced(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Never"
        AppLanguage.BANGLA -> "কখনও হয়নি"
    }
    fun bizStatusSynced(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Synced"
        AppLanguage.BANGLA -> "সিঙ্ক সম্পন্ন"
    }
    fun bizStatusSyncing(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Syncing..."
        AppLanguage.BANGLA -> "সিঙ্ক হচ্ছে..."
    }
    fun bizStatusOffline(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Offline (Queued)"
        AppLanguage.BANGLA -> "অফলাইন (কিউতে আছে)"
    }
    fun bizStatusSetupRequired(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Setup Required"
        AppLanguage.BANGLA -> "সেটআপ প্রয়োজন"
    }
    fun bizStatusAuthRequired(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Sign-in Required"
        AppLanguage.BANGLA -> "সাইন ইন প্রয়োজন"
    }
    fun bizSyncDetails(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Sync & Backup Status"
        AppLanguage.BANGLA -> "সিঙ্ক ও ব্যাকআপ বিবরণ"
    }
    fun bizPendingChanges(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Pending changes queued"
        AppLanguage.BANGLA -> "অপেক্ষমান পরিবর্তন"
    }

    // Business Reports & Export
    fun bizReports(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Reports"
        AppLanguage.BANGLA -> "রিপোর্ট"
    }
    fun bizExportPdf(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Export PDF"
        AppLanguage.BANGLA -> "পিডিএফ এক্সপোর্ট"
    }
    fun bizExportJpg(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Export JPG"
        AppLanguage.BANGLA -> "জেপিজি এক্সপোর্ট"
    }
    fun bizExportCsv(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Export CSV"
        AppLanguage.BANGLA -> "সিএসভি এক্সপোর্ট"
    }
    fun bizDailyReport(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Daily Report"
        AppLanguage.BANGLA -> "দৈনিক রিপোর্ট"
    }
    fun bizMonthlyReport(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Monthly Report"
        AppLanguage.BANGLA -> "চলতি মাস"
    }
    fun bizPrevMonthReport(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Previous Month"
        AppLanguage.BANGLA -> "গত মাস"
    }
    fun bizCustomRange(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Custom Range"
        AppLanguage.BANGLA -> "কাস্টম সময়কাল"
    }
    fun bizSelectBusiness(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Select Business"
        AppLanguage.BANGLA -> "ব্যবসা নির্বাচন করুন"
    }
    fun bizShareReport(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Share Report"
        AppLanguage.BANGLA -> "রিপোর্ট শেয়ার করুন"
    }
    fun bizSaveToDevice(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Save to Device"
        AppLanguage.BANGLA -> "ডিভাইসে সংরক্ষণ"
    }
    fun bizNoTransactionsFound(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "No transactions in selected period"
        AppLanguage.BANGLA -> "নির্বাচিত সময়কালে কোনো লেনদেন নেই"
    }
    fun bizExportSuccess(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Report exported successfully"
        AppLanguage.BANGLA -> "রিপোর্ট সফলভাবে এক্সপোর্ট হয়েছে"
    }
    fun bizExportFailed(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Failed to export report"
        AppLanguage.BANGLA -> "রিপোর্ট এক্সপোর্ট ব্যর্থ হয়েছে"
    }
}
