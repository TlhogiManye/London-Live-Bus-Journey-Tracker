package com.example.london_live_bus_journey_tracker.domain.usecase

import com.example.london_live_bus_journey_tracker.domain.common.Result
import com.example.london_live_bus_journey_tracker.domain.model.BusPosition
import com.example.london_live_bus_journey_tracker.domain.model.RouteSequence
import com.example.london_live_bus_journey_tracker.domain.repository.BusRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case for tracking a bus position using Virtual GPS.
 *
 * ## Virtual GPS Algorithm
 * Since TfL API doesn't expose live GPS coordinates:
 * 1. Cache route sequence on init (stop coordinates)
 * 2. Poll arrivals every 30 seconds
 * 3. Match vehicle's approaching stop to cached sequence
 * 4. Use stop coordinates as inferred bus position
 *
 * ## Usage
 * ```kotlin
 * trackBusPosition(vehicleId, lineId).collect { result ->
 *     result.onSuccess { position ->
 *         updateMapMarker(position.lat, position.lon)
 *     }
 * }
 * ```
 */
class TrackBusPositionUseCase @Inject constructor(
    private val busRepository: BusRepository
) {
    /**
     * Starts tracking a bus with periodic position updates.
     *
     * @param vehicleId Bus vehicle ID to track
     * @param lineId Bus line ID
     * @param direction Route direction ("inbound" or "outbound")
     * @return Flow of position updates every 30 seconds
     */
    operator fun invoke(
        vehicleId: String,
        lineId: String,
        direction: String = "inbound"
    ): Flow<Result<BusPosition>> = flow {
        // Validate inputs
        if (vehicleId.isBlank() || lineId.isBlank()) {
            emit(Result.Error("Vehicle ID and Line ID are required"))
            return@flow
        }

        // Load route sequence once (static data)
        val routeResult = busRepository.getRouteSequence(lineId, direction)
        if (routeResult is Result.Error) {
            emit(Result.Error("Failed to load route: ${routeResult.message}"))
            return@flow
        }

        val routeSequence = (routeResult as Result.Success).data

        // Poll for position updates
        while (true) {
            val positionResult = busRepository.getBusPosition(
                vehicleId = vehicleId,
                lineId = lineId,
                routeSequence = routeSequence
            )
            emit(positionResult)
            delay(POLLING_INTERVAL_MS)
        }
    }

    /**
     * Gets a single position update (non-streaming).
     */
    suspend fun getPosition(
        vehicleId: String,
        lineId: String,
        routeSequence: RouteSequence
    ): Result<BusPosition> {
        return busRepository.getBusPosition(vehicleId, lineId, routeSequence)
    }

    /**
     * Gets route sequence for map visualization.
     */
    suspend fun getRouteSequence(
        lineId: String,
        direction: String = "inbound"
    ): Result<RouteSequence> {
        return busRepository.getRouteSequence(lineId, direction)
    }

    companion object {
        const val POLLING_INTERVAL_MS = 30_000L
    }
}