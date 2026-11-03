package com.example.data

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.example.util.BanglaFormatters
import java.util.concurrent.ConcurrentHashMap

@Immutable
@Entity(tableName = "wallet_config")
data class WalletConfigEntity(
    @PrimaryKey val id: Int = 1,
    val bkashBalance: Int = 8450,
    val bkashVisible: Boolean = true,
    val dbblBalance: Int = 24500,
    val dbblVisible: Boolean = true,
    val showDbblSection: Boolean = true,
    val dbblAccountNo: String = "4829",
    val monthlyBudget: Int = 25000,
    val savingsGoal: Int = 5000,
    val lowBhangtiThreshold: Int = 8,
    val showTempMoneySection: Boolean = true,
    val soundEnabled: Boolean = true,
    val layoutMode: String = "tabs", // "tabs" or "full"
    val themeMode: String = "dark", // "dark" or "light"
    val language: String = "bn", // "bn" or "en"
    val biometricLockEnabled: Boolean = false,
    val biometricPinCode: String = "1234"
)

@Immutable
@Entity(tableName = "note_counts")
data class NoteCountEntity(
    @PrimaryKey val denom: Int,
    val count: Int
)

@Immutable
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val icon: String,
    val colorHex: String,
    val sortOrder: Int = 0
)

@Immutable
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val type: String, // "expense" or "inflow"
    val method: String, // "cash", "bkash", or "dbbl"
    val categoryId: String,
    val amount: Int,
    val note: String,
    val tenderedNotesEncoded: String = "",
    val tenderedTotal: Int = 0,
    val receivedNotesEncoded: String = "",
    val receivedTotal: Int = 0,
    val expectedChange: Int = 0,
    val discrepancy: Int = 0
) {
    @delegate:Ignore
    val tenderedNotes: Map<Int, Int> by lazy(LazyThreadSafetyMode.NONE) {
        BanglaFormatters.decodeNotesMap(tenderedNotesEncoded)
    }

    @delegate:Ignore
    val receivedNotes: Map<Int, Int> by lazy(LazyThreadSafetyMode.NONE) {
        BanglaFormatters.decodeNotesMap(receivedNotesEncoded)
    }
}

@Immutable
@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey val id: String,
    val type: String, // "receivable" or "payable"
    val person: String,
    val amount: Int,
    val timestamp: Long
)

@Immutable
@Entity(tableName = "temp_money")
data class TempMoneyEntity(
    @PrimaryKey val id: String,
    val title: String,
    val amount: Int,
    val whereStored: String, // "cash", "bkash", "dbbl", "other"
    val timestamp: Long,
    val done: Boolean = false
)

@Immutable
data class BanknoteStyleMeta(
    val denom: Int,
    val label: String,
    val tag: String,
    val monumentTitle: String,
    val metaDescription: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color,
    val borderColor: Color
)

object BanknoteCatalog {
    val styles: Map<Int, BanknoteStyleMeta> = mapOf(
        1000 to BanknoteStyleMeta(
            denom = 1000,
            label = "৳১,০০০ ভাঙতি/নোট",
            tag = "বেগুনি-স্বর্ণ",
            monumentTitle = "জাতীয় স্মৃতিসৌধ ও সংসদ ভবন",
            metaDescription = "আকার: ১৬০×৭০ মিমি | সম্মুখভাগ: জাতীয় স্মৃতিসৌধ ও শাপলা | পৃষ্ঠভাগ: জাতীয় সংসদ ভবন",
            primaryColor = Color(0xFF4C1D95),
            secondaryColor = Color(0xFF7C3AED),
            accentColor = Color(0xFFC084FC),
            borderColor = Color(0x8CC084FC)
        ),
        500 to BanknoteStyleMeta(
            denom = 500,
            label = "৳৫০০ ভাঙতি/নোট",
            tag = "সবুজ-বেগুনি",
            monumentTitle = "কেন্দ্রীয় শহীদ মিনার ও সুপ্রিম কোর্ট",
            metaDescription = "আকার: ১৫২×৬৫ মিমি | সম্মুখভাগ: কেন্দ্রীয় শহীদ মিনার / স্মৃতিসৌধ | পৃষ্ঠভাগ: বাংলাদেশ সুপ্রিম কোর্ট",
            primaryColor = Color(0xFF065F46),
            secondaryColor = Color(0xFF10B981),
            accentColor = Color(0xFF6EE7B7),
            borderColor = Color(0x8C6EE7B7)
        ),
        200 to BanknoteStyleMeta(
            denom = 200,
            label = "৳২০০ ভাঙতি/নোট",
            tag = "সোনালী-হলুদ",
            monumentTitle = "অপরাজেয় বাংলা ও গ্রামবাংলার নদী",
            metaDescription = "আকার: ১৪৬×৬৩ মিমি | রঙ: সোনালী হলুদ | সম্মুখভাগ: অপরাজেয় বাংলা ভাস্কর্য",
            primaryColor = Color(0xFF92400E),
            secondaryColor = Color(0xFFF59E0B),
            accentColor = Color(0xFFFDE68A),
            borderColor = Color(0x8CFDE68A)
        ),
        100 to BanknoteStyleMeta(
            denom = 100,
            label = "৳১০০ ভাঙতি/নোট",
            tag = "নীলচে-আকাশী",
            monumentTitle = "ষাট গম্বুজ মসজিদ ও সুন্দরবন",
            metaDescription = "আকার: ১৪০×৬২ মিমি | সম্মুখভাগ: বাগেরহাট ষাট গম্বুজ মসজিদ / তারা মসজিদ | পৃষ্ঠভাগ: সুন্দরবন",
            primaryColor = Color(0xFF1E3A8A),
            secondaryColor = Color(0xFF2563EB),
            accentColor = Color(0xFF93C5FD),
            borderColor = Color(0x8C93C5FD)
        ),
        50 to BanknoteStyleMeta(
            denom = 50,
            label = "৳৫০ ভাঙতি/নোট",
            tag = "কমলা-টেরাকোটা",
            monumentTitle = "আহসান মঞ্জিল ও শিল্পাচার্য জয়নুল আবেদিন",
            metaDescription = "আকার: ১৩০×৬০ মিমি | সম্মুখভাগ: আহসান মঞ্জিল / বাঘা মসজিদ | পৃষ্ঠভাগ: জয়নুল আবেদিনের চিত্রকর্ম",
            primaryColor = Color(0xFF7C2D12),
            secondaryColor = Color(0xFFEA580C),
            accentColor = Color(0xFFFDBA74),
            borderColor = Color(0x8CFDBA74)
        ),
        20 to BanknoteStyleMeta(
            denom = 20,
            label = "৳২০ ভাঙতি নোট",
            tag = "ফিরোজা-সবুজ",
            monumentTitle = "কান্তজীর মন্দির ও পাহাড়পুর বৌদ্ধবিহার",
            metaDescription = "আকার: ১২৭×৬০ মিমি | সম্মুখভাগ: কান্তজীর মন্দির / ছোট সোনা মসজিদ | পৃষ্ঠভাগ: পাহাড়পুর বৌদ্ধবিহার",
            primaryColor = Color(0xFF115E59),
            secondaryColor = Color(0xFF14B8A6),
            accentColor = Color(0xFF99F6E4),
            borderColor = Color(0x8C99F6E4)
        ),
        10 to BanknoteStyleMeta(
            denom = 10,
            label = "৳১০ ভাঙতি নোট",
            tag = "গোলাপি-ম্যাজেন্টা",
            monumentTitle = "বায়তুল মোকাররম জাতীয় মসজিদ",
            metaDescription = "আকার: ১২৩×৬০ মিমি | রঙ: গোলাপি ম্যাজেন্টা | সম্মুখভাগ: বায়তুল মোকাররম জাতীয় মসজিদ",
            primaryColor = Color(0xFF831843),
            secondaryColor = Color(0xFFDB2777),
            accentColor = Color(0xFFF9A8D4),
            borderColor = Color(0x8CF9A8D4)
        ),
        5 to BanknoteStyleMeta(
            denom = 5,
            label = "৳৫ ভাঙতি নোট/কয়েন",
            tag = "তারা মসজিদ/কুসুম্বা",
            monumentTitle = "তারা মসজিদ ও কুসুম্বা মসজিদ",
            metaDescription = "আকার: ১১৭×৬০ মিমি | সম্মুখভাগ: তারা মসজিদ / কুসুম্বা মসজিদ নওগাঁ",
            primaryColor = Color(0xFF57534E),
            secondaryColor = Color(0xFFA8A29E),
            accentColor = Color(0xFFE7E5E4),
            borderColor = Color(0x73E7E5E4)
        ),
        2 to BanknoteStyleMeta(
            denom = 2,
            label = "৳২ ভাঙতি নোট/কয়েন",
            tag = "শহীদ মিনার/দোয়েল",
            monumentTitle = "শহীদ মিনার ও জাতীয় পাখি দোয়েল",
            metaDescription = "আকার: ১০০×৬০ মিমি | সম্মুখভাগ: কেন্দ্রীয় শহীদ মিনার ও জাতীয় পাখি দোয়েল",
            primaryColor = Color(0xFF14532D),
            secondaryColor = Color(0xFF22C55E),
            accentColor = Color(0xFFBBF7D0),
            borderColor = Color(0x73BBF7D0)
        ),
        1 to BanknoteStyleMeta(
            denom = 1,
            label = "৳১ ভাঙতি কয়েন/নোট",
            tag = "হরিণ ও শাপলা প্রতীক",
            monumentTitle = "জাতীয় প্রতীক শাপলা ও চিত্রা হরিণ",
            metaDescription = "আকার: ১০০×৬০ মিমি | সম্মুখভাগ: জাতীয় প্রতীক শাপলা ও সুন্দরবনের চিত্রা হরিণ",
            primaryColor = Color(0xFF334155),
            secondaryColor = Color(0xFF64748B),
            accentColor = Color(0xFFCBD5E1),
            borderColor = Color(0x73CBD5E1)
        )
    )

    fun styleFor(denom: Int): BanknoteStyleMeta = styles[denom] ?: styles.getValue(100)

    val DEFAULT_CATEGORIES = listOf(
        CategoryEntity("rent", "বাড়ি ভাড়া", "🏠", "#00F0FF", 0),
        CategoryEntity("bills", "বিল ও ইউটিলিটি", "⚡", "#FACC15", 1),
        CategoryEntity("grocery", "মুদি ও বাজার", "🛒", "#00FFA3", 2),
        CategoryEntity("clothes", "পোশাক", "👕", "#A855F7", 3),
        CategoryEntity("food", "খাবার ও পানীয়", "🍔", "#FB923C", 4),
        CategoryEntity("transport", "অটো / যাতায়াত ভাড়া", "🛺", "#38BDF8", 5),
        CategoryEntity("impulse", "হঠকারী খরচ", "🛍️", "#FF0055", 6),
        CategoryEntity("misc", "বিবিধ", "📦", "#E2136E", 7)
    )

    val EMOJI_CHOICES = listOf("💊", "📚", "🎁", "☕", "🎮", "✈️", "💇", "🏋️", "📱", "🔧", "🪴", "🏥")

    private val hexColorCache = ConcurrentHashMap<String, Color>(32)

    fun parseHexColor(hex: String): Color {
        hexColorCache[hex]?.let { return it }
        val parsed = try {
            val clean = hex.removePrefix("#")
            val longVal = clean.toLong(16)
            if (clean.length == 6) {
                Color(0xFF000000 or longVal)
            } else {
                Color(longVal)
            }
        } catch (_: Exception) {
            Color(0xFF00F0FF)
        }
        hexColorCache[hex] = parsed
        return parsed
    }
}
