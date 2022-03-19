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
import java.util.Objects;

/**
 * Type of operation on entity.
 */
public enum EntityOp {
    READ("read"),
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete");

    private String id;

    EntityOp(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static EntityOp fromId(String id) {
        for (EntityOp it : EntityOp.values()) {
            if (Objects.equals(id, it.getId()))
                return it;
        }
        return null; // unknown id
    }
}