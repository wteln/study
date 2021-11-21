plugins {
    java
    id("org.springframework.boot") version "2.5.4"
    id("com.github.johnrengelman.shadow") version("7.1.0")
}

dependencies {
    implementation(project(":common"))
    implementation("com.baomidou:mybatis-plus-boot-starter:3.4.3")
    implementation("org.springframework.boot:spring-boot-starter-web:2.5.4")
    compileOnly("org.projectlombok:lombok:1.18.22")
    annotationProcessor("org.projectlombok:lombok:1.18.22")
    implementation("mysql:mysql-connector-java:8.0.27")
    implementation("org.apache.hadoop:hadoop-hdfs-client:2.10.1")
    implementation("org.apache.hadoop:hadoop-common:2.10.1") {
        exclude(module = "log4j")
        exclude(module = "slf4j-log4j12")
        exclude(group = "org.mortbay.jetty")
        exclude(group = "javax.servlet")
        exclude(group = "javax.servlet.jsp")
    }
}