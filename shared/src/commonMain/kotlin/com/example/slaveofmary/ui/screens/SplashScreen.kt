package com.example.slaveofmary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.slaveofmary.ui.theme.AppColors
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import slaveofmary.shared.generated.resources.Res
import slaveofmary.shared.generated.resources.loading
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    onNavigateToMain: () -> Unit
) {

    LaunchedEffect(Unit) {
        delay(2500.milliseconds)
        onNavigateToMain()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.appBackgroundBrush),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = AppColors.accent
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.loading),
            color = AppColors.textPrimary,
            fontSize = 18.sp
        )
    }
}