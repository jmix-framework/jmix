/*
 * Copyright 2019 Haulmont.
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
package io.jmix.core.security;

import javax.annotation.Nullable;

/**
 * Exception that is raised on different exceptions related to Row Level Security.
 * <p>
 */
public class RowLevelSecurityException extends RuntimeException {
    private static final long serialVersionUID = -3097861878301424338L;

    private final String entity;
    private final EntityOp entityOp;

    public RowLevelSecurityException(String message, @Nullable String entity) {
        super(message);
        this.entity = entity;
        this.entityOp = null;
    }

    public RowLevelSecurityException(String message, String entity, EntityOp entityOp) {
        super(message);
        this.entity = entity;
        this.entityOp = entityOp;
    }

    @Nullable
    public String getEntity() {
        return entity;
    }

    @Nullable
    public EntityOp getEntityOp() {
        return entityOp;
    }
}
