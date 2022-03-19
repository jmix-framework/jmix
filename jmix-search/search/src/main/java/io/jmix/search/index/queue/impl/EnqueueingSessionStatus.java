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

package io.jmix.search.index.queue.impl;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import javax.annotation.Nullable;

/**
 * Status of session.
 */
public enum EnqueueingSessionStatus implements EnumClass<String> {

    /**
     * Session should be processed normal way.
     */
    ACTIVE("ACTIVE"),
    /**
     * Session should be skipped.
     */
    SUSPENDED("SUSPENDED");

    private final String id;

    EnqueueingSessionStatus(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static EnqueueingSessionStatus fromId(String id) {
        for (EnqueueingSessionStatus at : EnqueueingSessionStatus.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}