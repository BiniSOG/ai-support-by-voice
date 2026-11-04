package com.binisog.voiceassistant.ui

import android.app.DatePickerDialog
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.binisog.voiceassistant.AssistantViewModel
import com.binisog.voiceassistant.UiState
import com.binisog.voiceassistant.brain.AccountOpeningAgent
import com.binisog.voiceassistant.brain.DateParser
import com.binisog.voiceassistant.brain.Lang
import com.binisog.voiceassistant.brain.Step
import kotlinx.coroutines.delay
import java.time.LocalDate

fun formatDuration(ms: Long): String {
    val s = (ms / 1000).coerceAtLeast(0)
    return "%02d:%02d".format(s / 60, s % 60)
}

/**
 * The account-opening flow, laid out like the design:
 * video call with Hana (greeting, name, date of birth, birth city, ..., FAN) → OTP → confirm your
 * information → creating your account → account created.
 */
@Composable
fun CallScreen(state: UiState, vm: AssistantViewModel) {
    when (state.step) {
        Step.OTP -> FlowPage(state, vm) { OtpPage(state, vm) }
        Step.REVIEW -> FlowPage(state, vm) { ReviewPage(state, vm) }
        Step.CREATING -> FlowPage(state, vm) { CreatingPage(state) }
        Step.DONE -> FlowPage(state, vm, showCamera = false) { SuccessPage(state, vm) }
        else -> VideoCallPage(state, vm)
    }
}

// ---------------------------------------------------------------------- shared pieces

@Composable
private fun BrandHeader(title: String, subtitle: String, trailing: @Composable () -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(Brand.Navy, Brand.Blue)))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(34.dp).clip(RoundedCornerShape(8.dp)).background(Brand.Gold), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.AccountBalance, null, tint = Brand.Navy, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(subtitle, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
        }
        trailing()
    }
}

@Composable
private fun RecBadge(recording: Boolean) {
    if (!recording) return
    val t = rememberInfiniteTransition(label = "rec")
    val a by t.animateFloat(1f, 0.25f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "recAlpha")
    Row(
        Modifier.clip(RoundedCornerShape(10.dp)).background(Color.Black.copy(alpha = 0.35f)).padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(7.dp).alpha(a).clip(CircleShape).background(Brand.Red))
        Spacer(Modifier.width(4.dp))
        Text("REC", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

/** Front camera self-view; the camera keeps recording the whole session. */
@Composable
private fun CameraPip(vm: AssistantViewModel, modifier: Modifier, width: Int = 84, height: Int = 112) {
    val owner = LocalLifecycleOwner.current
    Box(
        modifier.size(width.dp, height.dp).clip(RoundedCornerShape(12.dp))
            .border(1.5.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
            .background(Color.DarkGray)
    ) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    vm.camera.bind(owner, this)
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun MoreMenu(state: UiState, vm: AssistantViewModel) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }) { Icon(Icons.Default.MoreVert, "More", tint = Color.White) }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(
                text = { Text(if (state.lang == Lang.EN) "አማርኛ" else "English") },
                leadingIcon = { Icon(Icons.Default.Translate, null) },
                onClick = { open = false; vm.switchLanguageInCall() },
            )
            DropdownMenuItem(
                text = { Text(tr(state.lang, "Type instead", "በመጻፍ መልስ")) },
                leadingIcon = { Icon(Icons.Default.Keyboard, null) },
                onClick = { open = false; vm.toggleTyping() },
            )
            DropdownMenuItem(
                text = { Text(tr(state.lang, "Subtitles", "የጽሑፍ ትርጉም")) },
                leadingIcon = { Icon(Icons.Default.ClosedCaption, null) },
                onClick = { open = false; vm.toggleCaptions() },
            )
        }
    }
}

/** White pages of the flow (OTP, confirm, creating, success) under the "Super App" header. */
@Composable
private fun FlowPage(state: UiState, vm: AssistantViewModel, showCamera: Boolean = true, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().background(Color(0xFFF4F6FB))) {
        BrandHeader("Demo Bank", tr(state.lang, "Super App", "ሱፐር አፕ")) {
            RecBadge(state.recording)
            if (showCamera) {
                Spacer(Modifier.width(8.dp))
                CameraPip(vm, Modifier, width = 36, height = 48)
            }
        }
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).imePadding().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content,
        )
        HeardLine(state, dark = false)
    }
}

/** What the customer just said (small, so the conversation stays visible on every page). */
@Composable
private fun HeardLine(state: UiState, dark: Boolean) {
    val heard = state.liveCaption.ifEmpty { state.lastHeard }
    val listening = state.micOn && !state.micMuted && !state.speaking
    val color = if (dark) Color.White else Brand.Ink
    Row(
        Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val t = rememberInfiniteTransition(label = "dot")
        val a by t.animateFloat(0.3f, 1f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "dotA")
        Box(
            Modifier.size(8.dp).alpha(if (listening || state.speaking) a else 1f).clip(CircleShape)
                .background(if (state.speaking) Brand.Gold else if (listening) Brand.Green else Brand.Muted)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            when {
                heard.isNotEmpty() -> tr(state.lang, "You: ", "እርስዎ፦ ") + heard
                state.speaking -> tr(state.lang, "Hana is speaking", "ሐና እየተናገረች ነው")
                listening -> tr(state.lang, "Listening…", "እያዳመጠች ነው…")
                else -> ""
            },
            color = color.copy(alpha = 0.85f), fontSize = 13.sp, maxLines = 2,
        )
    }
}

/** Field with an icon, the heard/typed value and an edit pencil (design screens 4–6). */
@Composable
private fun ValueField(
    icon: ImageVector,
    label: String?,
    value: String,
    placeholder: String,
    digits: Boolean = false,
    onEdit: (() -> Unit)? = null,
    onTyped: ((String) -> Unit)? = null,
) {
    var editing by remember(value) { mutableStateOf(false) }
    var text by remember(value) { mutableStateOf(value) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(editing) { if (editing) runCatching { focus.requestFocus() } }
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White)
            .border(1.dp, Color(0xFFD6DEEF), RoundedCornerShape(12.dp))
            .clickable(enabled = !editing) { if (onEdit != null) onEdit() else if (onTyped != null) editing = true }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        if (label != null) Text(label, color = Brand.Muted, fontSize = 11.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Brand.Blue, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Box(Modifier.weight(1f)) {
                if (editing && onTyped != null) {
                    BasicTextField(
                        value = text,
                        onValueChange = { text = it },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 17.sp, color = Brand.Ink),
                        keyboardOptions = KeyboardOptions(keyboardType = if (digits) KeyboardType.Number else KeyboardType.Text),
                        keyboardActions = KeyboardActions(onDone = { editing = false; if (text.isNotBlank()) onTyped(text) }),
                        modifier = Modifier.fillMaxWidth().focusRequester(focus),
                    )
                } else {
                    Text(
                        value.ifEmpty { placeholder },
                        color = if (value.isEmpty()) Brand.Muted else Brand.Ink,
                        fontSize = 17.sp, fontWeight = if (value.isEmpty()) FontWeight.Normal else FontWeight.Medium,
                    )
                }
            }
            IconButton(onClick = {
                if (onEdit != null) onEdit()
                else if (editing && onTyped != null) { editing = false; if (text.isNotBlank()) onTyped(text) }
                else editing = true
            }) { Icon(if (editing) Icons.Default.Check else Icons.Default.Edit, "Edit", tint = Brand.Blue, modifier = Modifier.size(20.dp)) }
        }
    }
}

@Composable
private fun PrimaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick, enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Brand.Blue),
    ) { Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
}

// ---------------------------------------------------------------------- 2–6: video call

@Composable
private fun VideoCallPage(state: UiState, vm: AssistantViewModel) {
    val lang = state.lang
    Column(Modifier.fillMaxSize().background(Color.White)) {
        BrandHeader("Demo Bank", tr(lang, "AI Account Assistant", "የAI አካውንት ረዳት")) { MoreMenu(state, vm) }
        Box(Modifier.fillMaxWidth().weight(1f)) {
            AgentVideo(state.speaking, { vm.speakingLevel }, Modifier.fillMaxSize())
            Row(Modifier.align(Alignment.TopStart).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                CallTimer(state)
                Spacer(Modifier.width(6.dp))
                RecBadge(state.recording)
            }
            CameraPip(vm, Modifier.align(Alignment.TopEnd).padding(10.dp))
            if (state.modelsLoading) {
                Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    Text(tr(lang, "Connecting…", "በማገናኘት ላይ…"), color = Color.White)
                }
            }
            // Hana's speech bubble, as in the design.
            if (state.assistantText.isNotEmpty()) {
                Card(
                    Modifier.align(Alignment.BottomStart).padding(horizontal = 14.dp, vertical = 12.dp).fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
                    elevation = CardDefaults.cardElevation(3.dp),
                ) {
                    Text(
                        state.assistantText.substringBefore("\n\n"),
                        Modifier.padding(horizontal = 14.dp, vertical = 10.dp).verticalScroll(rememberScrollState()),
                        color = Brand.Ink, fontSize = 15.sp, lineHeight = 20.sp, maxLines = 6,
                    )
                }
            }
        }
        Column(
            Modifier.fillMaxWidth().background(Color.White).imePadding().padding(horizontal = 16.dp).padding(top = 12.dp)
        ) {
            StepInput(state, vm)
        }
        HeardLine(state, dark = false)
    }
}

@Composable
private fun CallTimer(state: UiState) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(500) } }
    Text(
        if (state.callStartedAt > 0) formatDuration(now - state.callStartedAt) else "00:00",
        color = Color.White, fontSize = 12.sp,
        modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Color.Black.copy(alpha = 0.35f)).padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

/** Bottom of the video call: what the design shows for each question. */
@Composable
private fun StepInput(state: UiState, vm: AssistantViewModel) {
    val lang = state.lang
    val context = LocalContext.current
    val pending = state.pendingValue
    when (state.step) {
        Step.GREETING -> CallControls(state, vm)
        Step.FULL_NAME -> {
            if (pending != null) {
                ValueField(Icons.Default.Person, tr(lang, "Full name", "ሙሉ ስም"), pending, "", onTyped = { vm.onUserText(it, typed = true) })
                Spacer(Modifier.height(10.dp))
                PrimaryButton(tr(lang, "Confirm", "አረጋግጥ")) { vm.confirm() }
            } else {
                ListeningBar(state, vm)
            }
        }
        Step.DOB -> {
            ValueField(
                Icons.Default.CalendarToday, null, pending ?: "",
                tr(lang, "Say or pick your date of birth", "የትውልድ ቀንዎን ይናገሩ ወይም ይምረጡ"),
                onEdit = {
                    val init = state.profile.dateOfBirth ?: LocalDate.of(1995, 1, 1)
                    DatePickerDialog(context, { _, y, m, d -> vm.pickDate(LocalDate.of(y, m + 1, d)) }, init.year, init.monthValue - 1, init.dayOfMonth).show()
                },
            )
            Spacer(Modifier.height(10.dp))
            PrimaryButton(tr(lang, "Confirm", "አረጋግጥ"), enabled = pending != null) { vm.confirm() }
        }
        Step.BIRTH_CITY -> {
            ValueField(Icons.Default.LocationOn, null, pending ?: "", tr(lang, "Say the city you were born in", "የተወለዱበትን ከተማ ይናገሩ"),
                onTyped = { vm.onUserText(it, typed = true) })
            SuggestionChips(state, vm)
            Spacer(Modifier.height(10.dp))
            PrimaryButton(tr(lang, "Confirm", "አረጋግጥ"), enabled = pending != null) { vm.confirm() }
        }
        Step.NATIONALITY -> {
            ValueField(Icons.Default.Public, null, pending ?: "", tr(lang, "Say your nationality", "ዜግነትዎን ይናገሩ"),
                onTyped = { vm.onUserText(it, typed = true) })
            SuggestionChips(state, vm)
            Spacer(Modifier.height(10.dp))
            PrimaryButton(tr(lang, "Confirm", "አረጋግጥ"), enabled = pending != null) { vm.confirm() }
        }
        Step.ACCOUNT_TYPE -> {
            ValueField(Icons.Default.AccountBalanceWallet, null, "", tr(lang, "Say or tap an account type", "የሂሳብ ዓይነት ይናገሩ ወይም ይምረጡ"))
            SuggestionChips(state, vm)
        }
        Step.FAN -> {
            val shown = pending ?: AccountOpeningAgent.formatGroups(state.digitBuffer)
            ValueField(Icons.Default.Badge, tr(lang, "FAN (National ID Number)", "FAN (የብሔራዊ መታወቂያ ቁጥር)"), shown,
                tr(lang, "Say or type your 12-digit FAN", "ባለ 12 አሃዝ FAN ይናገሩ ወይም ይጻፉ"), digits = true,
                onTyped = { vm.onUserText(it, typed = true) })
            Spacer(Modifier.height(10.dp))
            PrimaryButton(tr(lang, "Continue", "ቀጥል"), enabled = pending != null) { vm.confirm() }
        }
        else -> Unit
    }
    if (state.step != Step.GREETING && (state.step != Step.FULL_NAME || pending != null)) {
        Spacer(Modifier.height(10.dp))
        CallControls(state, vm, compact = true)
    }
}

/** Mic, camera, end call, more – as on design screen 2. */
@Composable
private fun CallControls(state: UiState, vm: AssistantViewModel, compact: Boolean = false) {
    val size = if (compact) 44 else 54
    Row(Modifier.fillMaxWidth().padding(vertical = if (compact) 0.dp else 6.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
        CircleButton(if (state.micMuted) Icons.Default.MicOff else Icons.Default.Mic, "Mute", if (state.micMuted) Brand.Muted else Brand.Sky,
            if (state.micMuted) Color.White else Brand.Blue, size) { vm.toggleMute() }
        CircleButton(Icons.Default.Videocam, "Camera", Brand.Sky, Brand.Blue, size) { }
        CircleButton(Icons.Default.CallEnd, "End call", Brand.Red, Color.White, size + 8) { vm.endCall() }
        CircleButton(Icons.Default.Keyboard, "Type instead", if (state.typing) Brand.Blue else Brand.Sky, if (state.typing) Color.White else Brand.Blue, size) { vm.toggleTyping() }
    }
    if (state.typing && state.step == Step.GREETING) {
        Spacer(Modifier.height(6.dp))
        PrimaryButton(tr(state.lang, "Yes, let's start", "አዎ፣ እንጀምር")) { vm.onUserText(if (state.lang == Lang.AM) "አዎ" else "yes", typed = true) }
    }
}

@Composable
private fun CircleButton(icon: ImageVector, description: String, bg: Color, tint: Color, size: Int, onClick: () -> Unit) {
    Box(Modifier.size(size.dp).clip(CircleShape).background(bg).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, description, tint = tint)
    }
}

/** Design screen 3: the blue "listening" bar with a waveform and the red end-call button. */
@Composable
private fun ListeningBar(state: UiState, vm: AssistantViewModel) {
    val t = rememberInfiniteTransition(label = "wave")
    val phase by t.animateFloat(0f, 1f, infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "phase")
    val active = state.liveCaption.isNotEmpty() || state.speaking
    Row(verticalAlignment = Alignment.CenterVertically) {
        Row(
            Modifier.weight(1f).height(56.dp).clip(RoundedCornerShape(28.dp))
                .background(Brush.horizontalGradient(listOf(Brand.Navy, Brand.Blue))).padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            for (i in 0 until 22) {
                val base = ((i * 37) % 10) / 10f
                val h = if (active) 6f + 26f * (base * phase + (1 - phase) * ((i * 53) % 10) / 10f) else 4f + 6f * base
                Box(Modifier.width(3.dp).height(h.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.9f)))
            }
        }
        Spacer(Modifier.width(12.dp))
        CircleButton(Icons.Default.CallEnd, "End call", Brand.Red, Color.White, 52) { vm.endCall() }
    }
    if (state.typing) {
        Spacer(Modifier.height(8.dp))
        ValueField(Icons.Default.Person, tr(state.lang, "Full name", "ሙሉ ስም"), "", tr(state.lang, "Type your full name", "ሙሉ ስምዎን ይጻፉ"),
            onTyped = { vm.onUserText(it, typed = true) })
    }
}

/** Answers Hana expects right now: say one, or tap it. */
@Composable
private fun SuggestionChips(state: UiState, vm: AssistantViewModel) {
    if (state.suggestions.isEmpty()) return
    Spacer(Modifier.height(8.dp))
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (s in state.suggestions) {
            Box(
                Modifier.clip(RoundedCornerShape(18.dp)).background(Brand.Sky)
                    .border(1.dp, Color(0xFFBCCBEA), RoundedCornerShape(18.dp))
                    .clickable { vm.onUserText(s, typed = true) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) { Text(s, color = Brand.Blue, fontSize = 14.sp, maxLines = 1) }
        }
    }
}

// ---------------------------------------------------------------------- 7: OTP

@Composable
private fun ColumnScope.OtpPage(state: UiState, vm: AssistantViewModel) {
    val lang = state.lang
    var typed by remember { mutableStateOf("") }
    val code = (if (typed.isNotEmpty()) typed else state.digitBuffer).take(6)
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(1000) } }
    val wait = (60 - (now - state.otpSentAt) / 1000).coerceAtLeast(0)

    Spacer(Modifier.height(16.dp))
    Box(Modifier.size(72.dp).clip(CircleShape).background(Brand.Sky), contentAlignment = Alignment.Center) {
        Icon(Icons.Default.VerifiedUser, null, tint = Brand.Blue, modifier = Modifier.size(40.dp))
    }
    Spacer(Modifier.height(16.dp))
    Text(tr(lang, "Verify with OTP", "በOTP ያረጋግጡ"), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Brand.Ink)
    Spacer(Modifier.height(8.dp))
    Text(
        tr(lang, "We've sent a one-time password (OTP) to your registered mobile number from National ID.",
            "ከብሔራዊ መታወቂያ ወደተመዘገበው ስልክ ቁጥርዎ የአንድ ጊዜ የይለፍ ኮድ (OTP) ልከናል።"),
        color = Brand.Muted, textAlign = TextAlign.Center, fontSize = 14.sp,
    )
    state.otpBanner?.let {
        Spacer(Modifier.height(10.dp))
        Text(tr(lang, "Demo SMS (offline): ", "የማሳያ መልእክት (ከመስመር ውጭ)፦ ") + it, color = Brand.Ink, fontSize = 13.sp,
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFFFFF4D6)).padding(horizontal = 10.dp, vertical = 6.dp))
    }
    Spacer(Modifier.height(20.dp))
    // Six boxes; tapping them opens the keyboard.
    Box(contentAlignment = Alignment.Center) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in 0 until 6) {
                Box(
                    Modifier.size(44.dp, 52.dp).clip(RoundedCornerShape(10.dp)).background(Color.White)
                        .border(1.5.dp, if (i == code.length) Brand.Blue else Color(0xFFD6DEEF), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) { Text(code.getOrNull(i)?.toString() ?: "", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = Brand.Ink) }
            }
        }
        BasicTextField(
            value = typed,
            onValueChange = { v -> typed = v.filter { it.isDigit() }.take(6) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            textStyle = TextStyle(color = Color.Transparent),
            modifier = Modifier.matchParentSizeCompat().alpha(0.01f),
        )
    }
    Spacer(Modifier.height(16.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(tr(lang, "Didn't receive the OTP? ", "ኮዱ አልደረሰዎትም? "), color = Brand.Muted, fontSize = 13.sp)
        Text(
            if (wait > 0) tr(lang, "Resend (${wait}s)", "እንደገና ላክ (${wait}s)") else tr(lang, "Resend", "እንደገና ላክ"),
            color = if (wait > 0) Brand.Muted else Brand.Blue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(enabled = wait == 0L) { typed = ""; vm.resendOtp() },
        )
    }
    Spacer(Modifier.height(24.dp))
    PrimaryButton(tr(lang, "Verify OTP", "OTP አረጋግጥ"), enabled = code.length == 6) { vm.onUserText(code, typed = true); typed = "" }
}

private fun Modifier.matchParentSizeCompat() = this.size(310.dp, 52.dp)

// ---------------------------------------------------------------------- 8: confirm your information

@Composable
private fun ColumnScope.ReviewPage(state: UiState, vm: AssistantViewModel) {
    val lang = state.lang
    val p = state.profile
    Text(tr(lang, "Confirm Your Information", "መረጃዎን ያረጋግጡ"), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Brand.Ink)
    Spacer(Modifier.height(6.dp))
    Text(
        tr(lang, "We retrieved the following details from the National ID database. Please review and confirm.",
            "የሚከተለውን መረጃ ከብሔራዊ መታወቂያ መረጃ ቋት አግኝተናል። እባክዎ ይመልከቱና ያረጋግጡ።"),
        color = Brand.Muted, fontSize = 13.sp, textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(14.dp))
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(vertical = 4.dp)) {
            InfoRow(Icons.Default.Person, tr(lang, "Full Name", "ሙሉ ስም"), p.fullName) { vm.edit(Step.FULL_NAME) }
            InfoRow(Icons.Default.CalendarToday, tr(lang, "Date of Birth", "የትውልድ ቀን"), p.dateOfBirth?.let { DateParser.display(it, lang) }) { vm.edit(Step.DOB) }
            InfoRow(Icons.Default.LocationOn, tr(lang, "Birth City", "የትውልድ ከተማ"), p.birthCity) { vm.edit(Step.BIRTH_CITY) }
            InfoRow(Icons.Default.Public, tr(lang, "Nationality", "ዜግነት"), p.nationality) { vm.edit(Step.NATIONALITY) }
            InfoRow(Icons.Default.AccountBalanceWallet, tr(lang, "Account Type", "የሂሳብ ዓይነት"), p.accountType?.display(lang)) { vm.edit(Step.ACCOUNT_TYPE) }
            InfoRow(Icons.Default.Badge, "FAN", p.fan?.let { AccountOpeningAgent.formatGroups(it) }, last = true) { vm.edit(Step.FAN) }
        }
    }
    Spacer(Modifier.height(18.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = { vm.edit(Step.FULL_NAME) }, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp)) {
            Text(tr(lang, "Edit", "አስተካክል"))
        }
        Button(
            onClick = { vm.confirm() }, modifier = Modifier.weight(2f).height(48.dp), shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Brand.Blue),
        ) { Text(tr(lang, "Confirm & Create Account", "አረጋግጥና ክፈት"), fontSize = 14.sp) }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String?, last: Boolean = false, onEdit: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onEdit).padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Brand.Blue, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, color = Brand.Muted, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(value ?: "—", color = Brand.Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.weight(1.3f))
    }
    if (!last) HorizontalDivider(color = Brand.Sky)
}

// ---------------------------------------------------------------------- 9: creating your account

@Composable
private fun ColumnScope.CreatingPage(state: UiState) {
    val lang = state.lang
    val items = listOf(
        tr(lang, "Verifying identity (National ID)", "ማንነትን በማረጋገጥ ላይ (ብሔራዊ መታወቂያ)"),
        tr(lang, "Retrieving your details", "መረጃዎን በማምጣት ላይ"),
        tr(lang, "Creating Demo Bank account…", "የዴሞ ባንክ አካውንት በመክፈት ላይ…"),
        tr(lang, "Setting up your profile", "መገለጫዎን በማዘጋጀት ላይ"),
    )
    Spacer(Modifier.height(30.dp))
    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { (state.creatingProgress / 4f).coerceIn(0.05f, 1f) },
            modifier = Modifier.size(120.dp), strokeWidth = 7.dp, color = Brand.Blue, trackColor = Brand.Sky,
        )
        Icon(Icons.Default.AccountBalance, null, tint = Brand.Blue, modifier = Modifier.size(52.dp))
    }
    Spacer(Modifier.height(22.dp))
    Text(tr(lang, "Creating Your Account…", "አካውንትዎ እየተከፈተ ነው…"), fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Brand.Ink)
    Spacer(Modifier.height(22.dp))
    items.forEachIndexed { i, label ->
        Row(Modifier.fillMaxWidth().padding(vertical = 7.dp, horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            when {
                i < state.creatingProgress -> Icon(Icons.Default.CheckCircle, null, tint = Brand.Green)
                i == state.creatingProgress -> CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                else -> Icon(Icons.Default.RadioButtonUnchecked, null, tint = Brand.Muted)
            }
            Spacer(Modifier.width(12.dp))
            Text(label, color = Brand.Ink, fontSize = 15.sp)
        }
    }
}

// ---------------------------------------------------------------------- 10: success

@Composable
private fun ColumnScope.SuccessPage(state: UiState, vm: AssistantViewModel) {
    val lang = state.lang
    val p = state.profile
    val context = LocalContext.current
    Spacer(Modifier.height(16.dp))
    Box(Modifier.size(76.dp).clip(CircleShape).background(Brand.Green), contentAlignment = Alignment.Center) {
        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(48.dp))
    }
    Spacer(Modifier.height(14.dp))
    Text(tr(lang, "Account Created Successfully! 🎉", "አካውንትዎ በተሳካ ሁኔታ ተከፍቷል! 🎉"), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Brand.Ink, textAlign = TextAlign.Center)
    Spacer(Modifier.height(6.dp))
    Text(tr(lang, "Your Demo Bank account is now active. You can start using it immediately.", "የዴሞ ባንክ አካውንትዎ አሁን ንቁ ነው። ወዲያውኑ መጠቀም ይችላሉ።"),
        color = Brand.Muted, textAlign = TextAlign.Center, fontSize = 14.sp)
    Spacer(Modifier.height(18.dp))
    Card(colors = CardDefaults.cardColors(containerColor = Brand.Sky), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(tr(lang, "Account Number", "የአካውንት ቁጥር"), color = Brand.Muted, fontSize = 12.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(p.accountNumber?.let { AccountOpeningAgent.formatGroups(it) } ?: "", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Brand.Ink, modifier = Modifier.weight(1f))
                IconButton(onClick = { vm.copyAccountNumber(context) }) { Icon(Icons.Default.ContentCopy, "Copy", tint = Brand.Blue) }
            }
            HorizontalDivider(color = Color.White, modifier = Modifier.padding(vertical = 6.dp))
            Text(tr(lang, "Account Type", "የሂሳብ ዓይነት"), color = Brand.Muted, fontSize = 12.sp)
            Text(p.accountType?.display(lang) ?: "", fontWeight = FontWeight.SemiBold, color = Brand.Ink)
        }
    }
    Spacer(Modifier.height(18.dp))
    PrimaryButton(tr(lang, "Go to My Account", "ወደ አካውንቴ")) { vm.showMyAccount() }
    Spacer(Modifier.height(10.dp))
    OutlinedButton(onClick = { vm.downloadAccountDetails(context) }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp)) {
        Icon(Icons.Default.Download, null)
        Spacer(Modifier.width(8.dp))
        Text(tr(lang, "Download Account Details", "የአካውንት መረጃ አውርድ"))
    }
    Spacer(Modifier.height(14.dp))
    Text(
        when {
            state.savingVideo -> tr(lang, "Saving the call recording…", "የጥሪው ቀረጻ እየተቀመጠ ነው…")
            state.callVideo != null -> tr(lang, "Call recording saved (video with both voices).", "የጥሪው ቀረጻ ተቀምጧል (ቪዲዮ ከሁለቱም ድምፆች ጋር)።")
            else -> ""
        },
        fontSize = 11.sp, color = Brand.Muted, textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(6.dp))
    androidx.compose.material3.TextButton(onClick = { vm.goHome() }) { Text(tr(lang, "Back to Home", "ወደ መነሻ ተመለስ")) }
}
