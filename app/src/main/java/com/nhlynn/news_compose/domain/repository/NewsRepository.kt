package com.nhlynn.news_compose.domain.repository

import com.nhlynn.news_compose.domain.model.Article
import com.nhlynn.news_compose.domain.model.Resource

interface NewsRepository {
    suspend fun getTopHeadLines(
        category: String
    ): Resource<List<Article>>

    suspend fun searchNews(
        query: String
    ): Resource<List<Article>>
}