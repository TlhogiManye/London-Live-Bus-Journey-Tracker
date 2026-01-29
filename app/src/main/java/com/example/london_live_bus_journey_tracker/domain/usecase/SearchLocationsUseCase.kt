package com.example.london_live_bus_journey_tracker.domain.usecase

import com.example.london_live_bus_journey_tracker.domain.common.Result
import com.example.london_live_bus_journey_tracker.domain.model.Location
import com.example.london_live_bus_journey_tracker.domain.repository.LocationRepository
import javax.inject.Inject

/**
 * Use case for searching locations in the TfL network.
 *
 * **Single Responsibility Principle**: This class handles only location search logic.
 *
 * ## Usage
 * ```kotlin
 * class SearchViewModel @Inject constructor(
 *     private val searchLocations: SearchLocationsUseCase
 * ) {
 *     fun search(query: String) = viewModelScope.launch {
 *         searchLocations(query).onSuccess { locations ->
 *             _uiState.value = UiState.Success(locations)
 *         }.onError { message ->
 *             _uiState.value = UiState.Error(message)
 *         }
 *     }
 * }
 * ```
 */
class SearchLocationsUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    /**
     * Searches for locations matching the query.
     *
     * @param query Search term (returns empty list if < 2 characters)
     * @return [Result] with matching locations
     */
    suspend operator fun invoke(query: String): Result<List<Location>> {
        if (query.length < MIN_QUERY_LENGTH) {
            return Result.Success(emptyList())
        }

        return locationRepository.searchLocations(query.trim())
    }

    companion object {
        /** Minimum query length to trigger a search. */
        const val MIN_QUERY_LENGTH = 2
    }
}