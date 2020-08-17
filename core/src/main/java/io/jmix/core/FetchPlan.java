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
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

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
     * Includes all local non-system properties.
     */
    public static final String LOCAL = "_local";//todo taimanov include system properties but exclude in View/ViewBuilder

    /**
     * Includes only properties contained in {@link io.jmix.core.metamodel.annotation.InstanceName}.
     */
    public static final String INSTANCE_NAME = "_instance_name";

    /**
     * Includes all local non-system properties and properties defined by {@link io.jmix.core.metamodel.annotation.InstanceName}
     * (effectively {@link #INSTANCE_NAME} + {@link #LOCAL}).
     */
    public static final String BASE = "_base";//todo taimanov include system properties but exclude in View/ViewBuilder

    private static final long serialVersionUID = 4313784222934349594L;

    protected Class<? extends JmixEntity> entityClass;

    private String name;

    protected Map<String, FetchPlanProperty> properties = new LinkedHashMap<>();

    protected boolean loadPartialEntities;

    protected FetchPlan(Class<? extends JmixEntity> entityClass, String name) {
        this.entityClass = entityClass;
        this.name = name != null ? name : "";
    }

    FetchPlan(Class<? extends JmixEntity> entityClass, String name, List<FetchPlanProperty> properties, boolean loadPartialEntities) {
        this(entityClass, name);
        this.loadPartialEntities = loadPartialEntities;

        for (FetchPlanProperty property : properties) {
            this.properties.put(property.getName(), property);
        }
    }




    public static FetchPlan copy(FetchPlan fetchPlan) {
        Preconditions.checkNotNullArgument(fetchPlan, "fetchPlan is null");

        FetchPlan copy = new FetchPlan(fetchPlan.entityClass,
                fetchPlan.name,
                new LinkedList<>(fetchPlan.getProperties()),
                fetchPlan.loadPartialEntities);

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
    public Class<? extends JmixEntity> getEntityClass() {
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
        return Collections.unmodifiableCollection(properties.values());
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
     */
    public boolean loadPartialEntities() {
        return loadPartialEntities;
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
