package com.example.london_live_bus_journey_tracker.domain.model

data class Location(
    val id: String,
    val name: String,
    val type: LocationType,
    val lat: Double? = null,
    val lon: Double? = null,
    val modes: List<String> = emptyList()
)

enum class LocationType {
    STOP_POINT,
    STATION,
    ADDRESS,
    STREET,
    UNKNOWN
}