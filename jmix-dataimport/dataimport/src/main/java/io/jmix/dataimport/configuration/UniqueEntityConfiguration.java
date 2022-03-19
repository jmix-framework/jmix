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

package io.jmix.dataimport.configuration;

import java.util.List;

/**
 * Allows configuring how to process a case if the entity with the same values of the particular properties already exists.
 * To create an instance of UniqueEntityConfiguration, it is required to specify:
 * <ul>
 *     <li>Entity property names: names of the properties by which values the duplicate entity will be searched</li>
 *     <li>Duplicate entity policy: policy to process a found duplicate</li>
 * </ul>
 */
public class UniqueEntityConfiguration {
    protected List<String> entityPropertyNames;
    protected DuplicateEntityPolicy duplicateEntityPolicy;

    public UniqueEntityConfiguration(List<String> entityPropertyNames, DuplicateEntityPolicy duplicateEntityPolicy) {
        this.entityPropertyNames = entityPropertyNames;
        this.duplicateEntityPolicy = duplicateEntityPolicy;
    }

    public List<String> getEntityPropertyNames() {
        return entityPropertyNames;
    }

    public DuplicateEntityPolicy getDuplicateEntityPolicy() {
        return duplicateEntityPolicy;
    }
}
