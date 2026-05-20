package com.nfccard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nfccard.data.BusinessCard
import com.nfccard.ui.DarkSurface
import com.nfccard.ui.TextSecondary

@Composable
fun CardView(card: BusinessCard, mini: Boolean = false) {
    val accent = parseColor(card.colorHex)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(if (mini) 14.dp else 20.dp))
            .background(DarkSurface)
    ) {
        // 배경 원형 장식
        Box(
            modifier = Modifier
                .size(160.dp)
                .offset(x = 60.dp, y = (-40).dp)
                .background(accent.copy(alpha = 0.08f), RoundedCornerShape(80.dp))
                .align(Alignment.TopEnd)
        )

        Column {
            // 상단 컬러 바
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(accent)
            )

            Column(modifier = Modifier.padding(if (mini) 14.dp else 20.dp)) {
                // 회사명
                Text(
                    card.company.uppercase(),
                    fontSize = if (mini) 10.sp else 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(6.dp))

                // 이름
                Text(
                    card.name,
                    fontSize = if (mini) 20.sp else 26.sp,
                    color = Color(0xFFEEEEFF),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(4.dp))

                // 직함
                Text(
                    card.title,
                    fontSize = 13.sp,
                    color = accent,
                    fontWeight = FontWeight.Medium
                )

                if (!mini) {
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFF1E1E32))
                    Spacer(Modifier.height(16.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (card.phone.isNotEmpty()) ContactRow("📞", card.phone)
                        if (card.email.isNotEmpty()) ContactRow("✉️", card.email)
                        if (card.website.isNotEmpty()) ContactRow("🌐", card.website)
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactRow(icon: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(icon, fontSize = 14.sp, modifier = Modifier.width(22.dp))
        Text(value, fontSize = 13.sp, color = Color(0xFFAAAACC), maxLines = 1)
    }
}

fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color(0xFF00F5C4)
    }
}
