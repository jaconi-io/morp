plugins {
    java
    id("org.springframework.boot") version "2.7.1"
    id("org.springframework.experimental.aot") version "0.12.0"
    id("com.avast.gradle.docker-compose") version "0.14.9"
}

apply(plugin = "io.spring.dependency-management")

group = "io.jaconi"
version = "0.0.1-SNAPSHOT"

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
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.mock-server:mockserver-spring-test-listener:5.13.2")
    testImplementation("org.jsoup:jsoup:1.15.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

dockerCompose {

    //isRequiredBy(project.tasks.test)

    // provides system properties '<service>.host' (and others)
    //exposeAsSystemProperties(project.tasks.test)
}

tasks {
    bootBuildImage {
        // builder = "paketobuildpacks/builder:tiny"
        imageName = "docker.io/jaconi/${project.name}:${project.version}"
        environment = mapOf(
            "BP_NATIVE_IMAGE" to "true"
        )
    }

    test {
        useJUnitPlatform()
    }
}
