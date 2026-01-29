package com.example.london_live_bus_journey_tracker.domain.repository

import com.example.london_live_bus_journey_tracker.domain.common.Result
import com.example.london_live_bus_journey_tracker.domain.model.BusArrival
import com.example.london_live_bus_journey_tracker.domain.model.BusPosition
import com.example.london_live_bus_journey_tracker.domain.model.RouteSequence

/**
 * Repository interface for bus arrival and tracking operations.
 *
 * Following the **Interface Segregation Principle** (SOLID), this interface
 * is focused specifically on bus-related concerns.
 */
interface BusRepository {

    /**
     * Gets live arrival predictions for a bus line.
     *
     * @param lineId Bus line ID (e.g., "24")
     * @return [Result.Success] with arrivals sorted by time, or [Result.Error] on failure
     */
    suspend fun getArrivalsForLine(lineId: String): Result<List<BusArrival>>

    /**
     * Gets the ordered stop sequence for a bus route.
     *
     * Essential for the Virtual GPS feature.
     *
     * @param lineId Bus line ID
     * @param direction "inbound" or "outbound"
     * @return [Result.Success] with route sequence, or [Result.Error] on failure
     */
    suspend fun getRouteSequence(
        lineId: String,
        direction: String = "inbound"
    ): Result<RouteSequence>

    /**
     * Infers bus position using Virtual GPS algorithm.
     *
     * 1. Fetches arrival prediction for the vehicle
     * 2. Matches approaching stop to route sequence
     * 3. Uses stop coordinates as bus position
     *
     * @param vehicleId Bus vehicle ID to track
     * @param lineId Bus line ID
     * @param routeSequence Pre-cached route sequence
     * @return [Result.Success] with inferred position, or [Result.Error] if unavailable
     */
    suspend fun getBusPosition(
        vehicleId: String,
        lineId: String,
        routeSequence: RouteSequence
    ): Result<BusPosition>
}