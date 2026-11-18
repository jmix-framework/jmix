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
import org.jspecify.annotations.Nullable;

import java.io.Serializable;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * The result of a {@link FileIntegrityChecker} check of one file.
 */
@NullMarked
public class FileIntegrityCheckResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private final FileRef fileRef;
    private final FileIntegrityStatus status;
    private final @Nullable FileDigest expected;
    private final @Nullable FileDigest actual;

    /**
     * Creates a result.
     *
     * @param fileRef  the checked file reference
     * @param status   the outcome of the check
     * @param expected the digest kept in the file reference, {@code null} if there is none
     * @param actual   the digest calculated from the stored file, {@code null} if it was not calculated
     */
    public FileIntegrityCheckResult(FileRef fileRef, FileIntegrityStatus status,
                                    @Nullable FileDigest expected, @Nullable FileDigest actual) {
        checkNotNullArgument(fileRef);
        checkNotNullArgument(status);
        this.fileRef = fileRef;
        this.status = status;
        this.expected = expected;
        this.actual = actual;
    }

    /**
     * Returns the checked file reference.
     */
    public FileRef getFileRef() {
        return fileRef;
    }

    /**
     * Returns the outcome of the check.
     */
    public FileIntegrityStatus getStatus() {
        return status;
    }

    /**
     * Returns the digest kept in the file reference or {@code null} if there is none.
     */
    public @Nullable FileDigest getExpected() {
        return expected;
    }

    /**
     * Returns the digest calculated from the stored file or {@code null} if it was not calculated, that is when the
     * file is missing or the file reference has no digest.
     */
    public @Nullable FileDigest getActual() {
        return actual;
    }
}
