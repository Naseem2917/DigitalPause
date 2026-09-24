package com.digitalpause.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

val PillShape = RoundedCornerShape(percent = 50)
val CardShape = RoundedCornerShape(16.dp)
val HeroCardShape = RoundedCornerShape(20.dp)
val ModalSheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
