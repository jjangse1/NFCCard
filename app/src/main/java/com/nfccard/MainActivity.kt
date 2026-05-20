package com.nfccard

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.nfccard.data.CardRepository
import com.nfccard.data.nfcPayloadToCard
import com.nfccard.ui.NFCCardApp
import com.nfccard.utils.NFCManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var repo: CardRepository

    // NFC 태그 감지 콜백 (NFCScreen에서 등록)
    var onTagDiscovered: ((Tag) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        repo = CardRepository(applicationContext)

        setContent {
            NFCCardApp(
                nfcAvailable = NFCManager.isNfcAvailable(this),
                nfcEnabled = NFCManager.isNfcEnabled(this)
            )
        }

        // 앱 밖에서 NFC 태그 감지로 실행된 경우 처리
        handleNfcIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        NFCManager.enableForegroundDispatch(this, nfcAdapter)
    }

    override fun onPause() {
        super.onPause()
        NFCManager.disableForegroundDispatch(this, nfcAdapter)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNfcIntent(intent)
    }

    private fun handleNfcIntent(intent: Intent) {
        if (intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED ||
            intent.action == NfcAdapter.ACTION_TAG_DISCOVERED
        ) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG) ?: return
            onTagDiscovered?.invoke(tag) ?: run {
                // onTagDiscovered 미등록 → 자동 저장 시도
                val text = NFCManager.readTextFromTag(tag)
                if (text != null) {
                    lifecycleScope.launch {
                        try { repo.addContact(nfcPayloadToCard(text)) } catch (_: Exception) {}
                    }
                }
            }
        }
    }
}
