import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
    java
    jacoco
    `jvm-test-suite`
    id("io.freefair.lombok") version "6.5.1"
    id("org.springframework.boot") version "3.0.0-RC1"
    id("io.spring.dependency-management") version "1.1.0"
    id("org.graalvm.buildtools.native") version "0.9.16"
    id("com.github.rising3.semver") version "0.8.1"
    id("org.barfuin.gradle.jacocolog") version "2.0.0"
    id("org.sonarqube") version "3.4.0.2513"
}

// Workaround for native image:
configurations.forEach { it.exclude("org.apache.logging.log4j", "log4j-api") }

// Upgrade Selenium, so it plays nice with OpenTelemetry
extra["selenium.version"] = "4.5.3"

group = "io.jaconi"
version = "1.2.3"

val registry = "ghcr.io/jaconi-io"

repositories {
    // mavenLocal()
    mavenCentral()
    maven(url = "https://repo.spring.io/release")
    maven(url = "https://repo.spring.io/milestone")
}

dependencies {
    implementation("org.reflections:reflections:0.10.2")

    // json logging
    implementation("net.logstash.logback:logstash-logback-encoder:7.2")
    implementation("ch.qos.logback:logback-classic")

    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:2022.0.0-RC1"))

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    implementation("org.springframework.cloud:spring-cloud-gateway-webflux")

    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")

    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.session:spring-session-data-redis")

    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine")

    implementation("org.apache.commons:commons-lang3")
    implementation("commons-codec:commons-codec:1.15")

    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    // workaround to be able to develop on ARM Mac :-(
    runtimeOnly("io.netty:netty-resolver-dns-native-macos:4.1.77.Final:osx-aarch_64")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude("org.hamcrest", "hamcrest")
    }
    testImplementation("org.mock-server:mockserver-spring-test-listener:5.14.0") {
        exclude("org.hamcrest", "hamcrest")
    }
    testImplementation("org.jsoup:jsoup:1.15.2")
    testImplementation("io.projectreactor:reactor-test")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

sonarqube {
    properties {
        property("sonar.projectKey", "jaconi-io_morp")
        property("sonar.organization", "jaconi-io")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

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
                implementation(project)
                implementation("org.jsoup:jsoup:1.15.2")
                // testcontainers core
                implementation("org.testcontainers:junit-jupiter:1.17.5")
                implementation("org.testcontainers:testcontainers:1.17.5")
                // testcontainers containers
                implementation("org.testcontainers:selenium:1.17.5")
                implementation("com.github.dasniko:testcontainers-keycloak:2.3.0")
                implementation("org.testcontainers:mockserver:1.17.5")
                // selenium itself
                implementation("org.seleniumhq.selenium:selenium-api")
                implementation("org.seleniumhq.selenium:selenium-chrome-driver")
                implementation("org.seleniumhq.selenium:selenium-remote-driver")
            }
            targets {
                all {
                    testTask.configure {
                        mustRunAfter(tasks.named("dockerBuild"))
                        testLogging.exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
                        testLogging.showStandardStreams = true
                    }
                }
            }
        }
    }
}

tasks.create<Exec>("dockerBuild") {
    mustRunAfter(tasks.test)
    executable("docker")
    args(listOf("build", "-t", "${registry}/${project.name}:${project.version}", "-t", "${registry}/${project.name}:latest", "."))
}

tasks.create<Exec>("dockerBuildPush") {
    executable("docker")
    args(listOf("buildx", "build", "--platform", "linux/amd64,linux/arm64", "-t", "${registry}/${project.name}:${project.version}", "--push", "."))
}

tasks.withType<BootBuildImage> {
    mustRunAfter(tasks.test)
    imageName.value("${registry}/${project.name}:${project.version}")
    publish.value(false)
    environment.putAll(mapOf(
        "BP_NATIVE_IMAGE" to setOf("x86_64", "amd64").contains(System.getProperty("os.arch")).toString(),
        "BPE_APPEND_JAVA_TOOL_OPTIONS" to "-XX:MaxDirectMemorySize=100M",
        "BPE_DELIM_JAVA_TOOL_OPTIONS" to " ",
    ))
    tags.add("${registry}/${project.name}:latest")
    docker {
        publishRegistry {
            url.value("ghcr.io")
            username.value("${System.getenv("GITHUB_USER")}")
            password.value("${System.getenv("GITHUB_TOKEN")}")
        }
    }
}

tasks.check {
    dependsOn(tasks.test)
    dependsOn(tasks.named("dockerBuild"))
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
