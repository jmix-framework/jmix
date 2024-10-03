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

package io.jmix.multitenancy.core;

import io.jmix.core.annotation.TenantId;

/**
 * Should be implemented by the User entity to provide tenant id for current authentication.
 *
 * @deprecated use {@link TenantId} instead
 */
@Deprecated(since = "2.4", forRemoval = true)
public interface AcceptsTenant {

    /**
     * Returns tenant id of the user.
     */
    String getTenantId();
}
