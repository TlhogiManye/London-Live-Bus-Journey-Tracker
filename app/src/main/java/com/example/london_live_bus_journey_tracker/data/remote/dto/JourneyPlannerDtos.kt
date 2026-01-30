package com.example.london_live_bus_journey_tracker.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from the TfL Journey Planner API.
 *
 * Endpoint: GET /Journey/JourneyResults/{from}/to/{to}
 *
 * @property journeys List of possible journey options
 * @property fromLocationDisambiguation Options when origin is ambiguous
 * @property toLocationDisambiguation Options when destination is ambiguous
 */
@Serializable
data class JourneyPlannerResponse(
    @SerialName("journeys")
    val journeys: List<JourneyDto>? = null,

    @SerialName("fromLocationDisambiguation")
    val fromLocationDisambiguation: DisambiguationDto? = null,

    @SerialName("toLocationDisambiguation")
    val toLocationDisambiguation: DisambiguationDto? = null
)

/**
 * Disambiguation options when a location query matches multiple places.
 *
 * @property disambiguationOptions List of possible locations to choose from
 */
@Serializable
data class DisambiguationDto(
    @SerialName("disambiguationOptions")
    val disambiguationOptions: List<DisambiguationOptionDto>? = null
)

/**
 * Single disambiguation option representing a possible location match.
 *
 * @property place The place details
 */
@Serializable
data class DisambiguationOptionDto(
    @SerialName("place")
    val place: PlaceDto? = null
)

/**
 * Place information used in disambiguation.
 *
 * @property id Unique place identifier
 * @property name Human-readable name
 * @property lat Latitude
 * @property lon Longitude
 */
@Serializable
data class PlaceDto(
    @SerialName("id")
    val id: String? = null,

    @SerialName("commonName")
    val name: String? = null,

    @SerialName("lat")
    val lat: Double? = null,

    @SerialName("lon")
    val lon: Double? = null
)

/**
 * Journey option from the planner.
 *
 * @property legs Individual segments of the journey
 * @property duration Total duration in minutes
 */
@Serializable
data class JourneyDto(
    @SerialName("legs")
    val legs: List<LegDto> = emptyList(),

    @SerialName("duration")
    val duration: Int? = null
)

/**
 * Individual leg (segment) of a journey.
 *
 * @property instruction Navigation instruction
 * @property routeOptions Available route options for this leg
 * @property mode Transport mode for this leg
 * @property duration Duration of this leg in minutes
 * @property path Route path with coordinates
 * @property departurePoint Starting point of this leg
 * @property arrivalPoint Ending point of this leg
 */
@Serializable
data class LegDto(
    @SerialName("instruction")
    val instruction: InstructionDto? = null,

    @SerialName("routeOptions")
    val routeOptions: List<RouteOptionDto> = emptyList(),

    @SerialName("mode")
    val mode: ModeDto? = null,

    @SerialName("duration")
    val duration: Int? = null,

    @SerialName("path")
    val path: PathDto? = null,

    @SerialName("departurePoint")
    val departurePoint: LegPointDto? = null,

    @SerialName("arrivalPoint")
    val arrivalPoint: LegPointDto? = null
)

/**
 * Path data containing route coordinates.
 *
 * @property lineString JSON string of coordinates: "[[lon,lat],[lon,lat],...]"
 * @property stopPoints Intermediate stops along the path
 */
@Serializable
data class PathDto(
    @SerialName("lineString")
    val lineString: String? = null,

    @SerialName("stopPoints")
    val stopPoints: List<PathStopPointDto>? = null
)

/**
 * Stop point in the path.
 */
@Serializable
data class PathStopPointDto(
    @SerialName("id")
    val id: String? = null,

    @SerialName("name")
    val name: String? = null
)

/**
 * Departure or arrival point for a leg.
 */
@Serializable
data class LegPointDto(
    @SerialName("commonName")
    val commonName: String? = null,

    @SerialName("lat")
    val lat: Double? = null,

    @SerialName("lon")
    val lon: Double? = null,

    @SerialName("icsCode")
    val icsCode: String? = null,

    @SerialName("naptanId")
    val naptanId: String? = null
)

/**
 * Navigation instruction for a journey leg.
 *
 * @property summary Brief description (e.g., "Take bus 24 to Oxford Circus")
 * @property detailed Detailed description
 */
@Serializable
data class InstructionDto(
    @SerialName("summary")
    val summary: String? = null,

    @SerialName("detailed")
    val detailed: String? = null
)

/**
 * Route option for a journey leg.
 *
 * @property id Route identifier
 * @property name Route name/number (e.g., "24")
 * @property lineIdentifier Line details
 */
@Serializable
data class RouteOptionDto(
    @SerialName("id")
    val id: String? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("lineIdentifier")
    val lineIdentifier: LineIdentifierDto? = null
)

/**
 * Line identifier details.
 *
 * @property id Line ID (e.g., "24")
 * @property name Line name
 */
@Serializable
data class LineIdentifierDto(
    @SerialName("id")
    val id: String? = null,

    @SerialName("name")
    val name: String? = null
)

/**
 * Transport mode information.
 *
 * @property id Mode identifier (e.g., "bus", "tube")
 * @property name Human-readable mode name
 */
@Serializable
data class ModeDto(
    @SerialName("id")
    val id: String? = null,

    @SerialName("name")
    val name: String? = null
)