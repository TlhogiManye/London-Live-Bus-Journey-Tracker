package com.example.london_live_bus_journey_tracker.domain.model

/**
 * Represents a bus journey option between two locations.
 *
 * @property lineId Bus line ID (e.g., "24")
 * @property lineName Display name of the line
 * @property routeNumber Route number displayed on the bus (e.g., "24")
 * @property viaDescription Route description (e.g., "Bus via Pimlico")
 * @property durationMinutes Estimated journey time
 * @property routePath List of coordinates for the route polyline
 * @property originLat Origin latitude
 * @property originLon Origin longitude
 * @property destinationLat Destination latitude
 * @property destinationLon Destination longitude
 * @property intermediateStops Intermediate stop coordinates
 */
data class JourneyOption(
    val lineId: String,
    val lineName: String,
    val routeNumber: String,
    val viaDescription: String,
    val durationMinutes: Int,
    val routePath: List<Pair<Double, Double>> = emptyList(),
    val originLat: Double? = null,
    val originLon: Double? = null,
    val destinationLat: Double? = null,
    val destinationLon: Double? = null,
    val intermediateStops: List<Pair<Double, Double>> = emptyList()
)