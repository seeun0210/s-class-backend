dependencies {
    implementation(project(":SClass-Common"))
    implementation(project(":SClass-Domain"))
    implementation(project(":SClass-Infrastructure"))

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.2")

    testImplementation("io.mockk:mockk:1.13.16")
    testImplementation("org.springframework.boot:spring-boot-webmvc-test")
    testImplementation(platform("software.amazon.awssdk:bom:2.31.15"))
    testImplementation("software.amazon.awssdk:s3")
    testImplementation(platform("com.google.cloud:libraries-bom:26.55.0"))
    testImplementation("com.google.cloud:google-cloud-storage")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter:1.20.4")
}
