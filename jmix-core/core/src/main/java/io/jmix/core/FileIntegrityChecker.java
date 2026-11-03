/*
 * Copyright 2026 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.core;

import org.jspecify.annotations.NullMarked;

/**
 * Checks on demand that a file saved in a {@link FileStorage} has not been corrupted or substituted.
 * <p>
 * The file is compared with the {@link FileDigest} kept in its {@link FileRef}. The check has no side effects:
 * it does not change the file reference, the entity or the storage, and may run concurrently.
 */
@NullMarked
public interface FileIntegrityChecker {

    /**
     * Checks the file denoted by the file reference.
     * <p>
     * The check reads the file, unless the storage has a cheaper way to calculate its digest.
     *
     * @param reference file reference
     * @return the result of the check
     * @throws IllegalArgumentException if there is no storage with the name from the file reference
     * @throws FileStorageException     if the file exists, but the storage fails to read it
     */
    FileIntegrityCheckResult check(FileRef reference);
}
