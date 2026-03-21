dependencies {
    implementation(project(":SClass-Common"))
    implementation(project(":SClass-Domain"))
    implementation(project(":SClass-Infrastructure"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.2")
}
