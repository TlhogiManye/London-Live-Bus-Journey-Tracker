package com.example.london_live_bus_journey_tracker.presentation.screens.journey

import JourneyResultsUiState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.london_live_bus_journey_tracker.domain.model.JourneyOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class JourneyResultsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(JourneyResultsUiState())
    val uiState: StateFlow<JourneyResultsUiState> = _uiState.asStateFlow()

    init {
        val fromId = savedStateHandle.get<String>("fromId") ?: ""
        val fromName = savedStateHandle.get<String>("fromName") ?: ""
        val toId = savedStateHandle.get<String>("toId") ?: ""
        val toName = savedStateHandle.get<String>("toName") ?: ""

        _uiState.update { state ->
            state.copy(
                fromId = fromId,
                fromName = fromName,
                toId = toId,
                toName = toName
            )
        }

        loadJourneyResults()
    }

    private fun loadJourneyResults() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            delay(800)

            val mockOptions = listOf(
                JourneyOption(
                    lineId = "24",
                    lineName = "24",
                    routeNumber = "24",
                    viaDescription = "Bus via Pimlico",
                    durationMinutes = 25
                ),
                JourneyOption(
                    lineId = "38",
                    lineName = "38",
                    routeNumber = "38",
                    viaDescription = "Bus via Notting Hill",
                    durationMinutes = 30
                ),
                JourneyOption(
                    lineId = "38",
                    lineName = "38",
                    routeNumber = "38",
                    viaDescription = "Bus via Green Park",
                    durationMinutes = 28
                )
            )

            _uiState.update { state ->
                state.copy(
                    journeyOptions = mockOptions,
                    isLoading = false
                )
            }
        }
    }

    fun retry() {
        loadJourneyResults()
    }
}