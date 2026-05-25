plugins {
    java
    alias(libs.plugins.vaadin)
    alias(libs.plugins.spring)
    alias(libs.plugins.spotless)
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}
spotless {
    java {
        endWithNewline()
        palantirJavaFormat()
        removeUnusedImports()
        forbidWildcardImports()
    }
}
dependencies {
    implementation(platform(libs.spring.boot.bom))
    implementation(libs.vaadin.spring.boot.starter)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.aspectj)
    runtimeOnly(libs.micrometer.registry.prometheus)
    developmentOnly(libs.vaadin.dev)
}
