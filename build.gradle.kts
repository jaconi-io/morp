import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
    java
    jacoco
    `jvm-test-suite`
    id("io.freefair.lombok") version "9.2.0"
    id("org.springframework.boot") version "4.0.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.graalvm.buildtools.native") version "0.11.4"
    id("com.github.rising3.semver") version "0.8.2"
    id("org.barfuin.gradle.jacocolog") version "4.0.1"
    id("org.sonarqube") version "7.2.2.6593"
    id("se.ascp.gradle.gradle-versions-filter") version "0.1.16"
}

group = "io.jaconi"
version = "4.0.4"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.springframework.cloud:spring-cloud-gateway-dependencies:5.0.1"))

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.cloud:spring-cloud-starter-gateway-server-webmvc")

    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.session:spring-session-data-redis")

    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine")

    implementation("org.apache.commons:commons-lang3")

    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude("org.hamcrest", "hamcrest")
    }
    testImplementation("org.mock-server:mockserver-spring-test-listener:5.15.0") {
        exclude("org.hamcrest", "hamcrest")
    }
    testImplementation("org.springframework:spring-webflux")
    testImplementation("org.jsoup:jsoup:1.22.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
}

sonarqube {
    properties {
        property("sonar.projectKey", "jaconi-io_morp")
        property("sonar.organization", "jaconi-io")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

extra["testcontainersVersion"] = "2.0.2"

// setup separate test suites for unit and integration tests
testing {
    suites {

        // the default 'test' suite running unit tests via junit 5
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }

        val integrationTest by registering(JvmTestSuite::class) {
            sources {
                java {
                    setSrcDirs(listOf("src/integrationTest/java"))
                }
            }

            dependencies {
                implementation(project())
                implementation("org.jsoup:jsoup:1.22.1")
                // testcontainers core
                implementation("org.testcontainers:testcontainers-junit-jupiter")
                implementation("org.testcontainers:testcontainers")
                // testcontainers containers
                implementation("org.testcontainers:testcontainers-selenium")
                implementation("com.github.dasniko:testcontainers-keycloak:4.1.1")
                implementation("org.testcontainers:testcontainers-mockserver")
                // selenium itself
                implementation("org.seleniumhq.selenium:selenium-api")
                implementation("org.seleniumhq.selenium:selenium-chrome-driver")
                implementation("org.seleniumhq.selenium:selenium-remote-driver")
                // Spring WebFlux only for testing
                implementation("org.springframework:spring-webflux")
                implementation("io.projectreactor.netty:reactor-netty")
            }
            dependencyManagement {
                imports {
                    mavenBom("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}")
                }
            }
            targets {
                all {
                    testTask.configure {
                        mustRunAfter(tasks.bootBuildImage)
                        testLogging.exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
                        testLogging.showStandardStreams = true
                    }
                }
            }
        }
    }
}

tasks.withType<BootBuildImage> {
    mustRunAfter(tasks.test)
    // We ran into an UnsatisfiedLinkError when starting with the default runImage
    // and created the following ticket: https://github.com/spring-projects/spring-boot/issues/48230
    // Setting the following runImage, as suggested, fixed the issue:
    runImage.set("paketobuildpacks/ubuntu-noble-run:latest")
    imageName.value("ghcr.io/jaconi-io/${project.name}:latest")
    environment.putAll(mapOf(
        "BPE_APPEND_JAVA_TOOL_OPTIONS" to "-XX:MaxDirectMemorySize=100M",
        "BPE_DELIM_JAVA_TOOL_OPTIONS" to " ",
    ))
}

tasks.check {
    dependsOn(tasks.test)
    dependsOn(tasks.bootBuildImage)
    dependsOn(testing.suites.named("integrationTest"))
    dependsOn(tasks.jacocoTestReport)
}

tasks.test {
    useJUnitPlatform()
    testLogging.exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    testLogging.showStandardStreams = true
}

tasks.jacocoTestReport {
    mustRunAfter(tasks.test)
    mustRunAfter(testing.suites.named("integrationTest"))
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
    }
}

subprojects {
    if (File(projectDir, "src/main").exists()) {
        apply(plugin = "org.sonarqube")
        sonarqube {
            properties {
                property("sonar.coverage.jacoco.xmlReportPaths", tasks.jacocoTestReport.get().reports.xml.outputLocation.toString())
            }
        }
    }
}

tasks.sonarqube {
    dependsOn(tasks.jacocoTestReport)
}


configurations {
    get("integrationTestImplementation").apply {
        extendsFrom(configurations.implementation.get(), configurations.testImplementation.get())
    }
}
