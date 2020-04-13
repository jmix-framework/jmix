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
package io.jmix.ui.model.impl;

import io.jmix.core.AppBeans;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.Entity;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

public abstract class AbstractComparator<T> implements Comparator<T> {

    protected boolean asc;

    protected int nullsLast;

    protected Metadata metadata;
    protected MetadataTools metadataTools;

    protected AbstractComparator(boolean asc) {
        // todo PersistenceManager or DbmsFeatures
//        PersistenceManagerClient persistenceManager = AppBeans.get(PersistenceManagerClient.NAME, PersistenceManagerClient.class);
        this.nullsLast = 1; //persistenceManager.isNullsLastSorting() ? 1 : -1;
        this.metadata = AppBeans.get(Metadata.NAME);
        metadataTools = AppBeans.get(MetadataTools.NAME, MetadataTools.class);
        this.asc = asc;
    }

    protected int __compare(Object o1, Object o2) {
        int c = compareAsc(o1, o2);
        return asc ? c : -c;
    }

    protected int compareAsc(Object o1, Object o2) {
        int c;
        if (o1 instanceof String && o2 instanceof String) {
            c = ((String) o1).compareToIgnoreCase((String) o2);
        } else if (o1 instanceof Comparable && o2 instanceof Comparable) {
            c = ((Comparable) o1).compareTo(o2);
        } else if (o1 instanceof Entity && o2 instanceof Entity) {
            MetaClass metaClass = metadata.getClass(o1.getClass());
            Collection<MetaProperty> namePatternProperties = metadataTools.getInstanceNameRelatedProperties(metaClass, true);
            if (namePatternProperties.isEmpty()) {
                String instanceName1 = metadataTools.getInstanceName((Entity) o1);
                String instanceName2 = metadataTools.getInstanceName((Entity) o2);
                c = instanceName1.compareToIgnoreCase(instanceName2);
            } else {
                c = 0;
                for (MetaProperty property : namePatternProperties) {
                    Object v1 = EntityValues.getValue(((Entity) o1), property.getName());
                    Object v2 = EntityValues.getValue(((Entity) o2), property.getName());
                    c = compareAsc(v1, v2);
                    if (c != 0)
                        break;
                }
            }
        } else if (Objects.equals(o1, o2)) {
            c = 0;
        } else if (o1 == null && o2 != null) {
            c = nullsLast;
        } else {
            c = -nullsLast;
        }
        return c;
    }
}
