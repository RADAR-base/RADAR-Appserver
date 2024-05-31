/*
 *
 *  *  Copyright 2024 The Hyve
 *  *
 *  *  Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *
 */

package org.radarbase.appserver.service.storage;

import java.util.UUID;

public abstract class RandomUuidFilenameStorageService {

    // Storing files under their original filename is a security risk, as it can be used to
    // overwrite existing files. This method generates a random filename to mitigate this risk.
    // See https://owasp.org/www-community/vulnerabilities/Unrestricted_File_Upload
    String generateRandomFilename(String originalFilename) {
        return UUID.randomUUID() + getFileExtension(originalFilename);
    }

    private String getFileExtension(String originalFilename) {
        int lastDot = originalFilename.lastIndexOf('.');
        if (lastDot < 0) {
            return "";
        } else {
            return originalFilename.substring(lastDot);
        }
    }

}
