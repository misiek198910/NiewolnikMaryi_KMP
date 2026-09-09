package com.example.slaveofmary.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.slaveofmary.ui.components.AdBanner
import com.example.slaveofmary.ui.theme.AppColors
import com.russhwolf.settings.Settings
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import slaveofmary.shared.generated.resources.Res
import slaveofmary.shared.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowennyScreen(
    onNavigateToText: (file: String, title: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val settings = remember { Settings() }
    var refreshTrigger by remember { mutableStateOf(0) }
    val listItems = stringArrayResource(Res.array.listView2_items1)

    // Indeks nowenny, dla której otwarto okno potwierdzenia odznaczenia dni (-1 = zamknięte).
    var clearDialogNovena by remember { mutableIntStateOf(-1) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.main_button3),
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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val isLandscape = maxWidth > maxHeight

            val onClearNovena: (Int) -> Unit = { novenaIndex ->
                clearDialogNovena = novenaIndex
            }

            if (isLandscape) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Lewa strona: reklama, wyśrodkowana pionowo.
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.42f)
                            .navigationBarsPadding()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        AdBanner()
                    }

                    // Prawa strona: przewijalna lista nowenn (mniejsze karty).
                    NowennyList(
                        listItems = listItems,
                        settings = settings,
                        refreshTrigger = refreshTrigger,
                        scrollState = scrollState,
                        compact = true,
                        onNavigateToText = onNavigateToText,
                        onClearNovena = onClearNovena,
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.58f)
                            .padding(horizontal = 12.dp)
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    NowennyList(
                        listItems = listItems,
                        settings = settings,
                        refreshTrigger = refreshTrigger,
                        scrollState = scrollState,
                        compact = false,
                        onNavigateToText = onNavigateToText,
                        onClearNovena = onClearNovena,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    )

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

        // --- OKNO DIALOGOWE POTWIERDZENIA ODZNACZENIA DNI ---
        if (clearDialogNovena != -1) {
            AlertDialog(
                onDismissRequest = { clearDialogNovena = -1 },
                title = {
                    Text(
                        text = stringResource(Res.string.progress_delete_title),
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = stringResource(Res.string.nowenny_clear_confirm),
                        fontFamily = FontFamily.Serif
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val novenaIndex = clearDialogNovena
                            for (day in 0 until 9) {
                                settings.putBoolean("pref_nowenna_${novenaIndex}_$day", false)
                            }
                            refreshTrigger++
                            clearDialogNovena = -1
                        }
                    ) {
                        Text(stringResource(Res.string.traktat_clear_marks), color = AppColors.danger, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { clearDialogNovena = -1 }
                    ) {
                        Text(stringResource(Res.string.cancel), color = AppColors.textSecondary, fontFamily = FontFamily.Serif)
                    }
                },
                containerColor = AppColors.surface
            )
        }
    }
}

@Composable
private fun NowennyList(
    listItems: List<String>,
    settings: Settings,
    refreshTrigger: Int,
    scrollState: ScrollState,
    compact: Boolean,
    onNavigateToText: (file: String, title: String) -> Unit,
    onClearNovena: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        val dayTitles = (1..9).map { stringResource(Res.string.day_number, it) }

        listItems.forEachIndexed { index, title ->
            NowennaAccordionCard(
                title = title,
                novenaIndex = index,
                daysCount = 9,
                settings = settings,
                refreshTrigger = refreshTrigger,
                compact = compact,
                onDayClick = { dayIndex, displayDay ->
                    val fileName = "Now_${index}_${dayIndex}"
                    onNavigateToText(fileName, dayTitles.getOrElse(displayDay - 1) { "" })
                },
                onClearRequest = { onClearNovena(index) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun NowennaAccordionCard(
    title: String,
    novenaIndex: Int,
    daysCount: Int,
    settings: Settings,
    refreshTrigger: Int,
    compact: Boolean = false,
    onDayClick: (dayIndex: Int, displayDay: Int) -> Unit,
    onClearRequest: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val cardGradient = Brush.linearGradient(
        colors = listOf(AppColors.cardGradientStart, AppColors.cardGradientEnd)
    )

    val headerPadding = if (compact) 14.dp else 20.dp
    val contentPadding = if (compact) 14.dp else 20.dp
    val circleSize = if (compact) 40.dp else 48.dp
    val cellSpacing = if (compact) 12.dp else 16.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (compact) 6.dp else 8.dp),
        shape = RoundedCornerShape(if (compact) 16.dp else 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = AppColors.onCard
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (compact) 4.dp else 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardGradient)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(
                        horizontal = headerPadding,
                        vertical = if (compact) 10.dp else headerPadding
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) 15.sp else 18.sp,
                    fontFamily = FontFamily.Serif
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = AppColors.onCard
                )
            }

            AnimatedVisibility(visible = expanded) {
                val itemsPerRow = 5
                val chunkedDays = (0 until daysCount).chunked(itemsPerRow)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = contentPadding, end = contentPadding, bottom = contentPadding),
                    verticalArrangement = Arrangement.spacedBy(cellSpacing)
                ) {
                    for (rowItems in chunkedDays) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(cellSpacing)
                        ) {
                            for (i in rowItems) {
                                val displayDay = i + 1
                                val key = "pref_nowenna_${novenaIndex}_$i"

                                var isRead by remember(refreshTrigger) {
                                    mutableStateOf(settings.getBoolean(key, false))
                                }

                                val bgColor = if (isRead) AppColors.success else AppColors.dayCircle
                                val textColor = if (isRead) AppColors.onCard else AppColors.dayCircleText

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(circleSize)
                                            .clip(CircleShape)
                                            .background(bgColor)
                                            .clickable {
                                                isRead = true
                                                settings.putBoolean(key, true)
                                                onDayClick(i, displayDay)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = displayDay.toString(),
                                            color = textColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = if (compact) 14.sp else 16.sp,
                                            fontFamily = FontFamily.Serif
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = stringResource(Res.string.day_number, displayDay),
                                        color = AppColors.onCard,
                                        fontSize = if (compact) 11.sp else 12.sp,
                                        fontFamily = FontFamily.Serif
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onClearRequest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (compact) 40.dp else 45.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.danger)
                    ) {
                        Text(
                            text = stringResource(Res.string.traktat_clear_marks),
                            fontWeight = FontWeight.Bold,
                            fontSize = if (compact) 13.sp else 14.sp,
                            color = AppColors.onCard,
                            fontFamily = FontFamily.Serif
                        )
                    }
                }
            }
        }
    }
}
