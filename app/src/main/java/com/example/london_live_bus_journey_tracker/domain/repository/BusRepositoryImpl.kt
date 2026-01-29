package com.example.london_live_bus_journey_tracker.data.repository

import com.example.london_live_bus_journey_tracker.data.mapper.TflMapper
import com.example.london_live_bus_journey_tracker.data.remote.api.TflApiService
import com.example.london_live_bus_journey_tracker.domain.common.Result
import com.example.london_live_bus_journey_tracker.domain.model.BusArrival
import com.example.london_live_bus_journey_tracker.domain.model.BusPosition
import com.example.london_live_bus_journey_tracker.domain.model.RouteSequence
import com.example.london_live_bus_journey_tracker.domain.repository.BusRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [BusRepository] using TfL API.
 *
 * Includes the Virtual GPS algorithm for inferring bus positions
 * from arrival predictions and route sequence data.
 */
@Singleton
class BusRepositoryImpl @Inject constructor(
    private val apiService: TflApiService,
    private val mapper: TflMapper,
    private val ioDispatcher: CoroutineDispatcher
) : BusRepository {

    /**
     * Gets live arrival predictions for a bus line.
     */
    override suspend fun getArrivalsForLine(lineId: String): Result<List<BusArrival>> {
        return withContext(ioDispatcher) {
            try {
                val response = apiService.getAllLineArrivals(lineId)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val arrivals = mapper.mapToBusArrivals(body)
                        Result.Success(arrivals)
                    } else {
                        Result.Success(emptyList())
                    }
                } else {
                    Result.Error("Failed to get arrivals: ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error(
                    message = e.message ?: "Network error",
                    exception = e
                )
            }
        }
    }

    /**
     * Gets the ordered stop sequence for a bus route.
     */
    override suspend fun getRouteSequence(
        lineId: String,
        direction: String
    ): Result<RouteSequence> {
        return withContext(ioDispatcher) {
            try {
                val response = apiService.getRouteSequence(lineId, direction)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val routeSequence = mapper.mapToRouteSequence(body)
                        if (routeSequence != null) {
                            Result.Success(routeSequence)
                        } else {
                            Result.Error("No stops found in route")
                        }
                    } else {
                        Result.Error("Empty response from route sequence")
                    }
                } else {
                    Result.Error("Failed to get route: ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error(
                    message = e.message ?: "Network error",
                    exception = e
                )
            } as Result<RouteSequence>
        }
    }

    /**
     * Infers bus position using Virtual GPS algorithm.
     *
     * ## Algorithm
     * 1. Get arrivals for the line
     * 2. Find the prediction for our specific vehicle
     * 3. Match the approaching stop to the route sequence
     * 4. Use the stop's coordinates as the bus position
     * 5. Get the next stop for "next stop" display
     */
    override suspend fun getBusPosition(
        vehicleId: String,
        lineId: String,
        routeSequence: RouteSequence
    ): Result<BusPosition> {
        return withContext(ioDispatcher) {
            // Get current arrivals
            val arrivalsResult = getArrivalsForLine(lineId)

            if (arrivalsResult is Result.Error) {
                return@withContext arrivalsResult
            }

            val arrivals = (arrivalsResult as Result.Success).data

            // Find the arrival for our vehicle
            val vehicleArrival = arrivals.find { it.vehicleId == vehicleId }

            if (vehicleArrival == null) {
                return@withContext Result.Error("Vehicle $vehicleId not found")
            }

            // Find the stop in our route sequence
            val currentStop = routeSequence.findStopById(vehicleArrival.naptanId)

            if (currentStop == null) {
                return@withContext Result.Error("Vehicle not on tracked route")
            }

            // Get the next stop
            val nextStop = routeSequence.getNextStop(vehicleArrival.naptanId)

            val position = BusPosition(
                vehicleId = vehicleId,
                lat = currentStop.lat,
                lon = currentStop.lon,
                currentStopName = currentStop.name,
                nextStopName = nextStop?.name,
                timeToNextStopSeconds = vehicleArrival.timeToStationSeconds
            )

            Result.Success(position)
        }
    }
}