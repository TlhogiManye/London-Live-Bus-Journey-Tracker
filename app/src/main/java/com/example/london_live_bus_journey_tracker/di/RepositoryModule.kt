package com.example.london_live_bus_journey_tracker.di

import com.example.london_live_bus_journey_tracker.data.mapper.TflMapper
import com.example.london_live_bus_journey_tracker.data.remote.api.TflApiService
import com.example.london_live_bus_journey_tracker.data.repository.BusRepositoryImpl
import com.example.london_live_bus_journey_tracker.data.repository.LocationRepositoryImpl
import com.example.london_live_bus_journey_tracker.domain.repository.BusRepository
import com.example.london_live_bus_journey_tracker.domain.repository.JourneyRepository
import com.example.london_live_bus_journey_tracker.domain.repository.JourneyRepositoryImpl
import com.example.london_live_bus_journey_tracker.domain.repository.LocationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

/**
 * Hilt module for binding repository interfaces to implementations.
 *
 * **Dependency Inversion Principle**: Domain layer depends on abstractions,
 * this module provides the concrete implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /**
     * Provides [LocationRepository] implementation.
     */
    @Provides
    @Singleton
    fun provideLocationRepository(
        apiService: TflApiService,
        mapper: TflMapper,
        dispatcher: CoroutineDispatcher
    ): LocationRepository {
        return LocationRepositoryImpl(apiService, mapper, dispatcher)
    }

    /**
     * Provides [JourneyRepository] implementation.
     */
    @Provides
    @Singleton
    fun provideJourneyRepository(
        apiService: TflApiService,
        mapper: TflMapper,
        dispatcher: CoroutineDispatcher
    ): JourneyRepository {
        return JourneyRepositoryImpl(apiService, mapper, dispatcher)
    }

    /**
     * Provides [BusRepository] implementation.
     */
    @Provides
    @Singleton
    fun provideBusRepository(
        apiService: TflApiService,
        mapper: TflMapper,
        dispatcher: CoroutineDispatcher
    ): BusRepository {
        return BusRepositoryImpl(apiService, mapper, dispatcher)
    }
}