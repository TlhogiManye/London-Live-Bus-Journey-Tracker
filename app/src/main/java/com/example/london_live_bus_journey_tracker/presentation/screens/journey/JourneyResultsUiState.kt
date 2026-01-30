package com.example.london_live_bus_journey_tracker.presentation.screens.journey

import com.example.london_live_bus_journey_tracker.domain.model.JourneyOption

/**
 * UI state for the Journey Results screen.
 */
data class JourneyResultsUiState(
    val fromId: String = "",
    val fromName: String = "",
    val toId: String = "",
    val toName: String = "",
    val journeyOptions: List<JourneyOption> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    // Route data for map display (from first journey option or fallback)
    val originLat: Double? = null,
    val originLon: Double? = null,
    val destinationLat: Double? = null,
    val destinationLon: Double? = null,
    val routePath: List<Pair<Double, Double>> = emptyList()
)