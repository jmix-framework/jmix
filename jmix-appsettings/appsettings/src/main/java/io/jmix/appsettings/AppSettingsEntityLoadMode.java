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

package io.jmix.appsettings;

/**
 * Defines how application settings entity should be resolved for the current context.
 */
public enum AppSettingsEntityLoadMode {

    /**
     * Loads settings for reading.
     * <p>
     * Use this mode when effective settings values are needed. For tenant-aware settings it returns the tenant-specific
     * record if it exists, otherwise falls back to the global record.
     */
    FOR_READ,

    /**
     * Loads settings for saving.
     * <p>
     * Use this mode when the returned entity will be updated and saved. For tenant-aware settings it returns the
     * tenant-specific record if it exists, otherwise creates a new tenant-specific record instead of updating the
     * global fallback record.
     */
    FOR_SAVE
}
