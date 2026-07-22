package nu.westlin.studiobooking

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    // TODO pwestlin: Clock?
    runApplication<Application>(*args)
}
