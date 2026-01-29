package com.example.london_live_bus_journey_tracker.domain.model

data class TrackingBusPosition(
    val lat: Double,
    val lon: Double,
    val naptanId: String,
    val stopName: String
)
