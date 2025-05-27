package org.radarbase.appserver.jersey.service.quartz

class SimpleQuartzNamingStrategy : QuartzNamingStrategy {
    override fun getTriggerName(userName: String, messageId: String): String {
        return "$TRIGGER_PREFIX$userName-$messageId"
    }

    override fun getJobKeyName(userName: String, messageId: String): String {
        return "$JOB_PREFIX$userName-$messageId"
    }

    override fun getMessageId(key: String): String? {
        val keys: List<String> = key.split("-")
        return keys.lastOrNull()
    }

    companion object {
        private const val TRIGGER_PREFIX = "message-trigger-"
        private const val JOB_PREFIX = "message-jobdetail-"
    }
}
