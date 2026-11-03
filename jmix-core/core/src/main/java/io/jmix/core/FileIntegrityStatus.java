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
 * The outcome of a {@link FileIntegrityChecker} check.
 * <p>
 * Constants may be added in later versions, so code that handles the status should have a fallback branch.
 */
@NullMarked
public enum FileIntegrityStatus {

    /**
     * The digest of the file equals the one kept in the {@link FileRef}.
     */
    VALID,

    /**
     * The digest of the file differs from the one kept in the {@link FileRef}: the file is corrupted or substituted.
     * A digest that is malformed also leads to this status.
     */
    MODIFIED,

    /**
     * The file does not exist in the storage.
     */
    MISSING,

    /**
     * The file exists, but the {@link FileRef} has no digest to compare it with: the file was saved before the
     * digest was introduced, or by a storage that does not keep it.
     */
    NO_REFERENCE
}
