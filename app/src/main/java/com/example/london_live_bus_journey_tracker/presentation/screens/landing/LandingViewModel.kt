package com.example.london_live_bus_journey_tracker.presentation.screens.landing

import androidx.lifecycle.ViewModel
import com.example.london_live_bus_journey_tracker.domain.model.RecentSearch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * ViewModel for the Landing screen.
 *
 * Manages recent search history.
 * TODO: Inject GetRecentSearchesUseCase when local storage is implemented.
 */
@HiltViewModel
class LandingViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(LandingUiState())
    val uiState: StateFlow<LandingUiState> = _uiState.asStateFlow()

    init {
        loadRecentSearches()
    }

    private fun loadRecentSearches() {
        // TODO: Replace with Room/DataStore implementation
        // Using ICS codes for journey planning compatibility
        // Victoria Station: 1000248, Oxford Circus: 1000173
        // Paddington Station: 1000174, Liverpool Street: 1000138
        val mockSearches = listOf(
            RecentSearch(
                id = "1",
                fromId = "1000248",  // Victoria Station ICS code
                fromName = "Victoria Station",
                toId = "1000173",    // Oxford Circus ICS code
                toName = "Oxford Circus",
                routeNumber = "24",
                viaDescription = "Bus via Pimlico",
                durationMinutes = 25
            ),
            RecentSearch(
                id = "2",
                fromId = "1000174",  // Paddington Station ICS code
                fromName = "Paddington Station",
                toId = "1000138",    // Liverpool Street ICS code
                toName = "Liverpool Street",
                routeNumber = "38",
                viaDescription = "Bus via Notting Hill",
                durationMinutes = 30
            )
        )

        // Sample bus stops in central London for map display
        val sampleBusStops = listOf(
            LandingBusStop("490000252S", "Victoria Station", 51.4965, -0.1447),
            LandingBusStop("490000173R", "Oxford Circus", 51.5152, -0.1418),
            LandingBusStop("490000174A", "Paddington Station", 51.5154, -0.1755),
            LandingBusStop("490000138W", "Liverpool Street", 51.5178, -0.0823),
            LandingBusStop("490000077H", "Euston Station", 51.5282, -0.1337),
            LandingBusStop("490000129S", "King's Cross", 51.5308, -0.1238),
            LandingBusStop("490000254E", "Waterloo Station", 51.5035, -0.1132),
            LandingBusStop("490000235A", "Trafalgar Square", 51.5081, -0.1280),
            LandingBusStop("490000179Z", "Piccadilly Circus", 51.5100, -0.1347),
            LandingBusStop("490000134A", "Leicester Square", 51.5112, -0.1281)
        )

        _uiState.update {
            it.copy(
                recentSearches = mockSearches,
                busStops = sampleBusStops
            )
        }
    }

    fun addRecentSearch(search: RecentSearch) {
        _uiState.update { state ->
            val updated = listOf(search) + state.recentSearches.filter { it.id != search.id }
            state.copy(recentSearches = updated.take(MAX_RECENT_SEARCHES))
        }
    }

    fun clearRecentSearches() {
        _uiState.update { it.copy(recentSearches = emptyList()) }
    }

    companion object {
        private const val MAX_RECENT_SEARCHES = 10
    }
}