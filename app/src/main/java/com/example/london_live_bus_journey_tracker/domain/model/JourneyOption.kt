package com.example.london_live_bus_journey_tracker.domain.model

data class JourneyOption(
    val lineId: String,
    val lineName: String,
    val routeNumber: String,
    val viaDescription: String,
    val durationMinutes: Int
)