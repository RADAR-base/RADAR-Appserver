package org.radarbase.appserver.jersey.entity

import java.time.Instant

interface Scheduled {
    val scheduledTime: Instant?
}
