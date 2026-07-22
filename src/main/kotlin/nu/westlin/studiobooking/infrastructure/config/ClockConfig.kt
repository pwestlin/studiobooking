package nu.westlin.studiobooking.infrastructure.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
class ClockConfig {

    @Bean
    fun clock(): Clock {
        // Returnerar systemklockan i UTC (eller systemDefault() om du föredrar lokal tidszon)
        return Clock.systemUTC()
    }
}