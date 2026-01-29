package com.example.london_live_bus_journey_tracker.presentation.screens.tracking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.london_live_bus_journey_tracker.domain.model.BusPosition
import com.example.london_live_bus_journey_tracker.domain.model.RouteStop
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject





@HiltViewModel
class TrackingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null
    private val routeStopsCache = mutableListOf<RouteStop>()

    init {
        val lineId = savedStateHandle.get<String>("lineId") ?: ""
        val lineName = savedStateHandle.get<String>("lineName") ?: ""
        val vehicleId = savedStateHandle.get<String>("vehicleId") ?: ""
        val destinationName = savedStateHandle.get<String>("destinationName") ?: ""

        _uiState.update { state ->
            state.copy(
                lineId = lineId,
                lineName = lineName,
                vehicleId = vehicleId,
                destinationName = destinationName
            )
        }

        loadRouteSequence()
    }

    private fun loadRouteSequence() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                delay(500)

                // Mock route sequence data
                val mockStops = listOf(
                    RouteStop("490000001A", "Victoria Station", 51.4965, -0.1447, 1),
                    RouteStop("490000002B", "Pimlico", 51.4893, -0.1334, 2),
                    RouteStop("490000003C", "Westminster", 51.5014, -0.1248, 3),
                    RouteStop("490000004D", "Trafalgar Square", 51.5080, -0.1281, 4),
                    RouteStop("490000005E", "Charing Cross", 51.5074, -0.1278, 5),
                    RouteStop("490000006F", "Oxford Circus", 51.5152, -0.1418, 6),
                    RouteStop("490000007G", "Warren Street", 51.5247, -0.1384, 7),
                    RouteStop("490000008H", "Euston Station", 51.5282, -0.1337, 8)
                )

                routeStopsCache.clear()
                routeStopsCache.addAll(mockStops)

                _uiState.update { state ->
                    state.copy(
                        routeStops = mockStops,
                        isLoading = false
                    )
                }

                startPolling()

            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = "Failed to load route"
                    )
                }
            }
        }
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                fetchBusPosition()
                delay(POLLING_INTERVAL_MS)
            }
        }
    }

    private suspend fun fetchBusPosition() {
        try {
            delay(300)

            // Mock arrival data - simulating bus at Trafalgar Square
            val mockNaptanId = "490000004D"
            val mockTimeToStation = 5

            // Virtual GPS: Find the stop coordinates from our route sequence
            val matchedStop = routeStopsCache.find { it.naptanId == mockNaptanId }

            if (matchedStop != null) {
                val busPosition = BusPosition(
                    lat = matchedStop.lat,
                    lon = matchedStop.lon,
                    naptanId = matchedStop.naptanId,
                    stopName = matchedStop.name
                )

                // Find next stop in sequence
                val currentIndex = routeStopsCache.indexOfFirst { it.naptanId == mockNaptanId }
                val nextStop = if (currentIndex >= 0 && currentIndex < routeStopsCache.size - 1) {
                    routeStopsCache[currentIndex + 1]
                } else {
                    null
                }

                // Update route stops with current position marker
                val updatedStops = routeStopsCache.map { stop ->
                    stop.copy(isCurrentStop = stop.naptanId == mockNaptanId)
                }

                _uiState.update { state ->
                    state.copy(
                        busPosition = busPosition,
                        routeStops = updatedStops,
                        nextStopName = nextStop?.name ?: state.destinationName,
                        timeToNextStop = mockTimeToStation,
                        errorMessage = null
                    )
                }
            }

        } catch (e: Exception) {
            // Continue polling even if one request fails
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun retry() {
        loadRouteSequence()
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }

    companion object {
        private const val POLLING_INTERVAL_MS = 30_000L
    }
}