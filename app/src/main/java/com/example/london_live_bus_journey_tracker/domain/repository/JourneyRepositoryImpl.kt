package com.example.london_live_bus_journey_tracker.domain.repository

import com.example.london_live_bus_journey_tracker.data.mapper.TflMapper
import com.example.london_live_bus_journey_tracker.data.remote.api.TflApiService
import com.example.london_live_bus_journey_tracker.domain.common.Result
import com.example.london_live_bus_journey_tracker.domain.model.JourneyOption
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [JourneyRepository] using TfL Journey Planner API.
 */
@Singleton
class JourneyRepositoryImpl @Inject constructor(
    private val apiService: TflApiService,
    private val mapper: TflMapper,
    private val ioDispatcher: CoroutineDispatcher
) : JourneyRepository {

    /**
     * Plans a journey between two locations.
     *
     * Executes on IO dispatcher for network safety.
     */
    override suspend fun planJourney(
        fromId: String,
        toId: String
    ): Result<List<JourneyOption>> {
        return withContext(ioDispatcher) {
            try {
                val response = apiService.planJourney(
                    from = fromId,
                    to = toId,
                    mode = "bus"
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    val journeys = body?.journeys

                    if (journeys != null && journeys.isNotEmpty()) {
                        val options = mapper.mapToJourneyOptions(journeys)
                        Result.Success(options)
                    } else {
                        // Check for disambiguation
                        val fromOptions = body?.fromLocationDisambiguation?.disambiguationOptions
                        val toOptions = body?.toLocationDisambiguation?.disambiguationOptions

                        if (!fromOptions.isNullOrEmpty() || !toOptions.isNullOrEmpty()) {
                            Result.Error("Location is ambiguous. Please be more specific.")
                        } else {
                            Result.Error("No routes found between these locations")
                        }
                    }
                } else {
                    when (response.code()) {
                        404 -> Result.Error("No routes found")
                        400 -> Result.Error("Invalid locations")
                        else -> Result.Error("Journey planning failed: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                Result.Error(
                    message = e.message ?: "Network error",
                    exception = e
                )
            }
        }
    }
}