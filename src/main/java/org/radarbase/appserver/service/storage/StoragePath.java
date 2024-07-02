package org.radarbase.appserver.service.storage;

import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents path on Object Storage for uploaded files.
 * <p>
 * The storage path is constructed to include required arguments (projectId, subjectId, topicId and filename)
 * and optional arguments (prefix, collectPerDay, folderTimestampPattern, fileTimestampPattern). The path will follow
 * the format: prefix/projectId/subjectId/topicId/[day folder]/timestamp_filename.extension.
 * The day folder is included if collectPerDay is set to true. File extensions are converted to lowercase.
 * </p>
 *
 * <p>
 * Usage example:
 * </p>
 * <pre>
 * StoragePath path = StoragePath.builder()
 *     .prefix("uploads")
 *     .projectId("project1")
 *     .subjectId("subjectA")
 *     .topicId("topicX")
 *     .collectPerDay(true)
 *     .filename("example.txt")
 *     .build();
 *
 * System.out.println(path.getFullPath();
 * 'uploads/project1/subjectA/topicX/20220101/20220101_example.txt'
 *
 * System.out.println(path.getPathInTopicDir()
 * '20220101/20220101_example.txt'
 * </pre>
 */
public class StoragePath {

    private String pathInBucket;
    private String pathInTopicDirectory;

    public StoragePath (String pathInBucket, String pathInTopicDirectory) {
        this.pathInBucket = pathInBucket;
        this.pathInTopicDirectory = pathInTopicDirectory;
    }

    public String getFullPath() {
        return pathInBucket;
    }

    public String getPathInTopicDir() {
        return pathInTopicDirectory;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String prefix = "";
        private String filename = "";
        private boolean collectPerDay = false;
        private String projectId = "";
        private String subjectId = "";
        private String topicId = "";
        private String folderTimestampPattern = "yyyyMMdd";
        private String fileTimestampPattern = "yyyyMMddHHmmss";
        private String dirSep = "/";

        public Builder filename(String filename) {
            this.filename = filename;
            return this;
        }

        public Builder prefix(String prefix) {
            Assert.notNull(prefix, "Prefix must not be null");
            this.prefix = prefix;
            return this;
        }

        public Builder collectPerDay(boolean collectPerDay) {
            this.collectPerDay = collectPerDay;
            return this;
        }

        public Builder projectId(String projectId) {
            Assert.notNull(projectId, "Project Id must not be null");
            this.projectId = projectId;
            return this;
        }

        public Builder subjectId(String subjectId) {
            Assert.notNull(subjectId, "Subject Id must not be null");
            this.subjectId = subjectId;
            return this;
        }

        public Builder topicId(String topicId) {
            Assert.notNull(topicId, "Topic Id must not be null");
            this.topicId = topicId;
            return this;
        }

        public Builder dayFolderPattern(String dayFolderPattern) {
            Assert.notNull(dayFolderPattern, "Day folder pattern must not be null");
            this.folderTimestampPattern = dayFolderPattern;
            return this;
        }

        public Builder fileTimestampPattern(String fileTimestampPattern) {
            Assert.notNull(fileTimestampPattern, "File timestamp pattern must not be null");
            this.fileTimestampPattern = fileTimestampPattern;
            return this;
        }

        public StoragePath build() {
            Assert.isTrue(!filename.isBlank(), "Filename must be set.");
            Assert.isTrue(!projectId.isBlank(), "Project Id must be set.");
            Assert.isTrue(!subjectId.isBlank(), "Subject Id must be set.");
            Assert.isTrue(!topicId.isBlank(), "Topic Id must be set.");

            String pathInTopicDir = Stream.of(
                    this.collectPerDay ? getDayFolder() : "",
                    // Storing files under their original filename is a security risk, as it can be used to
                    // overwrite existing files. We generate a random filename server-side to mitigate this risk.
                    // See https://owasp.org/www-community/vulnerabilities/Unrestricted_File_Upload
                    generateRandomFilename(this.filename)
            ).filter(s -> !s.isBlank())
            .collect(Collectors.joining(this.dirSep));

            String fullPath = Stream.of(
                    this.prefix,
                    projectId,
                    subjectId,
                    topicId,
                    pathInTopicDir
            ).filter(s -> !s.isBlank())
            .collect(Collectors.joining(this.dirSep));

            return new StoragePath(fullPath, pathInTopicDir);
        }

        private String generateRandomFilename(String originalFilename) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(this.fileTimestampPattern));
            return timestamp + "_" + UUID.randomUUID() + getFileExtension(originalFilename);
        }

        private String getDayFolder() {
            return LocalDate.now().format(DateTimeFormatter.ofPattern(this.folderTimestampPattern));
        }

        private String getFileExtension(String originalFilename) {
            int lastDot = originalFilename.lastIndexOf('.');
            if (lastDot < 0) {
                return "";
            } else {
                return originalFilename.substring(lastDot).toLowerCase();
            }
        }

    }

}
