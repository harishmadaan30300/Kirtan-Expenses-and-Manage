package com.example.ui.util

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AppLanguage

enum class BookingCategory(
    val id: String,
    val hindiTitle: String,
    val englishTitle: String,
    val hinglishTitle: String,
    val icon: String,
    val descriptionHindi: String,
    val descriptionEnglish: String,
    val badgeColor: Color = Color(0xFFD97706)
) {
    GARDEN_HALL(
        id = "garden_hall",
        hindiTitle = "Garden/Hall (गार्डन / हॉल)",
        englishTitle = "Garden / Hall",
        hinglishTitle = "Garden/Hall",
        icon = "🏛️",
        descriptionHindi = "मैरिज गार्डन, बैंक्वेट हॉल, धर्मशाला या स्थल बुकिंग",
        descriptionEnglish = "Banquet hall, lawn, marriage garden or venue booking",
        badgeColor = Color(0xFF2563EB)
    ),
    SINGERS(
        id = "singers",
        hindiTitle = "Singers (गायक / कलाकार)",
        englishTitle = "Singers",
        hinglishTitle = "Singers",
        icon = "🎤",
        descriptionHindi = "भजन गायक, मुख्य कलाकार, सहयोगी एवं भजन मंडली",
        descriptionEnglish = "Bhajan singers, lead artists, choir & troupe",
        badgeColor = Color(0xFFD97706)
    ),
    MUSICIAN(
        id = "musician",
        hindiTitle = "Musician (संगीतकार / वादक)",
        englishTitle = "Musician",
        hinglishTitle = "Musician",
        icon = "🪕",
        descriptionHindi = "ढोलक, हारमोनियम, ऑक्टोपैड, तबला व बांसुरी वादक",
        descriptionEnglish = "Harmonium, dholak, octapad, tabla & instrumentalists",
        badgeColor = Color(0xFF7C3AED)
    ),
    SOUND(
        id = "sound",
        hindiTitle = "Sound (साउंड सिस्टम)",
        englishTitle = "Sound",
        hinglishTitle = "Sound",
        icon = "🔊",
        descriptionHindi = "माइक, लाइन अरे स्पीकर्स, साउंड ऑपरेटर, मिक्सर",
        descriptionEnglish = "Line-array speakers, mics, audio console & monitors",
        badgeColor = Color(0xFF059669)
    ),
    SHRINGAR(
        id = "shringar",
        hindiTitle = "Shringar (श्रृंगार)",
        englishTitle = "Shringar",
        hinglishTitle = "Shringar",
        icon = "🌸",
        descriptionHindi = "ठाकुर जी व बाबा का दिव्य श्रृंगार, पोशाक, माला व इत्र",
        descriptionEnglish = "Deity divine shringar, garments, fresh garlands & attar",
        badgeColor = Color(0xFFDB2777)
    ),
    LIGHT(
        id = "light",
        hindiTitle = "Light (लाइट व्यवस्था)",
        englishTitle = "Light",
        hinglishTitle = "Light",
        icon = "💡",
        descriptionHindi = "फोकस लाइट, स्टेज लाइटिंग, सजावटी लाइट्स व जनरेटर",
        descriptionEnglish = "Stage wash lights, sharpy, ambient focus & generator",
        badgeColor = Color(0xFFEAB308)
    ),
    DECORATION(
        id = "decoration",
        hindiTitle = "Decoration (डेकोरेशन / सजावट)",
        englishTitle = "Decoration",
        hinglishTitle = "Decoration",
        icon = "🌺",
        descriptionHindi = "ताजे फूलों की सजावट, पंडाल, प्रवेश द्वार, गुब्बारे",
        descriptionEnglish = "Fresh flower decoration, entrance arch & thematic pandal",
        badgeColor = Color(0xFFE11D48)
    ),
    PHOTOGRAPHY(
        id = "photography",
        hindiTitle = "Photography (फोटोग्राफी)",
        englishTitle = "Photography",
        hinglishTitle = "Photography",
        icon = "📷",
        descriptionHindi = "एचडी फोटो, 4K वीडियो, लाइव टेलीकास्ट, ड्रोन कवरेज",
        descriptionEnglish = "HD photo, 4K video recording, live YouTube/FB stream & reels",
        badgeColor = Color(0xFF0284C7)
    ),
    DARBAR(
        id = "darbar",
        hindiTitle = "Darbar (दरबार सजावट)",
        englishTitle = "Darbar",
        hinglishTitle = "Darbar",
        icon = "👑",
        descriptionHindi = "भव्य खाटू श्याम / बालाजी दरबार, सिंहासन, छत्र व झांकी",
        descriptionEnglish = "Grand devotional darbar set, divine throne, chatra & jhanki",
        badgeColor = Color(0xFFB45309)
    ),
    HALWAI(
        id = "halwai",
        hindiTitle = "Halwai (हलवाई सेवा)",
        englishTitle = "Halwai",
        hinglishTitle = "Halwai",
        icon = "🥣",
        descriptionHindi = "हलवाई टीम, सवामणी, छप्पन भोग, चूरमा व खीर कारीगर",
        descriptionEnglish = "Head halwai, sawamani prasad, sweets & chhappan bhog chef",
        badgeColor = Color(0xFFEA580C)
    ),
    CATERING(
        id = "catering",
        hindiTitle = "Catering (कैटरिंग / भोजन)",
        englishTitle = "Catering",
        hinglishTitle = "Catering",
        icon = "🍽️",
        descriptionHindi = "भक्तों हेतु भोजन/लंगर, बफेट स्टॉल, पानी व क्रॉकरी",
        descriptionEnglish = "Prasad langar buffet, dining setup, mineral water & service staff",
        badgeColor = Color(0xFF16A34A)
    ),
    STAGE(
        id = "stage",
        hindiTitle = "Stage (स्टेज व्यवस्था)",
        englishTitle = "Stage",
        hinglishTitle = "Stage",
        icon = "🎪",
        descriptionHindi = "स्टेज प्लेटफॉर्म, ट्रस्ट, कारपेट, वीआईपी सोफा व बैकड्रॉप",
        descriptionEnglish = "Stage platform, truss setup, red carpet, VIP sofas & backdrop",
        badgeColor = Color(0xFF9333EA)
    ),
    OTHERS(
        id = "others",
        hindiTitle = "Others (अन्य सेवाएं)",
        englishTitle = "Others",
        hinglishTitle = "Others",
        icon = "📦",
        descriptionHindi = "पंडित जी, सुरक्षा गार्ड, सफाई, स्वागत दल व विविध",
        descriptionEnglish = "Priest, security guards, sanitation, welcome committee & misc",
        badgeColor = Color(0xFF475569)
    );

    fun getDisplayName(language: AppLanguage): String {
        return when (language) {
            AppLanguage.HINDI -> hindiTitle
            AppLanguage.ENGLISH -> englishTitle
            AppLanguage.HINGLISH -> hinglishTitle
        }
    }

    fun getDescription(language: AppLanguage): String {
        return when (language) {
            AppLanguage.HINDI -> descriptionHindi
            AppLanguage.ENGLISH -> descriptionEnglish
            AppLanguage.HINGLISH -> "$descriptionHindi ($descriptionEnglish)"
        }
    }

    companion object {
        val ALL_CATEGORIES = values().toList()

        fun fromId(id: String): BookingCategory {
            return values().find { it.id.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true) }
                ?: OTHERS
        }
    }
}
