package org.radarbase.appserver.jersey.service.quartz

interface QuartzNamingStrategy {
    fun getTriggerName(userName: String, messageId: String): String

    fun getJobKeyName(userName: String, messageId: String): String

    fun getMessageId(key: String): String?
}
