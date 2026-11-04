package com.binisog.voiceassistant.session

import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.binisog.voiceassistant.brain.AccountOpeningAgent
import com.binisog.voiceassistant.brain.DateParser
import com.binisog.voiceassistant.brain.Lang
import com.binisog.voiceassistant.brain.Profile
import com.binisog.voiceassistant.ui.tr
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** "Download Account Details": a one-page PDF, saved to Downloads (and the session folder). */
object AccountPdf {

    /** Returns a human-readable location of the saved file. */
    fun save(context: Context, profile: Profile, lang: Lang, sessionDir: File?): String {
        val doc = PdfDocument()
        val page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create()) // A4 at 72 dpi
        val c = page.canvas
        val navy = Color.rgb(0x0B, 0x2F, 0x7A)
        val header = Paint().apply { color = navy }
        c.drawRect(0f, 0f, 595f, 110f, header)
        val white = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textSize = 26f; typeface = Typeface.DEFAULT_BOLD }
        c.drawText("Demo Bank", 40f, 55f, white)
        c.drawText(tr(lang, "Account Details", "የአካውንት መረጃ"), 40f, 88f, Paint(white).apply { textSize = 16f; typeface = Typeface.DEFAULT })

        val label = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(0x6B, 0x72, 0x80); textSize = 13f }
        val value = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(0x14, 0x21, 0x3D); textSize = 16f; typeface = Typeface.DEFAULT_BOLD }
        val line = Paint().apply { color = Color.rgb(0xE8, 0xEE, 0xFB); strokeWidth = 1f }
        val rows = listOf(
            tr(lang, "Account Number", "የአካውንት ቁጥር") to (profile.accountNumber?.let { AccountOpeningAgent.formatGroups(it) } ?: "—"),
            tr(lang, "Account Type", "የሂሳብ ዓይነት") to (profile.accountType?.display(lang) ?: "—"),
            tr(lang, "Full Name", "ሙሉ ስም") to (profile.fullName ?: "—"),
            tr(lang, "Date of Birth", "የትውልድ ቀን") to (profile.dateOfBirth?.let { DateParser.display(it, lang) } ?: "—"),
            tr(lang, "Birth City", "የትውልድ ከተማ") to (profile.birthCity ?: "—"),
            tr(lang, "Nationality", "ዜግነት") to (profile.nationality ?: "—"),
            "FAN" to (profile.fan?.let { AccountOpeningAgent.formatGroups(it) } ?: "—"),
            tr(lang, "Branch", "ቅርንጫፍ") to tr(lang, "Digital (AI Video Assistant)", "ዲጂታል (AI ቪዲዮ ረዳት)"),
            tr(lang, "Status", "ሁኔታ") to tr(lang, "Active", "ንቁ"),
        )
        var y = 160f
        for ((k, v) in rows) {
            c.drawText(k, 40f, y, label)
            c.drawText(v, 220f, y, value)
            c.drawLine(40f, y + 14f, 555f, y + 14f, line)
            y += 42f
        }
        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        c.drawText(tr(lang, "Generated on this phone (offline) $now", "በዚህ ስልክ የተዘጋጀ (ከመስመር ውጭ) $now"), 40f, 800f, label)
        doc.finishPage(page)

        val name = "DemoBank-Account-${profile.accountNumber ?: "details"}.pdf"
        try {
            sessionDir?.let { dir -> File(dir, "account_details.pdf").outputStream().use { doc.writeTo(it) } }
            if (Build.VERSION.SDK_INT >= 29) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, name)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { doc.writeTo(it) }
                    return "Downloads/$name"
                }
            }
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
            val f = File(dir, name)
            f.outputStream().use { doc.writeTo(it) }
            return f.absolutePath
        } finally {
            doc.close()
        }
    }
}
