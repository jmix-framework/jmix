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
package io.jmix.core;

import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.Creatable;
import io.jmix.core.entity.SoftDelete;
import io.jmix.core.entity.Updatable;
import io.jmix.core.entity.Versioned;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

/**
 * Class to declare a graph of objects that must be retrieved from the database.
 * <p>
 * A view can be constructed in Java code or defined in XML and deployed
 * to the {@link FetchPlanRepository} for recurring usage.
 * </p>
 * There are the following predefined view types:
 * <ul>
 * <li>{@link #LOCAL}</li>
 * <li>{@link #INSTANCE_NAME}</li>
 * <li>{@link #BASE}</li>
 * </ul>
 */
public class FetchPlan implements Serializable {

    /**
     * Parameters object to be used in constructors.
     */
    public static class FetchPlanParams {
        protected List<FetchPlan> src = Collections.emptyList();
        protected Class<? extends Entity> entityClass;
        protected String name;
        protected boolean includeSystemProperties;

        public <T extends FetchPlanParams> T src(FetchPlan src) {
            this.src = Collections.singletonList(src);
            //noinspection unchecked
            return (T) this;
        }

        public void src(List<FetchPlan> sources) {
            this.src = sources;
        }

        public <T extends FetchPlanParams> T entityClass(Class<? extends Entity> entityClass) {
            this.entityClass = entityClass;
            //noinspection unchecked
            return (T) this;
        }

        public <T extends FetchPlanParams> T name(String name) {
            this.name = name;
            //noinspection unchecked
            return (T) this;
        }

        public <T extends FetchPlanParams> T includeSystemProperties(boolean includeSystemProperties) {
            this.includeSystemProperties = includeSystemProperties;
            //noinspection unchecked
            return (T) this;
        }
    }

    /**
     * Includes all local non-system properties.
     */
    public static final String LOCAL = "_local";

    /**
     * Includes only properties contained in {@link io.jmix.core.metamodel.annotation.InstanceName}.
     */
    public static final String INSTANCE_NAME = "_instance_name";

    /**
     * Includes all local non-system properties and properties defined by {@link io.jmix.core.metamodel.annotation.InstanceName}
     * (effectively {@link #INSTANCE_NAME} + {@link #LOCAL}).
     */
    public static final String BASE = "_base";

    private static final long serialVersionUID = 4313784222934349594L;

    private Class<? extends Entity> entityClass;

    private String name;

    private Map<String, FetchPlanProperty> properties = new LinkedHashMap<>();

    private boolean loadPartialEntities;

    public FetchPlan(Class<? extends Entity> entityClass) {
        this(entityClass, "", true);
    }

    public FetchPlan(Class<? extends Entity> entityClass, boolean includeSystemProperties) {
        this(entityClass, "", includeSystemProperties);
    }

    public FetchPlan(Class<? extends Entity> entityClass, String name) {
        this(entityClass, name, true);
    }

    public FetchPlan(Class<? extends Entity> entityClass, String name, boolean includeSystemProperties) {
        this(new FetchPlanParams().entityClass(entityClass)
                .name(name)
                .includeSystemProperties(includeSystemProperties)
        );
    }

    public FetchPlan(FetchPlan src, String name, boolean includeSystemProperties) {
        this(src, null, name, includeSystemProperties);
    }

    public FetchPlan(FetchPlan src, @Nullable Class<? extends Entity> entityClass, String name, boolean includeSystemProperties) {
        this(new FetchPlanParams().src(src)
                .entityClass(entityClass != null ? entityClass : src.entityClass)
                .name(name)
                .includeSystemProperties(includeSystemProperties)
        );
    }

    public FetchPlan(FetchPlanParams viewParams) {
        this.entityClass = viewParams.entityClass;
        this.name = viewParams.name != null ? viewParams.name : "";
        if (viewParams.includeSystemProperties) {
            for (String propertyName : findSystemProperties(entityClass)) {
                addProperty(propertyName);
            }
        }
        List<FetchPlan> sources = viewParams.src;

        if (isNotEmpty(sources)) {
            Class<? extends Entity> entityClass = sources.get(0).entityClass;

            if (this.entityClass == null) {
                this.entityClass = entityClass;
            }

            for (FetchPlan view : sources) {
                putProperties(this.properties, view.getProperties());
            }
        }
    }

    protected void putProperties(Map<String, FetchPlanProperty> thisProperties, Collection<FetchPlanProperty> sourceProperties) {
        for (FetchPlanProperty sourceProperty : sourceProperties) {
            String sourcePropertyName = sourceProperty.getName();

            if (thisProperties.containsKey(sourcePropertyName)) {
                FetchPlan sourcePropertyView = sourceProperty.getFetchPlan();

                if (sourcePropertyView != null && isNotEmpty(sourcePropertyView.getProperties())) {

                    Map<String, FetchPlanProperty> thisViewProperties = thisProperties.get(sourcePropertyName).getFetchPlan().properties;
                    putProperties(thisViewProperties, sourcePropertyView.getProperties());
                }

            } else {
                thisProperties.put(sourceProperty.getName(), sourceProperty);
            }
        }
    }

    public static FetchPlan copy(FetchPlan fetchPlan) {
        Preconditions.checkNotNullArgument(fetchPlan, "fetchPlan is null");

        FetchPlanParams params = new FetchPlanParams()
                .entityClass(fetchPlan.getEntityClass())
                .name(fetchPlan.getName());
        FetchPlan copy = new FetchPlan(params);
        for (FetchPlanProperty property : fetchPlan.getProperties()) {
            copy.addProperty(property.getName(), copyNullable(property.getFetchPlan()), property.getFetchMode());
        }

        return copy;
    }

    @Nullable
    public static FetchPlan copyNullable(@Nullable FetchPlan fetchPlan) {
        if (fetchPlan == null) {
            return null;
        }
        return copy(fetchPlan);
    }

    /**
     * @return entity class this view belongs to
     */
    public Class<? extends Entity> getEntityClass() {
        return entityClass;
    }

    /**
     * @return view name, unique within an entity
     */
    public String getName() {
        return name;
    }

    /**
     * @return collection of properties
     */
    public Collection<FetchPlanProperty> getProperties() {
        return properties.values();
    }

    /**
     * Add a property to this view.
     *
     * @param name      property name
     * @param view      a view for a reference attribute, or null
     * @param fetchMode fetch mode for a reference attribute
     * @return this view instance for chaining
     */
    public FetchPlan addProperty(String name, @Nullable FetchPlan view, FetchMode fetchMode) {
        properties.put(name, new FetchPlanProperty(name, view, fetchMode));
        return this;
    }

    @Deprecated
    public FetchPlan addProperty(String name, @Nullable FetchPlan view, boolean lazy) {
        properties.put(name, new FetchPlanProperty(name, view, lazy));
        return this;
    }

    /**
     * Add a property to this view.
     *
     * @param name property name
     * @param view a view for a reference attribute, or null
     * @return this view instance for chaining
     */
    public FetchPlan addProperty(String name, FetchPlan view) {
        properties.put(name, new FetchPlanProperty(name, view));
        return this;
    }

    /**
     * Add a property to this view.
     *
     * @param name property name
     * @return this view instance for chaining
     */
    public FetchPlan addProperty(String name) {
        properties.put(name, new FetchPlanProperty(name, null));
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FetchPlan view = (FetchPlan) o;

        return entityClass.equals(view.entityClass) && name.equals(view.name);
    }

    @Override
    public int hashCode() {
        int result = entityClass.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return entityClass.getName() + "/" + name;
    }

    /**
     * Get directly owned view property by name.
     *
     * @param name property name
     * @return view property instance or null if it is not found
     */
    @Nullable
    public FetchPlanProperty getProperty(String name) {
        return properties.get(name);
    }

    /**
     * Check if a directly owned property with the given name exists in the view.
     *
     * @param name property name
     * @return true if such property found
     */
    public boolean containsProperty(String name) {
        return properties.containsKey(name);
    }

    /**
     * If true, the view affects loading of local attributes. If false, only reference attributes are affected and
     * local are always loaded.
     *
     * @see #setLoadPartialEntities(boolean)
     */
    public boolean loadPartialEntities() {
        return loadPartialEntities;
    }

    /**
     * Specifies whether the view affects loading of local attributes. By default only reference attributes are affected and
     * local are always loaded.
     *
     * @param loadPartialEntities true to affect loading of local attributes
     * @return this view instance for chaining
     */
    public FetchPlan setLoadPartialEntities(boolean loadPartialEntities) {
        this.loadPartialEntities = loadPartialEntities;
        return this;
    }

    protected Set<String> findSystemProperties(Class entityClass) {
        Set<String> result = new LinkedHashSet<>();

        Metadata metadata = AppBeans.get(Metadata.NAME);
        MetadataTools metadataTools = AppBeans.get(MetadataTools.NAME);
        MetaClass metaClass = metadata.getClass(entityClass);

        String pkName = metadataTools.getPrimaryKeyName(metaClass);
        if (pkName != null) {
            result.add(pkName);
        }

        addSystemPropertiesFrom(Versioned.class, entityClass, metaClass, metadataTools, result);
        addSystemPropertiesFrom(Creatable.class, entityClass, metaClass, metadataTools, result);
        addSystemPropertiesFrom(Updatable.class, entityClass, metaClass, metadataTools, result);
        addSystemPropertiesFrom(SoftDelete.class, entityClass, metaClass, metadataTools, result);

        return result;
    }

    protected void addSystemPropertiesFrom(Class<?> baseClass, Class<?> entityClass, MetaClass metaClass,
                                           MetadataTools metadataTools, Set<String> result) {
        if (baseClass.isAssignableFrom(entityClass)) {
            for (String property : getInterfaceProperties(baseClass)) {
                MetaProperty metaProperty = metaClass.findProperty(property);
                if (metaProperty != null && metadataTools.isPersistent(metaProperty)) {
                    result.add(property);
                }
            }
        }
    }

    protected List<String> getInterfaceProperties(Class<?> intf) {
        List<String> result = new ArrayList<>();
        for (Method method : intf.getDeclaredMethods()) {
            if (method.getName().startsWith("get") && method.getParameterTypes().length == 0) {
                result.add(StringUtils.uncapitalize(method.getName().substring(3)));
            }
        }
        return result;
    }
}
