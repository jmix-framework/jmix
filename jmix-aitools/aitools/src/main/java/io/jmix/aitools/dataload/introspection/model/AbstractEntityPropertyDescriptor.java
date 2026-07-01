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

/**
 * Base {@link EntityPropertyDescriptor} implementation holding the attributes common to all property kinds.
 */
public abstract class AbstractEntityPropertyDescriptor implements EntityPropertyDescriptor {

    protected String name;

    protected List<String> localizedNames;

    protected String javaType;

    protected String propertyType;

    @Nullable
    protected Boolean identifier;

    protected Boolean persistent;

    protected Boolean mandatory;

    @Nullable
    protected String comment;

    public AbstractEntityPropertyDescriptor(String name,
                                            List<String> localizedNames,
                                            String javaType,
                                            String propertyType,
                                            @Nullable Boolean identifier,
                                            Boolean persistent,
                                            Boolean mandatory,
                                            @Nullable String comment) {
        this.name = name;
        this.localizedNames = localizedNames;
        this.javaType = javaType;
        this.propertyType = propertyType;
        this.identifier = identifier;
        this.persistent = persistent;
        this.mandatory = mandatory;
        this.comment = comment;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getLocalizedNames() {
        return localizedNames;
    }

    @Override
    public String getJavaType() {
        return javaType;
    }

    @Override
    public String getPropertyType() {
        return propertyType;
    }

    @Nullable
    @Override
    public Boolean getIdentifier() {
        return identifier;
    }

    @Override
    public Boolean getPersistent() {
        return persistent;
    }

    @Override
    public Boolean getMandatory() {
        return mandatory;
    }

    @Nullable
    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + fieldsToString() + '}';
    }

    protected String fieldsToString() {
        return "name='" + name + '\'' +
                ", localizedNames=" + localizedNames +
                ", javaType='" + javaType + '\'' +
                ", propertyType='" + propertyType + '\'' +
                ", identifier=" + identifier +
                ", persistent=" + persistent +
                ", mandatory=" + mandatory +
                ", comment='" + comment + '\'';
    }
}
