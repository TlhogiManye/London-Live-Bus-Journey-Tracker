package com.example.london_live_bus_journey_tracker.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from the TfL StopPoint Search API.
 *
 * Endpoint: GET /StopPoint/Search/{query}
 *
 * @property matches List of matching stop points
 * @property total Total number of results available
 */
@Serializable
data class StopPointSearchResponse(
    @SerialName("matches")
    val matches: List<StopPointMatchDto> = emptyList(),

    @SerialName("total")
    val total: Int = 0
)

/**
 * Individual stop point match from search results.
 *
 * @property id Unique identifier for the stop point (NaPTAN ID e.g., "940GZZLUVIC")
 * @property icsId ICS code for journey planner (e.g., "1000248") - preferred for Journey API
 * @property name Human-readable name (e.g., "Victoria Station")
 * @property lat Latitude coordinate
 * @property lon Longitude coordinate
 * @property modes Transport modes available (e.g., ["bus", "tube"])
 * @property zone Fare zone (if applicable)
 */
@Serializable
data class StopPointMatchDto(
    @SerialName("id")
    val id: String,

    @SerialName("icsId")
    val icsId: String? = null,

    @SerialName("name")
    val name: String,

    @SerialName("lat")
    val lat: Double? = null,

    @SerialName("lon")
    val lon: Double? = null,

    @SerialName("modes")
    val modes: List<String> = emptyList(),

    @SerialName("zone")
    val zone: String? = null
)