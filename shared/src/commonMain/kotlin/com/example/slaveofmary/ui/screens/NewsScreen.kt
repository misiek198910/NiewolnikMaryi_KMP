package com.example.slaveofmary.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.example.slaveofmary.data.entity.NewsItem
import com.example.slaveofmary.data.repository.NewsRepository
import com.example.slaveofmary.data.viewmodel.InformationViewModel
import com.example.slaveofmary.ui.components.AdBanner
import com.example.slaveofmary.ui.theme.AppColors
import com.example.slaveofmary.remote.RemoteClient
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import slaveofmary.shared.generated.resources.Res
import slaveofmary.shared.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InformationScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: InformationViewModel = viewModel(factory = viewModelFactory {
        initializer {
            val apiService = RemoteClient.apiService
            val repository = NewsRepository(apiService)
            InformationViewModel(repository)
        }
    })

    val newsList by viewModel.newsList.collectAsState()
    val isLoading by viewModel.isLoadingNews.collectAsState()
    val errorMessage by viewModel.newsErrorMessage.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.main_button_info),
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

                    // Prawa strona: wiadomości.
                    NewsContent(
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        newsList = newsList,
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.58f)
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    NewsContent(
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        newsList = newsList,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
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
    }
}

@Composable
private fun NewsContent(
    isLoading: Boolean,
    errorMessage: String?,
    newsList: List<NewsItem>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = AppColors.accent
            )
        } else if (!errorMessage.isNullOrEmpty()) {
            Text(
                text = errorMessage,
                color = AppColors.danger,
                modifier = Modifier.align(Alignment.Center),
                fontFamily = FontFamily.Serif
            )
        } else {
            if (newsList.isEmpty()) {
                Text(
                    text = stringResource(Res.string.news_empty),
                    modifier = Modifier.align(Alignment.Center),
                    fontFamily = FontFamily.Serif
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(newsList) { item ->
                        if (item.isVisible) {
                            NewsItemCard(item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewsItemCard(item: NewsItem) {
    val formattedDate = remember(item.timestamp) {
        val ts = item.timestamp
        if (ts == null) {
            ""
        } else {
            try {
                val instant = Instant.fromEpochMilliseconds(ts)
                val timeZone: TimeZone = TimeZone.currentSystemDefault()
                val date = instant.toLocalDateTime(timeZone)

                val day = date.dayOfMonth.toString().padStart(2, '0')
                val month = date.monthNumber.toString().padStart(2, '0')
                val year = date.year

                "$day.$month.$year"
            } catch (e: Exception) {
                ""
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.newsCard,
            contentColor = AppColors.textPrimary
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (!item.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = stringResource(Res.string.news_image_desc),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    onState = { state ->
                        if (state is AsyncImagePainter.State.Error) {
                            println("Błąd pobierania obrazka: ${state.result.throwable}")
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = item.title,
                color = AppColors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )

            if (formattedDate.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedDate,
                    color = AppColors.textSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Serif
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.content,
                color = AppColors.textPrimary,
                fontSize = 14.sp,
                fontFamily = FontFamily.Serif
            )
        }
    }
}