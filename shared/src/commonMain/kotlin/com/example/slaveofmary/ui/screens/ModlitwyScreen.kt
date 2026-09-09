package com.example.slaveofmary.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.slaveofmary.ui.components.AdBanner
import com.example.slaveofmary.ui.theme.AppColors
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import slaveofmary.shared.generated.resources.Res
import slaveofmary.shared.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModlitwyScreen(
    onNavigateToText: (file: String, title: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var expanded by remember { mutableStateOf(false) }

    val categories = stringArrayResource(Res.array.spinner4_items)
    val selectedCategory = categories.getOrNull(selectedCategoryIndex) ?: ""

    val listItems = when (selectedCategoryIndex) {
        0 -> stringArrayResource(Res.array.listView1_items1)
        1 -> stringArrayResource(Res.array.listView1_items2)
        else -> emptyList()
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.main_button2),
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
                LandscapeModlitwyContent(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    onCategorySelected = {
                        selectedCategoryIndex = it
                        expanded = false
                    },
                    listItems = listItems,
                    selectedCategoryIndex = selectedCategoryIndex,
                    onNavigateToText = onNavigateToText
                )
            } else {
                PortraitModlitwyContent(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    onCategorySelected = {
                        selectedCategoryIndex = it
                        expanded = false
                    },
                    listItems = listItems,
                    selectedCategoryIndex = selectedCategoryIndex,
                    onNavigateToText = onNavigateToText
                )
            }
        }
    }
}

@Composable
private fun PortraitModlitwyContent(
    categories: List<String>,
    selectedCategory: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onCategorySelected: (Int) -> Unit,
    listItems: List<String>,
    selectedCategoryIndex: Int,
    onNavigateToText: (file: String, title: String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            ModlitwyCategoryDropdown(
                categories = categories,
                selectedCategory = selectedCategory,
                expanded = expanded,
                onExpandedChange = onExpandedChange,
                onCategorySelected = onCategorySelected
            )

            Spacer(modifier = Modifier.height(16.dp))

            ModlitwyList(
                listItems = listItems,
                selectedCategoryIndex = selectedCategoryIndex,
                onNavigateToText = onNavigateToText,
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            AdBanner()
        }
    }
}

@Composable
private fun LandscapeModlitwyContent(
    categories: List<String>,
    selectedCategory: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onCategorySelected: (Int) -> Unit,
    listItems: List<String>,
    selectedCategoryIndex: Int,
    onNavigateToText: (file: String, title: String) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Lewa strona: spinner + reklama, wyśrodkowane pionowo.
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.42f)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ModlitwyCategoryDropdown(
                categories = categories,
                selectedCategory = selectedCategory,
                expanded = expanded,
                onExpandedChange = onExpandedChange,
                onCategorySelected = onCategorySelected
            )

            Spacer(modifier = Modifier.height(12.dp))

            AdBanner()
        }

        // Prawa strona: przewijalna lista modlitw dosunięta do dołu.
        ModlitwyList(
            listItems = listItems,
            selectedCategoryIndex = selectedCategoryIndex,
            onNavigateToText = onNavigateToText,
            compact = true,
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.58f)
                .padding(vertical = 8.dp, horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.Bottom)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModlitwyCategoryDropdown(
    categories: List<String>,
    selectedCategory: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onCategorySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { onExpandedChange(!expanded) },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedCategory,
            onValueChange = {},
            readOnly = true,
            label = {
                Text(
                    text = stringResource(Res.string.modlitwy_rodzaj),
                    fontFamily = FontFamily.Serif
                )
            },
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
            onDismissRequest = { onExpandedChange(false) }
        ) {
            categories.forEachIndexed { index, selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption, fontFamily = FontFamily.Serif) },
                    onClick = { onCategorySelected(index) }
                )
            }
        }
    }
}

@Composable
private fun ModlitwyList(
    listItems: List<String>,
    selectedCategoryIndex: Int,
    onNavigateToText: (file: String, title: String) -> Unit,
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    compact: Boolean = false
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = verticalArrangement
    ) {
        itemsIndexed(listItems) { position, title ->
            ModlitwaItemCard(title = title, compact = compact) {
                val fileName = "Mo_${selectedCategoryIndex}_${position}"
                onNavigateToText(fileName, "")
            }
        }
    }
}

@Composable
fun ModlitwaItemCard(title: String, compact: Boolean = false, onClick: () -> Unit) {
    val cardGradient = Brush.linearGradient(
        colors = listOf(AppColors.cardGradientStart, AppColors.cardGradientEnd)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (compact) Modifier else Modifier.padding(vertical = 8.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(if (compact) 16.dp else 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = AppColors.onCard
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (compact) 4.dp else 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardGradient)
                .padding(
                    horizontal = if (compact) 14.dp else 20.dp,
                    vertical = if (compact) 10.dp else 20.dp
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = if (compact) 15.sp else 18.sp,
                fontFamily = FontFamily.Serif
            )
        }
    }
}
