package com.example.london_live_bus_journey_tracker.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from the TfL Route Sequence API.
 *
 * Endpoint: GET /Line/{lineId}/Route/Sequence/{direction}
 *
 * This endpoint is essential for the "Virtual GPS" feature, providing
 * the ordered list of stops with coordinates.
 *
 * @property lineId Line identifier
 * @property lineName Line name
 * @property direction Route direction (inbound/outbound)
 * @property stopPointSequences Ordered list of stops
 */
@Serializable
data class RouteSequenceResponse(
    @SerialName("lineId")
    val lineId: String? = null,

    @SerialName("lineName")
    val lineName: String? = null,

    @SerialName("direction")
    val direction: String? = null,

    @SerialName("stopPointSequences")
    val stopPointSequences: List<StopPointSequenceDto> = emptyList()
)

/**
 * Sequence of stops for a route direction.
 *
 * @property stopPoint Ordered list of stop points
 */
@Serializable
data class StopPointSequenceDto(
    @SerialName("stopPoint")
    val stopPoint: List<SequenceStopPointDto> = emptyList()
)

/**
 * Individual stop in a route sequence.
 *
 * Used by Virtual GPS to map arrival predictions to coordinates.
 *
 * @property id Stop point ID (NaPTAN ID)
 * @property name Stop name
 * @property lat Latitude coordinate
 * @property lon Longitude coordinate
 */
@Serializable
data class SequenceStopPointDto(
    @SerialName("id")
    val id: String? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("lat")
    val lat: Double? = null,

    @SerialName("lon")
    val lon: Double? = null
)