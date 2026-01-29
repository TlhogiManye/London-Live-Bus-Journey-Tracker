package com.example.london_live_bus_journey_tracker.domain.repository

import com.example.london_live_bus_journey_tracker.domain.common.Result
import com.example.london_live_bus_journey_tracker.domain.model.JourneyOption

/**
 * Repository interface for journey planning operations.
 *
 * Following the **Interface Segregation Principle** (SOLID), this interface
 * focuses solely on journey planning concerns.
 */
interface JourneyRepository {

    /**
     * Plans a journey between two locations.
     *
     * @param fromId Origin location ID
     * @param toId Destination location ID
     * @return [Result.Success] with journey options, or [Result.Error] on failure
     */
    suspend fun planJourney(
        fromId: String,
        toId: String
    ): Result<List<JourneyOption>>
}