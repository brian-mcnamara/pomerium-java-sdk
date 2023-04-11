import com.google.protobuf.gradle.GenerateProtoTask

plugins {
    id("java")
    id("com.google.protobuf") version "0.9.2"
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.grpc:grpc-protobuf:1.53.0")
    implementation("com.google.protobuf:protobuf-java:3.22.2")
    implementation("io.envoyproxy.controlplane:api:1.0.37")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.getByName<GenerateProtoTask>("generateProto") {
    addIncludeDir(files("./ext"))
}

sourceSets {
    main {
        proto {
            srcDir("proto")
        }
    }
}