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
        _uiState.update { it.copy(recentSearches = mockSearches) }
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