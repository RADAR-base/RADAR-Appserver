package org.radarbase.appserver.service.storage

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
class StoragePathTest {

    companion object {
        private const val PREFIX = "prefix"
        private const val FILENAME = "example.txt"
        private const val PROJECT_ID = "project1"
        private const val SUBJECT_ID = "subjectA"
        private const val TOPIC_ID = "topicX"
        private const val SIMPLE_LOCALFILE_PATTERN = "[0-9]+_[a-z0-9-]+\\.txt"
    }

    @Test
    fun minimalValidPath() {
        val path = StoragePath.builder()
            .filename(FILENAME)
            .projectId(PROJECT_ID)
            .subjectId(SUBJECT_ID)
            .topicId(TOPIC_ID)
            .build()
        assertTrue(path.fullPath.matches("$PROJECT_ID/$SUBJECT_ID/$TOPIC_ID/[0-9]+_[a-z0-9-]+\\.txt".toRegex()))
        assertTrue(path.pathInTopicDir.matches(SIMPLE_LOCALFILE_PATTERN.toRegex()))
    }

    @Test
    fun includeDayFolder() {
        val path = StoragePath.builder()
            .filename(FILENAME)
            .projectId(PROJECT_ID)
            .subjectId(SUBJECT_ID)
            .topicId(TOPIC_ID)
            .collectPerDay(true)
            .build()
        assertTrue(path.fullPath.matches("$PROJECT_ID/$SUBJECT_ID/$TOPIC_ID/[0-9]+/[0-9]+_[a-z0-9-]+\\.txt".toRegex()))
        assertTrue(path.pathInTopicDir.matches("[0-9]+/[0-9]+_[a-z0-9-]+\\.txt".toRegex()))
    }

    @Test
    fun includePrefix() {
        val path = StoragePath.builder()
            .prefix(PREFIX)
            .filename(FILENAME)
            .projectId(PROJECT_ID)
            .subjectId(SUBJECT_ID)
            .topicId(TOPIC_ID)
            .build()
        assertTrue(path.fullPath.matches("$PREFIX/$PROJECT_ID/$SUBJECT_ID/$TOPIC_ID/[0-9]+_[a-z0-9-]+\\.txt".toRegex()))
        assertTrue(path.pathInTopicDir.matches(SIMPLE_LOCALFILE_PATTERN.toRegex()))
    }

    @Test
    fun testLowercaseExtension() {
        val path = StoragePath.builder()
            .filename("example.TXT")
            .projectId(PROJECT_ID)
            .subjectId(SUBJECT_ID)
            .topicId(TOPIC_ID)
            .build()
        assertTrue(path.fullPath.matches("$PROJECT_ID/$SUBJECT_ID/$TOPIC_ID/[0-9]+_[a-z0-9-]+\\.txt".toRegex()))
        assertTrue(path.pathInTopicDir.matches(SIMPLE_LOCALFILE_PATTERN.toRegex()))
    }

    @Test
    fun testAllCombined() {
        val path = StoragePath.builder()
            .prefix(PREFIX)
            .filename("example.TXT")
            .projectId(PROJECT_ID)
            .subjectId(SUBJECT_ID)
            .topicId(TOPIC_ID)
            .collectPerDay(true)
            .build()
        assertTrue(path.fullPath.matches("$PREFIX/$PROJECT_ID/$SUBJECT_ID/$TOPIC_ID/[0-9]+/[0-9]+_[a-z0-9-]+\\.txt".toRegex()))
        assertTrue(path.pathInTopicDir.matches("[0-9]+/[0-9]+_[a-z0-9-]+\\.txt".toRegex()))
    }

    @Test
    fun testDotsInFilename() {
        val path = StoragePath.builder()
            .filename("example.com.txt")
            .projectId(PROJECT_ID)
            .subjectId(SUBJECT_ID)
            .topicId(TOPIC_ID)
            .build()
        assertTrue(path.fullPath.matches("$PROJECT_ID/$SUBJECT_ID/$TOPIC_ID/[0-9]+_[a-z0-9-]+\\.txt".toRegex()))
        assertTrue(path.pathInTopicDir.matches(SIMPLE_LOCALFILE_PATTERN.toRegex()))
    }

    @Test
    fun testThrowsIllegalArguments() {
        assertThrows<IllegalArgumentException> {
            StoragePath.builder()
                .projectId(PROJECT_ID)
                .subjectId(SUBJECT_ID)
                .topicId(TOPIC_ID)
                .build()
        }
        assertThrows<IllegalArgumentException> {
            StoragePath.builder()
                .filename(FILENAME)
                .subjectId(SUBJECT_ID)
                .topicId(TOPIC_ID)
                .build()
        }
        assertThrows<IllegalArgumentException> {
            StoragePath.builder()
                .filename(FILENAME)
                .projectId(PROJECT_ID)
                .topicId(TOPIC_ID)
                .build()
        }
        assertThrows<IllegalArgumentException> {
            StoragePath.builder()
                .filename(FILENAME)
                .projectId(PROJECT_ID)
                .subjectId(SUBJECT_ID)
                .build()
        }
    }
}
