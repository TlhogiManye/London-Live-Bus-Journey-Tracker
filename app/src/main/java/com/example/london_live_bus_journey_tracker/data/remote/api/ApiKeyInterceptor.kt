package com.example.london_live_bus_journey_tracker.data.remote.api

import com.example.london_live_bus_journey_tracker.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp interceptor that adds the TfL API key to all requests.
 *
 * The API key is retrieved from BuildConfig, which is populated from
 * local.properties at build time. This keeps the API key out of version control.
 *
 * ## Configuration
 * Add to local.properties:
 * ```
 * TFL_API_KEY=your_api_key_here
 * ```
 *
 * ## SOLID Principles
 * - **Single Responsibility**: Only handles API key injection
 * - **Open/Closed**: Can be extended without modification
 */
@Singleton
class ApiKeyInterceptor @Inject constructor() : Interceptor {

    /**
     * Intercepts HTTP requests and adds the API key as a query parameter.
     *
     * @param chain The interceptor chain
     * @return The response from the server
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url

        // Add API key as query parameter
        val newUrl = originalUrl.newBuilder()
            .addQueryParameter(QUERY_PARAM_APP_KEY, BuildConfig.TFL_API_KEY)
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }

    companion object {
        private const val QUERY_PARAM_APP_KEY = "app_key"
    }
}