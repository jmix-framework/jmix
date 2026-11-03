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

package io.jmix.core.impl;

import io.jmix.core.FileDigest;
import io.jmix.core.FileIntegrityCheckResult;
import io.jmix.core.FileIntegrityChecker;
import io.jmix.core.FileIntegrityStatus;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageException;
import io.jmix.core.FileStorageLocator;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

@NullMarked
@Component("core_FileIntegrityChecker")
public class FileIntegrityCheckerImpl implements FileIntegrityChecker {

    @Autowired
    protected FileStorageLocator fileStorageLocator;

    @Override
    public FileIntegrityCheckResult check(FileRef reference) {
        checkNotNullArgument(reference);

        FileStorage fileStorage = fileStorageLocator.getByName(reference.getStorageName());
        FileDigest expected = FileDigest.fromFileRef(reference);

        if (expected == null) {
            FileIntegrityStatus status = fileStorage.fileExists(reference)
                    ? FileIntegrityStatus.NO_REFERENCE
                    : FileIntegrityStatus.MISSING;
            return new FileIntegrityCheckResult(reference, status, null, null);
        }

        FileDigest actual;
        try {
            actual = fileStorage.calculateDigest(reference, expected.getAlgorithm());
        } catch (FileStorageException e) {
            // A storage may report a missing file with any exception type, so the file is looked up explicitly.
            if (fileStorage.fileExists(reference)) {
                throw e;
            }
            return new FileIntegrityCheckResult(reference, FileIntegrityStatus.MISSING, expected, null);
        }

        FileIntegrityStatus status = expected.equals(actual)
                ? FileIntegrityStatus.VALID
                : FileIntegrityStatus.MODIFIED;
        return new FileIntegrityCheckResult(reference, status, expected, actual);
    }
}
