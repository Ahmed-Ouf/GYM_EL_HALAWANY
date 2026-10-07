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

val PrimaryLight = Color(0xFF0F766E)
val OnPrimaryLight = Color(0xFFFFFFFF)
val SecondaryLight = Color(0xFF4338CA)
val OnSecondaryLight = Color(0xFFFFFFFF)
val BackgroundLight = Color(0xFFF8FAFC)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF0F172A)
val OnSurfaceVariantLight = Color(0xFF64748B)
val OutlineLight = Color(0xFFE2E8F0)
val ErrorLight = Color(0xFFDC2626)

val PrimaryDark = Color(0xFF2DD4BF)
val SecondaryDark = Color(0xFF818CF8)
val BackgroundDark = Color(0xFF0F172A)
val SurfaceDark = Color(0xFF1E293B)
val OnSurfaceDark = Color(0xFFF1F5F9)
val OnSurfaceVariantDark = Color(0xFF94A3B8)
val OutlineDark = Color(0xFF334155)

object StatusColorsLight {
    val Active = Color(0xFF16A34A)
    val ActiveContainer = Color(0xFF16A34A).copy(alpha = 0.12f)
    val Warning = Color(0xFFF59E0B)
    val WarningContainer = Color(0xFFF59E0B).copy(alpha = 0.12f)
    val Expired = Color(0xFFDC2626)
    val ExpiredContainer = Color(0xFFDC2626).copy(alpha = 0.12f)
}

object StatusColorsDark {
    val Active = Color(0xFF4ADE80)
    val ActiveContainer = Color(0xFF4ADE80).copy(alpha = 0.12f)
    val Warning = Color(0xFFFBBF24)
    val WarningContainer = Color(0xFFFBBF24).copy(alpha = 0.12f)
    val Expired = Color(0xFFF87171)
    val ExpiredContainer = Color(0xFFF87171).copy(alpha = 0.12f)
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