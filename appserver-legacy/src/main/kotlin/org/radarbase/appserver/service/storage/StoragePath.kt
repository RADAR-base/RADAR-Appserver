/*
 *
 *  Copyright 2024 The Hyve
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.radarbase.appserver.service.storage

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

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

data class StoragePath(val pathInBucket: String, val pathInTopicDir: String) {

    companion object {
        fun builder() = Builder()
    }

    class Builder {
        private var pathPrefix: String = ""
        private var file: String = ""
        private var doCollectPerDay: Boolean = false
        private var project: String = ""
        private var subject: String = ""
        private var topic: String = ""
        private var folderPattern: String = "yyyyMMdd"
        private var filePattern: String = "yyyyMMddHHmmss"
        private val dirSep: String = "/"

        fun filename(filename: String) = apply {
            file = filename
        }

        fun prefix(prefix: String) = apply {
            pathPrefix = prefix
        }

        fun collectPerDay(collectPerDay: Boolean) = apply {
            doCollectPerDay = collectPerDay
        }

        fun projectId(projectId: String) = apply {
            project = projectId
        }

        fun subjectId(subjectId: String) = apply {
            subject = subjectId
        }

        fun topicId(topicId: String) = apply {
            topic = topicId
        }

        fun dayFolderPattern(pattern: String) = apply {
            folderPattern = pattern
        }

        fun fileTimestampPattern(pattern: String) = apply {
            filePattern = pattern
        }

        fun build(): StoragePath {
            require(file.isNotBlank()) { "Filename must be set." }
            require(project.isNotBlank()) { "Project Id must be set." }
            require(subject.isNotBlank()) { "Subject Id must be set." }
            require(topic.isNotBlank()) { "Topic Id must be set." }

            val pathInTopicDir = buildPathInTopicDir()
            val fullPath = listOf(pathPrefix, project, subject, topic, pathInTopicDir)
                .filter { it.isNotBlank() }
                .joinToString(dirSep)

            return StoragePath(fullPath, pathInTopicDir)
        }

        private fun buildPathInTopicDir(): String {
            val dayFolder = if (doCollectPerDay) getDayFolder() else ""
            return listOf(dayFolder, generateRandomFilename(file))
                .filter { it.isNotBlank() }
                .joinToString(dirSep)
        }

        private fun generateRandomFilename(originalFilename: String): String {
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(filePattern))
            return "${timestamp}_${UUID.randomUUID()}${getFileExtension(originalFilename)}"
        }

        private fun getDayFolder(): String = LocalDate.now().format(DateTimeFormatter.ofPattern(folderPattern))

        private fun getFileExtension(originalFilename: String): String {
            val lastDot = originalFilename.lastIndexOf('.')
            return if (lastDot < 0) "" else originalFilename.substring(lastDot).lowercase(Locale.ENGLISH)
        }
    }
}
