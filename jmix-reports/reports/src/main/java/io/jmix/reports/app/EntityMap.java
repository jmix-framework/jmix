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
package io.jmix.reports.app;

import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;

import javax.annotation.Nonnull;
import java.util.*;

public class EntityMap implements Map<String, Object> {
    private static final Logger log = LoggerFactory.getLogger(EntityMap.class);

    public static final String INSTANCE_NAME_KEY = "_instanceName";

    protected Metadata metadata;
    protected MetadataTools metadataTools;
    protected InstanceNameProvider instanceNameProvider;

    protected Entity instance;
    protected FetchPlan fetchPlan;
    protected HashMap<String, Object> explicitData;

    protected boolean loaded = false;

    public EntityMap(Entity entity, BeanFactory beanFactory) {
        this.instance = entity;
        this.explicitData = new HashMap<>();
        this.metadata = beanFactory.getBean(Metadata.class);
        this.metadataTools = beanFactory.getBean(MetadataTools.class);
        this.instanceNameProvider = beanFactory.getBean(InstanceNameProvider.class);
    }

    public EntityMap(Entity entity, FetchPlan loadedAttributes, BeanFactory beanFactory) {
        this(entity, beanFactory);
        fetchPlan = loadedAttributes;
    }

    @Override
    public int size() {
        return explicitData.size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        if (explicitData.containsKey(key)) {
            return true;
        } else {
            MetaClass metaClass = getMetaClass(instance);
            for (MetaProperty property : metaClass.getProperties()) {
                if (Objects.equals(property.getName(), key))
                    return true;
            }
        }
        return false;
    }

    private MetaClass getMetaClass(Entity entity) {
        return metadata.getClass(instance);
    }

    @Override
    public boolean containsValue(Object value) {
        loadAllProperties();
        return explicitData.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        Object value = getValue(instance, key);

        if (value != null) return value;

        return explicitData.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return explicitData.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return explicitData.remove(key);
    }

    @Override
    public void putAll(@Nonnull Map<? extends String, ?> m) {
        explicitData.putAll(m);
    }

    @Override
    public void clear() {
        explicitData.clear();
    }

    @Nonnull
    @Override
    public Set<String> keySet() {
        loadAllProperties();
        return explicitData.keySet();
    }

    @Nonnull
    @Override
    public Collection<Object> values() {
        loadAllProperties();
        return explicitData.values();
    }

    @Nonnull
    @Override
    public Set<Entry<String, Object>> entrySet() {
        loadAllProperties();
        return explicitData.entrySet();
    }

    protected void loadAllProperties() {
        if (!loaded) {
            MetaClass metaClass = getMetaClass(instance);
            String pkName = metadataTools.getPrimaryKeyName(metaClass);
            for (MetaProperty property : metaClass.getProperties()) {
                if (fetchPlan != null && fetchPlan.getProperty(property.getName()) != null) {
                    explicitData.put(property.getName(), getValue(instance, property.getName()));
                } else if (fetchPlan != null && Objects.equals(pkName, property.getName())) {
                    explicitData.put(property.getName(), getValue(instance, property.getName()));
                } else if (fetchPlan == null) {
                    explicitData.put(property.getName(), getValue(instance, property.getName()));
                }
            }

            explicitData.put(INSTANCE_NAME_KEY, instanceNameProvider.getInstanceName(instance));

            loaded = true;
        }
    }

    protected Object getValue(Entity instance, Object key) {
        if (key == null) {
            return null;
        }

        String path = String.valueOf(key);
        if (path.endsWith(INSTANCE_NAME_KEY)) {
            if (StringUtils.isNotBlank(path.replace(INSTANCE_NAME_KEY, ""))) {
                Object value = getValue(instance, path.replace("." + INSTANCE_NAME_KEY, ""));
                if (value instanceof Entity) {
                    return instanceNameProvider.getInstanceName((Entity) value);
                }
            } else {
                try {
                    return instanceNameProvider.getInstanceName(instance);
                } catch (Exception e) {
                    log.trace("Suppressed error from underlying EntityMap instance.getInstanceName", e);
                    return null;
                }
            }
        }

        Object value = null;
        try {
            value = EntityValues.getValue(instance, path);
        } catch (Exception e) {
            log.trace("Suppressed error from underlying EntityMap instance.getValue", e);
        }

        if (value == null) {
            try {
                value = EntityValues.getValueEx(instance, path);
            } catch (Exception e) {
                log.trace("Suppressed error from underlying EntityMap instance.getValue", e);
            }
        }
        return value;
    }

    public Entity getInstance() {
        return instance;
    }

    public FetchPlan getFetchPlan() {
        return fetchPlan;
    }
}