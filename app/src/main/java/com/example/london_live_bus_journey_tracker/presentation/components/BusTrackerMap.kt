package com.example.london_live_bus_journey_tracker.presentation.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.london_live_bus_journey_tracker.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * Data class representing a stop marker on the map.
 */
data class StopMarker(
    val id: String,
    val name: String,
    val position: LatLng,
    val isCurrentStop: Boolean = false,
    val isOrigin: Boolean = false,
    val isDestination: Boolean = false
)

/**
 * Data class representing a bus marker on the map.
 */
data class BusMarker(
    val vehicleId: String,
    val position: LatLng,
    val title: String
)

/**
 * Default London center coordinates.
 */
val LONDON_CENTER = LatLng(51.5074, -0.1278)

/**
 * Route polyline colors matching Figma designs.
 */
object RouteColors {
    val Blue = Color(0xFF2196F3)
    val Gray = Color(0xFF9E9E9E)
}

/** Center anchor offset for markers */
private val CENTER_ANCHOR = Offset(0.5f, 0.5f)

/**
 * Container for all map marker icons.
 */
data class MapMarkerIcons(
    val start: BitmapDescriptor?,
    val end: BitmapDescriptor?,
    val busStop: BitmapDescriptor?,
    val busVehicle: BitmapDescriptor?,
    val currentStop: BitmapDescriptor?,
    val dot: BitmapDescriptor?
)

/**
 * Converts a vector drawable to a BitmapDescriptor for map markers.
 * Must only be called after GoogleMap is initialized (in onMapLoaded callback).
 */
private fun bitmapDescriptorFromVector(
    context: Context,
    @DrawableRes vectorResId: Int,
    width: Int,
    height: Int
): BitmapDescriptor? {
    return try {
        val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
        drawable.setBounds(0, 0, width, height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.draw(canvas)
        BitmapDescriptorFactory.fromBitmap(bitmap)
    } catch (e: Exception) {
        null
    }
}

/**
 * Creates all map marker icons. Must be called after map is loaded.
 */
private fun createMapMarkerIcons(context: Context): MapMarkerIcons {
    return MapMarkerIcons(
        start = bitmapDescriptorFromVector(context, R.drawable.ic_marker_start, 100, 100),
        end = bitmapDescriptorFromVector(context, R.drawable.ic_marker_end, 100, 100),
        busStop = bitmapDescriptorFromVector(context, R.drawable.ic_bus_marker, 50, 50),
        busVehicle = bitmapDescriptorFromVector(context, R.drawable.ic_bus, 100, 100),
        currentStop = bitmapDescriptorFromVector(context, R.drawable.ic_current_bus_stop, 100, 100),
        dot = bitmapDescriptorFromVector(context, R.drawable.ic_dot_icon, 10, 10)
    )
}

/**
 * Reusable Google Maps component for bus tracking.
 * Displays custom markers matching Figma designs.
 */
@Composable
fun BusTrackerMap(
    modifier: Modifier = Modifier,
    busMarkers: List<BusMarker> = emptyList(),
    stopMarkers: List<StopMarker> = emptyList(),
    routePath: List<LatLng> = emptyList(),
    routeColor: Color = RouteColors.Blue,
    originPosition: LatLng? = null,
    destinationPosition: LatLng? = null,
    originName: String = "From",
    destinationName: String = "To",
    onMapLoaded: () -> Unit = {}
) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LONDON_CENTER, 12f)
    }

    val mapProperties = remember {
        MapProperties(
            isMyLocationEnabled = false,
            maxZoomPreference = 18f,
            minZoomPreference = 10f
        )
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,
            zoomGesturesEnabled = true,
            scrollGesturesEnabled = true,
            tiltGesturesEnabled = false,
            rotationGesturesEnabled = false,
            compassEnabled = false,
            mapToolbarEnabled = false
        )
    }

    // Icons created lazily after map loads
    var icons by remember { mutableStateOf<MapMarkerIcons?>(null) }

    // Auto-fit camera to show all markers
    LaunchedEffect(busMarkers, stopMarkers, originPosition, destinationPosition, routePath) {
        val allPoints = mutableListOf<LatLng>()
        busMarkers.forEach { allPoints.add(it.position) }
        stopMarkers.forEach { allPoints.add(it.position) }
        originPosition?.let { allPoints.add(it) }
        destinationPosition?.let { allPoints.add(it) }
        if (routePath.isNotEmpty()) allPoints.addAll(routePath)

        if (allPoints.isNotEmpty()) {
            if (allPoints.size == 1) {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(allPoints.first(), 15f))
            } else {
                val boundsBuilder = LatLngBounds.Builder()
                allPoints.forEach { boundsBuilder.include(it) }
                cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 120))
            }
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = mapUiSettings,
        onMapLoaded = {
            // Create icons after map is ready
            if (icons == null) {
                icons = createMapMarkerIcons(context)
            }
            onMapLoaded()
        }
    ) {
        val currentIcons = icons ?: return@GoogleMap

        // Draw route polyline
        if (routePath.size >= 2) {
            Polyline(points = routePath, color = routeColor, width = 12f)
        }

        // Draw intermediate stop markers (small dots)
        stopMarkers.filter { !it.isOrigin && !it.isDestination && !it.isCurrentStop }.forEach { stop ->
            Marker(
                state = MarkerState(position = stop.position),
                title = stop.name,
                icon = currentIcons.dot,
                anchor = CENTER_ANCHOR
            )
        }

        // Draw current stop markers
        stopMarkers.filter { it.isCurrentStop && !it.isOrigin && !it.isDestination }.forEach { stop ->
            Marker(
                state = MarkerState(position = stop.position),
                title = stop.name,
                snippet = "Current Stop",
                icon = currentIcons.currentStop,
                anchor = CENTER_ANCHOR
            )
        }

        // Draw origin marker
        originPosition?.let { pos ->
            Marker(
                state = MarkerState(position = pos),
                title = originName,
                snippet = "From",
                icon = currentIcons.start,
                anchor = CENTER_ANCHOR
            )
        }

        // Draw destination marker
        destinationPosition?.let { pos ->
            Marker(
                state = MarkerState(position = pos),
                title = destinationName,
                snippet = "To",
                icon = currentIcons.end,
                anchor = CENTER_ANCHOR
            )
        }

        // Draw bus vehicle markers
        busMarkers.forEach { bus ->
            Marker(
                state = MarkerState(position = bus.position),
                title = bus.title,
                snippet = "Vehicle: ${bus.vehicleId}",
                icon = currentIcons.busVehicle,
                anchor = CENTER_ANCHOR,
                zIndex = 1f
            )
        }
    }
}

/**
 * Simple map showing London center with optional bus stop markers.
 * Used on Landing screen.
 */
@Composable
fun SimpleLocationMap(
    modifier: Modifier = Modifier,
    center: LatLng = LONDON_CENTER,
    zoom: Float = 14f,
    busStops: List<StopMarker> = emptyList()
) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center, zoom)
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            zoomGesturesEnabled = true,
            scrollGesturesEnabled = true,
            tiltGesturesEnabled = false,
            rotationGesturesEnabled = false,
            compassEnabled = false,
            mapToolbarEnabled = false
        )
    }

    // Icons created lazily after map loads
    var icons by remember { mutableStateOf<MapMarkerIcons?>(null) }

    // Fit camera to show all stops
    LaunchedEffect(busStops) {
        if (busStops.isNotEmpty()) {
            if (busStops.size == 1) {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(busStops.first().position, 15f))
            } else {
                val boundsBuilder = LatLngBounds.Builder()
                busStops.forEach { boundsBuilder.include(it.position) }
                cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100))
            }
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = mapUiSettings,
        onMapLoaded = {
            if (icons == null) {
                icons = createMapMarkerIcons(context)
            }
        }
    ) {
        val currentIcons = icons ?: return@GoogleMap

        busStops.forEach { stop ->
            Marker(
                state = MarkerState(position = stop.position),
                title = stop.name,
                icon = currentIcons.busStop,
                anchor = CENTER_ANCHOR
            )
        }
    }
}

/**
 * Map showing journey route with origin/destination markers.
 * Used on Journey Results screen per Figma design.
 */
@Composable
fun JourneyRouteMap(
    modifier: Modifier = Modifier,
    originPosition: LatLng? = null,
    destinationPosition: LatLng? = null,
    originName: String = "From",
    destinationName: String = "To",
    routePath: List<LatLng> = emptyList(),
    intermediateStops: List<LatLng> = emptyList()
) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LONDON_CENTER, 12f)
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            zoomGesturesEnabled = true,
            scrollGesturesEnabled = true,
            tiltGesturesEnabled = false,
            rotationGesturesEnabled = false,
            compassEnabled = false,
            mapToolbarEnabled = false
        )
    }

    // Icons created lazily after map loads
    var icons by remember { mutableStateOf<MapMarkerIcons?>(null) }

    // Fit camera to show all points
    LaunchedEffect(originPosition, destinationPosition, routePath) {
        val allPoints = mutableListOf<LatLng>()
        originPosition?.let { allPoints.add(it) }
        destinationPosition?.let { allPoints.add(it) }
        if (routePath.isNotEmpty()) allPoints.addAll(routePath)

        if (allPoints.size >= 2) {
            val boundsBuilder = LatLngBounds.Builder()
            allPoints.forEach { boundsBuilder.include(it) }
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 120))
        } else if (allPoints.size == 1) {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(allPoints.first(), 14f))
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = mapUiSettings,
        onMapLoaded = {
            if (icons == null) {
                icons = createMapMarkerIcons(context)
            }
        }
    ) {
        val currentIcons = icons ?: return@GoogleMap

        // Draw blue route polyline
        if (routePath.size >= 2) {
            Polyline(points = routePath, color = RouteColors.Blue, width = 12f)
        }

        // Draw intermediate stops as bus stop markers
        intermediateStops.forEach { pos ->
            Marker(state = MarkerState(position = pos), icon = currentIcons.busStop, anchor = CENTER_ANCHOR)
        }

        // Draw origin marker
        originPosition?.let {
            Marker(
                state = MarkerState(position = it),
                title = originName,
                snippet = "From",
                icon = currentIcons.start,
                anchor = CENTER_ANCHOR
            )
        }

        // Draw destination marker
        destinationPosition?.let {
            Marker(
                state = MarkerState(position = it),
                title = destinationName,
                snippet = "To",
                icon = currentIcons.end,
                anchor = CENTER_ANCHOR
            )
        }
    }
}

/**
 * Map for Bus List screen showing route with bus positions.
 * Uses gray polyline per Figma design.
 */
@Composable
fun BusListMap(
    modifier: Modifier = Modifier,
    busMarkers: List<BusMarker> = emptyList(),
    originPosition: LatLng? = null,
    destinationPosition: LatLng? = null,
    originName: String = "From",
    destinationName: String = "To",
    routePath: List<LatLng> = emptyList(),
    intermediateStops: List<LatLng> = emptyList()
) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LONDON_CENTER, 12f)
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            zoomGesturesEnabled = true,
            scrollGesturesEnabled = true,
            tiltGesturesEnabled = false,
            rotationGesturesEnabled = false,
            compassEnabled = false,
            mapToolbarEnabled = false
        )
    }

    // Icons created lazily after map loads
    var icons by remember { mutableStateOf<MapMarkerIcons?>(null) }

    // Fit camera to show all points
    LaunchedEffect(busMarkers, originPosition, destinationPosition, routePath) {
        val allPoints = mutableListOf<LatLng>()
        busMarkers.forEach { allPoints.add(it.position) }
        originPosition?.let { allPoints.add(it) }
        destinationPosition?.let { allPoints.add(it) }
        if (routePath.isNotEmpty()) allPoints.addAll(routePath)

        if (allPoints.size >= 2) {
            val boundsBuilder = LatLngBounds.Builder()
            allPoints.forEach { boundsBuilder.include(it) }
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 120))
        } else if (allPoints.size == 1) {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(allPoints.first(), 14f))
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = mapUiSettings,
        onMapLoaded = {
            if (icons == null) {
                icons = createMapMarkerIcons(context)
            }
        }
    ) {
        val currentIcons = icons ?: return@GoogleMap

        // Draw gray route polyline
        if (routePath.size >= 2) {
            Polyline(points = routePath, color = RouteColors.Gray, width = 12f)
        }

        // Draw intermediate stops as bus stop markers
        intermediateStops.forEach { pos ->
            Marker(state = MarkerState(position = pos), icon = currentIcons.busStop, anchor = CENTER_ANCHOR)
        }

        // Draw origin marker
        originPosition?.let {
            Marker(
                state = MarkerState(position = it),
                title = originName,
                snippet = "From",
                icon = currentIcons.start,
                anchor = CENTER_ANCHOR
            )
        }

        // Draw destination marker
        destinationPosition?.let {
            Marker(
                state = MarkerState(position = it),
                title = destinationName,
                snippet = "To",
                icon = currentIcons.end,
                anchor = CENTER_ANCHOR
            )
        }

        // Draw bus markers
        busMarkers.forEach { bus ->
            Marker(
                state = MarkerState(position = bus.position),
                title = bus.title,
                snippet = "Vehicle: ${bus.vehicleId}",
                icon = currentIcons.busVehicle,
                anchor = CENTER_ANCHOR,
                zIndex = 1f
            )
        }
    }
}

/**
 * Map for Tracking screen showing active trip.
 * Uses blue polyline with current stop marker.
 */
@Composable
fun TrackingMap(
    modifier: Modifier = Modifier,
    busPosition: LatLng? = null,
    busTitle: String = "Bus",
    vehicleId: String = "",
    currentStopPosition: LatLng? = null,
    currentStopName: String = "",
    destinationPosition: LatLng? = null,
    destinationName: String = "To",
    routePath: List<LatLng> = emptyList(),
    intermediateStops: List<LatLng> = emptyList()
) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LONDON_CENTER, 12f)
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            zoomGesturesEnabled = true,
            scrollGesturesEnabled = true,
            tiltGesturesEnabled = false,
            rotationGesturesEnabled = false,
            compassEnabled = false,
            mapToolbarEnabled = false
        )
    }

    // Icons created lazily after map loads
    var icons by remember { mutableStateOf<MapMarkerIcons?>(null) }

    // Fit camera to show all points
    LaunchedEffect(busPosition, currentStopPosition, destinationPosition, routePath) {
        val allPoints = mutableListOf<LatLng>()
        busPosition?.let { allPoints.add(it) }
        currentStopPosition?.let { allPoints.add(it) }
        destinationPosition?.let { allPoints.add(it) }
        if (routePath.isNotEmpty()) allPoints.addAll(routePath)

        if (allPoints.size >= 2) {
            val boundsBuilder = LatLngBounds.Builder()
            allPoints.forEach { boundsBuilder.include(it) }
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 120))
        } else if (allPoints.size == 1) {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(allPoints.first(), 14f))
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = mapUiSettings,
        onMapLoaded = {
            if (icons == null) {
                icons = createMapMarkerIcons(context)
            }
        }
    ) {
        val currentIcons = icons ?: return@GoogleMap

        // Draw blue route polyline
        if (routePath.size >= 2) {
            Polyline(points = routePath, color = RouteColors.Blue, width = 12f)
        }

        // Draw intermediate stops as bus stop markers
        intermediateStops.forEach { pos ->
            Marker(state = MarkerState(position = pos), icon = currentIcons.busStop, anchor = CENTER_ANCHOR)
        }

        // Draw current stop marker
        currentStopPosition?.let {
            Marker(
                state = MarkerState(position = it),
                title = currentStopName,
                snippet = "Current Stop",
                icon = currentIcons.currentStop,
                anchor = CENTER_ANCHOR
            )
        }

        // Draw destination marker
        destinationPosition?.let {
            Marker(
                state = MarkerState(position = it),
                title = destinationName,
                snippet = "To",
                icon = currentIcons.end,
                anchor = CENTER_ANCHOR
            )
        }

        // Draw bus position marker
        busPosition?.let {
            Marker(
                state = MarkerState(position = it),
                title = busTitle,
                snippet = "Vehicle: $vehicleId",
                icon = currentIcons.busVehicle,
                anchor = CENTER_ANCHOR,
                zIndex = 1f
            )
        }
    }
}