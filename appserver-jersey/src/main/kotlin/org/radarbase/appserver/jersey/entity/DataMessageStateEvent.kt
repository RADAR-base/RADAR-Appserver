package org.radarbase.appserver.jersey.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.radarbase.appserver.jersey.event.state.MessageState
import java.time.Instant

@Entity
@Table(name = "data_message_state_events")
class DataMessageStateEvent : MessageStateEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null

    @field:NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "data_message_id", nullable = false)
    @field:OnDelete(action = OnDeleteAction.CASCADE)
    @field:JsonIgnore
    var dataMessage: DataMessage? = null

    constructor(
        dataMessage: DataMessage?,
        state: MessageState,
        time: Instant,
        associatedInfo: String?,
    ) : super(state, time, associatedInfo) {
        this.dataMessage = dataMessage
    }

    constructor()
}
