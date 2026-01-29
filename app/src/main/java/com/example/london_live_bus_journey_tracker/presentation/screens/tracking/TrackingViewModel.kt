package com.example.london_live_bus_journey_tracker.presentation.screens.tracking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.london_live_bus_journey_tracker.domain.common.Result
import com.example.london_live_bus_journey_tracker.domain.model.BusPosition
import com.example.london_live_bus_journey_tracker.domain.model.TrackingBusPosition
import com.example.london_live_bus_journey_tracker.domain.model.TrackingRouteStop
import com.example.london_live_bus_journey_tracker.domain.usecase.TrackBusPositionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Tracking screen.
 *
 * Tracks a specific bus in real-time using Virtual GPS.
 */
@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val trackBusPositionUseCase: TrackBusPositionUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    private var trackingJob: Job? = null
    private val lineId: String = savedStateHandle.get<String>("lineId") ?: ""
    private val vehicleId: String = savedStateHandle.get<String>("vehicleId") ?: ""

    init {
        val lineName = savedStateHandle.get<String>("lineName") ?: ""
        val destinationName = savedStateHandle.get<String>("destinationName") ?: ""

        _uiState.update {
            it.copy(
                lineId = lineId,
                lineName = lineName,
                vehicleId = vehicleId,
                destinationName = destinationName
            )
        }

        loadRouteAndStartTracking()
    }

    private fun loadRouteAndStartTracking() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // First load the route sequence
            when (val routeResult = trackBusPositionUseCase.getRouteSequence(lineId)) {
                is Result.Success -> {
                    val routeStops = routeResult.data.stops.map { stop ->
                        TrackingRouteStop(
                            naptanId = stop.naptanId,
                            name = stop.name,
                            lat = stop.lat,
                            lon = stop.lon,
                            sequence = stop.sequence,
                            isCurrentStop = false
                        )
                    }

                    _uiState.update { it.copy(routeStops = routeStops, isLoading = false) }

                    // Start collecting position updates
                    startTracking()
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = routeResult.message)
                    }
                }
            }
        }
    }

    private fun startTracking() {
        trackingJob?.cancel()
        trackingJob = viewModelScope.launch {
            trackBusPositionUseCase(vehicleId = vehicleId, lineId = lineId)
                .collect { result ->
                    when (result) {
                        is Result.Success -> updateBusPosition(result.data)
                        is Result.Error -> {
                            // Keep last known position on individual failures
                        }
                    }
                }
        }
    }

    private fun updateBusPosition(position: BusPosition) {
        val currentStops = _uiState.value.routeStops

        val updatedStops = currentStops.map { stop ->
            stop.copy(isCurrentStop = stop.name == position.currentStopName)
        }

        _uiState.update { state ->
            state.copy(
                busPosition = TrackingBusPosition(
                    lat = position.lat,
                    lon = position.lon,
                    stopName = position.currentStopName
                ),
                routeStops = updatedStops,
                nextStopName = position.nextStopName ?: state.destinationName,
                timeToNextStop = position.timeToNextStopSeconds / 60,
                errorMessage = null
            )
        }
    }

    fun stopPolling() {
        trackingJob?.cancel()
        trackingJob = null
    }

    fun retry() {
        loadRouteAndStartTracking()
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}