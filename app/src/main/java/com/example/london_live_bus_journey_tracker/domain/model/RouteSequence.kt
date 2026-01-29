package com.example.london_live_bus_journey_tracker.domain.model



/**
 * Ordered sequence of stops for a bus route.
 *
 * @property lineId Bus line ID
 * @property lineName Display name
 * @property direction Route direction ("inbound" or "outbound")
 * @property stops Ordered list of stops
 */
data class RouteSequence(
    val lineId: String,
    val lineName: String,
    val direction: String,
    val stops: List<RouteStop>
) {
    /** Finds a stop by its NaPTAN ID. */
    fun findStopById(naptanId: String): RouteStop? =
        stops.find { it.naptanId == naptanId }

    /** Gets the next stop after the given NaPTAN ID. */
    fun getNextStop(currentNaptanId: String): RouteStop? {
        val currentIndex = stops.indexOfFirst { it.naptanId == currentNaptanId }
        return if (currentIndex >= 0 && currentIndex < stops.lastIndex) {
            stops[currentIndex + 1]
        } else null
    }
}

