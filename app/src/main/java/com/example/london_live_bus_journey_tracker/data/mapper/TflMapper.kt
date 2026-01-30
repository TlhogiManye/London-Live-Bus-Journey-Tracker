package com.example.london_live_bus_journey_tracker.data.mapper

import com.example.london_live_bus_journey_tracker.data.remote.dto.ArrivalPredictionDto
import com.example.london_live_bus_journey_tracker.data.remote.dto.JourneyDto
import com.example.london_live_bus_journey_tracker.data.remote.dto.RouteSequenceResponse
import com.example.london_live_bus_journey_tracker.data.remote.dto.StopPointMatchDto
import com.example.london_live_bus_journey_tracker.domain.model.BusArrival
import com.example.london_live_bus_journey_tracker.domain.model.JourneyOption
import com.example.london_live_bus_journey_tracker.domain.model.Location
import com.example.london_live_bus_journey_tracker.domain.model.LocationType
import com.example.london_live_bus_journey_tracker.domain.model.RouteSequence
import com.example.london_live_bus_journey_tracker.domain.model.RouteStop
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mapper for converting TfL API DTOs to domain models.
 *
 * **Single Responsibility Principle**: Handles only data transformation.
 */
@Singleton
class TflMapper @Inject constructor() {

    // ========================================================================
    // Location Mapping
    // ========================================================================

    /**
     * Maps a stop point DTO to a Location domain model.
     *
     * Uses the original NaPTAN id for uniqueness (used as key in lists),
     * and icsId as the journey ID for the Journey Planner API.
     */
    fun mapToLocation(dto: StopPointMatchDto): Location {
        // Use icsId for journey planning, fall back to coordinates, then original id
        val journeyId = when {
            !dto.icsId.isNullOrBlank() -> dto.icsId
            dto.lat != null && dto.lon != null -> "${dto.lat},${dto.lon}"
            else -> dto.id
        }

        // Build address from available info
        val address = buildString {
            if (dto.modes.isNotEmpty()) {
                append(dto.modes.joinToString(", ") { it.replaceFirstChar { c -> c.uppercase() } })
            }
            if (!dto.zone.isNullOrBlank()) {
                if (isNotEmpty()) append(" • ")
                append("Zone ${dto.zone}")
            }
        }

        return Location(
            id = dto.id,  // Keep original NaPTAN ID for uniqueness
            journeyId = journeyId,  // ICS code for journey planning
            name = dto.name,
            address = address,
            type = LocationType.STOP_POINT,
            lat = dto.lat,
            lon = dto.lon,
            modes = dto.modes
        )
    }

    /**
     * Maps a list of stop point DTOs to Location domain models.
     */
    fun mapToLocations(dtos: List<StopPointMatchDto>): List<Location> {
        return dtos.map { mapToLocation(it) }
    }

    // ========================================================================
    // Journey Mapping
    // ========================================================================

    /**
     * Maps a journey DTO to a JourneyOption domain model.
     *
     * Extracts the primary bus leg from the journey including route path.
     */
    fun mapToJourneyOption(dto: JourneyDto): JourneyOption? {
        // Find the bus leg
        val busLeg = dto.legs.find { leg ->
            leg.mode?.id?.lowercase() == "bus"
        } ?: return null

        // Extract line information
        val routeOption = busLeg.routeOptions.firstOrNull() ?: return null
        val lineId = routeOption.lineIdentifier?.id
            ?: routeOption.id
            ?: return null

        val lineName = routeOption.lineIdentifier?.name
            ?: routeOption.name
            ?: lineId

        // Build via description
        val viaDescription = busLeg.instruction?.summary
            ?: "Bus $lineName"

        // Parse route path from all legs
        val routePath = mutableListOf<Pair<Double, Double>>()
        dto.legs.forEach { leg ->
            val lineString = leg.path?.lineString
            if (lineString != null) {
                routePath.addAll(parseLineString(lineString))
            }
        }

        // Get origin/destination from first and last legs
        val firstLeg = dto.legs.firstOrNull()
        val lastLeg = dto.legs.lastOrNull()

        val originLat = firstLeg?.departurePoint?.lat
        val originLon = firstLeg?.departurePoint?.lon
        val destinationLat = lastLeg?.arrivalPoint?.lat
        val destinationLon = lastLeg?.arrivalPoint?.lon

        return JourneyOption(
            lineId = lineId,
            lineName = lineName,
            routeNumber = lineName,
            viaDescription = viaDescription,
            durationMinutes = dto.duration ?: 0,
            routePath = routePath,
            originLat = originLat,
            originLon = originLon,
            destinationLat = destinationLat,
            destinationLon = destinationLon
        )
    }

    /**
     * Parses a lineString JSON into a list of coordinate pairs.
     * TfL API uses GeoJSON format: [[lon,lat],[lon,lat],...]
     * We return as Pair(lat, lon) for Google Maps compatibility.
     */
    private fun parseLineString(lineString: String): List<Pair<Double, Double>> {
        return try {
            val coordinates = mutableListOf<Pair<Double, Double>>()

            // Parse coordinate pairs - TfL format is [lon,lat] (GeoJSON standard)
            val regex = "\\[([\\d.-]+),\\s*([\\d.-]+)]".toRegex()
            regex.findAll(lineString).forEach { match ->
                val lon = match.groupValues[1].toDoubleOrNull()
                val lat = match.groupValues[2].toDoubleOrNull()
                if (lat != null && lon != null) {
                    // Return as (lat, lon) for Google Maps
                    coordinates.add(Pair(lat, lon))
                }
            }

            coordinates
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Maps journey DTOs to JourneyOption domain models.
     */
    fun mapToJourneyOptions(dtos: List<JourneyDto>): List<JourneyOption> {
        return dtos.mapNotNull { mapToJourneyOption(it) }
    }

    // ========================================================================
    // Bus Arrival Mapping
    // ========================================================================

    /**
     * Maps an arrival prediction DTO to a BusArrival domain model.
     */
    fun mapToBusArrival(dto: ArrivalPredictionDto): BusArrival? {
        return BusArrival(
            vehicleId = dto.vehicleId ?: return null,
            lineId = dto.lineId ?: return null,
            lineName = dto.lineName ?: dto.lineId ?: return null,
            destinationName = dto.destinationName ?: "Unknown",
            naptanId = dto.naptanId ?: return null,
            stationName = dto.stationName ?: "Unknown",
            timeToStationSeconds = dto.timeToStation ?: 0
        )
    }

    /**
     * Maps arrival prediction DTOs to BusArrival domain models.
     */
    fun mapToBusArrivals(dtos: List<ArrivalPredictionDto>): List<BusArrival> {
        return dtos.mapNotNull { mapToBusArrival(it) }
            .sortedBy { it.timeToStationSeconds }
    }

    // ========================================================================
    // Route Sequence Mapping
    // ========================================================================

    /**
     * Maps a route sequence response to a RouteSequence domain model.
     */
    fun mapToRouteSequence(dto: RouteSequenceResponse): RouteSequence? {
        val stops = dto.stopPointSequences
            .firstOrNull()
            ?.stopPoint
            ?.mapIndexedNotNull { index, stopDto ->
                RouteStop(
                    naptanId = stopDto.id ?: return@mapIndexedNotNull null,
                    name = stopDto.name ?: "Unknown",
                    lat = stopDto.lat ?: return@mapIndexedNotNull null,
                    lon = stopDto.lon ?: return@mapIndexedNotNull null,
                    sequence = index + 1
                )
            }
            ?: return null

        if (stops.isEmpty()) return null

        return RouteSequence(
            lineId = dto.lineId ?: return null,
            lineName = dto.lineName ?: dto.lineId ?: return null,
            direction = dto.direction ?: "inbound",
            stops = stops
        )
    }
}