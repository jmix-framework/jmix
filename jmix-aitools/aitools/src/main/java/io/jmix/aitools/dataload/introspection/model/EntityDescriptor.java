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

package io.jmix.aitools.dataload.introspection.model;

import org.jspecify.annotations.Nullable;

import java.util.List;

public class EntityDescriptor {

    protected String name;
    protected List<String> localizedNames;

    protected List<EntityPropertyDescriptor> properties;

    protected String comment;

    public EntityDescriptor(String name,
                            List<String> localizedNames,
                            List<EntityPropertyDescriptor> properties,
                            @Nullable String comment) {
        this.name = name;
        this.localizedNames = localizedNames;
        this.properties = properties;
        this.comment = comment;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public String getComment() {
        return comment;
    }

    public List<String> getLocalizedNames() {
        return localizedNames;
    }

    public List<EntityPropertyDescriptor> getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "name='" + name + '\'' +
                ", comment='" + comment + '\'' +
                ", localizedNames=" + localizedNames +
                ", properties=" + properties +
                '}';
    }
}
