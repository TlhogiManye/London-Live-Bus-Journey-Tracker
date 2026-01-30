package com.example.london_live_bus_journey_tracker.presentation.screens.landing

import com.example.london_live_bus_journey_tracker.domain.model.RecentSearch

/**
 * Data class for bus stop display on landing map.
 */
data class LandingBusStop(
    val id: String,
    val name: String,
    val lat: Double,
    val lon: Double
)

/**
 * UI state for the Landing screen.
 */
data class LandingUiState(
    val recentSearches: List<RecentSearch> = emptyList(),
    val busStops: List<LandingBusStop> = emptyList()
)