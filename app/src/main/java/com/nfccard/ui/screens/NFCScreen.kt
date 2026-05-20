package com.nfccard.ui.screens

import android.nfc.Tag
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nfccard.MainActivity
import com.nfccard.data.CardRepository
import com.nfccard.data.nfcPayloadToCard
import com.nfccard.ui.*
import com.nfccard.ui.components.CardView
import com.nfccard.utils.NFCManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class NFCMode { IDLE, SENDING, RECEIVING, SUCCESS, ERROR }

@Composable
fun NFCScreen(repo: CardRepository, nfcAvailable: Boolean, nfcEnabled: Boolean, activity: MainActivity) {
    val scope = rememberCoroutineScope()
    val myCard by repo.myCard.collectAsState(initial = null)
    var mode by remember { mutableStateOf(NFCMode.IDLE) }
    var statusMsg by remember { mutableStateOf("") }
    var receivedCard by remember { mutableStateOf<com.nfccard.data.BusinessCard?>(null) }

    // 펄스 애니메이션
    val pulseScale by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "scale"
    )
    val animScale = if (mode == NFCMode.SENDING || mode == NFCMode.RECEIVING) pulseScale else 1f

    // 태그 감지 콜백 등록
    DisposableEffect(mode) {
        if (mode == NFCMode.RECEIVING) {
            activity.onTagDiscovered = { tag: Tag ->
                scope.launch {
                    val text = withContext(Dispatchers.IO) { NFCManager.readTextFromTag(tag) }
                    if (text != null) {
                        try {
                            val card = nfcPayloadToCard(text)
                            repo.addContact(card)
                            receivedCard = card
                            mode = NFCMode.SUCCESS
                            statusMsg = "✓ ${card.name}님의 명함을 받았습니다!"
                        } catch (e: Exception) {
                            mode = NFCMode.ERROR
                            statusMsg = "명함 데이터를 읽을 수 없습니다"
                        }
                    } else {
                        mode = NFCMode.ERROR
                        statusMsg = "NFC 태그를 읽지 못했습니다"
                    }
                    kotlinx.coroutines.delay(3500)
                    mode = NFCMode.IDLE
                    receivedCard = null
                    activity.onTagDiscovered = null
                }
            }
        } else if (mode == NFCMode.SENDING) {
            activity.onTagDiscovered = { tag: Tag ->
                scope.launch {
                    val card = myCard ?: return@launch
                    val ok = withContext(Dispatchers.IO) {
                        NFCManager.writeTextToTag(tag, card.toNfcPayload())
                    }
                    mode = if (ok) NFCMode.SUCCESS else NFCMode.ERROR
                    statusMsg = if (ok) "✓ 명함을 전송했습니다!" else "쓰기 실패. 다시 시도해주세요."
                    kotlinx.coroutines.delay(3000)
                    mode = NFCMode.IDLE
                    activity.onTagDiscovered = null
                }
            }
        }
        onDispose { activity.onTagDiscovered = null }
    }

    // NFC 미지원
    if (!nfcAvailable) {
        Box(Modifier.fillMaxSize().background(DarkBg), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("📵", fontSize = 56.sp)
                Text("NFC를 사용할 수 없습니다", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("기기가 NFC를 지원하지 않거나\n설정에서 NFC가 꺼져 있습니다", color = TextSecondary, textAlign = TextAlign.Center, lineHeight = 22.sp)
            }
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().background(DarkBg).padding(horizontal = 24.dp).padding(top = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("NFC 명함 교환", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.5).sp)
                Text("두 폰을 맞대면 명함이 교환됩니다", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))
            }
        }

        Spacer(Modifier.height(32.dp))

        // NFC 아이콘 영역
        Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(190.dp).scale(animScale).border(1.dp, Accent.copy(alpha = 0.13f), CircleShape))
            Box(Modifier.size(140.dp).scale(animScale).border(1.dp, Accent.copy(alpha = 0.27f), CircleShape))
            Box(
                modifier = Modifier.size(90.dp).background(
                    when (mode) {
                        NFCMode.SUCCESS -> Accent.copy(alpha = 0.07f)
                        NFCMode.ERROR -> Color(0xFFFF6B6B).copy(alpha = 0.07f)
                        else -> DarkSurface
                    }, CircleShape
                ).border(2.dp, when (mode) {
                    NFCMode.SUCCESS -> Accent
                    NFCMode.ERROR -> Color(0xFFFF6B6B)
                    else -> Accent
                }, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(when (mode) {
                    NFCMode.IDLE -> "📲"
                    NFCMode.SENDING -> "📤"
                    NFCMode.RECEIVING -> "📥"
                    NFCMode.SUCCESS -> "✅"
                    NFCMode.ERROR -> "❌"
                }, fontSize = 36.sp)
            }
        }

        Spacer(Modifier.height(8.dp))

        if (statusMsg.isNotEmpty()) {
            Text(statusMsg, color = when (mode) {
                NFCMode.SUCCESS -> Accent
                NFCMode.ERROR -> Color(0xFFFF6B6B)
                else -> Color(0xFFAAAACC)
            }, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 20.sp)
        }

        receivedCard?.let {
            Spacer(Modifier.height(16.dp))
            CardView(it, mini = true)
        }

        Spacer(Modifier.height(24.dp))

        when (mode) {
            NFCMode.IDLE -> {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NFCActionCard(Modifier.weight(1f), "📤", "내 명함 보내기", "NFC 태그에 쓰기", Color(0xFF00F5C4).copy(0.27f)) {
                        if (myCard == null || myCard!!.name.isBlank()) {
                            statusMsg = "먼저 내 명함을 작성해주세요"
                        } else {
                            mode = NFCMode.SENDING
                            statusMsg = "NFC 태그에 폰을 가까이 대세요..."
                        }
                    }
                    NFCActionCard(Modifier.weight(1f), "📥", "명함 받기", "NFC 태그 읽기", Color(0xFF7B61FF).copy(0.27f)) {
                        mode = NFCMode.RECEIVING
                        statusMsg = "상대방 폰을 가까이 가져오세요..."
                    }
                }
            }
            NFCMode.SENDING, NFCMode.RECEIVING -> {
                OutlinedButton(
                    onClick = { mode = NFCMode.IDLE; statusMsg = ""; activity.onTagDiscovered = null },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF6B6B)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF6B6B).copy(0.44f))
                ) { Text("취소", fontWeight = FontWeight.SemiBold) }
            }
            else -> {}
        }

        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier.fillMaxWidth().background(DarkSurface, RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF1E1E32), RoundedCornerShape(12.dp)).padding(14.dp)
        ) {
            Text("💡 상대방도 이 앱이 있으면 자동 교환, 없으면 NFC 태그(카드/스티커)에 저장 후 읽기", fontSize = 12.sp, color = TextSecondary, textAlign = TextAlign.Center, lineHeight = 18.sp)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun NFCActionCard(modifier: Modifier, emoji: String, title: String, desc: String, borderColor: Color, onClick: () -> Unit) {
    Surface(
        modifier = modifier, onClick = onClick,
        shape = RoundedCornerShape(18.dp), color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(emoji, fontSize = 28.sp)
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(desc, fontSize = 11.sp, color = TextSecondary)
        }
    }
}

private fun com.nfccard.data.BusinessCard.toNfcPayload() = com.nfccard.data.toNfcPayload(this)
private fun com.nfccard.data.toNfcPayload(card: com.nfccard.data.BusinessCard) = card.toNfcPayload()
