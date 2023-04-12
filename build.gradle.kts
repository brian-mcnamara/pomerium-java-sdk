import com.google.protobuf.gradle.GenerateProtoTask
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import java.net.URL

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
}

val protodepDir = File(buildDir, "tmp${File.separator}protodep")
tasks.register("downloadProtodep") {
    outputs.file(protodepDir)
    val os = with(DefaultNativePlatform.getCurrentOperatingSystem()) {
        when {
            isLinux -> "linux"
            isMacOsX -> "darwin"
            isWindows -> "windows"

            else -> throw GradleException("OS not supported by protodep")
        }
    }
    val arch = with(DefaultNativePlatform.getCurrentArchitecture()) {
        when {
            isAmd64 -> "amd64"
            isArm -> "arm64"
            isI386 -> "386"

            else -> throw GradleException("Architecture is not supported")
        }
    }
    val downloadLink = "https://github.com/stormcat24/protodep/releases/download/v0.1.7/protodep_${os}_${arch}.tar.gz"
    val file = File(buildDir, "tmp${File.separator}protodep.tar.gz")
    URL(downloadLink).openConnection().getInputStream().use {
        it.copyTo(file.outputStream())
    }

    copy {
        from(tarTree(file))
        into(protodepDir)
    }
}

tasks.register("protodep", type=Exec::class) {
    dependsOn("downloadProtodep")
    inputs.file(file("protodep.toml"))
    outputs.files(files("ext", "proto"))

    val protodep = protodepDir.listFiles()[0]
    protodep.setExecutable(true)
    commandLine(protodep.absolutePath, "up", "-u")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.getByName<GenerateProtoTask>("generateProto") {
    dependsOn("protodep")
    addIncludeDir(files("./ext"))
}

sourceSets {
    main {
        proto {
            srcDir("proto")
        }
    }
}