package com.siowe.app.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * «Жидкое стекло»: полупрозрачная заливка, световой блик в углу и
 * тонкая градиентная кромка, как у iOS-карточек.
 */
fun Modifier.glass(
    shape: Shape = RoundedCornerShape(28.dp),
    strength: Float = 1f,
): Modifier = this
    .clip(shape)
    .background(
        Brush.linearGradient(
            listOf(Color.White.copy(alpha = 0.26f * strength), Color.White.copy(alpha = 0.07f * strength))
        )
    )
    .drawBehind {
        drawRect(
            Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.22f * strength), Color.Transparent),
                center = Offset(0f, 0f),
                radius = size.maxDimension * 0.9f,
            )
        )
    }
    .border(
        1.dp,
        Brush.linearGradient(
            listOf(Color.White.copy(alpha = 0.8f), Color.White.copy(alpha = 0.06f), Color.White.copy(alpha = 0.4f))
        ),
        shape,
    )

private fun skyColors(code: Int, isDay: Boolean): Pair<Color, Color> {
    if (!isDay) return when (code) {
        95, 96, 99 -> Color(0xFF1B1B38) to Color(0xFF454870)
        else -> Color(0xFF0B1230) to Color(0xFF2B3F80)
    }
    return when (code) {
        0, 1 -> Color(0xFF3F95F5) to Color(0xFF9AD0FF)
        2 -> Color(0xFF5B9BE0) to Color(0xFFB0D2F0)
        3, 45, 48 -> Color(0xFF7A8CA3) to Color(0xFFBAC5D2)
        51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> Color(0xFF3B4C6B) to Color(0xFF7189AB)
        71, 73, 75, 77, 85, 86 -> Color(0xFF8FA5C4) to Color(0xFFE0E9F4)
        95, 96, 99 -> Color(0xFF2B2D4A) to Color(0xFF5A5E88)
        else -> Color(0xFF3F95F5) to Color(0xFF9AD0FF)
    }
}

/** Градиентное небо + медленно плывущие размытые «капли» света под стеклом. */
@Composable
fun SkyBackground(code: Int, isDay: Boolean, content: @Composable BoxScope.() -> Unit) {
    val (top, bottom) = skyColors(code, isDay)
    val t by animateColorAsState(top, tween(1200), label = "top")
    val b by animateColorAsState(bottom, tween(1200), label = "bottom")
    val inf = rememberInfiniteTransition(label = "blobs")
    val a by inf.animateFloat(
        initialValue = -40f, targetValue = 40f,
        animationSpec = infiniteRepeatable(tween(9000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "a",
    )
    val c by inf.animateFloat(
        initialValue = 30f, targetValue = -30f,
        animationSpec = infiniteRepeatable(tween(12000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "c",
    )
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(t, b)))) {
        Box(
            Modifier.offset(x = (-70 + a).dp, y = (60 + c).dp).size(280.dp)
                .blur(70.dp).background(Color.White.copy(alpha = 0.35f), CircleShape)
        )
        Box(
            Modifier.offset(x = (180 + c).dp, y = (330 + a).dp).size(240.dp)
                .blur(80.dp).background(Color(0xFFFFB86B).copy(alpha = if (isDay) 0.30f else 0.12f), CircleShape)
        )
        Box(
            Modifier.offset(x = (-40 - c).dp, y = (620 - a).dp).size(300.dp)
                .blur(90.dp).background(Color(0xFF7C8CFF).copy(alpha = 0.30f), CircleShape)
        )
        content()
    }
}
