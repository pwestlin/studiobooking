@file:Suppress("unused")

package nu.westlin.studiobooking.test

import org.assertj.core.api.AbstractThrowableAssert

inline fun <reified T : Throwable> AbstractThrowableAssert<*, *>.isExactlyInstanceOf(): AbstractThrowableAssert<*, *> =
    this.isExactlyInstanceOf(T::class.java)

// Passa på att lägga till för vanliga instans-kontroller också
inline fun <reified T : Throwable> AbstractThrowableAssert<*, *>.isInstanceOf(): AbstractThrowableAssert<*, *> =
    this.isInstanceOf(T::class.java)