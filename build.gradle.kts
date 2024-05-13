import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
    java
    jacoco
    `jvm-test-suite`
    id("io.freefair.lombok") version "8.6"
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.5"
    id("org.graalvm.buildtools.native") version "0.10.1"
    id("com.github.rising3.semver") version "0.8.2"
    id("org.barfuin.gradle.jacocolog") version "3.1.0"
    id("org.sonarqube") version "5.0.0.4638"
    id("se.ascp.gradle.gradle-versions-filter") version "0.1.16"
}

group = "io.jaconi"
version = "1.3.1"

val registry = "ghcr.io/jaconi-io"

repositories {
    mavenCentral()
}

dependencies {

    // json logging
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")
    implementation("ch.qos.logback:logback-classic")

    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:2023.0.0"))

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    implementation("org.springframework.cloud:spring-cloud-gateway-webflux")

    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.session:spring-session-data-redis")

    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine")

    implementation("org.apache.commons:commons-lang3")
    implementation("commons-codec:commons-codec")

    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    // workaround to be able to develop on ARM Mac :-(
    runtimeOnly("io.netty:netty-resolver-dns-native-macos:4.1.85.Final:osx-aarch_64")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude("org.hamcrest", "hamcrest")
    }
    testImplementation("org.mock-server:mockserver-spring-test-listener:5.15.0") {
        exclude("org.hamcrest", "hamcrest")
    }
    testImplementation("org.jsoup:jsoup:1.17.2")
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

extra["testcontainersVersion"] = "1.19.6"

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
                implementation("org.jsoup:jsoup:1.17.2")
                // testcontainers core
                implementation("org.testcontainers:junit-jupiter")
                implementation("org.testcontainers:testcontainers")
                // testcontainers containers
                implementation("org.testcontainers:selenium")
                implementation("com.github.dasniko:testcontainers-keycloak:3.3.1")
                implementation("org.testcontainers:mockserver")
                // selenium itself
                implementation("org.seleniumhq.selenium:selenium-api")
                implementation("org.seleniumhq.selenium:selenium-chrome-driver")
                implementation("org.seleniumhq.selenium:selenium-remote-driver")
            }
            dependencyManagement {
                imports {
                    mavenBom("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}")
                }
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
