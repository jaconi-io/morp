FROM container-registry.oracle.com/graalvm/native-image:23 AS builder

# Install tar and gzip to extract the Maven binaries
RUN microdnf install --nodocs -y \
    findutils \
 && microdnf clean all \
 && rm -rf /var/cache/yum

WORKDIR /build

# Copy the source code into the image for building
COPY . /build

# Build
RUN ./gradlew nativeCompile

# The deployment Image
FROM container-registry.oracle.com/os/oraclelinux:9-slim

EXPOSE 8080
EXPOSE 8081

WORKDIR /workspace

# Copy the native executable into the container
COPY --from=builder /build/build/native/nativeCompile .
ENTRYPOINT ["/workspace/morp"]