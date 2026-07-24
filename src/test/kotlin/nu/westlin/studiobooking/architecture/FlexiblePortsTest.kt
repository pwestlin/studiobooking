package nu.westlin.studiobooking.architecture

import com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage
import com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameEndingWith
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses

@AnalyzeClasses(
    packages = ["nu.westlin.studiobooking"],
    importOptions = [ImportOption.DoNotIncludeTests::class]
)
class FlexiblePortsTest {

    // --- UTGÅENDE PORTAR (Ligger i domain, t.ex. *Repository eller *Port) ---

    @ArchTest
    val `repositories in domain must be interfaces`: ArchRule =
        classes()
            .that().resideInAPackage("..domain..")
            .and().haveSimpleNameEndingWith("Repository")
            .or().haveSimpleNameEndingWith("Port")
            .should().beInterfaces()

    @ArchTest
    val `repositories in domain must only be implemented by infrastructure`: ArchRule =
        classes()
            .that().implement(
                resideInAPackage("..domain..")
                    .and(simpleNameEndingWith("Repository").or(simpleNameEndingWith("Port")))
            )
            .should().resideInAPackage("..infrastructure..")

    @ArchTest
    val `domain interfaces must not depend on infrastructure`: ArchRule =
        noClasses()
            .that().resideInAPackage("..domain..")
            .and().areInterfaces()
            .should().dependOnClassesThat()
            .resideInAPackage("..infrastructure..")

    // --- INKOMMANDE PORTAR / USE CASES (Om du har interfaces i application) ---

    @ArchTest
    val `application interfaces must not depend on infrastructure`: ArchRule =
        noClasses()
            .that().resideInAPackage("..application..")
            .and().areInterfaces()
            .should().dependOnClassesThat()
            .resideInAPackage("..infrastructure..")
            .allowEmptyShould(true)
}