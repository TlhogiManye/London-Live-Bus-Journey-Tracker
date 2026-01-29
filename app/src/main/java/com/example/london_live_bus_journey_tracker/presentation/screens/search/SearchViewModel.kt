package com.example.london_live_bus_journey_tracker.presentation.screens.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.london_live_bus_journey_tracker.domain.model.Location
import com.example.london_live_bus_journey_tracker.domain.model.LocationType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject



data class RecentJourneySearch(
    val fromName: String,
    val toName: String,
    val displayText: String
)

enum class ActiveField {
    FROM, TO, NONE
}

@HiltViewModel
class SearchViewModel @Inject constructor(
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
        val recentSearches = listOf(
            RecentJourneySearch(
                fromName = "Victoria",
                toName = "Oxford St",
                displayText = "Victoria to Oxford St"
            ),
            RecentJourneySearch(
                fromName = "Paddington",
                toName = "Liverpool St",
                displayText = "Paddington to Liverpool St"
            ),
            RecentJourneySearch(
                fromName = "Victoria",
                toName = "Oxford Street",
                displayText = "Victoria to Oxford Street"
            )
        )

        _uiState.update { state ->
            state.copy(recentSearches = recentSearches)
        }
    }

    fun onFromTextChanged(text: String) {
        _uiState.update { state ->
            state.copy(
                fromText = text,
                fromLocation = null
            )
        }
        searchLocations(text)
    }

    fun onToTextChanged(text: String) {
        _uiState.update { state ->
            state.copy(
                toText = text,
                toLocation = null
            )
        }
        searchLocations(text)
    }

    fun onFromFocused() {
        _uiState.update { state ->
            state.copy(activeField = ActiveField.FROM)
        }
        if (_uiState.value.fromText.isNotEmpty()) {
            searchLocations(_uiState.value.fromText)
        } else {
            _uiState.update { it.copy(suggestions = emptyList()) }
        }
    }

    fun onToFocused() {
        _uiState.update { state ->
            state.copy(activeField = ActiveField.TO)
        }
        if (_uiState.value.toText.isNotEmpty()) {
            searchLocations(_uiState.value.toText)
        } else {
            _uiState.update { it.copy(suggestions = emptyList()) }
        }
    }

    fun onClearFrom() {
        _uiState.update { state ->
            state.copy(
                fromText = "",
                fromLocation = null,
                suggestions = emptyList()
            )
        }
    }

    fun onClearTo() {
        _uiState.update { state ->
            state.copy(
                toText = "",
                toLocation = null,
                suggestions = emptyList()
            )
        }
    }

    fun onSuggestionSelected(location: Location) {
        val currentState = _uiState.value

        when (currentState.activeField) {
            ActiveField.FROM -> {
                _uiState.update { state ->
                    state.copy(
                        fromText = location.name,
                        fromLocation = location,
                        suggestions = emptyList(),
                        activeField = ActiveField.TO
                    )
                }
            }
            ActiveField.TO -> {
                _uiState.update { state ->
                    state.copy(
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
        _uiState.update { state ->
            state.copy(
                fromText = recentSearch.fromName,
                toText = recentSearch.toName
            )
        }
    }

    private fun searchLocations(query: String) {
        searchJob?.cancel()

        if (query.length < 2) {
            _uiState.update { it.copy(suggestions = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }

            delay(300) // Debounce

            val mockResults = getMockLocations(query)

            _uiState.update { state ->
                state.copy(
                    suggestions = mockResults,
                    isSearching = false
                )
            }
        }
    }

    private fun getMockLocations(query: String): List<Location> {
        val allLocations = listOf(
            Location(
                id = "940GZZLUVIC",
                name = "Victoria Station",
                type = LocationType.STATION,
                lat = 51.4965,
                lon = -0.1447
            ),
            Location(
                id = "490000254W",
                name = "Victoria Bus Station",
                type = LocationType.STOP_POINT,
                lat = 51.4952,
                lon = -0.1443
            ),
            Location(
                id = "street_victoria",
                name = "Victoria Street",
                type = LocationType.STREET,
                lat = 51.4977,
                lon = -0.1391
            ),
            Location(
                id = "940GZZLUEUS",
                name = "Euston Station",
                type = LocationType.STATION,
                lat = 51.5282,
                lon = -0.1337
            ),
            Location(
                id = "940GZZLUOXC",
                name = "Oxford Circus",
                type = LocationType.STATION,
                lat = 51.5152,
                lon = -0.1418
            ),
            Location(
                id = "street_oxford",
                name = "Oxford Street",
                type = LocationType.STREET,
                lat = 51.5145,
                lon = -0.1445
            ),
            Location(
                id = "940GZZLUPAC",
                name = "Paddington Station",
                type = LocationType.STATION,
                lat = 51.5154,
                lon = -0.1755
            ),
            Location(
                id = "940GZZLULVT",
                name = "Liverpool Street Station",
                type = LocationType.STATION,
                lat = 51.5178,
                lon = -0.0823
            )
        )

        return allLocations.filter { location ->
            location.name.contains(query, ignoreCase = true)
        }
    }
}