package com.nfccard.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nfccard.data.BusinessCard
import com.nfccard.data.CardRepository
import com.nfccard.ui.*
import com.nfccard.ui.components.CardView
import com.nfccard.ui.components.parseColor
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ContactsScreen(repo: CardRepository) {
    val scope = rememberCoroutineScope()
    val contacts by repo.contacts.collectAsState(initial = emptyList())
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<BusinessCard?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val filtered = remember(contacts, query) {
        if (query.isBlank()) contacts
        else contacts.filter { c ->
            listOf(c.name, c.company, c.title, c.email).any { it.contains(query, ignoreCase = true) }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkBg).padding(top = 56.dp)) {
        // 헤더
        Row(modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("받은 명함", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.5).sp)
            Text("${contacts.size}장", fontSize = 16.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 4.dp))
        }

        Spacer(Modifier.height(16.dp))

        // 검색바
        OutlinedTextField(
            value = query, onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            placeholder = { Text("이름, 회사, 이메일 검색...", color = Color(0xFF444466)) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondary) },
            trailingIcon = if (query.isNotEmpty()) {{ IconButton({ query = "" }) { Icon(Icons.Default.Close, null, tint = TextSecondary) } }} else null,
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Accent, unfocusedBorderColor = Color(0xFF1E1E32),
                focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                cursorColor = Accent, focusedContainerColor = DarkSurface, unfocusedContainerColor = DarkSurface
            )
        )

        Spacer(Modifier.height(16.dp))

        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(if (contacts.isEmpty()) "📇" else "🔍", fontSize = 56.sp)
                    Text(
                        if (contacts.isEmpty()) "NFC로 명함을 받으면\n여기에 저장됩니다" else "검색 결과가 없습니다",
                        color = TextSecondary, textAlign = TextAlign.Center, lineHeight = 22.sp
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered, key = { it.id }) { card ->
                    ContactRow(card) { selected = card }
                }
            }
        }
    }

    // 상세 모달
    selected?.let { card ->
        Dialog(
            onDismissRequest = { selected = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(Modifier.fillMaxSize(), color = DarkBg) {
                Column(Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                    // 핸들
                    Box(Modifier.width(36.dp).height(4.dp).background(Color(0xFF2E2E4E), RoundedCornerShape(2.dp)).align(Alignment.CenterHorizontally))
                    Spacer(Modifier.height(16.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("명함 상세", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        TextButton({ selected = null }) { Text("닫기", color = Accent, fontWeight = FontWeight.SemiBold) }
                    }

                    Spacer(Modifier.height(20.dp))
                    CardView(card)

                    Spacer(Modifier.height(16.dp))

                    // 받은 날짜
                    Row(
                        Modifier.fillMaxWidth().background(DarkSurface, RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFF1E1E32), RoundedCornerShape(14.dp)).padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("받은 날짜", fontSize = 12.sp, color = TextSecondary)
                        Text(formatDate(card.receivedAt), fontSize = 13.sp, color = Color(0xFFAAAACC), fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(Modifier.height(16.dp))

                    // 삭제 버튼
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF6B6B)),
                        border = BorderStroke(1.dp, Color(0xFFFF6B6B).copy(0.44f))
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("명함 삭제", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    if (showDeleteDialog && selected != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = DarkSurface,
            title = { Text("명함 삭제", color = TextPrimary) },
            text = { Text("${selected!!.name}님의 명함을 삭제할까요?", color = TextSecondary) },
            confirmButton = {
                TextButton({
                    scope.launch { repo.deleteContact(selected!!.id) }
                    showDeleteDialog = false; selected = null
                }) { Text("삭제", color = Color(0xFFFF6B6B)) }
            },
            dismissButton = { TextButton({ showDeleteDialog = false }) { Text("취소", color = TextSecondary) } }
        )
    }
}

@Composable
private fun ContactRow(card: BusinessCard, onClick: () -> Unit) {
    val accent = parseColor(card.colorHex)
    Surface(onClick = onClick, shape = RoundedCornerShape(14.dp), color = DarkSurface,
        border = BorderStroke(1.dp, Color(0xFF1E1E32))) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(4.dp).height(72.dp).background(accent))
            Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(card.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("${card.title} · ${card.company}", fontSize = 12.sp, color = TextSecondary, maxLines = 1)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(formatDate(card.receivedAt), fontSize = 11.sp, color = Color(0xFF444466))
                    Text("›", fontSize = 18.sp, color = Color(0xFF444466))
                }
            }
        }
    }
}

private fun formatDate(ms: Long): String {
    return SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(Date(ms))
}
