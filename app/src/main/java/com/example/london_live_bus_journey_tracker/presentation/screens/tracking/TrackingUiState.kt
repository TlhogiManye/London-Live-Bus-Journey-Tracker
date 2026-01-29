package com.example.london_live_bus_journey_tracker.presentation.screens.tracking

import com.example.london_live_bus_journey_tracker.domain.model.BusPosition
import com.example.london_live_bus_journey_tracker.domain.model.RouteStop

data class TrackingUiState(
    val lineId: String = "",
    val lineName: String = "",
    val vehicleId: String = "",
    val destinationName: String = "",
    val busPosition: BusPosition? = null,
    val routeStops: List<RouteStop> = emptyList(),
    val nextStopName: String = "",
    val timeToNextStop: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)