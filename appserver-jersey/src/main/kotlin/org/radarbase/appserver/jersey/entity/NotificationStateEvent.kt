package org.radarbase.appserver.jersey.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.radarbase.appserver.jersey.event.state.MessageState
import java.time.Instant

@Entity
@Table(name = "notification_state_events")
class NotificationStateEvent : MessageStateEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null

    @field:NotNull
    @field:JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "notification_id", nullable = false)
    @field:OnDelete(action = OnDeleteAction.CASCADE)
    var notification: Notification? = null

    constructor(
        notification: Notification,
        state: MessageState,
        time: Instant,
        associatedInfo: String?,
    ) : super(state, time, associatedInfo) {
        this.notification = notification
    }

    constructor()
}
