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

package io.jmix.multitenancy;

/**
 * Thrown when the tenant of a record is asked for and the tenant attribute has not been fetched.
 * <p>
 * The caller can recover: load the record with the attribute in its fetch plan and ask again. That is why this
 * is a type of its own rather than an {@code IllegalArgumentException}, which in {@link Multitenancy} means an
 * entity with no tenant attribute at all, and that one is fixed in the code rather than retried.
 * <p>
 * A caller that can live without the answer asks {@link Multitenancy#isTenantIdReadable(Object)} first.
 */
public class TenantIdNotLoadedException extends RuntimeException {

    public TenantIdNotLoadedException(String message) {
        super(message);
    }
}
