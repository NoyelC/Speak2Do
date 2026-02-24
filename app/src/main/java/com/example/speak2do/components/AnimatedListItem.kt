package com.example.speak2do.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun AnimatedListItem(
    index: Int,
    content: @Composable () -> Unit
) {
    // Keep entrance motion subtle and cheap to avoid scroll jank on large lists.
    var visible by remember { mutableStateOf(index > 6) }

    LaunchedEffect(index) {
        if (!visible) {
            val delayMs = (index * 20L).coerceAtMost(120L)
            kotlinx.coroutines.delay(delayMs)
            visible = true
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "listItemAlpha"
    )
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 10f,
        animationSpec = tween(durationMillis = 180),
        label = "listItemOffsetY"
    )

    Box(
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha
            translationY = offsetY
        }
    ) {
        content()
    }
}
