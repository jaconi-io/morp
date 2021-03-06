plugins {
    java
    jacoco
    `jvm-test-suite`
    id("org.springframework.boot") version "2.7.1"
    id("org.springframework.experimental.aot") version "0.12.1"
    id("com.avast.gradle.docker-compose") version "0.16.8"
    id("com.github.rising3.semver") version "0.8.1"
    id("org.barfuin.gradle.jacocolog") version "2.0.0"
    id ("org.sonarqube") version "3.4.0.2513"
}

apply(plugin = "io.spring.dependency-management")

group = "io.jaconi"
version = "0.2.3"

repositories {
    mavenCentral()
    maven(url = "https://repo.spring.io/release")
}

dependencies {
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    compileOnly("org.projectlombok:lombok")
    compileOnly("org.springframework.experimental:spring-aot:0.12.1")
    implementation("org.reflections:reflections:0.10.2")

    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:2021.0.3"))

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    implementation("org.springframework.cloud:spring-cloud-gateway-webflux")

    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity5")

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
    testImplementation("org.mock-server:mockserver-spring-test-listener:5.13.2") {
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
            }
            targets {
                all {
                    testTask.configure {
                        mustRunAfter(tasks.test)
                    }

                    dockerCompose.isRequiredBy(testTask)
                    dockerCompose.exposeAsSystemProperties(testTask.get())
                    dockerCompose.useComposeFiles.add("src/integrationTest/resources/docker-compose.yaml")
                }
            }
        }
    }
}

tasks.bootBuildImage {
    // builder = "paketobuildpacks/builder:tiny"
    imageName = "ghcr.io/jaconi-io/${project.name}:${project.version}"
    environment = mapOf(
            "BP_NATIVE_IMAGE" to "true"
    )
    isPublish = false
    docker {
        publishRegistry {
            url = "ghcr.io"
            username = System.getenv("GITHUB_USER")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

tasks.check {
    dependsOn(tasks.test)
    dependsOn(testing.suites.named("integrationTest"))
    dependsOn(tasks.jacocoTestReport)
}

tasks.test {
    useJUnitPlatform()
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


tasks.composeUp {
    mustRunAfter(tasks.test)
}

configurations {
    get("integrationTestImplementation").apply {
        extendsFrom(configurations.implementation.get(), configurations.testImplementation.get())
    }
}
