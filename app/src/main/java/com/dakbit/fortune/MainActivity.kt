package com.dakbit.fortune

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class MainActivity : ComponentActivity() {
    companion object {
        const val EXTRA_OPEN_TODAY = "open_today"
    }

    private var resumeTick by mutableIntStateOf(0)
    private var openTodaySignal by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.ensureChannel(this)

        if (AlarmStore(this).load().enabled) {
            AlarmScheduler.scheduleNext(this)
        }

        if (intent?.getBooleanExtra(EXTRA_OPEN_TODAY, false) == true) {
            openTodaySignal++
        }

        val profileStore = ProfileStore(this)
        val engine = FortuneEngine(FortuneDataSource(this))

        setContent {
            val tick = resumeTick
            val todaySignal = openTodaySignal
            DakbitTheme {
                var profile by remember { mutableStateOf(profileStore.load()) }
                val today = remember(tick) { LocalDate.now() }
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MoonlitBackground {
                        if (profile == null) {
                            ProfileSetupScreen(
                                existing = null,
                                onSave = {
                                    profileStore.save(it)
                                    profile = it
                                },
                            )
                        } else {
                            val current = requireNotNull(profile)
                            val result = remember(current, today) { engine.calculate(current, today) }
                            FortuneApp(
                                profile = current,
                                result = result,
                                openTodaySignal = todaySignal,
                                onProfileChanged = {
                                    profileStore.save(it)
                                    profile = it
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        resumeTick++
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(EXTRA_OPEN_TODAY, false)) {
            openTodaySignal++
            intent.removeExtra(EXTRA_OPEN_TODAY)
        }
    }
}

@Composable
private fun MoonlitBackground(content: @Composable () -> Unit) {
    val stars = remember {
        listOf(
            0.05f to 0.08f, 0.15f to 0.15f, 0.27f to 0.05f, 0.38f to 0.19f,
            0.51f to 0.08f, 0.64f to 0.14f, 0.76f to 0.06f, 0.91f to 0.17f,
            0.09f to 0.31f, 0.22f to 0.26f, 0.43f to 0.34f, 0.58f to 0.27f,
            0.72f to 0.39f, 0.87f to 0.29f, 0.96f to 0.44f, 0.12f to 0.55f,
            0.34f to 0.48f, 0.48f to 0.61f, 0.68f to 0.52f, 0.83f to 0.65f,
            0.04f to 0.77f, 0.25f to 0.72f, 0.55f to 0.81f, 0.74f to 0.75f,
            0.94f to 0.87f, 0.17f to 0.93f, 0.41f to 0.89f, 0.66f to 0.96f,
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF11143C), Midnight, Color(0xFF17103A)),
                ),
            ),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            repeat(72) { index ->
                val x = ((index * 37 + 11) % 101) / 100f
                val y = ((index * 61 + 7) % 103) / 102f
                drawCircle(
                    color = Color.White.copy(alpha = if (index % 7 == 0) 0.62f else 0.28f),
                    radius = if (index % 9 == 0) 1.8f else 0.8f,
                    center = Offset(size.width * x, size.height * y),
                )
            }
            stars.forEachIndexed { index, (x, y) ->
                drawCircle(
                    color = if (index % 5 == 0) Amber.copy(alpha = 0.78f) else Color.White.copy(alpha = 0.56f),
                    radius = if (index % 4 == 0) 2.2f else 1.2f,
                    center = Offset(size.width * x, size.height * y),
                )
            }
            val constellation = listOf(
                Offset(0.07f, 0.28f), Offset(0.14f, 0.23f), Offset(0.22f, 0.27f),
                Offset(0.29f, 0.22f), Offset(0.35f, 0.30f), Offset(0.27f, 0.35f),
                Offset(0.18f, 0.32f), Offset(0.14f, 0.23f),
            )
            constellation.zipWithNext().forEach { (start, end) ->
                drawLine(
                    color = Color.White.copy(alpha = 0.14f),
                    start = Offset(size.width * start.x, size.height * start.y),
                    end = Offset(size.width * end.x, size.height * end.y),
                    strokeWidth = 1.2f,
                )
            }
            constellation.forEach { point ->
                drawCircle(
                    color = MoonIvory.copy(alpha = 0.65f),
                    radius = 2f,
                    center = Offset(size.width * point.x, size.height * point.y),
                )
            }
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Lavender.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(size.width * 0.72f, size.height * 0.18f),
                    radius = size.width * 0.42f,
                ),
                radius = size.width * 0.42f,
                center = Offset(size.width * 0.72f, size.height * 0.18f),
            )
        }
        content()
    }
}

@Composable
private fun ProfileSetupScreen(existing: UserProfile?, onSave: (UserProfile) -> Unit) {
    val context = LocalContext.current
    var nickname by remember { mutableStateOf(existing?.nickname.orEmpty()) }
    var birthDate by remember { mutableStateOf(existing?.birthDate) }
    var gender by remember { mutableStateOf(existing?.gender ?: Gender.MALE) }
    var error by remember { mutableStateOf<String?>(null) }
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy년 M월 d일") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        item {
            Image(
                painter = painterResource(R.drawable.dakbit_icon),
                contentDescription = "달빛운세 아이콘",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(24.dp)),
            )
            Spacer(Modifier.height(22.dp))
            Text(
                text = if (existing == null) "내 하루에\n따뜻한 빛 한 줄" else "내 정보 수정",
                style = MaterialTheme.typography.displaySmall,
                color = MoonIvory,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "생년월일로 오늘의 60갑자를 계산해 나만의 운세를 보여드려요.",
                style = MaterialTheme.typography.bodyLarge,
                color = Muted,
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it.take(12) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("닉네임") },
                    placeholder = { Text("예: 달빛") },
                    shape = RoundedCornerShape(16.dp),
                )
                OutlinedTextField(
                    value = birthDate?.format(formatter).orEmpty(),
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val initial = birthDate ?: LocalDate.of(1995, 1, 1)
                            DatePickerDialog(
                                context,
                                { _, year, month, day -> birthDate = LocalDate.of(year, month + 1, day) },
                                initial.year,
                                initial.monthValue - 1,
                                initial.dayOfMonth,
                            ).apply {
                                datePicker.maxDate = System.currentTimeMillis()
                                show()
                            }
                        },
                    readOnly = true,
                    enabled = false,
                    label = { Text("생년월일 (양력 기준)") },
                    placeholder = { Text("날짜를 선택해 주세요") },
                    shape = RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Ink,
                        disabledBorderColor = Line,
                        disabledLabelColor = Muted,
                        disabledPlaceholderColor = Muted,
                    ),
                )
            }
        }

        item {
            Text("패션 운세 기준", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Gender.entries.forEach { option ->
                    FilterChip(
                        selected = gender == option,
                        onClick = { gender = option },
                        label = { Text(option.label) },
                    )
                }
            }
            Text(
                "원본 데이터의 패션 문구 분류에만 사용하며 외부로 전송하지 않아요.",
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        if (error != null) {
            item { Text(requireNotNull(error), color = Coral, style = MaterialTheme.typography.bodyMedium) }
        }

        item {
            Button(
                onClick = {
                    when {
                        nickname.isBlank() -> error = "닉네임을 입력해 주세요."
                        birthDate == null -> error = "생년월일을 선택해 주세요."
                        birthDate!! < LocalDate.of(1900, 2, 20) ->
                            error = "1900년 2월 20일 이후 날짜를 선택해 주세요."
                        else -> onSave(UserProfile(nickname.trim(), birthDate!!, gender))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Amber, contentColor = Midnight),
            ) {
                Text(if (existing == null) "오늘의 운세 보기" else "저장하기")
            }
        }
    }
}

@Composable
private fun FortuneApp(
    profile: UserProfile,
    result: FortuneResult,
    openTodaySignal: Int,
    onProfileChanged: (UserProfile) -> Unit,
) {
    var selectedTab by remember { mutableStateOf(AppTab.TODAY) }
    var editingProfile by remember { mutableStateOf(false) }

    LaunchedEffect(openTodaySignal) {
        if (openTodaySignal > 0) {
            selectedTab = AppTab.TODAY
        }
    }

    if (editingProfile) {
        ProfileSetupScreen(
            existing = profile,
            onSave = {
                onProfileChanged(it)
                editingProfile = false
            },
        )
        return
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(
                containerColor = GlassStrong,
                modifier = Modifier.navigationBarsPadding(),
            ) {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Text(tab.emoji, fontWeight = FontWeight.Bold) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MoonIvory,
                            selectedTextColor = MoonIvory,
                            indicatorColor = Lavender.copy(alpha = 0.42f),
                            unselectedIconColor = Muted,
                            unselectedTextColor = Muted,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        when (selectedTab) {
            AppTab.TODAY -> TodayScreen(profile, result, Modifier.padding(innerPadding))
            AppTab.LUCKY -> LuckyScreen(result, Modifier.padding(innerPadding))
            AppTab.SETTINGS -> SettingsScreen(
                profile = profile,
                onEdit = { editingProfile = true },
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun TodayScreen(profile: UserProfile, result: FortuneResult, modifier: Modifier = Modifier) {
    var expanded by remember { mutableIntStateOf(-1) }
    val dateLabel = "${result.date.year}년 ${result.date.month.getDisplayName(TextStyle.FULL, Locale.KOREAN)} ${result.date.dayOfMonth}일"

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 34.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "달빛 운세",
                    color = MoonIvory,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal,
                    ),
                )
                Spacer(Modifier.height(8.dp))
                Text("안녕하세요, ${profile.nickname}님.", color = Ink, style = MaterialTheme.typography.bodyLarge)
                Text(dateLabel, color = Muted, style = MaterialTheme.typography.bodyMedium)
            }
        }

        item { FortuneHero(result.score, result.summary) }

        item {
            Text("오늘의 운세", style = MaterialTheme.typography.titleLarge, color = MoonIvory)
        }

        itemsIndexed(result.categories) { index, category ->
            FortuneCategoryCard(
                fortune = category,
                expanded = expanded == index,
                onClick = { expanded = if (expanded == index) -1 else index },
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Glass),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Lavender.copy(alpha = 0.48f)),
            ) {
                Column(Modifier.padding(22.dp)) {
                    Text("오늘의 스타일", color = Amber, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        result.styleAdvice,
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun FortuneHero(score: Int, summary: String) {
    val sections = remember(summary) { fortuneSections(summary) }
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = GlassStrong),
        border = BorderStroke(1.dp, Lavender.copy(alpha = 0.72f)),
        modifier = Modifier.shadow(
            elevation = 20.dp,
            shape = RoundedCornerShape(28.dp),
            ambientColor = Lavender.copy(alpha = 0.25f),
            spotColor = Lavender.copy(alpha = 0.25f),
        ),
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xCC4B476F), Color(0xB824234B)),
                    ),
                )
                .padding(horizontal = 22.dp, vertical = 24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MoonScore(score)
                Spacer(Modifier.width(20.dp))
                Column(Modifier.weight(1f)) {
                    Text("오늘의 종합 운세", color = Amber, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        scoreMessage(score),
                        color = Ink,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "달빛 점수 $score\n오늘의 흐름을 천천히 읽어보세요.",
                        color = Muted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Lavender.copy(alpha = 0.35f)),
            )
            Spacer(Modifier.height(20.dp))

            sections.forEachIndexed { index, paragraph ->
                Text(
                    text = listOf("오늘의 흐름", "마음에 새길 말", "달빛의 조언").getOrElse(index) {
                        "오늘의 메시지"
                    },
                    color = Amber,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(7.dp))
                Text(
                    text = paragraph,
                    color = Ink,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (index != sections.lastIndex) {
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun MoonScore(score: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(126.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                drawCircle(MoonIvory.copy(alpha = 0.08f), size.minDimension * 0.49f, center)
                drawCircle(MoonIvory.copy(alpha = 0.14f), size.minDimension * 0.43f, center)
            }
            Image(
                painter = painterResource(R.drawable.moon_score_orb),
                contentDescription = "달빛 점수 보름달",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(122.dp),
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "$score",
                    color = Midnight,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "오늘의 빛",
                    color = Midnight,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

private fun fortuneSections(summary: String): List<String> {
    val sentences = summary
        .trim()
        .split(Regex("(?<=[.!?])\\s+"))
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    if (sentences.isEmpty()) return listOf(summary)
    val chunkSize = maxOf(1, (sentences.size + 2) / 3)
    return sentences.chunked(chunkSize).map { it.joinToString(" ") }
}

private fun scoreMessage(score: Int): String = when {
    score >= 90 -> "환한 달빛처럼 좋은 기운이 머무는 날이에요."
    score >= 75 -> "차분한 달빛이 오늘의 선택을 비춰줄 거예요."
    score >= 60 -> "서두르지 않으면 흐름을 내 편으로 만들 수 있어요."
    else -> "잠시 숨을 고르며 마음의 균형을 지켜보세요."
}

@Composable
private fun FortuneCategoryCard(fortune: ScoredFortune, expanded: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Glass),
        border = BorderStroke(1.dp, Lavender.copy(alpha = 0.38f)),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Lavender.copy(alpha = 0.32f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(fortune.emoji, color = MoonIvory, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(14.dp))
                Text(
                    fortune.title,
                    color = Ink,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                fortune.score?.let {
                    Text("${it}점", color = Amber, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(8.dp))
                Text(if (expanded) "−" else "+", color = Muted)
            }
            if (expanded) {
                Spacer(Modifier.height(14.dp))
                Text(fortune.content, style = MaterialTheme.typography.bodyLarge, color = Ink)
            } else {
                Spacer(Modifier.height(9.dp))
                Text(
                    fortune.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun LuckyScreen(result: FortuneResult, modifier: Modifier = Modifier) {
    val items = listOf(
        Triple("행운의 방향", "⌖", result.luckyItems.direction),
        Triple("행운의 색", "●", result.luckyItems.color),
        Triple("행운의 숫자", "#", result.luckyItems.number),
        Triple("귀인의 띠", "♧", result.luckyItems.zodiacAnimal),
        Triple("행운의 아이템", "◇", result.luckyItems.fashionItem),
        Triple("행운의 별자리", "✦", result.luckyItems.starSign),
    )
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("오늘의 행운", style = MaterialTheme.typography.headlineSmall, color = MoonIvory)
            Text("오늘 하루에 가볍게 참고해 보세요.", color = Muted, modifier = Modifier.padding(top = 5.dp))
        }
        itemsIndexed(items) { index, item ->
            val accent = if (index % 2 == 0) Amber else Coral
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Glass),
                border = BorderStroke(1.dp, Lavender.copy(alpha = 0.38f)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(19.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(accent.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(item.second, color = MoonIvory, fontWeight = FontWeight.ExtraBold)
                    }
                    Spacer(Modifier.width(15.dp))
                    Column {
                        Text(item.first, style = MaterialTheme.typography.bodyMedium, color = Muted)
                        Text(item.third, style = MaterialTheme.typography.titleMedium, color = Ink)
                    }
                }
            }
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Lavender.copy(alpha = 0.18f)),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text(
                    "달빛 운세는 재미로 보는 운세 콘텐츠입니다. 가벼운 마음으로 보세요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    profile: UserProfile,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일")
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text("설정", style = MaterialTheme.typography.headlineSmall, color = MoonIvory)
        }
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Glass),
                border = BorderStroke(1.dp, Lavender.copy(alpha = 0.38f)),
            ) {
                Column(Modifier.padding(22.dp)) {
                    Text(profile.nickname, color = Ink, style = MaterialTheme.typography.titleLarge)
                    Text(profile.birthDate.format(formatter), color = Muted, modifier = Modifier.padding(top = 5.dp))
                    Text("패션 운세 기준 · ${profile.gender.label}", color = Muted)
                    Button(
                        onClick = onEdit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 18.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text("내 정보 수정")
                    }
                }
            }
        }
        item {
            AlarmSettingsCard()
        }
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Lavender.copy(alpha = 0.18f)),
                border = BorderStroke(1.dp, Lavender.copy(alpha = 0.32f)),
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("개인정보 안내", style = MaterialTheme.typography.titleMedium, color = Amber)
                    Text(
                        "입력한 정보와 운세 계산은 기기 안에서만 처리됩니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
        item {
            Text("달빛 운세 1.0.0", color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun AlarmSettingsCard() {
    val context = LocalContext.current
    val alarmStore = remember { AlarmStore(context) }
    var settings by remember { mutableStateOf(alarmStore.load()) }
    val timeLabel = remember(settings.hour, settings.minute) {
        String.format(Locale.KOREAN, "%d:%02d", settings.hour, settings.minute)
    }
    val notificationsAllowed =
        (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
            ) && NotificationManagerCompat.from(context).areNotificationsEnabled()

    fun persistSettings(newSettings: AlarmSettings) {
        settings = newSettings
        alarmStore.save(newSettings)
        if (newSettings.enabled) {
            AlarmScheduler.scheduleNext(context)
        } else {
            AlarmScheduler.cancel(context)
        }
    }

    LaunchedEffect(notificationsAllowed) {
        if (settings.enabled && !notificationsAllowed) {
            persistSettings(settings.copy(enabled = false))
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            persistSettings(settings.copy(enabled = true))
        } else {
            settings = settings.copy(enabled = false)
            alarmStore.save(settings.copy(enabled = false))
        }
    }

    fun enableAlarm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED -> {
                    persistSettings(settings.copy(enabled = true))
                }
                else -> notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            persistSettings(settings.copy(enabled = true))
        }
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Glass),
        border = BorderStroke(1.dp, Lavender.copy(alpha = 0.38f)),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("오늘의 운세 알림", style = MaterialTheme.typography.titleMedium, color = Amber)
                    Text(
                        "설정한 시간 무렵에 알림을 보내드려요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                Switch(
                    checked = settings.enabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            enableAlarm()
                        } else {
                            persistSettings(settings.copy(enabled = false))
                        }
                    },
                )
            }

            if (settings.enabled) {
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = timeLabel,
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    persistSettings(settings.copy(hour = hour, minute = minute))
                                },
                                settings.hour,
                                settings.minute,
                                true,
                            ).show()
                        },
                    readOnly = true,
                    enabled = false,
                    label = { Text("알림 시간") },
                    shape = RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Ink,
                        disabledBorderColor = Line,
                        disabledLabelColor = Muted,
                    ),
                )
                Text(
                    "알림을 누르면 오늘의 운세 화면으로 이동합니다. 소리와 진동은 휴대폰 설정을 따르며, 절전 상태에서는 조금 늦을 수 있습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}
