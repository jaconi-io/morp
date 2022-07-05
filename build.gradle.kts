plugins {
    java
    id("org.springframework.boot") version "2.7.1"
    id("org.springframework.experimental.aot") version "0.12.0"
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

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
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
