package com.example.london_live_bus_journey_tracker.data.repository

import com.example.london_live_bus_journey_tracker.data.mapper.TflMapper
import com.example.london_live_bus_journey_tracker.data.remote.api.TflApiService
import com.example.london_live_bus_journey_tracker.domain.common.Result
import com.example.london_live_bus_journey_tracker.domain.model.Location
import com.example.london_live_bus_journey_tracker.domain.repository.LocationRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [LocationRepository] using TfL API.
 *
 * **Open/Closed Principle**: Can be extended without modification.
 * **Dependency Inversion**: Depends on abstractions ([TflApiService]).
 */
@Singleton
class LocationRepositoryImpl @Inject constructor(
    private val apiService: TflApiService,
    private val mapper: TflMapper,
    private val ioDispatcher: CoroutineDispatcher
) : LocationRepository {

    /**
     * Searches for locations matching the query.
     *
     * Executes on IO dispatcher for network safety.
     */
    override suspend fun searchLocations(query: String): Result<List<Location>> {
        return withContext(ioDispatcher) {
            try {
                val response = apiService.searchStopPoints(
                    query = query,
                    modes = "bus",
                    maxResults = MAX_RESULTS
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val locations = mapper.mapToLocations(body.matches)
                        Result.Success(locations)
                    } else {
                        Result.Success(emptyList())
                    }
                } else {
                    Result.Error("Search failed: ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error(
                    message = e.message ?: "Network error",
                    exception = e
                )
            }
        }
    }

    companion object {
        private const val MAX_RESULTS = 25
    }
}