package com.nhlynn.news_compose.presentation.news_screen

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewModelScope
import com.nhlynn.news_compose.domain.model.Article
import com.nhlynn.news_compose.domain.model.Resource
import com.nhlynn.news_compose.domain.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsScreenViewModel
@Inject
constructor(private val newsRepository: NewsRepository) :
    ViewModel() {
    var articles by mutableStateOf<List<Article>>(emptyList())

    var state by mutableStateOf(NewsScreenState())

    var searchJob: Job? = null

    fun onEvent(event: NewsScreenEvent) {
        when (event) {
            is NewsScreenEvent.OnCategoryChanged -> {
                state = state.copy(category = event.category)
                getNewsArticle(state.category)
            }

            NewsScreenEvent.OnCloseIconClicked -> {
                state = state.copy(isSearchBarVisible = false)
                getNewsArticle(category = state.category)
            }

            is NewsScreenEvent.OnNewsCardClicked -> {
                state = state.copy(selectedArticle = event.article)
            }

            NewsScreenEvent.OnSearchIconClicked -> {
                state = state.copy(isSearchBarVisible = true, articles = emptyList())
            }

            is NewsScreenEvent.OnSearchQueryChanged -> {
                state = state.copy(searchQuery = event.searchQuery)
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(1000)
                    searchNews(state.searchQuery)
                }
            }
        }
    }

    init {
        getNewsArticle(category = "general")
    }

    private fun getNewsArticle(category: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            when (val response = newsRepository.getTopHeadLines(category)) {
                is Resource.Success -> {
                    articles = response.data ?: emptyList()
                    state = state.copy(
                        articles = response.data ?: emptyList(),
                        isLoading = false
                    )
                }

                is Resource.Error -> {
                    state = state.copy(
                        error = response.message,
                        articles = emptyList(),
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun searchNews(query: String) {
        if (query.isEmpty()) {
            return
        }
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            when (val response = newsRepository.searchNews(query)) {
                is Resource.Success -> {
                    articles = response.data ?: emptyList()
                    state = state.copy(
                        articles = response.data ?: emptyList(),
                        isLoading = false
                    )
                }

                is Resource.Error -> {
                    state = state.copy(
                        error = response.message,
                        articles = emptyList(),
                        isLoading = false
                    )
                }
            }
        }
    }
}