package nu.westlin.studiobooking.test

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

// Måste ha proxyBeanMethods = false om man har object isf class.
@TestConfiguration(proxyBeanMethods = false)
object SharedTestcontainersConfiguration {

    // Genom att initiera den i ett object startar containern en gång
    // när klassen laddas första gången.
    private val postgresContainer = PostgreSQLContainer(DockerImageName.parse("postgres:18-alpine")).apply {
        withReuse(true)
        start()
    }

    @Bean
    @ServiceConnection
    fun postgresContainerBean(): PostgreSQLContainer = postgresContainer
}