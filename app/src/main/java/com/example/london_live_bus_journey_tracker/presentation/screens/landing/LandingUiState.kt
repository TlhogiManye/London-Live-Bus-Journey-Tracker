package com.example.london_live_bus_journey_tracker.presentation.screens.landing

import com.example.london_live_bus_journey_tracker.domain.model.RecentSearch

/**
 * UI state for the Landing screen.
 */
data class LandingUiState(
    val recentSearches: List<RecentSearch> = emptyList()
)
