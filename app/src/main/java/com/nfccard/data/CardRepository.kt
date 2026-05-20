package com.nfccard.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// ─── 데이터 모델 ───────────────────────────────────────
data class BusinessCard(
    val id: String = System.currentTimeMillis().toString(),
    val name: String = "",
    val title: String = "",
    val company: String = "",
    val phone: String = "",
    val email: String = "",
    val website: String = "",
    val memo: String = "",
    val colorHex: String = "#00F5C4",
    val receivedAt: Long = System.currentTimeMillis()
)

fun BusinessCard.toNfcPayload(): String {
    return Gson().toJson(mapOf(
        "n" to name, "t" to title, "c" to company,
        "p" to phone, "e" to email, "w" to website,
        "col" to colorHex
    ))
}

fun nfcPayloadToCard(json: String): BusinessCard {
    val map = Gson().fromJson<Map<String, String>>(json, object : TypeToken<Map<String, String>>() {}.type)
    return BusinessCard(
        name = map["n"] ?: "",
        title = map["t"] ?: "",
        company = map["c"] ?: "",
        phone = map["p"] ?: "",
        email = map["e"] ?: "",
        website = map["w"] ?: "",
        colorHex = map["col"] ?: "#00F5C4",
        receivedAt = System.currentTimeMillis()
    )
}

// ─── DataStore ────────────────────────────────────────
private val Context.dataStore by preferencesDataStore(name = "nfc_cards")
private val MY_CARD_KEY = stringPreferencesKey("my_card")
private val CONTACTS_KEY = stringPreferencesKey("contacts")
private val gson = Gson()

class CardRepository(private val context: Context) {

    // 내 명함
    val myCard: Flow<BusinessCard?> = context.dataStore.data.map { prefs ->
        prefs[MY_CARD_KEY]?.let { gson.fromJson(it, BusinessCard::class.java) }
    }

    suspend fun saveMyCard(card: BusinessCard) {
        context.dataStore.edit { it[MY_CARD_KEY] = gson.toJson(card) }
    }

    // 받은 명함 목록
    val contacts: Flow<List<BusinessCard>> = context.dataStore.data.map { prefs ->
        val json = prefs[CONTACTS_KEY] ?: "[]"
        gson.fromJson<List<BusinessCard>>(json, object : TypeToken<List<BusinessCard>>() {}.type) ?: emptyList()
    }

    suspend fun addContact(card: BusinessCard) {
        context.dataStore.edit { prefs ->
            val current = gson.fromJson<MutableList<BusinessCard>>(
                prefs[CONTACTS_KEY] ?: "[]",
                object : TypeToken<MutableList<BusinessCard>>() {}.type
            ) ?: mutableListOf()
            val idx = current.indexOfFirst { it.email == card.email && card.email.isNotEmpty() }
            if (idx >= 0) current[idx] = card else current.add(0, card)
            prefs[CONTACTS_KEY] = gson.toJson(current)
        }
    }

    suspend fun deleteContact(id: String) {
        context.dataStore.edit { prefs ->
            val current = gson.fromJson<MutableList<BusinessCard>>(
                prefs[CONTACTS_KEY] ?: "[]",
                object : TypeToken<MutableList<BusinessCard>>() {}.type
            ) ?: mutableListOf()
            current.removeAll { it.id == id }
            prefs[CONTACTS_KEY] = gson.toJson(current)
        }
    }
}
