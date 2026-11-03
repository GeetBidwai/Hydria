package com.example.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.WaterOutline
import com.example.ui.theme.WaterSurface

/**
 * Standard reusable minimalist card with 16dp rounded corners, white surface,
 * and optional border for crisp hierarchy.
 */
@Composable
fun WaterCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = WaterSurface,
    borderColor: Color? = WaterOutline.copy(alpha = 0.5f),
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 0.dp,
    testTag: String = "water_card",
    onClick: (() -> Unit)? = null,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clickable(onClick = onClick)
    } else {
        modifier
            .fillMaxWidth()
            .testTag(testTag)
    }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = borderColor?.let { BorderStroke(1.dp, it) },
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content
        )
    }
}
