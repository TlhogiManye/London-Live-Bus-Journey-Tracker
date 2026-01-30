package com.example.london_live_bus_journey_tracker.data.repository

import com.example.london_live_bus_journey_tracker.data.mapper.TflMapper
import com.example.london_live_bus_journey_tracker.data.remote.api.TflApiService
import com.example.london_live_bus_journey_tracker.domain.common.Result
import com.example.london_live_bus_journey_tracker.domain.model.JourneyOption
import com.example.london_live_bus_journey_tracker.domain.repository.JourneyRepository
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
     *
     * Note: TfL API returns HTTP 300 for disambiguation responses.
     * We need to handle both 200 and 300 as valid responses.
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

                val responseCode = response.code()
                val body = response.body()

                // Handle both 200 (success) and 300 (disambiguation) responses
                when {
                    responseCode == 200 && body != null -> {
                        val journeys = body.journeys
                        if (journeys != null && journeys.isNotEmpty()) {
                            val options = mapper.mapToJourneyOptions(journeys)
                            if (options.isNotEmpty()) {
                                Result.Success(options)
                            } else {
                                Result.Error("No bus routes found between these locations")
                            }
                        } else {
                            Result.Error("No routes found between these locations")
                        }
                    }
                    responseCode == 300 && body != null -> {
                        // HTTP 300 means disambiguation is needed
                        val fromOptions = body.fromLocationDisambiguation?.disambiguationOptions
                        val toOptions = body.toLocationDisambiguation?.disambiguationOptions

                        val message = buildString {
                            append("Location is ambiguous. ")
                            if (!fromOptions.isNullOrEmpty()) {
                                append("Multiple 'from' locations found. ")
                            }
                            if (!toOptions.isNullOrEmpty()) {
                                append("Multiple 'to' locations found. ")
                            }
                            append("Please select a more specific location.")
                        }
                        Result.Error(message)
                    }
                    responseCode == 404 -> {
                        Result.Error("No routes found between these locations")
                    }
                    responseCode == 400 -> {
                        Result.Error("Invalid location format")
                    }
                    else -> {
                        Result.Error("Journey planning failed (${responseCode}): ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                Result.Error(
                    message = e.message ?: "Network error occurred",
                    exception = e
                )
            }
        }
    }
}