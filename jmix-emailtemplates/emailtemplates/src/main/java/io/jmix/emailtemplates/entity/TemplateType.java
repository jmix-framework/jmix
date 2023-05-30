/*
 * Copyright 2020 Haulmont.
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

package io.jmix.emailtemplates.entity;


import io.jmix.core.metamodel.datatype.impl.EnumClass;

import org.springframework.lang.Nullable;


public enum TemplateType implements EnumClass<String> {

    JSON("json"),
    REPORT("report");

    private String id;

    TemplateType(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static TemplateType fromId(String id) {
        for (TemplateType at : TemplateType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}