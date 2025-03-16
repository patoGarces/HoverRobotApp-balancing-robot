package com.app.hoverrobot.data.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.app.hoverrobot.data.models.Aggressiveness
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")


class StoreSettings @Inject constructor(@ApplicationContext private val context: Context) {

    private val KEY_AGGRESSIVENESS = intPreferencesKey("aggressiveness_key")

    suspend fun getAggressiveness() =
        Aggressiveness.entries[
            context.dataStore.data.map { preferences ->
                preferences[KEY_AGGRESSIVENESS]
            }.first() ?: 0]

    suspend fun saveAggressiveness(aggressiveness: Aggressiveness) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AGGRESSIVENESS] = aggressiveness.ordinal
        }
    }
}

