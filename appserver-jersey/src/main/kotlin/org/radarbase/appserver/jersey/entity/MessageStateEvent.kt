package org.radarbase.appserver.jersey.entity

import jakarta.persistence.Column
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.MappedSuperclass
import jakarta.validation.constraints.NotNull
import org.radarbase.appserver.jersey.event.state.MessageState
import java.time.Instant

@MappedSuperclass
class MessageStateEvent(
    @field:NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var state: MessageState? = null,

    @field:NotNull
    @Column(nullable = false)
    var time: Instant? = null,

    @Column(name = "associated_info", length = 1250)
    var associatedInfo: String? = null,
    )
