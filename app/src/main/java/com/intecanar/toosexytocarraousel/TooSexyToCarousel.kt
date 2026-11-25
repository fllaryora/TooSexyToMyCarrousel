package com.intecanar.toosexytocarraousel

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import androidx.compose.ui.zIndex
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch
import kotlin.math.*

@Composable
fun TooSexyToCarousel(
    images: List<Int>,
    modifier: Modifier = Modifier,
    cardWidth: Dp = 140.dp,
    cardHeight: Dp = 150.dp,
    //separacion angular entre fotografias
    angleStep: Float = 28f,
    //Curvatura y separación horizontal.
    radiusMultiplier: Float = 2.2f,
    projectionScale: Float = 1.3f
) {
    if (images.isEmpty()) return

    val density = LocalDensity.current
    val cardWidthPx = with(density) { cardWidth.toPx() }
    val radiusPx = cardWidthPx * radiusMultiplier
    val itemStepPx = cardWidthPx * 1.03f

    val pagePosition = remember(images.size) {
        Animatable(0f)
    }

    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .clipToBounds()
            .pointerInput(images.size, itemStepPx) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        coroutineScope.launch {
                            pagePosition.animateTo(
                                targetValue = pagePosition.value
                                    .roundToInt()
                                    .coerceIn(0, images.lastIndex)
                                    .toFloat(),
                                animationSpec = spring(
                                    dampingRatio = 0.75f,
                                    stiffness = 300f
                                )
                            )
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            pagePosition.animateTo(
                                pagePosition.value
                                    .roundToInt()
                                    .coerceIn(0, images.lastIndex)
                                    .toFloat()
                            )
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()

                        coroutineScope.launch {
                            pagePosition.snapTo(
                                (
                                        pagePosition.value -
                                                dragAmount / itemStepPx
                                        ).coerceIn(
                                        0f,
                                        images.lastIndex.toFloat()
                                    )
                            )
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {

        images.forEachIndexed { index, imageResource ->

            val angle = (
                    index - pagePosition.value
                    ) * angleStep

            val angleRadians = Math.toRadians(angle.toDouble())
            val cosine = cos(angleRadians).toFloat()
            val sine = sin(angleRadians).toFloat()

            // Ocultamos las caras que miran hacia atrás.
            if (cosine > 0f) {

                val horizontalPosition = radiusPx * sine * projectionScale

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset {
                            IntOffset(
                                x = horizontalPosition.roundToInt(),
                                y = 0
                            )
                        }
                        .zIndex(cosine)
                        .size(
                            width = cardWidth,
                            height = cardHeight
                        )
                        .graphicsLayer {
                            rotationY = -angle

                            cameraDistance = 12f * density.density

                            transformOrigin = TransformOrigin.Center

                            clip = true
                            shape = RoundedCornerShape(18.dp)

                            scaleX = projectionScale
                            scaleY = projectionScale
                        }
                ) {

                    Image(
                        painter = painterResource(imageResource),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}