/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.rest.api.service.filter.data;

import io.jmix.core.MessageTools;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MetaClassInfo {
    public String entityName;
    public String ancestor;
    public List<MetaPropertyInfo> properties = new ArrayList<>();

    public MetaClassInfo(MetaClass metaClass,
                         MessageTools messageTools,
                         DatatypeRegistry datatypeRegistry,
                         MetadataTools metadataTools) {
        this.entityName = metaClass.getName();
        this.ancestor = metaClass.getAncestor() != null ? metaClass.getAncestor().getName() : null;
        properties.addAll(metaClass.getProperties().stream()
                .map(metaProperty -> new MetaPropertyInfo(metaProperty, messageTools, datatypeRegistry, metadataTools))
                .collect(Collectors.toList()));
    }

    public String getEntityName() {
        return entityName;
    }

    public String getAncestor() {
        return ancestor;
    }

    public List<MetaPropertyInfo> getProperties() {
        return properties;
    }
}
