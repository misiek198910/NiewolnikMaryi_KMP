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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import org.jetbrains.compose.resources.stringResource
import slaveofmary.shared.generated.resources.Res
import slaveofmary.shared.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraktatScreen(
    onNavigateToText: (file: String, title: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val settings = remember { Settings() }

    var refreshTrigger by remember { mutableStateOf(0) }
    var expandedTab by remember { mutableIntStateOf(settings.getInt("traktat_expanded_tab", -1)) }

    // Stan okna potwierdzającego przeskoczenie w przód
    var confirmDialogGlobalDay by remember { mutableIntStateOf(-1) }
    var confirmDialogAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Stan okna potwierdzającego resetowanie dni
    var clearDialogSection by remember { mutableIntStateOf(-1) }

    val sections = listOf(12, 7, 7, 7)

    // Tytuły ekranu dla poszczególnych dni ("Dzień N") — wyliczane w kompozycji,
    // bo stringResource nie da się wywołać w lambdzie kliknięcia.
    val dayTitles = (1..sections.sum()).map { stringResource(Res.string.day_number, it) }

    fun getGlobalDay(idx: Int, localIndex: Int): Int {
        var sum = 0
        for (i in 0 until idx) sum += sections[i]
        return sum + localIndex + 1
    }

    fun hasUnreadPreviousDays(targetGlobalDay: Int): Boolean {
        var currentGlobal = 1
        for (idx in sections.indices) {
            for (i in 0 until sections[idx]) {
                if (currentGlobal < targetGlobalDay) {
                    if (!settings.getBoolean("pref_day_${idx}_$i", false)) {
                        return true
                    }
                }
                currentGlobal++
            }
        }
        return false
    }

    fun markDaysUpTo(targetGlobalDay: Int) {
        var currentGlobal = 1
        for (idx in sections.indices) {
            for (i in 0 until sections[idx]) {
                if (currentGlobal <= targetGlobalDay) {
                    settings.putBoolean("pref_day_${idx}_$i", true)
                }
                currentGlobal++
            }
        }
        refreshTrigger++
    }

    val nextDay = remember(refreshTrigger) {
        var day = -1
        var globalDayCounter = 1

        outer@ for (idx in sections.indices) {
            for (i in 0 until sections[idx]) {
                if (!settings.getBoolean("pref_day_${idx}_$i", false)) {
                    day = globalDayCounter
                    break@outer
                }
                globalDayCounter++
            }
        }
        day
    }

    val nextDayText = if (nextDay != -1) {
        stringResource(Res.string.traktat_next_day, nextDay)
    } else {
        stringResource(Res.string.traktat_all_done)
    }

    val handleDayClick = { idx: Int, localIndex: Int ->
        val targetGlobalDay = getGlobalDay(idx, localIndex)
        val action = {
            markDaysUpTo(targetGlobalDay)
            onNavigateToText("Tr_${idx}_$localIndex", dayTitles.getOrElse(targetGlobalDay - 1) { "" })
        }

        if (hasUnreadPreviousDays(targetGlobalDay)) {
            confirmDialogGlobalDay = targetGlobalDay
            confirmDialogAction = action
        } else {
            action()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.main_button1),
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

            if (isLandscape) {
                LandscapeTraktatContent(
                    nextDayText = nextDayText,
                    sections = sections,
                    expandedTab = expandedTab,
                    onToggleExpand = { idx ->
                        expandedTab = if (expandedTab == idx) -1 else idx
                        settings.putInt("traktat_expanded_tab", expandedTab)
                    },
                    settings = settings,
                    refreshTrigger = refreshTrigger,
                    onDayClick = handleDayClick,
                    onClearRequest = { idx -> clearDialogSection = idx },
                    onNavigateToText = onNavigateToText
                )
            } else {
                PortraitTraktatContent(
                    nextDayText = nextDayText,
                    sections = sections,
                    expandedTab = expandedTab,
                    onToggleExpand = { idx ->
                        expandedTab = if (expandedTab == idx) -1 else idx
                        settings.putInt("traktat_expanded_tab", expandedTab)
                    },
                    settings = settings,
                    refreshTrigger = refreshTrigger,
                    onDayClick = handleDayClick,
                    onClearRequest = { idx -> clearDialogSection = idx },
                    onNavigateToText = onNavigateToText
                )
            }
        }

        // --- OKNO DIALOGOWE POTWIERDZENIA ZAZNACZENIA W PRZÓD ---
        if (confirmDialogGlobalDay != -1) {
            AlertDialog(
                onDismissRequest = { confirmDialogGlobalDay = -1 },
                title = {
                    Text(
                        text = stringResource(Res.string.confirmation_title),
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = stringResource(Res.string.traktat_jump_confirm, confirmDialogGlobalDay),
                        fontFamily = FontFamily.Serif
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            confirmDialogAction?.invoke()
                            confirmDialogGlobalDay = -1
                        }
                    ) {
                        Text(stringResource(Res.string.yes), color = AppColors.accent, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { confirmDialogGlobalDay = -1 }
                    ) {
                        Text(stringResource(Res.string.no), color = AppColors.textSecondary, fontFamily = FontFamily.Serif)
                    }
                },
                containerColor = AppColors.surface
            )
        }

        // --- OKNO DIALOGOWE POTWIERDZENIA USUNIĘCIA POSTĘPU ---
        if (clearDialogSection != -1) {
            var clearAll by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { clearDialogSection = -1 },
                title = {
                    Text(
                        text = stringResource(Res.string.progress_delete_title),
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = stringResource(Res.string.traktat_clear_stage_confirm),
                            fontFamily = FontFamily.Serif
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { clearAll = !clearAll }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = clearAll,
                                onCheckedChange = { clearAll = it },
                                colors = CheckboxDefaults.colors(checkedColor = AppColors.accent)
                            )
                            Text(
                                text = stringResource(Res.string.traktat_clear_all_stages),
                                fontFamily = FontFamily.Serif,
                                fontSize = 14.sp
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val clearAllFlag = clearAll
                            // Jeśli zaznaczono Checkbox, usuwamy wszystko od pierwszego etapu (0) do ostatniego
                            val startIdx = if (clearAllFlag) 0 else clearDialogSection
                            val endIdx = if (clearAllFlag) sections.lastIndex else clearDialogSection

                            for (idxToClear in startIdx..endIdx) {
                                for (i in 0 until sections[idxToClear]) {
                                    settings.putBoolean("pref_day_${idxToClear}_$i", false)
                                }
                            }
                            refreshTrigger++
                            clearDialogSection = -1
                        }
                    ) {
                        Text(stringResource(Res.string.progress_delete_confirm), color = AppColors.danger, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { clearDialogSection = -1 }
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
private fun PortraitTraktatContent(
    nextDayText: String,
    sections: List<Int>,
    expandedTab: Int,
    onToggleExpand: (Int) -> Unit,
    settings: Settings,
    refreshTrigger: Int,
    onDayClick: (Int, Int) -> Unit,
    onClearRequest: (Int) -> Unit,
    onNavigateToText: (String, String) -> Unit
) {
    val scrollState = rememberScrollState()

    val introTitle = stringResource(Res.string.traktat_title_intro)
    val actTitle = stringResource(Res.string.traktat_title_act)
    val termsTitle = stringResource(Res.string.traktat_title_terms)

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            // Wyśrodkowanie w pionie: jeśli treść jest krótsza niż ekran, zostaje
            // wyśrodkowana; jeśli jest dłuższa (rozwinięte akordeony), po prostu
            // przewija się normalnie od góry — Arrangement.Center nie psuje scrolla.
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            TraktatSimpleCard(title = stringResource(Res.string.traktat_button1)) {
                onNavigateToText("wprowadzenie", introTitle)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = nextDayText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = AppColors.textPrimary,
                    fontFamily = FontFamily.Serif
                )
            }

            TraktatAccordionCard(
                title = stringResource(Res.string.traktat_button2),
                idx = 0,
                daysCount = sections[0],
                startGlobalDay = 1,
                settings = settings,
                refreshTrigger = refreshTrigger,
                isExpanded = expandedTab == 0,
                onToggleExpand = { onToggleExpand(0) },
                onDayClick = { dayIndex, _ -> onDayClick(0, dayIndex) },
                onClearRequest = { onClearRequest(0) }
            )

            TraktatAccordionCard(
                title = stringResource(Res.string.traktat_button3),
                idx = 1,
                daysCount = sections[1],
                startGlobalDay = 13,
                settings = settings,
                refreshTrigger = refreshTrigger,
                isExpanded = expandedTab == 1,
                onToggleExpand = { onToggleExpand(1) },
                onDayClick = { dayIndex, _ -> onDayClick(1, dayIndex) },
                onClearRequest = { onClearRequest(1) }
            )

            TraktatAccordionCard(
                title = stringResource(Res.string.traktat_button4),
                idx = 2,
                daysCount = sections[2],
                startGlobalDay = 20,
                settings = settings,
                refreshTrigger = refreshTrigger,
                isExpanded = expandedTab == 2,
                onToggleExpand = { onToggleExpand(2) },
                onDayClick = { dayIndex, _ -> onDayClick(2, dayIndex) },
                onClearRequest = { onClearRequest(2) }
            )

            TraktatAccordionCard(
                title = stringResource(Res.string.traktat_button5),
                idx = 3,
                daysCount = sections[3],
                startGlobalDay = 27,
                settings = settings,
                refreshTrigger = refreshTrigger,
                isExpanded = expandedTab == 3,
                onToggleExpand = { onToggleExpand(3) },
                onDayClick = { dayIndex, _ -> onDayClick(3, dayIndex) },
                onClearRequest = { onClearRequest(3) }
            )

            TraktatSimpleCard(title = stringResource(Res.string.traktat_button6)) {
                onNavigateToText("Akt", actTitle)
            }

            TraktatSimpleCard(title = stringResource(Res.string.traktat_button7)) {
                onNavigateToText("terminy", termsTitle)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            AdBanner()
        }
    }
}

@Composable
private fun LandscapeTraktatContent(
    nextDayText: String,
    sections: List<Int>,
    expandedTab: Int,
    onToggleExpand: (Int) -> Unit,
    settings: Settings,
    refreshTrigger: Int,
    onDayClick: (Int, Int) -> Unit,
    onClearRequest: (Int) -> Unit,
    onNavigateToText: (String, String) -> Unit
) {
    val introTitle = stringResource(Res.string.traktat_title_intro)
    val actTitle = stringResource(Res.string.traktat_title_act)
    val termsTitle = stringResource(Res.string.traktat_title_terms)

    Row(modifier = Modifier.fillMaxSize()) {
        // Lewo: trzy proste przyciski (bez akordeonów), przewijalne osobno od reklamy,
        // która jest przypięta na stałe na dole tej kolumny.
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.42f)
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = nextDayText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = AppColors.textPrimary,
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                TraktatSimpleListItem(title = stringResource(Res.string.traktat_button1)) {
                    onNavigateToText("wprowadzenie", introTitle)
                }
                TraktatSimpleListItem(title = stringResource(Res.string.traktat_button6)) {
                    onNavigateToText("Akt", actTitle)
                }
                TraktatSimpleListItem(title = stringResource(Res.string.traktat_button7)) {
                    onNavigateToText("terminy", termsTitle)
                }
            }

            // Przypięte na stałe, poza scrollem powyżej.
            AdBanner()
        }

        // Prawo: cztery akordeony z dniami, przewijalne osobno od lewej strony.
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.58f)
                .padding(vertical = 8.dp, horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                TraktatAccordionCard(
                    title = stringResource(Res.string.traktat_button2),
                    idx = 0,
                    daysCount = sections[0],
                    startGlobalDay = 1,
                    settings = settings,
                    refreshTrigger = refreshTrigger,
                    isExpanded = expandedTab == 0,
                    onToggleExpand = { onToggleExpand(0) },
                    onDayClick = { dayIndex, _ -> onDayClick(0, dayIndex) },
                    onClearRequest = { onClearRequest(0) },
                    titleFontSize = 15.sp,
                    headerPadding = 12.dp,
                    cornerRadius = 16.dp
                )
            }
            item {
                TraktatAccordionCard(
                    title = stringResource(Res.string.traktat_button3),
                    idx = 1,
                    daysCount = sections[1],
                    startGlobalDay = 13,
                    settings = settings,
                    refreshTrigger = refreshTrigger,
                    isExpanded = expandedTab == 1,
                    onToggleExpand = { onToggleExpand(1) },
                    onDayClick = { dayIndex, _ -> onDayClick(1, dayIndex) },
                    onClearRequest = { onClearRequest(1) },
                    titleFontSize = 15.sp,
                    headerPadding = 12.dp,
                    cornerRadius = 16.dp
                )
            }
            item {
                TraktatAccordionCard(
                    title = stringResource(Res.string.traktat_button4),
                    idx = 2,
                    daysCount = sections[2],
                    startGlobalDay = 20,
                    settings = settings,
                    refreshTrigger = refreshTrigger,
                    isExpanded = expandedTab == 2,
                    onToggleExpand = { onToggleExpand(2) },
                    onDayClick = { dayIndex, _ -> onDayClick(2, dayIndex) },
                    onClearRequest = { onClearRequest(2) },
                    titleFontSize = 15.sp,
                    headerPadding = 12.dp,
                    cornerRadius = 16.dp
                )
            }
            item {
                TraktatAccordionCard(
                    title = stringResource(Res.string.traktat_button5),
                    idx = 3,
                    daysCount = sections[3],
                    startGlobalDay = 27,
                    settings = settings,
                    refreshTrigger = refreshTrigger,
                    isExpanded = expandedTab == 3,
                    onToggleExpand = { onToggleExpand(3) },
                    onDayClick = { dayIndex, _ -> onDayClick(3, dayIndex) },
                    onClearRequest = { onClearRequest(3) },
                    titleFontSize = 15.sp,
                    headerPadding = 12.dp,
                    cornerRadius = 16.dp
                )
            }
        }
    }
}

/** Kompaktowy wariant TraktatSimpleCard — te same wymiary co MenuListItem z MainScreen (landscape). */
@Composable
private fun TraktatSimpleListItem(title: String, onClick: () -> Unit) {
    val cardGradient = Brush.linearGradient(
        colors = listOf(AppColors.cardGradientStart, AppColors.cardGradientEnd)
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = AppColors.onCard
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardGradient)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                fontFamily = FontFamily.Serif
            )
        }
    }
}

@Composable
fun TraktatSimpleCard(title: String, onClick: () -> Unit) {
    val cardGradient = Brush.linearGradient(
        colors = listOf(AppColors.cardGradientStart, AppColors.cardGradientEnd)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
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
                .padding(20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                fontFamily = FontFamily.Serif
            )
        }
    }
}

@Composable
fun TraktatAccordionCard(
    title: String,
    idx: Int,
    daysCount: Int,
    startGlobalDay: Int,
    settings: Settings,
    refreshTrigger: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onDayClick: (dayIndex: Int, displayDay: Int) -> Unit,
    onClearRequest: () -> Unit,
    titleFontSize: androidx.compose.ui.unit.TextUnit = 18.sp,
    headerPadding: androidx.compose.ui.unit.Dp = 20.dp,
    cornerRadius: androidx.compose.ui.unit.Dp = 24.dp
) {
    val cardGradient = Brush.linearGradient(
        colors = listOf(AppColors.cardGradientStart, AppColors.cardGradientEnd)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = AppColors.onCard
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardGradient)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(headerPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = titleFontSize,
                    fontFamily = FontFamily.Serif
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = AppColors.onCard
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                val itemsPerRow = 5
                val chunkedDays = (0 until daysCount).chunked(itemsPerRow)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    for (rowItems in chunkedDays) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            for (i in rowItems) {
                                val displayDay = i + 1
                                val globalDay = startGlobalDay + i
                                val key = "pref_day_${idx}_$i"

                                val isRead = remember(refreshTrigger) {
                                    settings.getBoolean(key, false)
                                }

                                val bgColor = if (isRead) AppColors.success else AppColors.dayCircle
                                val textColor = if (isRead) AppColors.onCard else AppColors.dayCircleText

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(bgColor)
                                            .clickable {
                                                onDayClick(i, displayDay)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = displayDay.toString(),
                                            color = textColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            fontFamily = FontFamily.Serif
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = stringResource(Res.string.day_short, globalDay),
                                        color = AppColors.onCard,
                                        fontSize = 12.sp,
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
                            .height(45.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.danger)
                    ) {
                        Text(
                            text = stringResource(Res.string.traktat_clear_marks),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AppColors.onCard,
                            fontFamily = FontFamily.Serif
                        )
                    }
                }
            }
        }
    }
}