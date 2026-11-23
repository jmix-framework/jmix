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
 *
 * @param fileRef  the checked file reference
 * @param status   the outcome of the check
 * @param expected the digest kept in the file reference, {@code null} if there is none
 * @param actual   the digest calculated from the stored file, {@code null} if it was not calculated, that is when the
 *                 file is missing or the file reference has no digest
 */
@NullMarked
public record FileIntegrityCheckResult(FileRef fileRef,
                                       FileIntegrityStatus status,
                                       @Nullable FileDigest expected,
                                       @Nullable FileDigest actual) implements Serializable {

    public FileIntegrityCheckResult {
        checkNotNullArgument(fileRef);
        checkNotNullArgument(status);
    }
}
