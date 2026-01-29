package com.example.london_live_bus_journey_tracker.presentation.screens.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.london_live_bus_journey_tracker.domain.common.Result
import com.example.london_live_bus_journey_tracker.domain.model.Location
import com.example.london_live_bus_journey_tracker.domain.usecase.SearchLocationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Search screen.
 *
 * Manages location search with debouncing and suggestion selection.
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchLocationsUseCase: SearchLocationsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        val prefillFrom = savedStateHandle.get<String>("prefillFrom")
        val prefillTo = savedStateHandle.get<String>("prefillTo")

        _uiState.update { state ->
            state.copy(
                fromText = prefillFrom ?: "",
                toText = prefillTo ?: ""
            )
        }

        loadRecentSearches()
    }

    private fun loadRecentSearches() {
        // TODO: Replace with Room/DataStore implementation
        val recentSearches = listOf(
            RecentJourneySearch("Victoria", "Oxford St", "Victoria to Oxford St"),
            RecentJourneySearch("Paddington", "Liverpool St", "Paddington to Liverpool St"),
            RecentJourneySearch("Victoria", "Oxford Street", "Victoria to Oxford Street")
        )
        _uiState.update { it.copy(recentSearches = recentSearches) }
    }

    fun onFromTextChanged(text: String) {
        _uiState.update { it.copy(fromText = text, fromLocation = null, errorMessage = null) }
        searchLocations(text)
    }

    fun onToTextChanged(text: String) {
        _uiState.update { it.copy(toText = text, toLocation = null, errorMessage = null) }
        searchLocations(text)
    }

    fun onFromFocused() {
        _uiState.update { it.copy(activeField = ActiveField.FROM) }
        if (_uiState.value.fromText.isNotEmpty()) {
            searchLocations(_uiState.value.fromText)
        } else {
            _uiState.update { it.copy(suggestions = emptyList()) }
        }
    }

    fun onToFocused() {
        _uiState.update { it.copy(activeField = ActiveField.TO) }
        if (_uiState.value.toText.isNotEmpty()) {
            searchLocations(_uiState.value.toText)
        } else {
            _uiState.update { it.copy(suggestions = emptyList()) }
        }
    }

    fun onClearFrom() {
        _uiState.update { it.copy(fromText = "", fromLocation = null, suggestions = emptyList()) }
    }

    fun onClearTo() {
        _uiState.update { it.copy(toText = "", toLocation = null, suggestions = emptyList()) }
    }

    fun onSuggestionSelected(location: Location) {
        when (_uiState.value.activeField) {
            ActiveField.FROM -> {
                _uiState.update {
                    it.copy(
                        fromText = location.name,
                        fromLocation = location,
                        suggestions = emptyList(),
                        activeField = ActiveField.TO
                    )
                }
            }
            ActiveField.TO -> {
                _uiState.update {
                    it.copy(
                        toText = location.name,
                        toLocation = location,
                        suggestions = emptyList(),
                        activeField = ActiveField.NONE
                    )
                }
            }
            ActiveField.NONE -> { }
        }
    }

    fun onRecentSearchSelected(recentSearch: RecentJourneySearch) {
        _uiState.update { it.copy(fromText = recentSearch.fromName, toText = recentSearch.toName) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun searchLocations(query: String) {
        searchJob?.cancel()

        if (query.length < SearchLocationsUseCase.MIN_QUERY_LENGTH) {
            _uiState.update { it.copy(suggestions = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            delay(DEBOUNCE_DELAY_MS)

            when (val result = searchLocationsUseCase(query)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(suggestions = result.data, isSearching = false, errorMessage = null)
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(suggestions = emptyList(), isSearching = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    companion object {
        private const val DEBOUNCE_DELAY_MS = 300L
    }
}