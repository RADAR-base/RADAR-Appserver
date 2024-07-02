package org.radarbase.appserver.service.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
class StoragePathTest {

    @Test
    void minimalValidPath() {
        StoragePath path = StoragePath.builder()
            .filename("example.txt")
            .projectId("project1")
            .subjectId("subjectA")
            .topicId("topicX")
            .build();
        assertTrue(path.getFullPath().matches("project1/subjectA/topicX/[0-9]+_[a-z0-9-]+\\.txt"));
        assertTrue(path.getPathInTopicDir().matches("[0-9]+_[a-z0-9-]+\\.txt"));
    }

    @Test
    void includeDayFolder() {
        StoragePath path = StoragePath.builder()
            .filename("example.txt")
            .projectId("project1")
            .subjectId("subjectA")
            .topicId("topicX")
            .collectPerDay(true)
            .build();
        assertTrue(path.getFullPath().matches("project1/subjectA/topicX/[0-9]+/[0-9]+_[a-z0-9-]+\\.txt"));
        assertTrue(path.getPathInTopicDir().matches("[0-9]+/[0-9]+_[a-z0-9-]+\\.txt"));
    }

    @Test
    void includePrefix() {
        StoragePath path = StoragePath.builder()
            .prefix("prefix")
            .filename("example.txt")
            .projectId("project1")
            .subjectId("subjectA")
            .topicId("topicX")
            .build();
        assertTrue(path.getFullPath().matches("prefix/project1/subjectA/topicX/[0-9]+_[a-z0-9-]+\\.txt"));
        assertTrue(path.getPathInTopicDir().matches("[0-9]+_[a-z0-9-]+\\.txt"));
    }

    @Test
    void testLowercaseExtension() {
        StoragePath path = StoragePath.builder()
            .filename("example.TXT")
            .projectId("project1")
            .subjectId("subjectA")
            .topicId("topicX")
            .build();
        assertTrue(path.getFullPath().matches("project1/subjectA/topicX/[0-9]+_[a-z0-9-]+\\.txt"));
        assertTrue(path.getPathInTopicDir().matches("[0-9]+_[a-z0-9-]+\\.txt"));
    }

    @Test
    void testAllCombined() {
        StoragePath path = StoragePath.builder()
            .prefix("prefix")
            .filename("example.TXT")
            .projectId("project1")
            .subjectId("subjectA")
            .topicId("topicX")
            .collectPerDay(true)
            .build();
        assertTrue(path.getFullPath().matches("prefix/project1/subjectA/topicX/[0-9]+/[0-9]+_[a-z0-9-]+\\.txt"));
        assertTrue(path.getPathInTopicDir().matches("[0-9]+/[0-9]+_[a-z0-9-]+\\.txt"));
    }

    @Test
    void testDotsInFilename() {
        StoragePath path = StoragePath.builder()
            .filename("example.com.txt")
            .projectId("project1")
            .subjectId("subjectA")
            .topicId("topicX")
            .build();
        assertTrue(path.getFullPath().matches("project1/subjectA/topicX/[0-9]+_[a-z0-9-]+\\.txt"));
        assertTrue(path.getPathInTopicDir().matches("[0-9]+_[a-z0-9-]+\\.txt"));
    }

    @Test
    void testThrowsIllegalArguments() {
        assertThrows(IllegalArgumentException.class, () -> StoragePath.builder()
            .projectId("project1")
            .subjectId("subjectA")
            .topicId("topicX")
            .build());
        assertThrows(IllegalArgumentException.class, () -> StoragePath.builder()
            .filename("example.txt")
            .subjectId("subjectA")
            .topicId("topicX")
            .build());
        assertThrows(IllegalArgumentException.class, () -> StoragePath.builder()
            .filename("example.txt")
            .projectId("project1")
            .topicId("topicX")
            .build());
        assertThrows(IllegalArgumentException.class, () -> StoragePath.builder()
            .filename("example.txt")
            .projectId("project1")
            .subjectId("subjectA")
            .build());
    }

}