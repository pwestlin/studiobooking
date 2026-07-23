plugins {
    kotlin("jvm") version "2.4.10"
    kotlin("plugin.spring") version "2.4.10"
    id("org.springframework.boot") version "4.1.0"
    //id("io.spring.dependency-management") version "1.1.7"
}

group = "nu.westlin"
version = "0.0.1-SNAPSHOT"
description = "studiobooking"

repositories {
    mavenCentral()
}

dependencies {
    val springBootBom = platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)

    @Suppress("AvoidDuplicateDependencies")
    implementation(springBootBom)
    @Suppress("AvoidDuplicateDependencies")
    developmentOnly(springBootBom)

    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation(platform("org.springframework.modulith:spring-modulith-bom:2.1.0"))
    implementation("org.springframework.modulith:spring-modulith-events-api")
    implementation("org.springframework.modulith:spring-modulith-events-jdbc")
    implementation("org.springframework.modulith:spring-modulith-events-jackson")

    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-data-jdbc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-jdbc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("com.ninja-squad:springmockk:5.0.1")
    testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
    testImplementation("org.awaitility:awaitility-kotlin")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// TODO pwestlin: Add Detekt (my own plugin :))

kotlin {
    jvmToolchain(25)

    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
