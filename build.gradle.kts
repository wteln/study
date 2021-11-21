plugins {
    scala
    java
}
group = "com.mhy.work"
version = "1.0-SNAPSHOT"


allprojects {
    apply(plugin = "scala")

    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        mavenCentral()
    }

    dependencies {
        // https://mvnrepository.com/artifact/com.baomidou/mybatis-plus

        testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    }

    tasks.getByName<Test>("test") {
        useJUnitPlatform()
    }
}

tasks.create("product", type = Copy::class) {
    group = "build"
    from("server/build/libs/")
    from("tasks/build/libs/")
    into("build/libs/")
}.dependsOn(":w2:server:bootJar")
    .dependsOn(":w2:tasks:shadowJar")