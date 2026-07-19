package com.noor.data.remote

import com.noor.domain.model.Ayah
import com.noor.domain.model.Tafsir
import com.noor.domain.model.TafsirOrigin
import android.os.Build
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URL
import javax.inject.Inject
import javax.net.ssl.HttpsURLConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

internal class QuranEncTafsirDataSource @Inject constructor() : TafsirRemoteDataSource {
    override suspend fun fetch(ayah: Ayah): Tafsir = withContext(Dispatchers.IO) {
        val url = URL("$BASE_URL/${ayah.surahNumber}/${ayah.numberInSurah}")
        check(url.protocol == "https" && url.host == EXPECTED_HOST) {
            "Tafsir endpoint must remain on the approved HTTPS host."
        }
        val connection = url.openConnection() as HttpsURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.instanceFollowRedirects = false
            connection.useCaches = false
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", USER_AGENT)

            val status = connection.responseCode
            if (status != HttpsURLConnection.HTTP_OK) {
                throw IOException("Tafsir service returned HTTP $status")
            }
            val contentType = connection.contentType.orEmpty().lowercase()
            if (!contentType.startsWith("application/json")) {
                throw IOException("Tafsir response is not JSON")
            }
            val declaredLength: Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connection.contentLengthLong
            } else {
                connection.contentLength.toLong()
            }
            if (declaredLength > MAX_RESPONSE_BYTES) {
                throw IOException("Tafsir response is too large")
            }

            val body = connection.inputStream.use(::readBoundedUtf8)
            QuranEncTafsirParser.parse(
                body = body,
                ayah = ayah,
                fetchedAtEpochMillis = System.currentTimeMillis(),
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun readBoundedUtf8(input: java.io.InputStream): String {
        val output = ByteArrayOutputStream(minOf(16_384, MAX_RESPONSE_BYTES))
        val buffer = ByteArray(8_192)
        var totalBytes = 0
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            totalBytes += count
            if (totalBytes > MAX_RESPONSE_BYTES) {
                throw IOException("Tafsir response is too large")
            }
            output.write(buffer, 0, count)
        }
        return output.toString(Charsets.UTF_8.name())
    }

    private companion object {
        const val EXPECTED_HOST = "quranenc.com"
        const val BASE_URL = "https://quranenc.com/api/v1/translation/aya/arabic_moyassar"
        const val USER_AGENT = "Noor-Android/1.0"
        const val CONNECT_TIMEOUT_MS = 10_000
        const val READ_TIMEOUT_MS = 15_000
        const val MAX_RESPONSE_BYTES = 1_048_576
    }
}

internal object QuranEncTafsirParser {
    private const val SOURCE_KEY = "arabic_moyassar"
    private const val SOURCE_NAME = "التفسير الميسر — QuranEnc.com"

    fun parse(body: String, ayah: Ayah, fetchedAtEpochMillis: Long): Tafsir {
        if (body.isBlank()) throw IOException("Tafsir response is empty")
        val result = try {
            val root = JSONObject(body)
            root.optJSONObject("result") ?: root
        } catch (error: JSONException) {
            throw IOException("Tafsir response contains invalid JSON", error)
        }

        val responseSurah = result.optInt("sura", -1)
        val responseAyah = result.optInt("aya", -1)
        if (responseSurah != ayah.surahNumber || responseAyah != ayah.numberInSurah) {
            throw IOException("Tafsir response does not match the requested ayah")
        }

        val text = result.optString("translation")
        if (text.isBlank()) throw IOException("Tafsir response does not contain translation text")
        val footnotes = result.optString("footnotes")
            .takeUnless { it.isBlank() || it.equals("null", ignoreCase = true) }

        return Tafsir(
            ayahId = ayah.id,
            text = text,
            footnotes = footnotes,
            sourceKey = SOURCE_KEY,
            sourceName = SOURCE_NAME,
            fetchedAtEpochMillis = fetchedAtEpochMillis,
            origin = TafsirOrigin.NETWORK,
        )
    }
}
