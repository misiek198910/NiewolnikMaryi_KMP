package com.example.slaveofmary.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.example.slaveofmary.ui.components.AdBanner
import com.example.slaveofmary.ui.theme.AppColors
import slaveofmary.shared.generated.resources.Res
import slaveofmary.shared.generated.resources.*

@Composable
fun MainScreen(
    onNavigateToTraktat: () -> Unit,
    onNavigateToModlitwy: () -> Unit,
    onNavigateToNowenny: () -> Unit,
    onNavigateToInformacje: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val isLandscape = maxWidth > maxHeight

            val menuItems = listOf(
                MenuItemData(stringResource(Res.string.main_button1), Icons.AutoMirrored.Filled.MenuBook, onNavigateToTraktat),
                MenuItemData(stringResource(Res.string.main_button2), Icons.Filled.Favorite, onNavigateToModlitwy),
                MenuItemData(stringResource(Res.string.main_button3), Icons.Filled.AutoStories, onNavigateToNowenny),
                MenuItemData(stringResource(Res.string.main_button_info), Icons.Filled.Info, onNavigateToInformacje),
                MenuItemData(stringResource(Res.string.main_button5), Icons.Filled.Settings, onNavigateToSettings)
            )

            if (isLandscape) {
                LandscapeMainContent(menuItems)
            } else {
                PortraitMainContent(menuItems)
            }
        }
    }
}

private data class MenuItemData(
    val text: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
private fun PortraitMainContent(menuItems: List<MenuItemData>) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo + menu są wyśrodkowane pionowo jako jeden blok, żeby na wysokim ekranie
        // (iPad) nie zostawała ogromna pusta przestrzeń między logo a przyciskami.
        // Reklama zostaje osobno, dosunięta do dołu.
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo: duże, zajmuje większość szerokości (na tablecie do 720.dp);
            // limit wysokości tylko po to, by nie zdominowało całego ekranu.
            Image(
                painter = painterResource(Res.drawable.niewolnik_maryi_logo),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
                    .padding(vertical = 16.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Menu: 2 kolumny, ostatnia pozycja (Ustawienia) na całą szerokość.
            // Szerokość ograniczona, żeby kafelki miały "przyciskowy" rozmiar także na iPadzie.
            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                menuItems.chunked(2).forEach { rowItems ->
                    if (rowItems.size == 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { item ->
                                MenuCard(
                                    text = item.text,
                                    icon = item.icon,
                                    onClick = item.onClick,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    } else {
                        MenuCard(
                            text = rowItems[0].text,
                            icon = rowItems[0].icon,
                            onClick = rowItems[0].onClick,
                            fullWidth = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        AdBanner()
    }
}

@Composable
private fun LandscapeMainContent(menuItems: List<MenuItemData>) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Lewa strona: logo + reklama, wyśrodkowane pionowo.
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.42f)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.niewolnik_maryi_logo),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .widthIn(max = 360.dp)
                    .fillMaxWidth()
                    .heightIn(max = 140.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            AdBanner()
        }

        // Prawa strona: 6 przycisków ułożonych pionowo, przewijalna lista dosunięta do dołu.
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.58f)
                .padding(vertical = 8.dp, horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.Bottom)
        ) {
            items(menuItems) { item ->
                MenuListItem(text = item.text, icon = item.icon, onClick = item.onClick)
            }
        }
    }
}

@Composable
fun MenuCard(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fullWidth: Boolean = false
) {
    val cardGradient = Brush.linearGradient(
        colors = listOf(AppColors.cardGradientStart, AppColors.cardGradientEnd)
    )
    val shape = RoundedCornerShape(22.dp)

    Card(
        onClick = onClick,
        modifier = modifier
            // Niższe kafelki = bardziej "przyciskowy" wygląd. Karta na całą szerokość
            // ma proporcjonalnie większy współczynnik, żeby mieć tę samą wysokość co pozostałe.
            .aspectRatio(if (fullWidth) 3.2f else 1.55f),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = AppColors.onCard
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 3.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(cardGradient)
        ) {
            // Delikatna "szklana" poświata w górnej części karty.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.55f)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.16f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.16f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp,
                    fontFamily = FontFamily.Serif
                )
            }
        }
    }
}

@Composable
private fun MenuListItem(text: String, icon: ImageVector, onClick: () -> Unit) {
    val cardGradient = Brush.linearGradient(
        colors = listOf(AppColors.cardGradientStart, AppColors.cardGradientEnd)
    )

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = AppColors.onCard
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardGradient)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.White.copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                fontFamily = FontFamily.Serif,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}