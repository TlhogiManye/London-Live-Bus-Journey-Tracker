package com.example.london_live_bus_journey_tracker.presentation.screens.journey

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.london_live_bus_journey_tracker.domain.common.Result
import com.example.london_live_bus_journey_tracker.domain.usecase.GetJourneyOptionsUseCase
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
 */
@HiltViewModel
class JourneyResultsViewModel @Inject constructor(
    private val getJourneyOptionsUseCase: GetJourneyOptionsUseCase,
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
                    _uiState.update {
                        it.copy(journeyOptions = result.data, isLoading = false, errorMessage = null)
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(journeyOptions = emptyList(), isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun retry() {
        loadJourneyResults()
    }
}