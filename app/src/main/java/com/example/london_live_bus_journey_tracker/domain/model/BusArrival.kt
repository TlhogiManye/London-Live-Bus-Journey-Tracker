package com.example.london_live_bus_journey_tracker.domain.model

/**
 * Represents a live bus arrival prediction.
 *
 * @property vehicleId Unique bus identifier
 * @property lineId Bus line ID
 * @property lineName Display name of the line
 * @property destinationName Final destination
 * @property naptanId Stop point NaPTAN ID
 * @property stationName Name of the stop
 * @property timeToStationSeconds Seconds until arrival
 */
data class BusArrival(
    val vehicleId: String,
    val lineId: String,
    val lineName: String,
    val destinationName: String,
    val naptanId: String,
    val stationName: String,
    val timeToStationSeconds: Int
) {
    /** Time to arrival in minutes. */
    val timeToStationMinutes: Int
        get() = timeToStationSeconds / 60

    /** User-friendly display string (e.g., "2 Min" or "Due"). */
    val displayTime: String
        get() = when {
            timeToStationSeconds < 60 -> "Due"
            timeToStationMinutes == 1 -> "1 Min"
            else -> "$timeToStationMinutes Min"
        }
}