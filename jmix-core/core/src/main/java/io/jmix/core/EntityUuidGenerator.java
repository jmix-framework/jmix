/*
 * Copyright 2025 Haulmont.
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

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Generates UUID for using as entity identifiers.
 */
@Component("core_EntityIdGenerator")
public class EntityUuidGenerator implements EnvironmentAware {

    private boolean useLegacyUuid;

    /**
     * @return new UUID
     */
    public UUID generate() {
        if (useLegacyUuid)
            return UuidProvider.createUuid();
        else
            return UuidProvider.createUuidV7();
    }

    @Override
    public void setEnvironment(Environment environment) {
        String property = environment.getProperty("jmix.core.legacy-entity-uuid");
        useLegacyUuid = Boolean.parseBoolean(property);
    }
}
