package com.nhlynn.news_compose.network.repository

import com.nhlynn.news_compose.domain.model.Article
import com.nhlynn.news_compose.domain.model.Resource
import com.nhlynn.news_compose.domain.repository.NewsRepository
import com.nhlynn.news_compose.network.ApiService
import java.lang.Exception

class NewsRepositoryImpl(private val apiService: ApiService) : NewsRepository {
    override suspend fun getTopHeadLines(category: String): Resource<List<Article>> {
        return try {
            val response = apiService.getBreakingNews(category = category)
            Resource.Success(response.articles)
        } catch (e: Exception) {
            Resource.Error(message = "Failed to fetch news ${e.message}")
        }
    }

    override suspend fun searchNews(query: String): Resource<List<Article>> {
        return try {
            val response = apiService.searchNews(query = query)
            Resource.Success(response.articles)
        } catch (e: Exception) {
            Resource.Error(message = "Failed to fetch news ${e.message}")
        }
    }
}