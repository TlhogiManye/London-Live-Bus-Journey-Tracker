package com.example.london_live_bus_journey_tracker.presentation.screens.buslist

import com.example.london_live_bus_journey_tracker.domain.model.BusArrival

/**
 * UI state for the Bus List screen.
 */
data class BusListUiState(
    val lineId: String = "",
    val lineName: String = "",
    val fromName: String = "",
    val toName: String = "",
    val buses: List<BusArrival> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)