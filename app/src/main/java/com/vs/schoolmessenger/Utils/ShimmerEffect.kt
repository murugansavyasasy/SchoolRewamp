package com.vs.schoolmessenger.Utils

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerLoadingItem() {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.9f),
        Color.Gray.copy(alpha = 0.3f),
        Color.LightGray.copy(alpha = 0.9f)
    )

    val transition = rememberInfiniteTransition()
    val shimmerTranslate = transition.animateFloat(
        initialValue = -300f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Shimmer effect for Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp) // Adjusted to match ImageView size
                .background(
                    brush = Brush.horizontalGradient(
                        colors = shimmerColors,
                        startX = shimmerTranslate.value,
                        endX = shimmerTranslate.value + 300
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Shimmer effect for Title (TextView)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(20.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = shimmerColors,
                        startX = shimmerTranslate.value,
                        endX = shimmerTranslate.value + 300
                    ),
                    shape = RoundedCornerShape(4.dp)
                )
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Shimmer effect for Subtitle (TextView)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(15.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = shimmerColors,
                        startX = shimmerTranslate.value,
                        endX = shimmerTranslate.value + 300
                    ),
                    shape = RoundedCornerShape(4.dp)
                )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Shimmer effect for Video Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp) // Adjust to match your video player size
                .background(
                    brush = Brush.horizontalGradient(
                        colors = shimmerColors,
                        startX = shimmerTranslate.value,
                        endX = shimmerTranslate.value + 300
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
        )
    }
}
