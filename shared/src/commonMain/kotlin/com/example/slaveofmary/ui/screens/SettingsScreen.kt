package com.example.slaveofmary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import com.example.slaveofmary.billing.isSubscriptionAvailable
import com.example.slaveofmary.data.db.AppDatabaseProvider
import com.example.slaveofmary.data.repository.SubscriptionRepository
import com.example.slaveofmary.ui.components.AdBanner
import com.example.slaveofmary.ui.theme.AppColors
import com.example.slaveofmary.util.LocaleManager
import org.jetbrains.compose.resources.stringResource
import slaveofmary.shared.generated.resources.Res
import slaveofmary.shared.generated.resources.*

private data class LanguageOption(val code: String, val displayName: String)

private val availableLanguages = listOf(
    LanguageOption("pl", "Polski"),
    LanguageOption("en", "English")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSubscription: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit
) {
    val scrollState = rememberScrollState()

    val subscriptionRepository = remember {
        SubscriptionRepository(AppDatabaseProvider.database.subscriptionDao())
    }
    val isPremium by subscriptionRepository.isPremiumFlow.collectAsState(initial = null)

    var selectedLanguageCode by remember {
        mutableStateOf(LocaleManager.getCurrentLanguage())
    }
    var expanded by remember { mutableStateOf(false) }

    val selectedLanguage = availableLanguages.firstOrNull { it.code == selectedLanguageCode }
        ?: availableLanguages.first()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.main_button5),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    titleContentColor = AppColors.textPrimary,
                    navigationIconContentColor = AppColors.textPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                SettingsSectionLabel(icon = Icons.Filled.Language, text = stringResource(Res.string.settings_language))

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedLanguage.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontSize = 16.sp
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        availableLanguages.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.displayName, fontFamily = FontFamily.Serif) },
                                onClick = {
                                    expanded = false
                                    if (option.code != selectedLanguageCode) {
                                        selectedLanguageCode = option.code
                                        LocaleManager.setLanguage(option.code)
                                        LocaleManager.applyLanguageChange()
                                    }
                                }
                            )
                        }
                    }
                }

                if (isSubscriptionAvailable) {
                    Spacer(modifier = Modifier.height(28.dp))

                    SettingsSectionLabel(icon = Icons.Filled.WorkspacePremium, text = stringResource(Res.string.settings_subscriptions))

                    Spacer(modifier = Modifier.height(8.dp))

                    SubscriptionCard(
                        isPremium = isPremium,
                        onClick = onNavigateToSubscription
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                SettingsSectionLabel(icon = Icons.Filled.PrivacyTip, text = stringResource(Res.string.settings_privacy))

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToPrivacyPolicy)
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.privacy_policy_open),
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Serif,
                        color = AppColors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = AppColors.accent
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
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

@Composable
private fun SettingsSectionLabel(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.accent,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            fontFamily = FontFamily.Serif,
            color = AppColors.textPrimary
        )
    }
}

@Composable
private fun SubscriptionCard(isPremium: Boolean?, onClick: () -> Unit) {
    val cardGradient = Brush.linearGradient(
        colors = listOf(AppColors.cardGradientStart, AppColors.cardGradientEnd)
    )

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = AppColors.onCard
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardGradient)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.subscription_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Serif
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isPremium == true) {
                            stringResource(Res.string.subscription_card_active_desc)
                        } else {
                            stringResource(Res.string.subscription_card_inactive_desc)
                        },
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Serif
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isPremium == true) stringResource(Res.string.subscription_status_active) else stringResource(Res.string.subscription_buy),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Serif
                    )
                }
            }
        }
    }
}