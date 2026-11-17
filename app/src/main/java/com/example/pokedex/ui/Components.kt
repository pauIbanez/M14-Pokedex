package com.example.pokedex.ui

import android.content.res.AssetManager
import android.graphics.BitmapFactory
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pokedex.data.PokemonType
import java.util.concurrent.ConcurrentHashMap
import android.graphics.Color as AndroidColor

/** Two-letter language of the device ("es", "en", ...), used to pick localized JSON text. */
@Composable
fun appLanguage(): String = LocalConfiguration.current.locales[0].language

fun PokemonType.composeColor(): Color = Color(AndroidColor.parseColor(color))

// ---------------------------------------------------------------------------------------------
// Sprites

/** The sprites are tiny PNGs, so decoding them once and keeping them all in memory is cheap. */
private object SpriteCache {
    private val cache = ConcurrentHashMap<String, ImageBitmap>()

    fun get(assets: AssetManager, path: String): ImageBitmap? =
        cache[path] ?: runCatching {
            assets.open(path).use { BitmapFactory.decodeStream(it) }.asImageBitmap()
        }.getOrNull()?.also { cache[path] = it }
}

@Composable
fun PokemonSprite(path: String, contentDescription: String?, modifier: Modifier = Modifier) {
    val assets = LocalContext.current.assets
    val bitmap = remember(path) { SpriteCache.get(assets, path) } ?: return
    // Pixel art: nearest-neighbour scaling keeps it sharp instead of blurry.
    Image(
        bitmap = bitmap,
        contentDescription = contentDescription,
        modifier = modifier,
        filterQuality = FilterQuality.None,
    )
}

// ---------------------------------------------------------------------------------------------
// Type badge

@Composable
fun TypeChip(type: PokemonType, modifier: Modifier = Modifier, large: Boolean = false) {
    val color = type.composeColor()
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color)
            .padding(horizontal = if (large) 14.dp else 8.dp, vertical = if (large) 4.dp else 2.dp),
    ) {
        Text(
            text = type.name.get(appLanguage()).uppercase(),
            color = Color.White,
            fontSize = if (large) 13.sp else 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Poké Ball capture indicator

/**
 * A filled Poké Ball when captured, an empty outlined circle when not (like the in-game Pokédex).
 * It pops slightly when the state changes.
 */
@Composable
fun PokeballIndicator(captured: Boolean, modifier: Modifier = Modifier, size: Dp = 26.dp) {
    val scale by animateFloatAsState(
        targetValue = if (captured) 1f else 0.85f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "pokeballScale",
    )
    val outline = MaterialTheme.colorScheme.outline
    val ink = Color(0xFF222224)
    val red = Color(0xFFE3350D)
    Canvas(
        modifier
            .size(size)
            .graphicsLayer { scaleX = scale; scaleY = scale }
    ) {
        val r = this.size.minDimension / 2
        val stroke = r * 0.14f
        if (!captured) {
            drawCircle(outline, radius = r - stroke / 2, style = Stroke(stroke))
            drawCircle(outline, radius = r * 0.22f, style = Stroke(stroke * 0.8f))
            return@Canvas
        }
        // Top red half, bottom white half
        drawArc(red, 180f, 180f, useCenter = true, size = this.size)
        drawArc(Color.White, 0f, 180f, useCenter = true, size = this.size)
        // Middle band and outline
        drawRect(ink, topLeft = Offset(0f, center.y - stroke / 2), size = Size(this.size.width, stroke))
        drawCircle(ink, radius = r - stroke / 2, style = Stroke(stroke))
        // Button
        drawCircle(ink, radius = r * 0.32f)
        drawCircle(Color.White, radius = r * 0.18f)
    }
}

// ---------------------------------------------------------------------------------------------
// Icons (Material paths; avoids pulling in the material-icons library for five icons)

private fun icon(name: String, path: String): ImageVector =
    ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
        .addPath(pathData = addPathNodes(path), fill = SolidColor(Color.Black))
        .build()

object Icons {
    val Back = icon("back", "M20,11H7.83l5.59,-5.59L12,4l-8,8 8,8 1.41,-1.41L7.83,13H20v-2z")
    val ChevronLeft = icon("prev", "M15.41,7.41L14,6l-6,6 6,6 1.41,-1.41L10.83,12z")
    val ChevronRight = icon("next", "M10,6L8.59,7.41 13.17,12l-4.58,4.59L10,18l6,-6z")
    val Close = icon(
        "close",
        "M19,6.41L17.59,5 12,10.59 6.41,5 5,6.41 10.59,12 5,17.59 6.41,19 12,13.41 17.59,19 19,17.59 13.41,12z",
    )
    val Search = icon(
        "search",
        "M15.5,14h-0.79l-0.28,-0.27C15.41,12.59 16,11.11 16,9.5 16,5.91 13.09,3 9.5,3S3,5.91 3,9.5 " +
            "5.91,16 9.5,16c1.61,0 3.09,-0.59 4.23,-1.57l0.27,0.28v0.79l5,4.99L20.49,19l-4.99,-5z" +
            "M9.5,14C7.01,14 5,11.99 5,9.5S7.01,5 9.5,5 14,7.01 14,9.5 11.99,14 9.5,14z",
    )
}
