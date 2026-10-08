package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.RedAccent

@Composable
fun ToolboxLogo(
    size: Dp = 48.dp,
    modifier: Modifier = Modifier,
    showBorder: Boolean = true
) {
    val shape = RoundedCornerShape(size * 0.24f)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(Color(0xFF0D0E11))
            .then(
                if (showBorder) {
                    Modifier.border(1.dp, RedAccent.copy(alpha = 0.35f), shape)
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.toolbox_logo),
            contentDescription = "ToolBox Logo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(size)
        )
    }
}
