plugins {
    java
    id("io.freefair.lombok") version "5.3.0"
}

group = "dev.zeldus"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testCompile("junit", "junit", "4.12")
}
