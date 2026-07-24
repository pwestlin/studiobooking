package nu.westlin.studiobooking.architecture

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.library.Architectures.onionArchitecture

@Suppress("PropertyName")
@AnalyzeClasses(
    packages = ["nu.westlin.studiobooking"],
    importOptions = [ImportOption.DoNotIncludeTests::class]
)
class OnionArchitectureTest {

    @ArchTest
    val `onion architecture layers are respected`: ArchRule =
        onionArchitecture()
            .domainModels(
                "..domain.model..",
                "..domain.event..",
                "..domain.exception.."
            )
            .domainServices("..domain")
            .applicationServices("..application..")
            .adapter("rest", "..infrastructure.rest..")
            .adapter("persistence", "..infrastructure.persistence..")
            .adapter("notification", "..infrastructure.notification..")
}