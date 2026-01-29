package com.example.london_live_bus_journey_tracker.domain.model

data class BusArrivalItem(
    val vehicleId: String,
    val stationName: String,
    val naptanId: String,
    val timeToStationMinutes: Int,
    val destinationName: String
) {
    val displayTime: String
        get() = when {
            timeToStationMinutes <= 0 -> "Due"
            timeToStationMinutes == 1 -> "1 Min"
            else -> "$timeToStationMinutes Min"
        }
}
