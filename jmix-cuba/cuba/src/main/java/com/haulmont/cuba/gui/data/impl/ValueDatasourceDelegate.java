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

package com.haulmont.cuba.gui.data.impl;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.ValueLoadContext;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.impl.keyvalue.KeyValueMetaClass;
import io.jmix.core.impl.keyvalue.KeyValueMetaClassFactory;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.Enumeration;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.data.PersistenceHints;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public class ValueDatasourceDelegate {

    protected String storeName;

    protected String idName;

    protected CollectionDatasourceImpl ds;

    public ValueDatasourceDelegate(CollectionDatasourceImpl datasource) {
        this.ds = datasource;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public void setIdName(String name) {
        this.idName = name;
    }

    public void addProperty(String name) {
        checkNotNullArgument(name, "name is null");

        AppBeans.get(KeyValueMetaClassFactory.class)
                .configurer((KeyValueMetaClass) ds.metaClass).addProperty(name, String.class);
    }

    public void addProperty(String name, Class type) {
        checkNotNullArgument(name, "name is null");
        checkNotNullArgument(type, "type is null");

        AppBeans.get(KeyValueMetaClassFactory.class)
                .configurer((KeyValueMetaClass) ds.metaClass).addProperty(name, type);
    }

    public void addProperty(String name, Datatype datatype) {
        checkNotNullArgument(name, "name is null");
        checkNotNullArgument(datatype, "type is null");

        AppBeans.get(KeyValueMetaClassFactory.class)
                .configurer((KeyValueMetaClass) ds.metaClass).addProperty(name, datatype);
    }

    protected void loadData(Map<String, Object> params) {
        if (ds.needLoading()) {
            ValueLoadContext context = beforeLoadValues(params);
            if (context == null) {
                return;
            }
            try {
                List<KeyValueEntity> entities = ds.dataSupplier.loadValues(context);

                afterLoadValues(params, context, entities);
            } catch (Throwable e) {
                ds.dataLoadError = e;
            }
        }
    }

    protected ValueLoadContext beforeLoadValues(Map<String, Object> params) {
        ValueLoadContext context = new ValueLoadContext();

        ValueLoadContext.Query q = (ValueLoadContext.Query) ds.createDataQuery(context, params);
        if (q == null) {
            ds.detachListener(ds.data.values());
            ds.data.clear();
            return null;
        }

        if (ds.firstResult > 0)
            q.setFirstResult(ds.firstResult);

        if (ds.maxResults > 0) {
            q.setMaxResults(ds.maxResults);
        }

        if (storeName != null)
            context.setStoreName(storeName);

        context.setHint(PersistenceHints.SOFT_DELETION, ds.isSoftDeletion());

        context.setIdName(idName);
        for (MetaProperty property : ds.metaClass.getProperties()) {
            context.addProperty(property.getName());
        }

        ds.dataLoadError = null;
        return context;
    }

    protected void afterLoadValues(Map<String, Object> params, ValueLoadContext context, List<KeyValueEntity> entities) {
        ds.detachListener(ds.data.values());
        ds.data.clear();

        boolean hasEnumerations = ds.metaClass.getOwnProperties().stream()
                .anyMatch(p -> p.getRange().isEnum());

        if (!hasEnumerations) {
            for (KeyValueEntity entity : entities) {
                ds.data.put(entity.getId(), entity);
                ds.attachListener(entity);
                entity.setInstanceMetaClass(ds.metaClass);
            }
        } else {
            List<MetaProperty> enumProperties = getEnumProperties(ds.metaClass);

            for (KeyValueEntity entity : entities) {
                convertEnumValues(entity, enumProperties);

                ds.data.put(entity.getId(), entity);
                ds.attachListener(entity);
                entity.setInstanceMetaClass(ds.metaClass);
            }
        }
    }

    protected List<MetaProperty> getEnumProperties(MetaClass metaClass) {
        return metaClass.getOwnProperties().stream()
                .filter(p -> p.getRange().isEnum())
                .collect(Collectors.toList());
    }

    protected void convertEnumValues(KeyValueEntity entity, List<MetaProperty> enumProperties) {
        try {
            for (MetaProperty enumProperty : enumProperties) {
                Object enumValue = entity.getValue(enumProperty.getName());
                if (enumValue != null) {
                    Enumeration enumeration = enumProperty.getRange().asEnumeration();
                    entity.setValue(enumProperty.getName(), enumeration.parse(String.valueOf(enumValue)));
                }
            }
        } catch (ParseException e) {
            throw new RuntimeException("Unable to convert enum id to enum instance for EnumClass");
        }
    }
}
