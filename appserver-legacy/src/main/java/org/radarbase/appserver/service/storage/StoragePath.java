package org.radarbase.appserver.service.storage;

import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
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

    private transient String pathInBucket;
    private transient String pathInTopicDirectory;

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

        private transient String pathPrefix = "";
        private transient String file = "";
        private transient boolean doCollectPerDay = false;
        private transient String project = "";
        private transient String subject = "";
        private transient String topic = "";
        private transient String folderPattern = "yyyyMMdd";
        private transient String filePattern = "yyyyMMddHHmmss";
        private transient String dirSep = "/";

        public Builder filename(String filename) {
            this.file = filename;
            return this;
        }

        public Builder prefix(String prefix) {
            Assert.notNull(prefix, "Prefix must not be null");
            this.pathPrefix = prefix;
            return this;
        }

        public Builder collectPerDay(boolean collectPerDay) {
            this.doCollectPerDay = collectPerDay;
            return this;
        }

        public Builder projectId(String projectId) {
            Assert.notNull(projectId, "Project Id must not be null");
            this.project = projectId;
            return this;
        }

        public Builder subjectId(String subjectId) {
            Assert.notNull(subjectId, "Subject Id must not be null");
            this.subject = subjectId;
            return this;
        }

        public Builder topicId(String topicId) {
            Assert.notNull(topicId, "Topic Id must not be null");
            this.topic = topicId;
            return this;
        }

        public Builder dayFolderPattern(String dayFolderPattern) {
            Assert.notNull(dayFolderPattern, "Day folder pattern must not be null");
            this.folderPattern = dayFolderPattern;
            return this;
        }

        public Builder fileTimestampPattern(String fileTimestampPattern) {
            Assert.notNull(fileTimestampPattern, "File timestamp pattern must not be null");
            this.filePattern = fileTimestampPattern;
            return this;
        }

        public StoragePath build() {
            Assert.isTrue(!file.isBlank(), "Filename must be set.");
            Assert.isTrue(!project.isBlank(), "Project Id must be set.");
            Assert.isTrue(!subject.isBlank(), "Subject Id must be set.");
            Assert.isTrue(!topic.isBlank(), "Topic Id must be set.");

            String pathInTopicDir = buildPathInTopicDir();

            String fullPath = Stream.of(
                    this.pathPrefix,
                    project,
                    subject,
                    topic,
                    pathInTopicDir
            ).filter(s -> !s.isBlank())
            .collect(Collectors.joining(this.dirSep));

            return new StoragePath(fullPath, pathInTopicDir);
        }

        private String buildPathInTopicDir() {
            return Stream.of(
                    this.doCollectPerDay ? getDayFolder() : "",
                    // Storing files under their original filename is a security risk, as it can be used to
                    // overwrite existing files. We generate a random filename server-side to mitigate this risk.
                    // See https://owasp.org/www-community/vulnerabilities/Unrestricted_File_Upload
                    generateRandomFilename(this.file)
                ).filter(s -> !s.isBlank())
                .collect(Collectors.joining(this.dirSep));
        }

        private String generateRandomFilename(String originalFilename) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(this.filePattern));
            return timestamp + "_" + UUID.randomUUID() + getFileExtension(originalFilename);
        }

        private String getDayFolder() {
            return LocalDate.now().format(DateTimeFormatter.ofPattern(this.folderPattern));
        }

        private String getFileExtension(String originalFilename) {
            int lastDot = originalFilename.lastIndexOf('.');
            if (lastDot < 0) {
                return "";
            } else {
                return originalFilename.substring(lastDot).toLowerCase(Locale.ENGLISH);
            }
        }

    }

}
