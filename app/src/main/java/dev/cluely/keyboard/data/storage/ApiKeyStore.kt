package dev.cluely.keyboard.data.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

val Context.datastore by preferencesDataStore("cluely_preferences")

class ApiKeyStore(private val context: Context) {

    companion object {
        private val API_KEY = stringPreferencesKey("openrouter_api_key")
        private val SELECTED_MODEL = stringPreferencesKey("selected_model")
    }

    val apiKeyFlow = context.datastore.data.map { it[API_KEY] ?: "" }
    val selectedModelFlow = context.datastore.data.map { it[SELECTED_MODEL] ?: "gpt-4-vision" }

    suspend fun saveApiKey(apiKey: String) {
        context.datastore.edit {
            it[API_KEY] = apiKey
        }
    }

    suspend fun saveSelectedModel(model: String) {
        context.datastore.edit {
            it[SELECTED_MODEL] = model
        }
    }

    suspend fun getApiKey(): String {
        val pref = context.datastore.data.map { it[API_KEY] ?: "" }
        var result = ""
        pref.collect { result = it }
        return result
    }
}

suspend inline fun <T> kotlinx.coroutines.flow.Flow<T>.collect(crossinline action: suspend (T) -> Unit) {
    kotlinx.coroutines.flow.collect(this) { action(it) }
}