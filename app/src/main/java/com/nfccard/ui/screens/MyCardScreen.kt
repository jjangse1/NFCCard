package com.nfccard.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nfccard.data.BusinessCard
import com.nfccard.data.CardRepository
import com.nfccard.ui.*
import com.nfccard.ui.components.CardView
import kotlinx.coroutines.launch

private val COLORS = listOf("#00F5C4","#7B61FF","#FF6B6B","#FFD93D","#4ECDC4","#FF9A3C")

@Composable
fun MyCardScreen(repo: CardRepository) {
    val scope = rememberCoroutineScope()
    val myCard by repo.myCard.collectAsState(initial = null)
    var editing by remember { mutableStateOf(false) }
    var savedMsg by remember { mutableStateOf(false) }

    // 편집 폼 상태
    var name by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var colorHex by remember { mutableStateOf("#00F5C4") }
    var error by remember { mutableStateOf("") }

    LaunchedEffect(myCard) {
        myCard?.let {
            if (!editing) {
                name = it.name; title = it.title; company = it.company
                phone = it.phone; email = it.email; website = it.website
                colorHex = it.colorHex
            }
        }
        if (myCard == null) editing = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 56.dp, bottom = 24.dp)
    ) {
        // 헤더
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("내 명함", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.5).sp)
            OutlinedButton(
                onClick = {
                    if (editing) {
                        if (name.isBlank() || company.isBlank() || phone.isBlank() || email.isBlank()) {
                            error = "이름, 회사, 전화번호, 이메일은 필수입니다"
                        } else {
                            scope.launch {
                                repo.saveMyCard(BusinessCard(id = "mine", name = name, title = title, company = company, phone = phone, email = email, website = website, colorHex = colorHex))
                                editing = false; savedMsg = true; error = ""
                            }
                        }
                    } else editing = true
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Accent),
                border = BorderStroke(1.dp, Color(0xFF2E2E4E))
            ) {
                Text(if (editing) "저장" else "편집", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(24.dp))

        // 미리보기
        if (!editing && myCard != null) {
            CardView(myCard!!)
            if (savedMsg) {
                LaunchedEffect(Unit) { kotlinx.coroutines.delay(2000); savedMsg = false }
                Text("✓ 저장됨", color = Accent, fontSize = 13.sp, modifier = Modifier.padding(top = 10.dp).fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }

        // 편집 폼
        if (editing) {
            if (error.isNotEmpty()) {
                Text(error, color = Color(0xFFFF6B6B), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
            }

            // 색상 선택
            Text("카드 색상", fontSize = 11.sp, color = TextSecondary, letterSpacing = 1.5.sp, modifier = Modifier.padding(top = 14.dp, bottom = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                COLORS.forEach { c ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(c)))
                            .clickable { colorHex = c }
                            .then(if (colorHex == c) Modifier.border(3.dp, Color.White, CircleShape) else Modifier)
                    )
                }
            }

            CardField("이름 *", name, { name = it })
            CardField("직함", title, { title = it })
            CardField("회사 *", company, { company = it })
            CardField("전화번호 *", phone, { phone = it }, KeyboardType.Phone)
            CardField("이메일 *", email, { email = it }, KeyboardType.Email)
            CardField("웹사이트", website, { website = it }, KeyboardType.Uri)

            Spacer(Modifier.height(28.dp))
            Button(
                onClick = {
                    if (name.isBlank() || company.isBlank() || phone.isBlank() || email.isBlank()) {
                        error = "이름, 회사, 전화번호, 이메일은 필수입니다"
                    } else {
                        scope.launch {
                            repo.saveMyCard(BusinessCard(id = "mine", name = name, title = title, company = company, phone = phone, email = email, website = website, colorHex = colorHex))
                            editing = false; savedMsg = true; error = ""
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = DarkBg)
            ) {
                Text("저장하기", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun CardField(label: String, value: String, onChange: (String) -> Unit, keyboard: KeyboardType = KeyboardType.Text) {
    Column(modifier = Modifier.padding(top = 14.dp)) {
        Text(label, fontSize = 11.sp, color = TextSecondary, letterSpacing = 1.5.sp, modifier = Modifier.padding(bottom = 6.dp))
        OutlinedTextField(
            value = value, onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboard),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Accent, unfocusedBorderColor = Color(0xFF1E1E32),
                focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                cursorColor = Accent, focusedContainerColor = DarkSurface, unfocusedContainerColor = DarkSurface
            )
        )
    }
}
