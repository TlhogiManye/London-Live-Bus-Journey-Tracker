package com.example.london_live_bus_journey_tracker.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Bus arrival prediction from the TfL API.
 *
 * Endpoint: GET /Line/{lineId}/Arrivals/{stopPointId}
 * Endpoint: GET /Line/{lineId}/Arrivals
 *
 * @property id Unique prediction ID
 * @property vehicleId Bus vehicle identifier
 * @property naptanId Stop point NaPTAN ID
 * @property stationName Name of the stop
 * @property lineId Bus line ID
 * @property lineName Bus line name
 * @property destinationName Final destination
 * @property timeToStation Seconds until arrival
 * @property expectedArrival ISO 8601 arrival time
 */
@Serializable
data class ArrivalPredictionDto(
    @SerialName("id")
    val id: String? = null,

    @SerialName("vehicleId")
    val vehicleId: String? = null,

    @SerialName("naptanId")
    val naptanId: String? = null,

    @SerialName("stationName")
    val stationName: String? = null,

    @SerialName("lineId")
    val lineId: String? = null,

    @SerialName("lineName")
    val lineName: String? = null,

    @SerialName("destinationName")
    val destinationName: String? = null,

    @SerialName("timeToStation")
    val timeToStation: Int? = null,

    @SerialName("expectedArrival")
    val expectedArrival: String? = null
)