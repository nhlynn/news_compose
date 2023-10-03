package com.nhlynn.news_compose.network

import com.nhlynn.news_compose.domain.model.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("top-headlines")
    suspend fun getBreakingNews(
        @Query("category") category: String,
        @Query("country") country: String = "us",
        @Query("apiKey") apiKey: String = API_KEY
    ): NewsResponse

    @GET("everything")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("apiKey") apiKey: String = API_KEY
    ): NewsResponse

    companion object {
        const val BASE_URL = "https://newsapi.org/v2/"
        const val API_KEY = "3dd5dbb2fa9648b1ac34003a0adcc55a"
    }
}