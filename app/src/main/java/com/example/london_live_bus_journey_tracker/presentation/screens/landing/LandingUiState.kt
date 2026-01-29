import com.example.london_live_bus_journey_tracker.domain.model.RecentSearch

data class LandingUiState(
    val recentSearches: List<RecentSearch> = emptyList(),
    val isLoading: Boolean = false
)