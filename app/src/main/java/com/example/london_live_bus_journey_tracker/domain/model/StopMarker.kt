import com.google.android.gms.maps.model.LatLng

/**
 * Data class representing a stop marker on the map.
 */
data class StopMarker(
    val id: String,
    val name: String,
    val position: LatLng,
    val isCurrentStop: Boolean = false
)
