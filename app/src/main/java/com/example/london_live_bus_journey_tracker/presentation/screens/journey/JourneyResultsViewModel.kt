package com.example.london_live_bus_journey_tracker.presentation.screens.journey

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.london_live_bus_journey_tracker.domain.common.Result
import com.example.london_live_bus_journey_tracker.domain.usecase.GetJourneyOptionsUseCase
import com.example.london_live_bus_journey_tracker.domain.usecase.GetRouteSequenceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Journey Results screen.
 *
 * Fetches and displays available bus routes between two locations.
 * Also fetches route sequence to display the route on the map.
 */
@HiltViewModel
class JourneyResultsViewModel @Inject constructor(
    private val getJourneyOptionsUseCase: GetJourneyOptionsUseCase,
    private val getRouteSequenceUseCase: GetRouteSequenceUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(JourneyResultsUiState())
    val uiState: StateFlow<JourneyResultsUiState> = _uiState.asStateFlow()

    private val fromId: String = savedStateHandle.get<String>("fromId") ?: ""
    private val toId: String = savedStateHandle.get<String>("toId") ?: ""

    init {
        val fromName = savedStateHandle.get<String>("fromName") ?: ""
        val toName = savedStateHandle.get<String>("toName") ?: ""

        _uiState.update {
            it.copy(fromId = fromId, fromName = fromName, toId = toId, toName = toName)
        }

        loadJourneyResults()
    }

    private fun loadJourneyResults() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = getJourneyOptionsUseCase(fromId, toId)) {
                is Result.Success -> {
                    val options = result.data
                    val firstOption = options.firstOrNull()

                    Log.d("JourneyResults", "Got ${options.size} journey options")
                    Log.d("JourneyResults", "First option lineId: ${firstOption?.lineId}")

                    // Update UI with journey options first
                    _uiState.update {
                        it.copy(
                            journeyOptions = options,
                            isLoading = false,
                            errorMessage = null
                        )
                    }

                    // Now fetch route sequence to get coordinates for map
                    firstOption?.lineId?.let { lineId ->
                        fetchRouteSequenceForMap(lineId)
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            journeyOptions = emptyList(),
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    /**
     * Fetches route sequence to get stop coordinates for drawing the route on map.
     * This is needed because TfL Journey API often doesn't return route geometry.
     */
    private fun fetchRouteSequenceForMap(lineId: String) {
        viewModelScope.launch {
            Log.d("JourneyResults", "Fetching route sequence for lineId: $lineId")

            when (val result = getRouteSequenceUseCase(lineId)) {
                is Result.Success -> {
                    val routeSequence = result.data
                    val stops = routeSequence.stops

                    Log.d("JourneyResults", "Got ${stops.size} stops for route")

                    if (stops.isNotEmpty()) {
                        // Build route path from stop coordinates
                        val routePath = stops.map { stop ->
                            Pair(stop.lat, stop.lon)
                        }

                        // Get origin (first stop) and destination (last stop)
                        val firstStop = stops.first()
                        val lastStop = stops.last()

                        Log.d("JourneyResults", "Origin: ${firstStop.name} (${firstStop.lat}, ${firstStop.lon})")
                        Log.d("JourneyResults", "Destination: ${lastStop.name} (${lastStop.lat}, ${lastStop.lon})")

                        _uiState.update { state ->
                            state.copy(
                                originLat = firstStop.lat,
                                originLon = firstStop.lon,
                                destinationLat = lastStop.lat,
                                destinationLon = lastStop.lon,
                                routePath = routePath
                            )
                        }
                    }
                }
                is Result.Error -> {
                    Log.e("JourneyResults", "Failed to fetch route sequence: ${result.message}")
                    // Try fallback with known locations
                    applyFallbackCoordinates()
                }
            }
        }
    }

    /**
     * Applies fallback coordinates when route sequence fetch fails.
     */
    private fun applyFallbackCoordinates() {
        val originLat = parseCoordinateLat(fromId) ?: getKnownLocationLat(fromId)
        val originLon = parseCoordinateLon(fromId) ?: getKnownLocationLon(fromId)
        val destLat = parseCoordinateLat(toId) ?: getKnownLocationLat(toId)
        val destLon = parseCoordinateLon(toId) ?: getKnownLocationLon(toId)

        if (originLat != null && originLon != null && destLat != null && destLon != null) {
            val routePath = generateSimpleRoutePath(originLat, originLon, destLat, destLon)

            _uiState.update { state ->
                state.copy(
                    originLat = originLat,
                    originLon = originLon,
                    destinationLat = destLat,
                    destinationLon = destLon,
                    routePath = routePath
                )
            }
        }
    }

    /**
     * Parses latitude from a coordinate string like "51.4965,-0.1447".
     */
    private fun parseCoordinateLat(coordinateString: String): Double? {
        return try {
            if (coordinateString.contains(",")) {
                coordinateString.split(",").firstOrNull()?.toDoubleOrNull()
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Parses longitude from a coordinate string like "51.4965,-0.1447".
     */
    private fun parseCoordinateLon(coordinateString: String): Double? {
        return try {
            if (coordinateString.contains(",")) {
                coordinateString.split(",").getOrNull(1)?.toDoubleOrNull()
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Generates a simple route path with intermediate points between two locations.
     */
    private fun generateSimpleRoutePath(
        originLat: Double,
        originLon: Double,
        destLat: Double,
        destLon: Double
    ): List<Pair<Double, Double>> {
        val points = mutableListOf<Pair<Double, Double>>()
        val steps = 10

        for (i in 0..steps) {
            val fraction = i.toDouble() / steps
            val lat = originLat + (destLat - originLat) * fraction
            val lon = originLon + (destLon - originLon) * fraction
            points.add(Pair(lat, lon))
        }

        return points
    }

    /**
     * Returns known latitude for common London stations by ICS code.
     */
    private fun getKnownLocationLat(icsCode: String): Double? {
        return KNOWN_LOCATIONS[icsCode]?.first
    }

    /**
     * Returns known longitude for common London stations by ICS code.
     */
    private fun getKnownLocationLon(icsCode: String): Double? {
        return KNOWN_LOCATIONS[icsCode]?.second
    }

    companion object {
        private val KNOWN_LOCATIONS = mapOf(
            "1000248" to Pair(51.4965, -0.1447),  // Victoria Station
            "1000173" to Pair(51.5152, -0.1418),  // Oxford Circus
            "1000174" to Pair(51.5154, -0.1755),  // Paddington Station
            "1000138" to Pair(51.5178, -0.0823),  // Liverpool Street
            "1000077" to Pair(51.5282, -0.1337),  // Euston Station
            "1000129" to Pair(51.5308, -0.1238),  // King's Cross
            "1000254" to Pair(51.5035, -0.1132),  // Waterloo Station
            "1000235" to Pair(51.5081, -0.1280),  // Trafalgar Square
            "1000179" to Pair(51.5100, -0.1347),  // Piccadilly Circus
            "1000134" to Pair(51.5112, -0.1281),  // Leicester Square
            "1000013" to Pair(51.5013, -0.1416),  // Westminster
            "1000023" to Pair(51.5226, -0.1571)   // Baker Street
        )
    }

    fun retry() {
        loadJourneyResults()
    }
}