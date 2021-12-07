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

package io.jmix.search.index.mapping;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.search.index.mapping.impl.IndexMappingConfigurationSerializer;

import java.util.Map;

/**
 * Contains details about all fields
 */
@JsonSerialize(using = IndexMappingConfigurationSerializer.class)
public class IndexMappingConfiguration {

    protected final MetaClass entityMetaClass;

    protected final Map<String, MappingFieldDescriptor> fields;

    protected final DisplayedNameDescriptor displayedNameDescriptor;

    public IndexMappingConfiguration(MetaClass entityMetaClass, Map<String, MappingFieldDescriptor> fields, DisplayedNameDescriptor displayedNameDescriptor) {
        this.entityMetaClass = entityMetaClass;
        this.fields = fields;
        this.displayedNameDescriptor = displayedNameDescriptor;
    }

    public Map<String, MappingFieldDescriptor> getFields() {
        return fields;
    }

    public MetaClass getEntityMetaClass() {
        return entityMetaClass;
    }

    public DisplayedNameDescriptor getDisplayedNameDescriptor() {
        return displayedNameDescriptor;
    }
}
