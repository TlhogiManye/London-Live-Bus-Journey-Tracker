package com.example.london_live_bus_journey_tracker.presentation.screens.buslist

import BusListUiState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.london_live_bus_journey_tracker.domain.model.BusArrivalItem
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
class BusListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(BusListUiState())
    val uiState: StateFlow<BusListUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null

    init {
        val lineId = savedStateHandle.get<String>("lineId") ?: ""
        val lineName = savedStateHandle.get<String>("lineName") ?: ""
        val fromName = savedStateHandle.get<String>("fromName") ?: ""
        val toName = savedStateHandle.get<String>("toName") ?: ""

        _uiState.update { state ->
            state.copy(
                lineId = lineId,
                lineName = lineName,
                fromName = fromName,
                toName = toName
            )
        }

        startPolling()
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                loadBusArrivals()
                delay(POLLING_INTERVAL_MS)
            }
        }
    }

    private suspend fun loadBusArrivals() {
        val currentState = _uiState.value
        val isInitialLoad = currentState.buses.isEmpty() && currentState.isLoading

        if (!isInitialLoad) {
            _uiState.update { it.copy(isRefreshing = true) }
        }

        try {
            delay(500)

            // Mock data matching Figma design
            val mockBuses = listOf(
                BusArrivalItem(
                    vehicleId = "123",
                    stationName = "Stop A",
                    naptanId = "490000123A",
                    timeToStationMinutes = 2,
                    destinationName = "Euston Station"
                ),
                BusArrivalItem(
                    vehicleId = "456",
                    stationName = "Stop B",
                    naptanId = "490000456B",
                    timeToStationMinutes = 5,
                    destinationName = "Euston Station"
                ),
                BusArrivalItem(
                    vehicleId = "789",
                    stationName = "Stop C",
                    naptanId = "490000789C",
                    timeToStationMinutes = 8,
                    destinationName = "Euston Station"
                )
            )

            _uiState.update { state ->
                state.copy(
                    buses = mockBuses,
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = null
                )
            }
        } catch (e: Exception) {
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = "Failed to load bus arrivals"
                )
            }
        }
    }

    fun retry() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            loadBusArrivals()
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }

    companion object {
        private const val POLLING_INTERVAL_MS = 30_000L
    }
}