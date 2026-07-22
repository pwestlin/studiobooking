package nu.westlin.studiobooking.common

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @return a [Logger] of type `T`.
 */
inline fun <reified T> T.logger(): Logger = LoggerFactory.getLogger(T::class.java)
