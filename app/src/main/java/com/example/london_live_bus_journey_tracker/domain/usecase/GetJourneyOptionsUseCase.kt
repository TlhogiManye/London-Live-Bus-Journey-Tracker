package com.example.london_live_bus_journey_tracker.domain.usecase

import com.example.london_live_bus_journey_tracker.domain.common.Result
import com.example.london_live_bus_journey_tracker.domain.model.JourneyOption
import com.example.london_live_bus_journey_tracker.domain.repository.JourneyRepository
import javax.inject.Inject

/**
 * Use case for planning a journey between two locations.
 *
 * **Single Responsibility Principle**: Handles only journey planning logic.
 */
class GetJourneyOptionsUseCase @Inject constructor(
    private val journeyRepository: JourneyRepository
) {
    /**
     * Plans a journey and returns available bus routes.
     *
     * @param fromId Origin location ID
     * @param toId Destination location ID
     * @return [Result] with journey options sorted by duration
     */
    suspend operator fun invoke(fromId: String, toId: String): Result<List<JourneyOption>> {
        if (fromId.isBlank() || toId.isBlank()) {
            return Result.Error("Origin and destination are required")
        }

        if (fromId == toId) {
            return Result.Error("Origin and destination cannot be the same")
        }

        return journeyRepository.planJourney(fromId, toId).map { options ->
            options.sortedBy { it.durationMinutes }
        }
    }
}