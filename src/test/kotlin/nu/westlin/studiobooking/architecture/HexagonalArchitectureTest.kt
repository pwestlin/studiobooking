package nu.westlin.studiobooking.architecture

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses

@Suppress("PropertyName")
@AnalyzeClasses(
    packages = ["nu.westlin.studiobooking"],
    importOptions = [ImportOption.DoNotIncludeTests::class]
)
class HexagonalArchitectureTest {

    @ArchTest
    val `domain layer must not depend on application or infrastructure`: ArchRule =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..application..", "..infrastructure..", "..adapter..")

    @ArchTest
    val `domain layer must not depend on spring framework`: ArchRule =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("org.springframework..")

    @ArchTest
    val `application layer must not depend on infrastructure`: ArchRule =
        noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..infrastructure..", "..adapter..")
}