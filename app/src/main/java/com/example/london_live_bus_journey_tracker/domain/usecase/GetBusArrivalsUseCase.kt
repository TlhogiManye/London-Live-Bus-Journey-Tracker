package com.example.london_live_bus_journey_tracker.domain.usecase

import com.example.london_live_bus_journey_tracker.domain.common.Result
import com.example.london_live_bus_journey_tracker.domain.model.BusArrival
import com.example.london_live_bus_journey_tracker.domain.repository.BusRepository
import javax.inject.Inject

/**
 * Use case for fetching live bus arrival predictions.
 *
 * **Single Responsibility Principle**: Handles only arrival prediction logic.
 *
 * ## Polling
 * Call this use case every 30 seconds for real-time updates:
 * ```kotlin
 * viewModelScope.launch {
 *     while (isActive) {
 *         getBusArrivals(lineId).onSuccess { arrivals ->
 *             _uiState.value = UiState.Success(arrivals)
 *         }
 *         delay(30_000)
 *     }
 * }
 * ```
 */
class GetBusArrivalsUseCase @Inject constructor(
    private val busRepository: BusRepository
) {
    /**
     * Fetches live arrival predictions for a bus line.
     *
     * @param lineId Bus line ID (e.g., "24")
     * @return [Result] with arrivals sorted by time, filtered to next 30 minutes
     */
    suspend operator fun invoke(lineId: String): Result<List<BusArrival>> {
        if (lineId.isBlank()) {
            return Result.Error("Line ID is required")
        }

        return busRepository.getArrivalsForLine(lineId).map { arrivals ->
            arrivals
                .filter { it.timeToStationMinutes <= MAX_ARRIVAL_MINUTES }
                .distinctBy { it.vehicleId }
                .sortedBy { it.timeToStationSeconds }
        }
    }

    companion object {
        /** Only show arrivals within 30 minutes. */
        const val MAX_ARRIVAL_MINUTES = 30

        /** Recommended polling interval. */
        const val POLLING_INTERVAL_MS = 30_000L
    }
}