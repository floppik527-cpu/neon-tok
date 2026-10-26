package com.neontok.app

import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch

private val Ink = Color(0xFF090812)
private val Panel = Color(0xFF171426)
private val NeonPink = Color(0xFFFF4FA3)
private val NeonBlue = Color(0xFF56E0FF)
private val NeonPurple = Color(0xFF9B7BFF)

data class ClipItem(val uri: Uri, val title: String, val creator: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(9, 8, 18)
        window.navigationBarColor = android.graphics.Color.rgb(9, 8, 18)
        setContent { NeonTokApp() }
    }
}

@Composable
fun NeonTokApp() {
    val context = LocalContext.current
    var clips by remember { mutableStateOf(listOf<ClipItem>()) }
    var tab by remember { mutableStateOf(0) }
    var accent by remember { mutableStateOf(0) }
    var privateProfile by remember { mutableStateOf(true) }
    var saveHistory by remember { mutableStateOf(false) }
    var showLikes by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("Добавь свои видео, чтобы собрать ленту") }
    val accents = listOf(NeonPink, NeonBlue, NeonPurple)
    val tint = accents[accent]
    val pickVideo = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            clips = clips + ClipItem(uri, "Моё видео ${clips.size + 1}", "@you")
            message = "Видео добавлено в ленту"
            tab = 0
        }
    }
    MaterialTheme(colorScheme = darkColorScheme(
        background = Ink, surface = Panel, primary = tint,
        secondary = NeonBlue, onBackground = Color.White, onSurface = Color.White
    )) {
        Scaffold(
            containerColor = Ink,
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF100E1B), contentColor = Color.White) {
                    NavigationBarItem(selected = tab == 0, onClick = { tab = 0 },
                        icon = { Icon(Icons.Default.PlayCircle, null) }, label = { Text("Лента") })
                    NavigationBarItem(selected = tab == 1, onClick = { pickVideo.launch("video/*") },
                        icon = { Icon(Icons.Default.AddCircle, null) }, label = { Text("Загрузить") })
                    NavigationBarItem(selected = tab == 2, onClick = { tab = 2 },
                        icon = { Icon(Icons.Default.Tune, null) }, label = { Text("Настройки") })
                }
            }
        ) { padding ->
            when (tab) {
                0 -> FeedScreen(clips, tint, padding, onSave = { item ->
                    val ok = saveVideo(context, item.uri, item.title)
                    message = if (ok) "Сохранено в Movies/NEONTOK" else "Не удалось сохранить это видео"
                }, message = message)
                2 -> SettingsScreen(
                    tint = tint, accent = accent, onAccent = { accent = it },
                    privateProfile = privateProfile, onPrivate = { privateProfile = it },
                    saveHistory = saveHistory, onHistory = { saveHistory = it },
                    showLikes = showLikes, onShowLikes = { showLikes = it },
                    clipCount = clips.size, padding = padding
                )
                else -> FeedScreen(clips, tint, padding, onSave = {}, message = message)
            }
        }
    }
}

@Composable
fun FeedScreen(
    clips: List<ClipItem>, tint: Color, padding: PaddingValues,
    onSave: (ClipItem) -> Unit, message: String
) {
    if (clips.isEmpty()) {
        Box(
            Modifier.fillMaxSize().padding(padding)
                .background(Brush.verticalGradient(listOf(Color(0xFF211339), Ink))),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(28.dp)) {
                Box(
                    Modifier.size(92.dp).clip(RoundedCornerShape(28.dp))
                        .background(Brush.linearGradient(listOf(NeonPink, NeonPurple, NeonBlue))),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(58.dp)) }
                Spacer(Modifier.height(22.dp))
                Text("NEON TOK", fontSize = 30.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text("Твоя лента. Твои правила.", color = tint, fontSize = 15.sp)
                Spacer(Modifier.height(14.dp))
                Text(message, color = Color.LightGray)
                Spacer(Modifier.height(20.dp))
                Text("Нажми «Загрузить», чтобы добавить первое видео",
                    color = Color.White, fontSize = 14.sp)
                Spacer(Modifier.height(28.dp))
                Text("ЛОКАЛЬНЫЙ ПРОТОТИП • БЕЗ РЕКЛАМЫ",
                    color = Color.Gray, fontSize = 10.sp, letterSpacing = 1.sp)
            }
        }
    } else {
        val pagerState = rememberPagerState(pageCount = { clips.size })
        VerticalPager(state = pagerState, modifier = Modifier.fillMaxSize().padding(padding)) { page ->
            val item = clips[page]
            Box(Modifier.fillMaxSize().background(Color.Black)) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            setVideoURI(item.uri)
                            setOnPreparedListener { mp ->
                                mp.isLooping = true
                                start()
                            }
                        }
                    },
                    update = { view ->
                        if (view.tag != item.uri.toString()) {
                            view.tag = item.uri.toString()
                            view.setVideoURI(item.uri)
                            view.setOnPreparedListener { mp -> mp.isLooping = true; view.start() }
                        }
                    }
                )
                Box(Modifier.fillMaxSize().background(
                    Brush.verticalGradient(0f to Color.Transparent, 0.55f to Color.Transparent, 1f to Color(0xCC05040A))
                ))
                Column(Modifier.align(Alignment.BottomStart).padding(start = 18.dp, end = 86.dp, bottom = 28.dp)) {
                    Text(item.creator, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(item.title, color = Color.White, modifier = Modifier.padding(top = 6.dp))
                    Text("NEON TOK · локальное видео", color = tint, fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp))
                }
                Column(Modifier.align(Alignment.CenterEnd).padding(end = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    var liked by remember(item.uri) { mutableStateOf(false) }
                    IconButton(onClick = { liked = !liked }) {
                        Icon(Icons.Default.Favorite, null, tint = if (liked) NeonPink else Color.White,
                            modifier = Modifier.size(32.dp))
                    }
                    Text(if (liked) "Нравится" else "Лайк", color = Color.White, fontSize = 10.sp)
                    Spacer(Modifier.height(18.dp))
                    IconButton(onClick = { onSave(item) }) {
                        Icon(Icons.Default.Download, null, tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                    Text("Сохранить", color = Color.White, fontSize = 10.sp)
                }
                Text("NEON TOK", color = Color.White, fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp, modifier = Modifier.align(Alignment.TopCenter).padding(top = 18.dp))
            }
        }
    }
}

@Composable
fun SettingsScreen(
    tint: Color, accent: Int, onAccent: (Int) -> Unit,
    privateProfile: Boolean, onPrivate: (Boolean) -> Unit,
    saveHistory: Boolean, onHistory: (Boolean) -> Unit,
    showLikes: Boolean, onShowLikes: (Boolean) -> Unit,
    clipCount: Int, padding: PaddingValues
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 22.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Настройки", fontSize = 30.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text("Собери приложение под себя", color = tint, modifier = Modifier.padding(top = 4.dp))
        }
        item {
            SettingCard("Внешний вид", "Акцентный цвет") {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(top = 14.dp)) {
                    listOf(NeonPink, NeonBlue, NeonPurple).forEachIndexed { index, color ->
                        Box(
                            Modifier.size(42.dp).clip(CircleShape)
                                .background(color)
                                .clickable { onAccent(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (accent == index) Icon(Icons.Default.Check, null, tint = Ink)
                        }
                    }
                }
            }
        }
        item {
            SettingCard("Приватность", "Локальные настройки прототипа") {
                SwitchLine("Приватный профиль", privateProfile, onPrivate, tint)
                SwitchLine("Не сохранять историю просмотров", saveHistory, onHistory, tint)
                SwitchLine("Показывать лайки", showLikes, onShowLikes, tint)
            }
        }
        item {
            SettingCard("Медиа", "Видео на этом устройстве") {
                Text("Добавлено видео: $clipCount", color = Color.White, modifier = Modifier.padding(top = 10.dp))
                Text("Сохранение работает для доступных локальных видео. Сетевые ссылки и авторизация пока не подключены.",
                    color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }
        }
        item {
            SettingCard("О приложении", "NEON TOK · 0.1.0") {
                Text("Самостоятельный локальный прототип. Рекламные SDK не подключены.",
                    color = Color.LightGray, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
fun SettingCard(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Panel).padding(18.dp)
    ) {
        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Text(subtitle, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp))
        content()
    }
}

@Composable
fun SwitchLine(label: String, checked: Boolean, onChecked: (Boolean) -> Unit, tint: Color) {
    Row(
        Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White, modifier = Modifier.weight(1f), fontSize = 14.sp)
        Switch(checked = checked, onCheckedChange = onChecked, colors = SwitchDefaults.colors(checkedThumbColor = tint))
    }
}

fun saveVideo(context: android.content.Context, uri: Uri, title: String): Boolean {
    return try {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, "${title.replace(" ", "_")}.mp4")
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/NEONTOK")
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }
        val target = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values) ?: return false
        resolver.openInputStream(uri).use { input ->
            resolver.openOutputStream(target).use { output ->
                if (input == null || output == null) return false
                input.copyTo(output)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val finish = ContentValues().apply { put(MediaStore.Video.Media.IS_PENDING, 0) }
            resolver.update(target, finish, null, null)
        }
        true
    } catch (_: Exception) {
        false
    }
}
