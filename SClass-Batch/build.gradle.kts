dependencies {
    implementation(project(":SClass-Common"))
    implementation(project(":SClass-Domain"))
    implementation(project(":SClass-Infrastructure"))

    implementation("org.springframework.boot:spring-boot-starter-batch")

    testImplementation("io.mockk:mockk:1.13.16")
}
