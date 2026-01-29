package com.example.london_live_bus_journey_tracker.domain.common

/**
 * A sealed class representing the outcome of an operation.
 *
 * Provides type-safe error handling without exceptions for control flow.
 *
 * @param T The type of data in a successful result
 */
sealed class Result<out T> {

    /**
     * Represents a successful operation.
     *
     * @property data The resulting data
     */
    data class Success<out T>(val data: T) : Result<T>()

    /**
     * Represents a failed operation.
     *
     * @property message Human-readable error description
     * @property exception The underlying exception, if any
     */
    data class Error(
        val message: String,
        val exception: Throwable? = null
    ) : Result<Nothing>()

    /** Returns the data if [Success], or null otherwise. */
    fun getOrNull(): T? = (this as? Success)?.data

    /** Returns the data if [Success], or [defaultValue] otherwise. */
    fun getOrDefault(defaultValue: @UnsafeVariance T): T =
        (this as? Success)?.data ?: defaultValue

    /** Transforms the data if [Success]. */
    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }

    /** Executes [action] if [Success]. */
    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }

    /** Executes [action] if [Error]. */
    inline fun onError(action: (String) -> Unit): Result<T> {
        if (this is Error) action(message)
        return this
    }
}