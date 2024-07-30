package org.radarbase.appserver.service.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
class StoragePathTest {

    private static final String PREFIX = "prefix";
    private static final String FILENAME = "example.txt";
    private static final String PROJECT_ID = "project1";
    private static final String SUBJECT_ID = "subjectA";
    private static final String TOPIC_ID = "topicX";

    private static final String SIMPLE_LOCALFILE_PATTERN = "[0-9]+_[a-z0-9-]+\\.txt";

    @Test
    void minimalValidPath() {
        StoragePath path = StoragePath.builder()
            .filename(FILENAME)
            .projectId(PROJECT_ID)
            .subjectId(SUBJECT_ID)
            .topicId(TOPIC_ID)
            .build();
        assertTrue(path.getFullPath().matches("project1/subjectA/topicX/[0-9]+_[a-z0-9-]+\\.txt"));
        assertTrue(path.getPathInTopicDir().matches(SIMPLE_LOCALFILE_PATTERN));
    }

    @Test
    void includeDayFolder() {
        StoragePath path = StoragePath.builder()
            .filename(FILENAME)
            .projectId(PROJECT_ID)
            .subjectId(SUBJECT_ID)
            .topicId(TOPIC_ID)
            .collectPerDay(true)
            .build();
        assertTrue(path.getFullPath().matches("project1/subjectA/topicX/[0-9]+/[0-9]+_[a-z0-9-]+\\.txt"));
        assertTrue(path.getPathInTopicDir().matches("[0-9]+/[0-9]+_[a-z0-9-]+\\.txt"));
    }

    @Test
    void includePrefix() {
        StoragePath path = StoragePath.builder()
            .prefix(PREFIX)
            .filename(FILENAME)
            .projectId(PROJECT_ID)
            .subjectId(SUBJECT_ID)
            .topicId(TOPIC_ID)
            .build();
        assertTrue(path.getFullPath().matches("prefix/project1/subjectA/topicX/[0-9]+_[a-z0-9-]+\\.txt"));
        assertTrue(path.getPathInTopicDir().matches(SIMPLE_LOCALFILE_PATTERN));
    }

    @Test
    void testLowercaseExtension() {
        StoragePath path = StoragePath.builder()
            .filename("example.TXT")
            .projectId(PROJECT_ID)
            .subjectId(SUBJECT_ID)
            .topicId(TOPIC_ID)
            .build();
        assertTrue(path.getFullPath().matches("project1/subjectA/topicX/[0-9]+_[a-z0-9-]+\\.txt"));
        assertTrue(path.getPathInTopicDir().matches(SIMPLE_LOCALFILE_PATTERN));
    }

    @Test
    void testAllCombined() {
        StoragePath path = StoragePath.builder()
            .prefix(PREFIX)
            .filename("example.TXT")
            .projectId(PROJECT_ID)
            .subjectId(SUBJECT_ID)
            .topicId(TOPIC_ID)
            .collectPerDay(true)
            .build();
        assertTrue(path.getFullPath().matches("prefix/project1/subjectA/topicX/[0-9]+/[0-9]+_[a-z0-9-]+\\.txt"));
        assertTrue(path.getPathInTopicDir().matches("[0-9]+/[0-9]+_[a-z0-9-]+\\.txt"));
    }

    @Test
    void testDotsInFilename() {
        StoragePath path = StoragePath.builder()
            .filename("example.com.txt")
            .projectId(PROJECT_ID)
            .subjectId(SUBJECT_ID)
            .topicId(TOPIC_ID)
            .build();
        assertTrue(path.getFullPath().matches("project1/subjectA/topicX/[0-9]+_[a-z0-9-]+\\.txt"));
        assertTrue(path.getPathInTopicDir().matches(SIMPLE_LOCALFILE_PATTERN));
    }

    @Test
    void testThrowsIllegalArguments() {
        assertThrows(IllegalArgumentException.class, () -> StoragePath.builder()
            .projectId(PROJECT_ID)
            .subjectId(SUBJECT_ID)
            .topicId(TOPIC_ID)
            .build());
        assertThrows(IllegalArgumentException.class, () -> StoragePath.builder()
            .filename(FILENAME)
            .subjectId(SUBJECT_ID)
            .topicId(TOPIC_ID)
            .build());
        assertThrows(IllegalArgumentException.class, () -> StoragePath.builder()
            .filename(FILENAME)
            .projectId(PROJECT_ID)
            .topicId(TOPIC_ID)
            .build());
        assertThrows(IllegalArgumentException.class, () -> StoragePath.builder()
            .filename(FILENAME)
            .projectId(PROJECT_ID)
            .subjectId(SUBJECT_ID)
            .build());
    }

}