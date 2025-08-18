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

package org.radarbase.appserver.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("radar.storage.s3")
data class S3StorageProperties(
    var url: String? = null,
    var accessKey: String? = null,
    var secretKey: String? = null,
    var bucketName: String? = null,
    var path: Path = Path(),
) {
    data class Path(
        var prefix: String? = null,
        var collectPerDay: Boolean = false,
    )
}
