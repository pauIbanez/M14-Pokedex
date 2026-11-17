package com.example.pokedex.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

private val Context.capturedStore: DataStore<Preferences> by preferencesDataStore(name = "pokedex")

class PokedexRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    private val capturedKey = stringSetPreferencesKey("captured_ids")

    /** Reads and parses assets/pokedex.json. */
    suspend fun loadPokedex(): Pokedex = withContext(Dispatchers.IO) {
        val text = context.assets.open("pokedex.json").bufferedReader().use { it.readText() }
        json.decodeFromString<Pokedex>(text)
    }

    /** Ids of captured Pokémon, kept across app restarts. */
    val captured: Flow<Set<Int>> = context.capturedStore.data.map { prefs ->
        prefs[capturedKey].orEmpty().mapNotNull { it.toIntOrNull() }.toSet()
    }

    suspend fun setCaptured(id: Int, captured: Boolean) {
        context.capturedStore.edit { prefs ->
            val current = prefs[capturedKey].orEmpty()
            prefs[capturedKey] = if (captured) current + id.toString() else current - id.toString()
        }
    }
}
