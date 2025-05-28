package org.radarbase.appserver.jersey.dto.protocol

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.radarbase.appserver.jersey.utils.deserializer.Base64Deserializer

data class GithubContent(
    @field:JsonDeserialize(using = Base64Deserializer::class)
    var content: String? = null,

    var sha: String? = null,

    var size: String? = null,

    var url: String? = null,

    var node_id: String? = null,

    var encoding: String? = null,
)
