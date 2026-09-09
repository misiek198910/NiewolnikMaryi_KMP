package com.example.slaveofmary.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slaveofmary.data.entity.NewsItem
import com.example.slaveofmary.data.repository.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import slaveofmary.shared.generated.resources.Res
import slaveofmary.shared.generated.resources.news_error_fetch
import slaveofmary.shared.generated.resources.news_error_network

class InformationViewModel(private val repository: NewsRepository) : ViewModel() {

    private val _newsList = MutableStateFlow<List<NewsItem>>(emptyList())
    val newsList: StateFlow<List<NewsItem>> = _newsList.asStateFlow()

    private val _isLoadingNews = MutableStateFlow(false)
    val isLoadingNews: StateFlow<Boolean> = _isLoadingNews.asStateFlow()

    private val _newsErrorMessage = MutableStateFlow<String?>(null)
    val newsErrorMessage: StateFlow<String?> = _newsErrorMessage.asStateFlow()

    init {
        loadNews()
    }

    fun loadNews() {
        viewModelScope.launch {
            _isLoadingNews.value = true
            _newsErrorMessage.value = null

            try {
                val result = repository.fetchNews()
                if (result != null) {
                    _newsList.value = result
                } else {
                    _newsErrorMessage.value = getString(Res.string.news_error_fetch)
                }
            } catch (e: Exception) {
                _newsErrorMessage.value = getString(Res.string.news_error_network, e.message ?: "")
            } finally {
                _isLoadingNews.value = false
            }
        }
    }
}