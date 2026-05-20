package com.nfccard.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ContactPage
import androidx.compose.material.icons.outlined.Nfc
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.compose.*
import com.nfccard.MainActivity
import com.nfccard.data.CardRepository
import com.nfccard.ui.screens.ContactsScreen
import com.nfccard.ui.screens.MyCardScreen
import com.nfccard.ui.screens.NFCScreen

val DarkBg = Color(0xFF0A0A0F)
val DarkSurface = Color(0xFF12121E)
val DarkBorder = Color(0xFF1E1E32)
val Accent = Color(0xFF00F5C4)
val TextPrimary = Color(0xFFEEEEFF)
val TextSecondary = Color(0xFF666688)

@Composable
fun NFCCardApp(nfcAvailable: Boolean, nfcEnabled: Boolean) {
    val context = LocalContext.current
    val repo = remember { CardRepository(context) }
    val navController = rememberNavController()
    val navBackStack by navController.currentBackStackEntryAsState()
    val current = navBackStack?.destination?.route

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = DarkBg,
            surface = DarkSurface,
            primary = Accent,
            onBackground = TextPrimary,
            onSurface = TextPrimary,
        )
    ) {
        Scaffold(
            containerColor = DarkBg,
            bottomBar = {
                NavigationBar(containerColor = DarkSurface, tonalElevation = 0.dp) {
                    NavigationBarItem(
                        selected = current == "mycard",
                        onClick = { navController.navigate("mycard") { launchSingleTop = true } },
                        icon = { Icon(if (current == "mycard") Icons.Filled.Person else Icons.Outlined.Person, null) },
                        label = { Text("내 명함", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Accent, selectedTextColor = Accent,
                            unselectedIconColor = TextSecondary, unselectedTextColor = TextSecondary,
                            indicatorColor = Color(0xFF1A2E2A)
                        )
                    )
                    NavigationBarItem(
                        selected = current == "nfc",
                        onClick = { navController.navigate("nfc") { launchSingleTop = true } },
                        icon = { Icon(if (current == "nfc") Icons.Filled.Nfc else Icons.Outlined.Nfc, null) },
                        label = { Text("NFC 교환", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Accent, selectedTextColor = Accent,
                            unselectedIconColor = TextSecondary, unselectedTextColor = TextSecondary,
                            indicatorColor = Color(0xFF1A2E2A)
                        )
                    )
                    NavigationBarItem(
                        selected = current == "contacts",
                        onClick = { navController.navigate("contacts") { launchSingleTop = true } },
                        icon = { Icon(if (current == "contacts") Icons.Filled.ContactPage else Icons.Outlined.ContactPage, null) },
                        label = { Text("받은 명함", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Accent, selectedTextColor = Accent,
                            unselectedIconColor = TextSecondary, unselectedTextColor = TextSecondary,
                            indicatorColor = Color(0xFF1A2E2A)
                        )
                    )
                }
            }
        ) { padding ->
            NavHost(navController, startDestination = "mycard", Modifier.padding(padding)) {
                composable("mycard") { MyCardScreen(repo) }
                composable("nfc") {
                    val activity = context as MainActivity
                    NFCScreen(repo, nfcAvailable, nfcEnabled, activity)
                }
                composable("contacts") { ContactsScreen(repo) }
            }
        }
    }
}

// sp extension for non-Material contexts
private val Int.sp get() = androidx.compose.ui.unit.TextUnit(this.toFloat(), androidx.compose.ui.unit.TextUnitType.Sp)
private val Int.dp get() = androidx.compose.ui.unit.Dp(this.toFloat())
