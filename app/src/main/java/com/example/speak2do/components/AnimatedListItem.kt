package com.example.speak2do.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun AnimatedListItem(
    index: Int,
    content: @Composable () -> Unit
) {
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(30f) }

    LaunchedEffect(Unit) {
        val delay = (index * 60L).coerceAtMost(400L)
        kotlinx.coroutines.delay(delay)
        alpha.animateTo(1f, tween(300))
    }

    LaunchedEffect(Unit) {
        val delay = (index * 60L).coerceAtMost(400L)
        kotlinx.coroutines.delay(delay)
        offsetY.animateTo(0f, tween(300))
    }

    Box(
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha.value
            translationY = offsetY.value
        }
    ) {
        content()
    }
}
