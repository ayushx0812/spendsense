package com.example.spendsence.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendsence.util.BiometricHelper

@Composable
fun BiometricLockScreen(onUnlocked: () -> Unit) {
    val context = LocalContext.current
    val activity = remember(context) { BiometricHelper.findFragmentActivity(context) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Trigger the prompt automatically on first composition
    LaunchedEffect(Unit) {
        if (activity != null && BiometricHelper.isAvailable(context)) {
            BiometricHelper.showPrompt(
                activity = activity,
                onSuccess = { onUnlocked() },
                onError = { msg -> errorMessage = msg }
            )
        } else {
            // No biometric hardware / not enrolled – skip the lock screen
            onUnlocked()
        }
    }

    // Pulse animation for the fingerprint icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF003D33), Color(0xFF00695C))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // App icon placeholder
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(36.dp)
            )

            Text(
                text = "SpendSense",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            // Pulsing fingerprint button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Unlock with biometric",
                    tint = Color(0xFF4DB6AC),
                    modifier = Modifier.size(72.dp)
                )
            }

            Text(
                text = "Touch to unlock",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 15.sp
            )

            if (errorMessage != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = Color(0xFFFF8A80),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            // Manual retry button
            OutlinedButton(
                onClick = {
                    errorMessage = null
                    if (activity != null && BiometricHelper.isAvailable(context)) {
                        BiometricHelper.showPrompt(
                            activity = activity,
                            onSuccess = { onUnlocked() },
                            onError = { msg -> errorMessage = msg }
                        )
                    } else {
                        errorMessage = "Fingerprint or screen lock is not set up on this device."
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.4f))
            ) {
                Text("Try Again")
            }
        }
    }
}
