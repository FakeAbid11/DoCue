package com.example.docue.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    DoCueEmptyState(
        icon = icon,
        title = title,
        subtitle = subtitle,
        modifier = modifier
    )
}
