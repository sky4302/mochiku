package eu.kanade.presentation.more.profile

import android.content.Context
import android.net.Uri
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.random.Random

object MochikuProfileStore {
    private const val PREFS_NAME = "mochiku_profile_prefs"
    private const val KEY_USERNAME = "username"
    private const val KEY_PHOTO_PATH = "photo_path"
    private const val KEY_CARD_NUMBER = "card_number"
    private const val KEY_JOINED_DATE = "joined_date"

    fun getUsername(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USERNAME, null)?.ifBlank { null } ?: "Sky"
    }

    fun setUsername(context: Context, name: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_USERNAME, name.trim()).apply()
    }

    fun getPhotoPath(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val path = prefs.getString(KEY_PHOTO_PATH, null)
        if (path != null && File(path).exists()) {
            return path
        }
        return null
    }

    fun getCardNumber(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var num = prefs.getString(KEY_CARD_NUMBER, null)
        if (num.isNullOrBlank()) {
            num = (10_000_000_000L + Random.nextLong(89_999_999_999L)).toString()
            prefs.edit().putString(KEY_CARD_NUMBER, num).apply()
        }
        return num
    }

    fun getJoinedDate(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var date = prefs.getString(KEY_JOINED_DATE, null)
        if (date.isNullOrBlank()) {
            val now = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
            date = now.format(formatter)
            prefs.edit().putString(KEY_JOINED_DATE, date).apply()
        }
        return date
    }

    fun savePhoto(context: Context, uri: Uri): String? {
        return runCatching {
            val file = File(context.filesDir, "mochiku_avatar_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            val oldPath = getPhotoPath(context)
            if (oldPath != null && oldPath != file.absolutePath) {
                runCatching { File(oldPath).delete() }
            }
            val path = file.absolutePath
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_PHOTO_PATH, path).apply()
            path
        }.getOrNull()
    }

    fun clearPhoto(context: Context) {
        val oldPath = getPhotoPath(context)
        if (oldPath != null) {
            runCatching { File(oldPath).delete() }
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_PHOTO_PATH).apply()
    }
}
