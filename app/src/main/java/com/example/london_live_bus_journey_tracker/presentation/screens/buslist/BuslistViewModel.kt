package com.example.london_live_bus_journey_tracker.presentation.screens.buslist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.london_live_bus_journey_tracker.domain.common.Result
import com.example.london_live_bus_journey_tracker.domain.usecase.GetBusArrivalsUseCase
import com.example.london_live_bus_journey_tracker.domain.usecase.TrackBusPositionUseCase
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

/**
 * ViewModel for the Bus List screen.
 *
 * Fetches and displays live bus arrivals with automatic polling.
 * Also loads route sequence for map visualization.
 */
@HiltViewModel
class BusListViewModel @Inject constructor(
    private val getBusArrivalsUseCase: GetBusArrivalsUseCase,
    private val trackBusPositionUseCase: TrackBusPositionUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(BusListUiState())
    val uiState: StateFlow<BusListUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null
    private val lineId: String = savedStateHandle.get<String>("lineId") ?: ""

    init {
        val lineName = savedStateHandle.get<String>("lineName") ?: ""
        val fromName = savedStateHandle.get<String>("fromName") ?: ""
        val toName = savedStateHandle.get<String>("toName") ?: ""

        _uiState.update {
            it.copy(lineId = lineId, lineName = lineName, fromName = fromName, toName = toName)
        }

        loadRouteSequence()
        startPolling()
    }

    private fun loadRouteSequence() {
        viewModelScope.launch {
            when (val result = trackBusPositionUseCase.getRouteSequence(lineId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(routeStops = result.data.stops) }
                }
                is Result.Error -> {
                    // Route sequence is optional for display - don't show error
                    // The map will just show a simpler view without the route
                }
            }
        }
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                loadBusArrivals()
                delay(GetBusArrivalsUseCase.POLLING_INTERVAL_MS)
            }
        }
    }

    private suspend fun loadBusArrivals() {
        val currentState = _uiState.value
        val isInitialLoad = currentState.buses.isEmpty() && currentState.isLoading

        if (!isInitialLoad) {
            _uiState.update { it.copy(isRefreshing = true) }
        }

        when (val result = getBusArrivalsUseCase(lineId)) {
            is Result.Success -> {
                _uiState.update {
                    it.copy(
                        buses = result.data,
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = null
                    )
                }
            }
            is Result.Error -> {
                _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, errorMessage = result.message)
                }
            }
        }
    }

    fun retry() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            loadRouteSequence()
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
}