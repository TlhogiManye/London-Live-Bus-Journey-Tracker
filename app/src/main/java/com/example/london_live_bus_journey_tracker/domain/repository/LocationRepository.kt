package com.example.london_live_bus_journey_tracker.domain.repository

import com.example.london_live_bus_journey_tracker.domain.common.Result
import com.example.london_live_bus_journey_tracker.domain.model.Location

/**
 * Repository interface for location search operations.
 *
 * Following the **Dependency Inversion Principle** (SOLID), the domain layer
 * depends on this abstraction rather than concrete implementations.
 *
 * ## Implementation Notes
 * Implementations should:
 * - Handle network errors gracefully
 * - Return [Result.Error] with user-friendly messages
 * - Filter results to bus-accessible locations
 */
interface LocationRepository {

    /**
     * Searches for locations matching the query.
     *
     * @param query Search term (minimum 2 characters recommended)
     * @return [Result.Success] with matching locations, or [Result.Error] on failure
     */
    suspend fun searchLocations(query: String): Result<List<Location>>
}