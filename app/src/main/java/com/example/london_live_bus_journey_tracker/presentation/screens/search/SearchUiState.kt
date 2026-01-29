package com.example.london_live_bus_journey_tracker.presentation.screens.search

import com.example.london_live_bus_journey_tracker.domain.model.Location

data class SearchUiState(
    val fromText: String = "",
    val toText: String = "",
    val fromLocation: Location? = null,
    val toLocation: Location? = null,
    val activeField: ActiveField = ActiveField.FROM,
    val suggestions: List<Location> = emptyList(),
    val recentSearches: List<RecentJourneySearch> = emptyList(),
    val isSearching: Boolean = false,
    val errorMessage: String? = null
)