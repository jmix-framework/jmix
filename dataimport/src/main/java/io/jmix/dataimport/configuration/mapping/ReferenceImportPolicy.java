/*
 * Copyright 2021 Haulmont.
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

package io.jmix.dataimport.configuration.mapping;

/**
 * <br>
 * Available policies for import of reference property:
 * <ul>
 *     <li>Create: the reference is always created. Existing reference is not searched.</li>
 *     <li>Create if missing: the reference is created if an existing reference not found.</li>
 *     <li>Ignore if missing: the reference is not created if an existing reference not found.</li>
 *     <li>Fail if missing: if an existing reference not found, the import of entity fails.</li>
 * </ul>
 */
public enum ReferenceImportPolicy {
    /**
     * Reference is always created. Existing reference is not loaded
     */
    CREATE,
    /**
     * Reference is created if an existing reference not found
     */
    CREATE_IF_MISSING,
    /**
     * Reference is not created if an existing reference not found
     */
    IGNORE_IF_MISSING,
    /**
     * If an existing reference not found, the import of entity fails
     */
    FAIL_IF_MISSING
}
