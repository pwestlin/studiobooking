package nu.westlin.studiobooking.architecture

import com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage
import com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameEndingWith
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchIgnore
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses

@Suppress("PropertyName")
@AnalyzeClasses(
    packages = ["nu.westlin.studiobooking"],
    importOptions = [ImportOption.DoNotIncludeTests::class]
)
class HexagonalPortsTest {

    // --- INKOMMANDE PORTAR ---

    @ArchIgnore
    @ArchTest
    val `inbound ports must be interfaces`: ArchRule =
        classes()
            .that().resideInAPackage("..application.port.in..")
            .should().beInterfaces()

    @ArchIgnore
    @ArchTest
    val `inbound ports must not depend on infrastructure`: ArchRule =
        noClasses()
            .that().resideInAPackage("..application.port.in..")
            .should().dependOnClassesThat()
            .resideInAPackage("..infrastructure..")

    @ArchIgnore
    @ArchTest
    val `inbound ports must only be implemented by application services`: ArchRule =
        classes()
            .that().implement(resideInAPackage("..application.port.in.."))
            .should().resideInAPackage("..application.service..")


    // --- UTGÅENDE PORTAR ---

    @ArchIgnore
    @ArchTest
    val `outbound ports must be interfaces`: ArchRule =
        classes()
            .that().resideInAPackage("..domain.port.out..")
            .or().haveSimpleNameEndingWith("Repository")
            .should().beInterfaces()

    @ArchIgnore
    @ArchTest
    val `outbound ports must reside in domain`: ArchRule =
        classes()
            .that().haveSimpleNameEndingWith("Repository")
            .should().resideInAPackage("..domain..")

    @ArchIgnore
    @ArchTest
    val `outbound ports must not depend on infrastructure`: ArchRule =
        noClasses()
            .that().resideInAPackage("..domain.port.out..")
            .or().haveSimpleNameEndingWith("Repository")
            .should().dependOnClassesThat()
            .resideInAPackage("..infrastructure..")

    @ArchIgnore
    @ArchTest
    val `outbound ports must only be implemented by infrastructure adapters`: ArchRule =
        classes()
            .that().implement(
                resideInAPackage("..domain.port.out..")
                    .or(simpleNameEndingWith("Repository"))
            )
            .should().resideInAPackage("..infrastructure..")
}