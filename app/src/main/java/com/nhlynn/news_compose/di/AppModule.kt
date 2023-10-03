package com.nhlynn.news_compose.di

import com.nhlynn.news_compose.domain.repository.NewsRepository
import com.nhlynn.news_compose.network.ApiService
import com.nhlynn.news_compose.network.ApiService.Companion.BASE_URL
import com.nhlynn.news_compose.network.repository.NewsRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApi(): ApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideRepository(apiService: ApiService): NewsRepository {
        return NewsRepositoryImpl(apiService)
    }
}