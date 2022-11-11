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

package io.jmix.core;

import javax.annotation.Nullable;

/**
 * Provides user time zone information.
 * If User entity implements HasTimeZone, {@link io.jmix.core.security.CurrentAuthentication#getTimeZone} should use timezone from a User entity for all datetime operations.
 */
public interface HasTimeZone {

    /**
     * @return time zone id of the user
     */
    @Nullable
    String getTimeZoneId();
}
