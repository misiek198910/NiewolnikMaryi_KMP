package com.example.slaveofmary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import com.example.slaveofmary.billing.BillingController
import com.example.slaveofmary.billing.isSubscriptionAvailable
import com.example.slaveofmary.billing.rememberBillingController
import com.example.slaveofmary.data.db.AppDatabaseProvider
import com.example.slaveofmary.data.repository.SubscriptionRepository
import com.example.slaveofmary.ui.components.AdBanner
import com.example.slaveofmary.ui.theme.AppColors
import org.jetbrains.compose.resources.stringResource
import slaveofmary.shared.generated.resources.Res
import slaveofmary.shared.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    onNavigateBack: () -> Unit
) {
    // Zabezpieczenie: gdyby kiedyś powstało inne wejście do tego ekranu (np. deep link),
    // na platformach bez działającego billingu i tak natychmiast wracamy — ekran subskrypcji
    // nigdy nie jest widoczny tam, gdzie zakup nie jest jeszcze możliwy.
    LaunchedEffect(Unit) {
        if (!isSubscriptionAvailable) onNavigateBack()
    }

    val scrollState = rememberScrollState()
    val uriHandler = LocalUriHandler.current

    val repository = remember {
        SubscriptionRepository(AppDatabaseProvider.database.subscriptionDao())
    }
    val billingController = rememberBillingController()

    val isPremium by repository.isPremiumFlow.collectAsState(initial = null)
    val loadingText = stringResource(Res.string.loading)
    val monthlyPrice by billingController?.monthlyPrice?.collectAsState(initial = loadingText)
        ?: remember { androidx.compose.runtime.mutableStateOf("") }
    val yearlyPrice by billingController?.yearlyPrice?.collectAsState(initial = loadingText)
        ?: remember { androidx.compose.runtime.mutableStateOf("") }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.subscription_title),
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
                    .padding(16.dp)
            ) {
                SubscriptionCard(
                    isPremium = isPremium,
                    billingController = billingController,
                    monthlyPrice = monthlyPrice,
                    yearlyPrice = yearlyPrice,
                    onOpenTerms = { uriHandler.openUri("https://www.apple.com/legal/internet-services/itunes/dev/stdeula/") },
                    onOpenPrivacy = { uriHandler.openUri("https://privacypolicy.mivs.dev/niewolnik-maryi.html") }
                )

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
private fun SubscriptionCard(
    isPremium: Boolean?,
    billingController: BillingController?,
    monthlyPrice: String,
    yearlyPrice: String,
    onOpenTerms: () -> Unit,
    onOpenPrivacy: () -> Unit
) {
    val cardGradient = Brush.linearGradient(
        colors = listOf(AppColors.cardGradientStart, AppColors.cardGradientEnd)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = AppColors.onCard
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardGradient)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.subscription_premium_version),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )

            Text(
                text = stringResource(Res.string.subscription_premium_desc),
                fontSize = 14.sp,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp, bottom = 20.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    BenefitRow(text = stringResource(Res.string.subscription_benefit_noads))
                    Spacer(modifier = Modifier.height(10.dp))
                    BenefitRow(text = stringResource(Res.string.subscription_benefit_support))
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 20.dp),
                color = Color.White.copy(alpha = 0.25f)
            )

            Text(
                text = stringResource(Res.string.subscription_status),
                fontSize = 13.sp,
                fontFamily = FontFamily.Serif,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = when (isPremium) {
                    true -> stringResource(Res.string.subscription_status_active)
                    false -> stringResource(Res.string.subscription_status_inactive)
                    null -> stringResource(Res.string.subscription_status_checking)
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = if (isPremium == true) AppColors.success else AppColors.onCard,
                modifier = Modifier.padding(top = 2.dp, bottom = 24.dp)
            )

            when {
                billingController == null -> {
                    Text(
                        text = stringResource(Res.string.subscription_ios_pending),
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Serif,
                        textAlign = TextAlign.Center,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                isPremium == true -> {
                    Button(
                        onClick = { billingController.manage() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.onCard,
                            contentColor = AppColors.cardGradientEnd
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.subscription_manage),
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                else -> {
                    Button(
                        onClick = { billingController.buyMonthly() },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.onCard,
                            contentColor = AppColors.cardGradientEnd
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = monthlyPrice,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { billingController.buyYearly() },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.onCard,
                            contentColor = AppColors.cardGradientEnd
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = yearlyPrice,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { billingController.restore() },
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text(stringResource(Res.string.subscription_restore), color = AppColors.onCard, fontFamily = FontFamily.Serif, fontSize = 14.sp)
                    }

                    Text(
                        text = stringResource(Res.string.subscription_autorenew_info),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Serif,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onOpenTerms) {
                            Text(stringResource(Res.string.subscription_terms), color = Color.White.copy(alpha = 0.7f), fontFamily = FontFamily.Serif, fontSize = 10.sp)
                        }
                        Text("|", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                        TextButton(onClick = onOpenPrivacy) {
                            Text(stringResource(Res.string.subscription_privacy), color = Color.White.copy(alpha = 0.7f), fontFamily = FontFamily.Serif, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BenefitRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = AppColors.gold
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, fontFamily = FontFamily.Serif, fontSize = 15.sp)
    }
}