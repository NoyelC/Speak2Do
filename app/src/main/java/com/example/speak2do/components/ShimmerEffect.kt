package com.example.speak2do.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.speak2do.ui.theme.CardBackground
import com.example.speak2do.ui.theme.ShimmerHighlight

@Composable
fun ShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    return Brush.linearGradient(
        colors = listOf(
            CardBackground,
            ShimmerHighlight,
            CardBackground
        ),
        start = Offset(translateAnim.value - 300f, translateAnim.value - 300f),
        end = Offset(translateAnim.value, translateAnim.value)
    )
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    height: Dp = 20.dp,
    cornerRadius: Dp = 10.dp
) {
    val brush = ShimmerBrush()
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush)
    )
}

@Composable
fun ShimmerTaskCard() {
    val brush = ShimmerBrush()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
    ) {
        // Left accent bar shimmer
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(brush)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Checkbox + title row
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ShimmerBox(
                    modifier = Modifier.size(24.dp),
                    height = 24.dp,
                    cornerRadius = 4.dp
                )
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f),
                    height = 18.dp
                )
            }
            // DateTime + duration row
            Row(
                modifier = Modifier.padding(start = 34.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShimmerBox(
                    modifier = Modifier.width(80.dp),
                    height = 14.dp
                )
                ShimmerBox(
                    modifier = Modifier.width(50.dp),
                    height = 14.dp
                )
            }
        }
    }
}

@Composable
fun ShimmerTaskList(count: Int = 4) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(count) {
            ShimmerTaskCard()
        }
    }
}
