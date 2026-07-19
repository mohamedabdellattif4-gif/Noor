package com.noor.core.common

/**
 * Domain-friendly result type used to model successful and failed operations
 * without coupling callers to transport or persistence exceptions.
 */
sealed interface Result<out T> {
    data class Success<T>(val value: T) : Result<T>
    data class Failure(val throwable: Throwable) : Result<Nothing>
}
