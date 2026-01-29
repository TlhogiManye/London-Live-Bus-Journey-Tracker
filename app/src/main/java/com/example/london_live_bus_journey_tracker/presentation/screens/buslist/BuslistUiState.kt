import com.example.london_live_bus_journey_tracker.domain.model.BusArrivalItem

data class BusListUiState(
    val lineId: String = "",
    val lineName: String = "",
    val fromName: String = "",
    val toName: String = "",
    val buses: List<BusArrivalItem> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)
