package com.example.delulu.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.*
import java.io.InputStream

enum class DeluluScreen {
    HOME,
    ASK_DELULU,
    STUDY,
    PLANNER,
    FOCUS,
    PROGRESS,
    SETTINGS
}

object BackgroundConfig {
    // Relative paths in assets/backgrounds/
    const val HOME_BG = "backgrounds/home.jpg"
    const val STUDY_BG = "backgrounds/study.jpg"
    const val ASK_DELULU_BG = "backgrounds/ask_delulu.jpg"
    const val PLANNER_BG = "backgrounds/planner.jpg"
    const val PROGRESS_BG = "backgrounds/progress.jpg"
    const val SETTINGS_BG = "backgrounds/settings.jpg"
    const val FOCUS_BG = "backgrounds/study.jpg"

    fun getAssetPathForScreen(screen: DeluluScreen): String {
        return when (screen) {
            DeluluScreen.HOME -> HOME_BG
            DeluluScreen.ASK_DELULU -> ASK_DELULU_BG
            DeluluScreen.STUDY -> STUDY_BG
            DeluluScreen.PLANNER -> PLANNER_BG
            DeluluScreen.FOCUS -> FOCUS_BG
            DeluluScreen.PROGRESS -> PROGRESS_BG
            DeluluScreen.SETTINGS -> SETTINGS_BG
        }
    }

    fun hasCustomAsset(context: Context, path: String): Boolean {
        return try {
            val stream: InputStream = context.assets.open(path)
            stream.close()
            true
        } catch (e: Exception) {
            false
        }
    }
}

@Composable
fun DeluluBackgroundWrapper(
    screen: DeluluScreen,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val assetPath = BackgroundConfig.getAssetPathForScreen(screen)
    val hasAsset = remember(assetPath) { BackgroundConfig.hasCustomAsset(context, assetPath) }
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        if (hasAsset) {
            // Load custom student background with readability scrim
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data("file:///android_asset/$assetPath")
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Overlay scrim for high contrast accessibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isDark) Color(0xDD0B0F19) else Color(0xCCF8FAFC)
                    )
            )
        } else {
            // Clean, aesthetic fallback gradient
            val fallbackGradient = when (screen) {
                DeluluScreen.HOME -> Brush.verticalGradient(
                    colors = if (isDark)
                        listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF0B0F19))
                    else
                        listOf(Color(0xFFEEF2FF), Color(0xFFF8FAFC), Color(0xFFF1F5F9))
                )
                DeluluScreen.ASK_DELULU -> Brush.verticalGradient(
                    colors = if (isDark)
                        listOf(Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF0F172A))
                    else
                        listOf(Color(0xFFEDE9FE), Color(0xFFF5F3FF), Color(0xFFF8FAFC))
                )
                DeluluScreen.STUDY -> Brush.verticalGradient(
                    colors = if (isDark)
                        listOf(Color(0xFF064E3B), Color(0xFF0F172A), Color(0xFF022C22))
                    else
                        listOf(Color(0xFFECFDF5), Color(0xFFF0FDF4), Color(0xFFF8FAFC))
                )
                DeluluScreen.PLANNER -> Brush.verticalGradient(
                    colors = if (isDark)
                        listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF0B0F19))
                    else
                        listOf(Color(0xFFF1F5F9), Color(0xFFF8FAFC), Color(0xFFE2E8F0))
                )
                DeluluScreen.FOCUS -> Brush.verticalGradient(
                    colors = if (isDark)
                        listOf(Color(0xFF1A103C), Color(0xFF0F172A), Color(0xFF050510))
                    else
                        listOf(Color(0xFFFAF5FF), Color(0xFFF3E8FF), Color(0xFFF8FAFC))
                )
                DeluluScreen.PROGRESS -> Brush.verticalGradient(
                    colors = if (isDark)
                        listOf(Color(0xFF1E1B4B), Color(0xFF0F172A), Color(0xFF111827))
                    else
                        listOf(Color(0xFFEEF2FF), Color(0xFFF1F5F9), Color(0xFFFFFFFF))
                )
                DeluluScreen.SETTINGS -> Brush.verticalGradient(
                    colors = if (isDark)
                        listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0B0F19))
                    else
                        listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9), Color(0xFFFFFFFF))
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(fallbackGradient)
            )
        }

        // Render screen content on top
        content()
    }
}
