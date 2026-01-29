package com.example.london_live_bus_journey_tracker.domain.model

data class TrackingRouteStop(
    val naptanId: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val sequence: Int,
    val isCurrentStop: Boolean = false
)