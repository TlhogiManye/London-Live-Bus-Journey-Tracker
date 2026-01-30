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

/** Bottom-center anchor for pin markers (tip of pin) */
private val BOTTOM_CENTER_ANCHOR = Offset(0.5f, 1.0f)

/**
 * Container for all map marker icons.
 *
 * Usage per screen:
 * - Landing: busStopLanding (orange bus stop marker)
 * - Journey Results: dot (gray), start (green), end (red)
 * - Bus List: dot (gray), start, end, busVehicle (yellow bus for live buses)
 * - Tracking: dot, currentStop (blue), end, busVehicle
 */
data class MapMarkerIcons(
    val start: BitmapDescriptor?,
    val end: BitmapDescriptor?,
    val busStopLanding: BitmapDescriptor?,  // Orange bus stop for Landing screen
    val dot: BitmapDescriptor?,              // Small gray dot for route stops
    val busVehicle: BitmapDescriptor?,       // Yellow bus for live bus positions
    val currentStop: BitmapDescriptor?       // Blue dot for current location
)

/**
 * Converts a vector drawable to a BitmapDescriptor for map markers.
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
 *
 * Icon mapping per Figma:
 * - start: Green pin for origin (ic_marker_start)
 * - end: Red pin for destination (ic_marker_end)
 * - busStopLanding: Orange bus stop marker for Landing screen (ic_bus_stop)
 * - dot: Small gray dot for route intermediate stops (ic_dot_icon)
 * - busVehicle: Yellow bus for actual live buses (ic_bus_marker)
 * - currentStop: Blue dot for current location (ic_current_bus_stop)
 */
private fun createMapMarkerIcons(context: Context): MapMarkerIcons {
    return MapMarkerIcons(
        start = bitmapDescriptorFromVector(context, R.drawable.ic_marker_start, 80, 100),
        end = bitmapDescriptorFromVector(context, R.drawable.ic_marker_end, 80, 100),
        busStopLanding = bitmapDescriptorFromVector(context, R.drawable.ic_bus_stop, 50, 50),
        dot = bitmapDescriptorFromVector(context, R.drawable.ic_dot_icon, 80, 80),
        busVehicle = bitmapDescriptorFromVector(context, R.drawable.ic_bus_marker, 80, 80),
        currentStop = bitmapDescriptorFromVector(context, R.drawable.ic_current_bus_stop, 50, 50)
    )
}

/**
 * Reusable Google Maps component for bus tracking.
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

    var icons by remember { mutableStateOf<MapMarkerIcons?>(null) }

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
                cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100))
            }
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = mapUiSettings,
        onMapLoaded = {
            if (icons == null) {
                icons = createMapMarkerIcons(context)
            }
            onMapLoaded()
        }
    ) {
        val currentIcons = icons ?: return@GoogleMap

        // Draw route polyline
        if (routePath.size >= 2) {
            Polyline(points = routePath, color = routeColor, width = 10f)
        }

        // Draw intermediate stop markers (small gray dots)
        stopMarkers.filter { !it.isOrigin && !it.isDestination && !it.isCurrentStop }.forEach { stop ->
            Marker(
                state = MarkerState(position = stop.position),
                title = stop.name,
                icon = currentIcons.dot,
                anchor = CENTER_ANCHOR
            )
        }

        // Draw current stop markers (blue dot)
        stopMarkers.filter { it.isCurrentStop && !it.isOrigin && !it.isDestination }.forEach { stop ->
            Marker(
                state = MarkerState(position = stop.position),
                title = stop.name,
                snippet = "Current Stop",
                icon = currentIcons.currentStop,
                anchor = CENTER_ANCHOR
            )
        }

        // Draw origin marker (green pin)
        originPosition?.let { pos ->
            Marker(
                state = MarkerState(position = pos),
                title = originName,
                snippet = "From",
                icon = currentIcons.start,
                anchor = BOTTOM_CENTER_ANCHOR
            )
        }

        // Draw destination marker (red pin)
        destinationPosition?.let { pos ->
            Marker(
                state = MarkerState(position = pos),
                title = destinationName,
                snippet = "To",
                icon = currentIcons.end,
                anchor = BOTTOM_CENTER_ANCHOR
            )
        }

        // Draw bus vehicle markers (yellow bus)
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
            compassEnabled = true,
            mapToolbarEnabled = false
        )
    }

    var icons by remember { mutableStateOf<MapMarkerIcons?>(null) }

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

        // Use orange bus stop markers for Landing screen
        busStops.forEach { stop ->
            Marker(
                state = MarkerState(position = stop.position),
                title = stop.name,
                icon = currentIcons.busStopLanding,  // Orange bus stop icon
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

    var icons by remember { mutableStateOf<MapMarkerIcons?>(null) }

    LaunchedEffect(originPosition, destinationPosition, routePath) {
        val allPoints = mutableListOf<LatLng>()
        originPosition?.let { allPoints.add(it) }
        destinationPosition?.let { allPoints.add(it) }
        if (routePath.isNotEmpty()) allPoints.addAll(routePath)

        if (allPoints.size >= 2) {
            val boundsBuilder = LatLngBounds.Builder()
            allPoints.forEach { boundsBuilder.include(it) }
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100))
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
            Polyline(points = routePath, color = RouteColors.Blue, width = 10f)
        }

        // Draw intermediate stops as small gray dots
        intermediateStops.forEach { pos ->
            Marker(
                state = MarkerState(position = pos),
                icon = currentIcons.dot,  // Small gray dot for route stops
                anchor = CENTER_ANCHOR
            )
        }

        // Draw origin marker (green pin)
        originPosition?.let {
            Marker(
                state = MarkerState(position = it),
                title = originName,
                snippet = "From",
                icon = currentIcons.start,
                anchor = BOTTOM_CENTER_ANCHOR
            )
        }

        // Draw destination marker (red pin)
        destinationPosition?.let {
            Marker(
                state = MarkerState(position = it),
                title = destinationName,
                snippet = "To",
                icon = currentIcons.end,
                anchor = BOTTOM_CENTER_ANCHOR
            )
        }
    }
}

/**
 * Map for Bus List screen showing route with bus positions.
 * Uses gray polyline per Figma design.
 *
 * Icon usage:
 * - dot: Small gray dots for intermediate route stops
 * - busVehicle: Yellow bus markers for live bus positions
 * - start/end: Green/red pins for origin/destination
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

    var icons by remember { mutableStateOf<MapMarkerIcons?>(null) }

    LaunchedEffect(busMarkers, originPosition, destinationPosition, routePath) {
        val allPoints = mutableListOf<LatLng>()
        busMarkers.forEach { allPoints.add(it.position) }
        originPosition?.let { allPoints.add(it) }
        destinationPosition?.let { allPoints.add(it) }
        if (routePath.isNotEmpty()) allPoints.addAll(routePath)

        if (allPoints.size >= 2) {
            val boundsBuilder = LatLngBounds.Builder()
            allPoints.forEach { boundsBuilder.include(it) }
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100))
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
            Polyline(points = routePath, color = RouteColors.Gray, width = 10f)
        }

        // Draw intermediate stops as SMALL GRAY DOTS
        intermediateStops.forEach { pos ->
            Marker(
                state = MarkerState(position = pos),
                icon = currentIcons.dot,  // Small gray dot for route stops
                anchor = CENTER_ANCHOR
            )
        }

        // Draw origin marker (green pin)
        originPosition?.let {
            Marker(
                state = MarkerState(position = it),
                title = originName,
                snippet = "From",
                icon = currentIcons.start,
                anchor = BOTTOM_CENTER_ANCHOR
            )
        }

        // Draw destination marker (red pin)
        destinationPosition?.let {
            Marker(
                state = MarkerState(position = it),
                title = destinationName,
                snippet = "To",
                icon = currentIcons.end,
                anchor = BOTTOM_CENTER_ANCHOR
            )
        }

        // Draw actual bus markers (YELLOW BUS ICON - only for real live buses!)
        busMarkers.forEach { bus ->
            Marker(
                state = MarkerState(position = bus.position),
                title = bus.title,
                snippet = "Vehicle: ${bus.vehicleId}",
                icon = currentIcons.busVehicle,  // Yellow bus for live buses
                anchor = CENTER_ANCHOR,
                zIndex = 1f
            )
        }
    }
}

/**
 * Map for Tracking screen showing active trip.
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

    var icons by remember { mutableStateOf<MapMarkerIcons?>(null) }

    LaunchedEffect(busPosition, currentStopPosition, destinationPosition, routePath) {
        val allPoints = mutableListOf<LatLng>()
        busPosition?.let { allPoints.add(it) }
        currentStopPosition?.let { allPoints.add(it) }
        destinationPosition?.let { allPoints.add(it) }
        if (routePath.isNotEmpty()) allPoints.addAll(routePath)

        if (allPoints.size >= 2) {
            val boundsBuilder = LatLngBounds.Builder()
            allPoints.forEach { boundsBuilder.include(it) }
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100))
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
            Polyline(points = routePath, color = RouteColors.Blue, width = 10f)
        }

        // Draw intermediate stops as small gray dots
        intermediateStops.forEach { pos ->
            Marker(
                state = MarkerState(position = pos),
                icon = currentIcons.dot,  // Small gray dot for route stops
                anchor = CENTER_ANCHOR
            )
        }

        // Draw current stop marker (blue dot)
        currentStopPosition?.let {
            Marker(
                state = MarkerState(position = it),
                title = currentStopName,
                snippet = "Current Stop",
                icon = currentIcons.currentStop,  // Blue dot for current location
                anchor = CENTER_ANCHOR
            )
        }

        // Draw destination marker (red pin)
        destinationPosition?.let {
            Marker(
                state = MarkerState(position = it),
                title = destinationName,
                snippet = "To",
                icon = currentIcons.end,
                anchor = BOTTOM_CENTER_ANCHOR
            )
        }

        // Draw bus position marker (yellow bus)
        busPosition?.let {
            Marker(
                state = MarkerState(position = it),
                title = busTitle,
                snippet = "Vehicle: $vehicleId",
                icon = currentIcons.busVehicle,  // Yellow bus for live bus
                anchor = CENTER_ANCHOR,
                zIndex = 1f
            )
        }
    }
}