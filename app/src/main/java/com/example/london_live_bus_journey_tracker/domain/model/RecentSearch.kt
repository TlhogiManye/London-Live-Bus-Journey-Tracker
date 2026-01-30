package com.example.london_live_bus_journey_tracker.domain.model

/**
 * Represents a recent journey search for history tracking.
 *
 * @property id Unique identifier for this search entry
 * @property fromId Location ID for origin (ICS code, NaPTAN, or coordinates)
 * @property fromName Display name for origin
 * @property toId Location ID for destination
 * @property toName Display name for destination
 * @property routeNumber Bus route number
 * @property viaDescription Description of the route
 * @property durationMinutes Estimated journey time
 * @property timestamp When the search was performed
 */
data class RecentSearch(
    val id: String,
    val fromId: String,
    val fromName: String,
    val toId: String,
    val toName: String,
    val routeNumber: String,
    val viaDescription: String,
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis()
)