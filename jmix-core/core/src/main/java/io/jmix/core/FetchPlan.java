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

import org.springframework.lang.Nullable;
import java.io.Serializable;
import java.util.*;

/**
 * Class to declare a graph of objects that must be retrieved from the database.
 * <p>
 * A fetchPlan can be constructed in Java code or defined in XML and deployed
 * to the {@link FetchPlanRepository} for recurring usage.
 * </p>
 * There are the following predefined fetchPlan types:
 * <ul>
 * <li>{@link #LOCAL}</li>
 * <li>{@link #INSTANCE_NAME}</li>
 * <li>{@link #BASE}</li>
 * </ul>
 */
public class FetchPlan implements Serializable {

    /**
     * Includes all local properties.
     */
    public static final String LOCAL = "_local";

    /**
     * Includes only properties contained in {@link io.jmix.core.metamodel.annotation.InstanceName}.
     */
    public static final String INSTANCE_NAME = "_instance_name";

    /**
     * Includes all local properties and properties defined by {@link io.jmix.core.metamodel.annotation.InstanceName}
     * (effectively {@link #INSTANCE_NAME} + {@link #LOCAL}).
     */
    public static final String BASE = "_base";

    private static final long serialVersionUID = 4313784222934349594L;

    protected Class<?> entityClass;

    protected String name;

    protected Map<String, FetchPlanProperty> properties = new LinkedHashMap<>();

    protected boolean loadPartialEntities;

    protected FetchPlan(Class<?> entityClass, String name) {
        this.entityClass = entityClass;
        this.name = name != null ? name : "";
    }

    FetchPlan(Class<?> entityClass, String name, List<FetchPlanProperty> properties, boolean loadPartialEntities) {
        this(entityClass, name);
        this.loadPartialEntities = loadPartialEntities;

        for (FetchPlanProperty property : properties) {
            this.properties.put(property.getName(), property);
        }
    }

    /**
     * @return entity class this fetchPlan belongs to
     */
    public Class<?> getEntityClass() {
        return entityClass;
    }

    /**
     * @return fetchPlan name, unique within an entity
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

        FetchPlan fetchPlan = (FetchPlan) o;

        if (!(entityClass.equals(fetchPlan.entityClass) && name.equals(fetchPlan.name)
                && loadPartialEntities == fetchPlan.loadPartialEntities))
            return false;

        return properties.equals(fetchPlan.properties);
    }

    /**
     * @return whether this fetch plan contains all attributes of {@code fetchPlan} including nested plans attributes
     */
    public boolean isSupersetOf(FetchPlan fetchPlan) {
        if (!entityClass.equals(fetchPlan.entityClass))
            return false;

        for (FetchPlanProperty property : fetchPlan.getProperties()) {
            String propertyName = property.getName();
            if (!properties.containsKey(propertyName))
                return false;

            if (property.getFetchPlan() != null) {
                FetchPlan currentPlan = properties.get(propertyName).getFetchPlan();
                if ((currentPlan == null || !currentPlan.isSupersetOf(property.getFetchPlan())))
                    return false;
            }
        }
        return true;
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
     * Get directly owned fetchPlan property by name.
     *
     * @param name property name
     * @return fetchPlan property instance or null if it is not found
     */
    @Nullable
    public FetchPlanProperty getProperty(String name) {
        return properties.get(name);
    }

    /**
     * Check if a directly owned property with the given name exists in the fetchPlan.
     *
     * @param name property name
     * @return true if such property found
     */
    public boolean containsProperty(String name) {
        return properties.containsKey(name);
    }

    /**
     * If true, the fetchPlan affects loading of local attributes. If false, only reference attributes are affected and
     * local are always loaded.
     */
    public boolean loadPartialEntities() {
        return loadPartialEntities;
    }
}
