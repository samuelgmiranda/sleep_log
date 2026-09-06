package com.noom.interview.fullstack.sleep.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@Configuration
class ClockConfiguration(
    @Value("\${sleep.clock.fixed-instant:}") private val fixedInstant: String,
    @Value("\${sleep.clock.zone:}") private val zone: String
) {

    @Bean
    fun clock(): Clock {
        val zoneId = zone.takeIf { it.isNotBlank() }?.let(ZoneId::of) ?: ZoneId.systemDefault()
        return fixedInstant.takeIf { it.isNotBlank() }
            ?.let { Clock.fixed(Instant.parse(it), zoneId) }
            ?: Clock.system(zoneId)
    }
}
