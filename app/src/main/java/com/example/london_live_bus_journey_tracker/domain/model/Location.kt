package com.example.london_live_bus_journey_tracker.domain.model

/**
 * Represents a geographic location in the London transport network.
 *
 * Used throughout the app for bus stops, stations, and search results.
 *
 * @property id Unique identifier for display/keys (e.g., NaPTAN ID "940GZZLUVIC")
 * @property journeyId ID to use for journey planning (ICS code preferred)
 * @property name Human-readable name (e.g., "Victoria Station")
 * @property address Optional address or area description
 * @property type The category of this location
 * @property lat Latitude coordinate
 * @property lon Longitude coordinate
 * @property modes Transport modes available (e.g., ["bus", "tube"])
 */
data class Location(
    val id: String,
    val journeyId: String = "",
    val name: String,
    val address: String = "",
    val type: LocationType = LocationType.UNKNOWN,
    val lat: Double? = null,
    val lon: Double? = null,
    val modes: List<String> = emptyList()
) {
    /** Returns true if this location has valid coordinates. */
    val hasCoordinates: Boolean
        get() = lat != null && lon != null

    /** Returns the best ID to use for journey planning. */
    val effectiveJourneyId: String
        get() = journeyId.ifBlank { id }
}

/**
 * Types of locations in the TfL network.
 */
enum class LocationType {
    STOP_POINT,
    STATION,
    ADDRESS,
    STREET,
    UNKNOWN
}