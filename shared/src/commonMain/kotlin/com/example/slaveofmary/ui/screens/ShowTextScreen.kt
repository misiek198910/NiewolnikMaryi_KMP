package com.example.slaveofmary.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily // NOWY IMPORT
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.slaveofmary.ui.components.AdBanner
import com.example.slaveofmary.ui.theme.AppColors
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import slaveofmary.shared.generated.resources.Res
import slaveofmary.shared.generated.resources.*

@OptIn(ExperimentalResourceApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ShowTextScreen(
    fileName: String,
    title: String,
    onNavigateBack: () -> Unit
) {
    val loadingText = stringResource(Res.string.loading)
    var textContent by remember { mutableStateOf(AnnotatedString(loadingText)) }
    val scrollState = rememberScrollState()

    var baseFontSize by remember { mutableIntStateOf(16) }

    val currentLanguage = Locale.current.language
    val languageSuffix = if (currentLanguage.lowercase() == "pl") "_pl" else "_en"

    val paperColor = AppColors.background
    val textColor = AppColors.textPrimary
    val glassColor = AppColors.surfaceGlass

    LaunchedEffect(fileName, languageSuffix, baseFontSize) {
        try {
            val fullFileName = "${fileName}${languageSuffix}.txt"
            val bytes = Res.readBytes("files/$fullFileName")

            textContent = parseSimpleHtml(bytes.decodeToString(), baseFontSize)
        } catch (e: Exception) {
            val fullFileName = "${fileName}${languageSuffix}.txt"
            textContent = AnnotatedString(
                getString(Res.string.showtext_load_error, fullFileName, e.message ?: "")
            )
        }
    }

    Scaffold(
        containerColor = paperColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
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
        val onDecrease = { if (baseFontSize > 12) baseFontSize -= 2 }
        val onIncrease = { if (baseFontSize < 36) baseFontSize += 2 }

        val textBlock: @Composable (Modifier, Dp) -> Unit = { mod, topSpace ->
            Column(modifier = mod.verticalScroll(scrollState)) {
                Spacer(modifier = Modifier.height(topSpace))

                Text(
                    text = textContent,
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = FontFamily.Serif,
                        lineHeight = (baseFontSize * 1.5).sp
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isLandscape = maxWidth > maxHeight

            if (isLandscape) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding())
                ) {
                    // Lewa strona: pasek Aa +/- oraz reklama, wyśrodkowane pionowo.
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.42f)
                            .navigationBarsPadding()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        FontSizeBar(
                            textColor = textColor,
                            onDecrease = onDecrease,
                            onIncrease = onIncrease
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AdBanner()
                    }

                    // Prawa strona: przewijany tekst.
                    textBlock(
                        Modifier
                            .fillMaxHeight()
                            .weight(0.58f),
                        16.dp
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    textBlock(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        paddingValues.calculateTopPadding() + 16.dp
                    )

                    androidx.compose.material3.Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding(),
                        color = glassColor,
                        shadowElevation = 8.dp
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            FontSizeBar(
                                textColor = textColor,
                                onDecrease = onDecrease,
                                onIncrease = onIncrease
                            )
                            AdBanner()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FontSizeBar(
    textColor: Color,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onDecrease) {
            Icon(Icons.Filled.Remove, contentDescription = stringResource(Res.string.showtext_decrease_font), tint = textColor)
        }
        Text("Aa", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor, fontFamily = FontFamily.Serif)
        IconButton(onClick = onIncrease) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(Res.string.showtext_increase_font), tint = textColor)
        }
    }
}

fun parseSimpleHtml(html: String, baseFontSize: Int): AnnotatedString {
    var textWithNewLines = html
        .replace("<br\\s*/*>".toRegex(RegexOption.IGNORE_CASE), "\n")
        .replace("</br>".toRegex(RegexOption.IGNORE_CASE), "\n")

    // Autorzy plików czasem zamykają nagłówek jako <h1/> lub <h1 /> zamiast </h1>.
    // Normalizujemy to do <\hN>, żeby parser poprawnie wyłączał styl nagłówka.
    textWithNewLines = textWithNewLines.replace(
        Regex("<\\s*(h[1-6])\\s*/\\s*>", RegexOption.IGNORE_CASE)
    ) { "</${it.groupValues[1].lowercase()}>" }

    textWithNewLines = textWithNewLines.replace("\\n[\\s\\n]*\\n".toRegex(), "\n\n")
    textWithNewLines = textWithNewLines.trim()

    val h3FontSize = baseFontSize + 6

    return buildAnnotatedString {
        // Wszystkie nagłówki <h1>..<h6> traktujemy tak samo jak <h3>.
        val tagRegex = Regex("<(/?)b>|<(/?)i>|<(/?)u>|<(/?)h[1-6]>", RegexOption.IGNORE_CASE)
        var currentIndex = 0

        var isBold = false
        var isH3 = false
        var isItalic = false
        var isUnderline = false // Nowy stan dla podkreślenia

        val matches = tagRegex.findAll(textWithNewLines)
        for (match in matches) {
            if (match.range.first > currentIndex) {
                val part = textWithNewLines.substring(currentIndex, match.range.first)
                withStyle(
                    style = SpanStyle(
                        fontWeight = if (isBold || isH3) FontWeight.Bold else null,
                        fontStyle = if (isItalic) FontStyle.Italic else null,
                        textDecoration = if (isUnderline) TextDecoration.Underline else null,
                        fontSize = if (isH3) h3FontSize.sp else baseFontSize.sp
                    )
                ) {
                    append(part)
                }
            }

            val tag = match.value.lowercase()
            when {
                tag == "<b>" -> isBold = true
                tag == "</b>" -> isBold = false
                tag == "<i>" -> isItalic = true
                tag == "</i>" -> isItalic = false
                tag == "<u>" -> isUnderline = true
                tag == "</u>" -> isUnderline = false
                tag.startsWith("</h") -> isH3 = false
                tag.startsWith("<h") -> isH3 = true
            }
            currentIndex = match.range.last + 1
        }

        if (currentIndex < textWithNewLines.length) {
            val part = textWithNewLines.substring(currentIndex)
            withStyle(
                style = SpanStyle(
                    fontWeight = if (isBold || isH3) FontWeight.Bold else null,
                    fontStyle = if (isItalic) FontStyle.Italic else null,
                    textDecoration = if (isUnderline) TextDecoration.Underline else null, // Dodane podkreślenie na końcu tekstu
                    fontSize = if (isH3) h3FontSize.sp else baseFontSize.sp
                )
            ) {
                append(part)
            }
        }
    }
}