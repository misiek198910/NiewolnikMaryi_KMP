package com.example.slaveofmary.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.slaveofmary.remote.RemoteClient
import com.example.slaveofmary.ui.components.AdBanner
import com.example.slaveofmary.ui.theme.AppColors
import org.jetbrains.compose.resources.stringResource
import slaveofmary.shared.generated.resources.Res
import slaveofmary.shared.generated.resources.*

private const val PRIVACY_SERVICE_NAME = "niewolnik-maryi"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    val paperColor = AppColors.background
    val textColor = AppColors.textPrimary
    val glassColor = AppColors.surfaceGlass

    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    var content by remember { mutableStateOf<AnnotatedString?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        isError = false
        val raw = RemoteClient.apiService.getPrivacyPolicy(PRIVACY_SERVICE_NAME)
        if (raw != null) {
            content = parseSimpleHtml(raw, baseFontSize = 16)
        } else {
            isError = true
        }
        isLoading = false
    }

    Scaffold(
        containerColor = paperColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.privacy_policy_title),
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        fontFamily = FontFamily.Serif
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back),
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = glassColor,
                    scrolledContainerColor = glassColor,
                    titleContentColor = textColor,
                    navigationIconContentColor = textColor
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = AppColors.accent
                        )
                    }

                    isError -> {
                        Text(
                            text = stringResource(Res.string.privacy_policy_error),
                            color = AppColors.danger,
                            fontFamily = FontFamily.Serif,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }

                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = content ?: AnnotatedString(""),
                                color = textColor,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = FontFamily.Serif,
                                    lineHeight = 24.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                AdBanner()
            }
        }
    }
}
