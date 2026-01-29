package com.example.london_live_bus_journey_tracker.data.remote.api

import com.example.london_live_bus_journey_tracker.data.remote.dto.ArrivalPredictionDto
import com.example.london_live_bus_journey_tracker.data.remote.dto.JourneyPlannerResponse
import com.example.london_live_bus_journey_tracker.data.remote.dto.RouteSequenceResponse
import com.example.london_live_bus_journey_tracker.data.remote.dto.StopPointSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit service interface for the Transport for London (TfL) Unified API.
 *
 * This interface defines all API endpoints used by the London Live Bus Journey Tracker.
 * All methods are suspend functions for coroutine support.
 *
 * Base URL: https://api.tfl.gov.uk/
 *
 * ## Authentication
 * API key is required and added via [ApiKeyInterceptor].
 *
 * ## Rate Limiting
 * TfL API has rate limits. Consider implementing caching and request throttling.
 *
 * ## SOLID Principles
 * - **Interface Segregation**: This interface is focused solely on TfL API calls.
 * - **Dependency Inversion**: Repositories depend on this abstraction, not concrete implementations.
 *
 * @see <a href="https://api.tfl.gov.uk/">TfL API Documentation</a>
 */
interface TflApiService {

    // ========================================================================
    // Stop Point Endpoints
    // ========================================================================

    /**
     * Searches for stop points matching the given query.
     *
     * This endpoint is used for location autocomplete in the search screen.
     *
     * @param query Search term (minimum 3 characters recommended)
     * @param modes Comma-separated transport modes to filter (e.g., "bus")
     * @param maxResults Maximum number of results to return (default: 25)
     * @return [Response] containing [StopPointSearchResponse] with matching stops
     *
     * Example: GET /StopPoint/Search/victoria?modes=bus&maxResults=10
     */
    @GET("StopPoint/Search/{query}")
    suspend fun searchStopPoints(
        @Path("query") query: String,
        @Query("modes") modes: String? = "bus",
        @Query("maxResults") maxResults: Int? = 25
    ): Response<StopPointSearchResponse>

    // ========================================================================
    // Journey Planner Endpoints
    // ========================================================================

    /**
     * Plans a journey between two locations.
     *
     * This endpoint returns possible routes between origin and destination,
     * including bus options with estimated journey times.
     *
     * @param from Origin location (NaPTAN ID, ICS code, or coordinates)
     * @param to Destination location
     * @param mode Transport mode filter (e.g., "bus")
     * @param timeIs Whether time is "Departing" or "Arriving" (default: Departing)
     * @return [Response] containing [JourneyPlannerResponse] with journey options
     *
     * Example: GET /Journey/JourneyResults/940GZZLUVIC/to/940GZZLUOXC?mode=bus
     *
     * Note: If locations are ambiguous, the response will contain disambiguation options
     * instead of journey results.
     */
    @GET("Journey/JourneyResults/{from}/to/{to}")
    suspend fun planJourney(
        @Path("from") from: String,
        @Path("to") to: String,
        @Query("mode") mode: String? = "bus",
        @Query("timeIs") timeIs: String? = "Departing"
    ): Response<JourneyPlannerResponse>

    // ========================================================================
    // Line Arrivals Endpoints
    // ========================================================================

    /**
     * Gets live arrival predictions for a bus line at a specific stop.
     *
     * This endpoint provides real-time arrival information that updates
     * every 30 seconds in the TfL system.
     *
     * @param lineId Bus line identifier (e.g., "24", "38")
     * @param stopPointId NaPTAN ID of the stop
     * @return [Response] containing list of [ArrivalPredictionDto]
     *
     * Example: GET /Line/24/Arrivals/490000254W
     *
     * The response includes:
     * - Vehicle ID for tracking specific buses
     * - Time to station in seconds
     * - Destination information
     */
    @GET("Line/{lineId}/Arrivals/{stopPointId}")
    suspend fun getLineArrivals(
        @Path("lineId") lineId: String,
        @Path("stopPointId") stopPointId: String
    ): Response<List<ArrivalPredictionDto>>

    /**
     * Gets all arrival predictions for a bus line across all stops.
     *
     * This endpoint is used for the bus list screen to show all buses
     * currently operating on a line.
     *
     * @param lineId Bus line identifier (e.g., "24")
     * @return [Response] containing list of [ArrivalPredictionDto]
     *
     * Example: GET /Line/24/Arrivals
     */
    @GET("Line/{lineId}/Arrivals")
    suspend fun getAllLineArrivals(
        @Path("lineId") lineId: String
    ): Response<List<ArrivalPredictionDto>>

    // ========================================================================
    // Route Sequence Endpoints
    // ========================================================================

    /**
     * Gets the ordered sequence of stops for a bus route.
     *
     * This endpoint is essential for the "Virtual GPS" feature. It provides
     * the ordered list of stops with coordinates, allowing us to infer
     * bus positions based on arrival predictions.
     *
     * @param lineId Bus line identifier
     * @param direction Route direction: "inbound" or "outbound"
     * @return [Response] containing [RouteSequenceResponse] with ordered stops
     *
     * Example: GET /Line/24/Route/Sequence/inbound
     *
     * ## Virtual GPS Algorithm
     * 1. Cache this route sequence on tracking screen init
     * 2. Poll arrivals every 30 seconds
     * 3. Match arrival's naptanId to cached stop
     * 4. Use stop's lat/lon as inferred bus position
     */
    @GET("Line/{lineId}/Route/Sequence/{direction}")
    suspend fun getRouteSequence(
        @Path("lineId") lineId: String,
        @Path("direction") direction: String
    ): Response<RouteSequenceResponse>

    companion object {
        /** Base URL for TfL Unified API */
        const val BASE_URL = "https://api.tfl.gov.uk/"
    }
}