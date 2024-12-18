package org.radarbase.appserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["org.radarbase.appserver", "org.radarbase.fcm"])
class AppserverApplication

fun main(args : Array<String>) {
    runApplication<AppserverApplication>(*args)
}