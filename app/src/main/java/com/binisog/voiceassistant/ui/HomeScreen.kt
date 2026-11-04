package com.binisog.voiceassistant.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.binisog.voiceassistant.UiState
import com.binisog.voiceassistant.brain.Lang

/** Design screen 1: the "Super App" home with the highlighted *Create Account* tile. */
@Composable
fun HomeScreen(
    state: UiState,
    onLanguage: (Lang) -> Unit,
    onCreateAccount: () -> Unit,
    onSessions: () -> Unit,
    onDismissCrash: () -> Unit = {},
    onSaveServer: (String) -> Unit = {},
    onTestServer: () -> Unit = {},
) {
    val lang = state.lang
    var notice by remember { mutableStateOf<String?>(null) }
    var serverDialog by remember { mutableStateOf(false) }
    val soon = { notice = tr(lang, "Available after you open an account.", "አካውንት ከከፈቱ በኋላ ይገኛል።") }

    Column(Modifier.fillMaxSize().background(Color(0xFFF4F6FB))) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            // Blue header with logo, greeting and language.
            Column(
                Modifier.fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Brand.Navy, Brand.Blue)), RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp))
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Brand.Gold), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AccountBalance, null, tint = Brand.Navy)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Demo Bank", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(tr(lang, "Super App", "ሱፐር አፕ"), color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                    Icon(Icons.Default.Notifications, null, tint = Color.White)
                }
                Spacer(Modifier.height(18.dp))
                Text(tr(lang, "Welcome to", "እንኳን ወደ"), color = Color.White.copy(alpha = 0.9f), fontSize = 16.sp)
                Text(tr(lang, "Demo Bank Super App", "ዴሞ ባንክ ሱፐር አፕ በደህና መጡ"), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(tr(lang, "Language", "ቋንቋ"), color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                    for (l in Lang.entries) {
                        FilterChip(
                            selected = lang == l, onClick = { onLanguage(l) }, label = { Text(l.nativeName) },
                            colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                containerColor = Color.White.copy(alpha = 0.15f), labelColor = Color.White,
                                selectedContainerColor = Color.White, selectedLabelColor = Brand.Navy,
                            ),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                        )
                    }
                }
            }

            Column(Modifier.padding(16.dp)) {
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Tile(Icons.Default.PersonAdd, tr(lang, "Create Account", "አካውንት ክፈት"), highlighted = true, onClick = onCreateAccount, modifier = Modifier.weight(1f))
                            Tile(Icons.Default.Lock, tr(lang, "Login", "ግባ"), onClick = soon, modifier = Modifier.weight(1f))
                            Tile(Icons.Default.Send, tr(lang, "Send Money", "ገንዘብ ላክ"), onClick = soon, modifier = Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Tile(Icons.Default.Receipt, tr(lang, "Pay Bills", "ክፍያዎች"), onClick = soon, modifier = Modifier.weight(1f))
                            Tile(Icons.Default.PhoneAndroid, tr(lang, "Mobile Top-up", "የአየር ሰዓት"), onClick = soon, modifier = Modifier.weight(1f))
                            Tile(Icons.Default.Apps, tr(lang, "More", "ተጨማሪ"), onClick = onSessions, modifier = Modifier.weight(1f))
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                // Banner
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                        .background(Brush.horizontalGradient(listOf(Brand.Blue, Brand.Navy)))
                        .clickable(onClick = onCreateAccount).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(tr(lang, "Open your account in minutes", "አካውንትዎን በደቂቃዎች ይክፈቱ"), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                        Text(tr(lang, "with our AI Video Assistant", "በAI ቪዲዮ ረዳታችን"), color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                    }
                    Box(Modifier.size(36.dp).clip(CircleShape).background(Brand.Gold), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ChevronRight, null, tint = Brand.Navy)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WifiOff, null, tint = Brand.Muted, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (state.serverUrl.isNotBlank()) tr(lang, "AI runs on this phone, helped by your computer (${state.serverStatus}).", "AI በዚህ ስልክ ይሰራል፤ ኮምፒውተርዎም ያግዛል (${state.serverStatus})።")
                        else tr(lang, "Speech recognition, voice and assistant run 100% on this phone.", "የንግግር ማወቂያ፣ ድምፅ እና ረዳቱ ሙሉ በሙሉ በዚህ ስልክ ላይ ይሰራሉ።"),
                        color = Brand.Muted, fontSize = 12.sp,
                    )
                }
                if (state.modelsLoading) {
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text(state.modelsMessage, fontSize = 12.sp, color = Brand.Ink)
                    }
                } else if (state.modelsMessage.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Text(state.modelsMessage, fontSize = 12.sp, color = Brand.Red)
                }
                state.lastCrash?.let { crash ->
                    Spacer(Modifier.height(12.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))) {
                        Column(Modifier.padding(12.dp)) {
                            Text("The app stopped last time. Please send this to the developer:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Brand.Red)
                            androidx.compose.foundation.text.selection.SelectionContainer {
                                Text(crash.take(3000), fontSize = 10.sp, color = Brand.Ink, lineHeight = 12.sp)
                            }
                            TextButton(onClick = onDismissCrash) { Text("Dismiss") }
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(state.modelSummary, fontSize = 10.sp, color = Brand.Muted)
            }
        }

        // Bottom navigation: Home, Services, Support, More.
        Row(
            Modifier.fillMaxWidth().background(Color.White).navigationBarsPadding().padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            NavItem(Icons.Default.Home, tr(lang, "Home", "መነሻ"), selected = true) {}
            NavItem(Icons.Default.Apps, tr(lang, "Services", "አገልግሎቶች")) { soon() }
            NavItem(Icons.Default.SupportAgent, tr(lang, "Support", "እገዛ")) { serverDialog = true }
            NavItem(Icons.Default.MoreHoriz, tr(lang, "More", "ተጨማሪ")) { onSessions() }
        }
    }

    notice?.let {
        AlertDialog(
            onDismissRequest = { notice = null },
            confirmButton = { TextButton(onClick = { notice = null; onCreateAccount() }) { Text(tr(lang, "Create Account", "አካውንት ክፈት")) } },
            dismissButton = { TextButton(onClick = { notice = null }) { Text(tr(lang, "Close", "ዝጋ")) } },
            text = { Text(it) },
        )
    }
    if (serverDialog) LocalServerDialog(state, onDismiss = { serverDialog = false }, onSave = onSaveServer, onTest = onTestServer)
}

/**
 * Support → "AI on my computer". Optional: a computer on the same Wi-Fi running local-server/
 * transcribes with bigger models. The phone keeps its own models and uses them whenever the
 * computer is off or slow.
 */
@Composable
private fun LocalServerDialog(state: UiState, onDismiss: () -> Unit, onSave: (String) -> Unit, onTest: () -> Unit) {
    val lang = state.lang
    var url by remember { mutableStateOf(state.serverUrl) }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Computer, null) },
        title = { Text(tr(lang, "AI on my computer (optional)", "AI በኮምፒውተሬ (አማራጭ)")) },
        text = {
            Column {
                Text(
                    tr(lang, "The assistant always works offline on this phone. To also use the bigger models on your computer, run local-server on it (same Wi-Fi) and enter its address, e.g. http://192.168.1.20:8765. Leave empty to use the phone only.",
                        "ረዳቱ ሁልጊዜ በዚህ ስልክ ላይ ያለ ኢንተርኔት ይሰራል። በኮምፒውተርዎ ያሉትን ትላልቅ ሞዴሎች ለመጠቀም local-server ን ያስጀምሩና አድራሻውን ያስገቡ (ለምሳሌ http://192.168.1.20:8765)። ባዶ ከተዉት ስልኩ ብቻ ይሰራል።"),
                    fontSize = 13.sp,
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = url, onValueChange = { url = it.trim() }, singleLine = true, label = { Text("http://<computer-ip>:8765") })
                Spacer(Modifier.height(6.dp))
                Text(tr(lang, "Status: ", "ሁኔታ፦ ") + state.serverStatus.ifEmpty { "—" }, fontSize = 12.sp, color = Brand.Muted)
            }
        },
        confirmButton = { TextButton(onClick = { onSave(url); onTest() }) { Text(tr(lang, "Save & test", "አስቀምጥና ሞክር")) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(tr(lang, "Close", "ዝጋ")) } },
    )
}

@Composable
private fun NavItem(icon: ImageVector, label: String, selected: Boolean = false, onClick: () -> Unit) {
    Column(
        Modifier.clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val c = if (selected) Brand.Blue else Brand.Muted
        Icon(icon, label, tint = c)
        Text(label, color = c, fontSize = 11.sp)
    }
}

@Composable
private fun Tile(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier, highlighted: Boolean = false) {
    Card(
        modifier = modifier.aspectRatio(1f).clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        border = if (highlighted) BorderStroke(2.dp, Brand.Gold) else null,
        colors = CardDefaults.cardColors(containerColor = if (highlighted) Brand.Blue else Brand.Sky),
    ) {
        Column(Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            val tint = if (highlighted) Color.White else Brand.Blue
            Icon(icon, null, tint = tint)
            Spacer(Modifier.height(6.dp))
            Text(label, color = tint, fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 14.sp, textAlign = TextAlign.Center)
        }
    }
}

/** "Go to My Account" after the account is created. */
@Composable
fun MyAccountScreen(state: UiState, onHome: () -> Unit, onDownload: () -> Unit) {
    val lang = state.lang
    val p = state.profile
    Column(Modifier.fillMaxSize().background(Color(0xFFF4F6FB)).verticalScroll(rememberScrollState())) {
        Column(
            Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Brand.Navy, Brand.Blue)), RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp))
                .padding(20.dp)
        ) {
            Text(tr(lang, "My Account", "አካውንቴ"), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(p.fullName ?: "", color = Color.White.copy(alpha = 0.9f))
            Spacer(Modifier.height(18.dp))
            Text(tr(lang, "Available balance", "ያለ ቀሪ ሂሳብ"), color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
            Text("ETB 0.00", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                (p.accountType?.display(lang) ?: "") + " · " + (p.accountNumber?.let { com.binisog.voiceassistant.brain.AccountOpeningAgent.formatGroups(it) } ?: ""),
                color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp,
            )
        }
        Column(Modifier.padding(16.dp)) {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text(tr(lang, "Recent transactions", "የቅርብ ጊዜ ግብይቶች"), fontWeight = FontWeight.SemiBold, color = Brand.Ink)
                    Spacer(Modifier.height(8.dp))
                    Text(tr(lang, "No transactions yet. Deposit at any Demo Bank branch to get started.", "እስካሁን ግብይት የለም። በማንኛውም የዴሞ ባንክ ቅርንጫፍ ገቢ በማድረግ ይጀምሩ።"), color = Brand.Muted, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(14.dp))
            androidx.compose.material3.OutlinedButton(onClick = onDownload, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp)) {
                Text(tr(lang, "Download Account Details", "የአካውንት መረጃ አውርድ"))
            }
            Spacer(Modifier.height(10.dp))
            androidx.compose.material3.Button(onClick = onHome, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp)) {
                Text(tr(lang, "Back to Home", "ወደ መነሻ ተመለስ"))
            }
        }
    }
}
