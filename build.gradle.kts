plugins {
    java
    jacoco
    id("org.springframework.boot") version "2.7.1"
    id("org.springframework.experimental.aot") version "0.12.0"
    id("com.avast.gradle.docker-compose") version "0.16.8"
    id("com.github.rising3.semver") version "0.8.1"
    id("org.barfuin.gradle.jacocolog") version "2.0.0"
}

apply(plugin = "io.spring.dependency-management")

group = "io.jaconi"
version = "0.2.1"

repositories {
    mavenCentral()
    maven(url = "https://repo.spring.io/release")
}

dependencies {
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    compileOnly("org.projectlombok:lombok")

    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:2021.0.3"))

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    implementation("org.springframework.cloud:spring-cloud-gateway-webflux")

    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity5")

    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.session:spring-session-data-redis")

    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    // workaround to be able to develop on ARM Mac :-(
    runtimeOnly("io.netty:netty-resolver-dns-native-macos:4.1.77.Final:osx-aarch_64")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mock-server:mockserver-spring-test-listener:5.13.2")
    testImplementation("org.jsoup:jsoup:1.15.2")
    testImplementation("io.projectreactor:reactor-test")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
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
    dependsOn("test")
    dependsOn("integrationTest")
    dependsOn("jacocoTestReport")
}

tasks.test {
    useJUnitPlatform {
        excludeTags("integration")
    }
}

val integrationTest = task<Test>("integrationTest"){
    mustRunAfter("test")
    useJUnitPlatform {
        includeTags("integration")
    }
    dockerCompose.isRequiredBy(this)
    dockerCompose.exposeAsSystemProperties(this)
}

tasks.jacocoTestReport {
    mustRunAfter("test")
    mustRunAfter("integrationTest")
}

tasks.composeUp {
    mustRunAfter("test")
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
    }
}

