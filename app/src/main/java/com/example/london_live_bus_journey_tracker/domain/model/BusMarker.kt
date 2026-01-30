import com.google.android.gms.maps.model.LatLng

/**
 * Data class representing a bus marker on the map.
 */
data class BusMarker(
    val vehicleId: String,
    val position: LatLng,
    val title: String
)