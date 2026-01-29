package com.example.london_live_bus_journey_tracker.domain.model

data class RecentSearch(
    val id: String,
    val fromName: String,
    val toName: String,
    val routeNumber: String,
    val viaDescription: String,
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis()
)