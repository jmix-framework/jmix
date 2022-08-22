/*
 * Copyright 2022 Haulmont.
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

package io.jmix.securityflowui.model;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import javax.annotation.Nullable;


public enum ResourcePolicyType implements EnumClass<String> {

    VIEW(io.jmix.security.model.ResourcePolicyType.SCREEN),
    MENU(io.jmix.security.model.ResourcePolicyType.MENU),
    ENTITY(io.jmix.security.model.ResourcePolicyType.ENTITY),
    ENTITY_ATTRIBUTE(io.jmix.security.model.ResourcePolicyType.ENTITY_ATTRIBUTE),
    SPECIFIC(io.jmix.security.model.ResourcePolicyType.SPECIFIC),
    GRAPHQL(io.jmix.security.model.ResourcePolicyType.GRAPHQL);

    private String id;

    ResourcePolicyType(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static ResourcePolicyType fromId(String id) {
        for (ResourcePolicyType at : ResourcePolicyType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}