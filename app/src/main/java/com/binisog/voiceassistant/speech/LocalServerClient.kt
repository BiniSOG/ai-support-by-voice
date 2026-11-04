package com.binisog.voiceassistant.speech

import android.util.Log
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Optional helper on the customer's own computer (local-server/, same Wi-Fi, no internet).
 * The phone always has its own models; the computer's transcription is only added as one more
 * alternative when it answers quickly. Any failure means "phone only".
 */
class LocalServerClient(baseUrl: String) {
    private val base = baseUrl.trim().trimEnd('/')

    val enabled: Boolean get() = base.startsWith("http://") || base.startsWith("https://")

    /** "ok · <models>" or an error description. */
    fun health(): String = try {
        val c = open("$base/health", 2500)
        c.inputStream.bufferedReader().use { JSONObject(it.readText()) }.let { "ok · " + it.optString("models", "") }
    } catch (e: Exception) {
        "unreachable (${e.javaClass.simpleName})"
    }

    /** Transcribes 16 kHz mono audio, or null if the computer is off, slow, or unsure. */
    fun transcribe(samples: FloatArray, lang: String, timeoutMs: Int = 3500): String? {
        if (!enabled || samples.size < SAMPLE_RATE / 5) return null
        return try {
            val c = open("$base/transcribe?lang=$lang", timeoutMs)
            c.requestMethod = "POST"
            c.doOutput = true
            c.setRequestProperty("Content-Type", "audio/wav")
            c.outputStream.use { it.write(wav(samples)) }
            if (c.responseCode != 200) return null
            val json = c.inputStream.bufferedReader().use { JSONObject(it.readText()) }
            json.optString("text").trim().ifEmpty { null }
        } catch (e: Exception) {
            Log.i("LocalServer", "transcribe failed: ${e.message}")
            null
        }
    }

    private fun open(url: String, timeoutMs: Int) = (URL(url).openConnection() as HttpURLConnection).apply {
        connectTimeout = minOf(timeoutMs, 1500)
        readTimeout = timeoutMs
    }

    companion object {
        fun wav(x: FloatArray): ByteArray {
            val pcm = ByteBuffer.allocate(x.size * 2).order(ByteOrder.LITTLE_ENDIAN)
            for (v in x) pcm.putShort((v.coerceIn(-1f, 1f) * 32767).toInt().toShort())
            val data = pcm.array()
            val h = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
            h.put("RIFF".toByteArray()); h.putInt(36 + data.size); h.put("WAVE".toByteArray())
            h.put("fmt ".toByteArray()); h.putInt(16); h.putShort(1); h.putShort(1)
            h.putInt(SAMPLE_RATE); h.putInt(SAMPLE_RATE * 2); h.putShort(2); h.putShort(16)
            h.put("data".toByteArray()); h.putInt(data.size)
            return ByteArrayOutputStream().apply { write(h.array()); write(data) }.toByteArray()
        }
    }
}
