package com.example.simplecalc.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

val PrimaryLight = Color(0xFF5F33E1)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFEEE9FF)
val OnPrimaryContainerLight = Color(0xFF5F33E1)
val SecondaryLight = Color(0xFF9160F4)
val OnSecondaryLight = Color(0xFFFFFFFF)
val BackgroundLight = Color(0xFFF8F7FF)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF24252C)
val OnSurfaceVariantLight = Color(0xFF6E6A7C)
val OutlineLight = Color(0xFFECE7FF)
val ErrorLight = Color(0xFFDC2626)

val PrimaryDark = Color(0xFF8764FF)
val SecondaryDark = Color(0xFFB599FF)
val BackgroundDark = Color(0xFF14121E)
val SurfaceDark = Color(0xFF1F1C2C)
val OnSurfaceDark = Color(0xFFF3F0FF)
val OnSurfaceVariantDark = Color(0xFFAAA5BA)
val OutlineDark = Color(0xFF322E46)

// Figma Accent Badges & Category Colors
val AccentPink = Color(0xFFF478B8)
val AccentPinkContainer = Color(0xFFFFE4F2)
val AccentOrange = Color(0xFFFF7D53)
val AccentOrangeContainer = Color(0xFFFFE6D4)
val AccentBlue = Color(0xFF0087FF)
val AccentBlueContainer = Color(0xFFE3F2FF)
val AccentPurple = Color(0xFF9160F4)
val AccentPurpleContainer = Color(0xFFECE4FF)

object StatusColorsLight {
    val Active = Color(0xFF16A34A)
    val ActiveContainer = Color(0xFFDCFCE7)
    val Warning = Color(0xFFFF7D53)
    val WarningContainer = Color(0xFFFFE6D4)
    val Expired = Color(0xFFDC2626)
    val ExpiredContainer = Color(0xFFFEE2E2)
}

object StatusColorsDark {
    val Active = Color(0xFF4ADE80)
    val ActiveContainer = Color(0xFF4ADE80).copy(alpha = 0.16f)
    val Warning = Color(0xFFFF9141)
    val WarningContainer = Color(0xFFFF9141).copy(alpha = 0.16f)
    val Expired = Color(0xFFF87171)
    val ExpiredContainer = Color(0xFFF87171).copy(alpha = 0.16f)
}

@Preview(showBackground = true)
@Composable
fun ColorPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        val palette = listOf(
            "Primary" to PrimaryLight,
            "Secondary" to SecondaryLight,
            "Active" to StatusColorsLight.Active,
            "Warning" to StatusColorsLight.Warning,
            "Expired" to StatusColorsLight.Expired
        )
        palette.forEach { (name, color) ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(color)
                )
                Text(
                    text = name,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}