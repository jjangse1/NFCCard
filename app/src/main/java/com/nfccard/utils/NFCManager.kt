package com.nfccard.utils

import android.app.Activity
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import java.nio.charset.Charset

object NFCManager {

    fun isNfcAvailable(activity: Activity): Boolean =
        NfcAdapter.getDefaultAdapter(activity) != null

    fun isNfcEnabled(activity: Activity): Boolean =
        NfcAdapter.getDefaultAdapter(activity)?.isEnabled == true

    /** 포그라운드 디스패치 활성화 - 액티비티가 NFC 태그를 먼저 받음 */
    fun enableForegroundDispatch(activity: Activity, adapter: NfcAdapter?) {
        val intent = android.content.Intent(activity, activity::class.java).apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            activity, 0, intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_MUTABLE
        )
        adapter?.enableForegroundDispatch(activity, pendingIntent, null, null)
    }

    fun disableForegroundDispatch(activity: Activity, adapter: NfcAdapter?) {
        adapter?.disableForegroundDispatch(activity)
    }

    /** NDEF 태그에서 텍스트 읽기 */
    fun readTextFromTag(tag: Tag): String? {
        return try {
            val ndef = Ndef.get(tag) ?: return null
            ndef.connect()
            val message = ndef.ndefMessage ?: run { ndef.close(); return null }
            ndef.close()
            val record = message.records.firstOrNull() ?: return null
            parseTextRecord(record)
        } catch (e: Exception) {
            null
        }
    }

    /** NDEF 태그에 텍스트 쓰기 */
    fun writeTextToTag(tag: Tag, text: String): Boolean {
        return try {
            val record = createTextRecord(text)
            val message = NdefMessage(arrayOf(record))

            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                ndef.writeNdefMessage(message)
                ndef.close()
                true
            } else {
                val formatable = NdefFormatable.get(tag)
                formatable?.apply {
                    connect()
                    format(message)
                    close()
                } != null
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun createTextRecord(text: String): NdefRecord {
        val langBytes = "ko".toByteArray(Charset.forName("US-ASCII"))
        val textBytes = text.toByteArray(Charset.forName("UTF-8"))
        val payload = ByteArray(1 + langBytes.size + textBytes.size).also {
            it[0] = langBytes.size.toByte()
            langBytes.copyInto(it, 1)
            textBytes.copyInto(it, 1 + langBytes.size)
        }
        return NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, ByteArray(0), payload)
    }

    private fun parseTextRecord(record: NdefRecord): String? {
        return try {
            val payload = record.payload
            val langLength = payload[0].toInt() and 0x3F
            String(payload, 1 + langLength, payload.size - 1 - langLength, Charset.forName("UTF-8"))
        } catch (e: Exception) {
            null
        }
    }
}
