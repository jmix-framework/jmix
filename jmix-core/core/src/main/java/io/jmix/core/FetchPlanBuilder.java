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

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Builds {@link FetchPlan}s.
 * <p>
 * Use {@link FetchPlans} factory to get the builder.
 */
public class FetchPlanBuilder {

    protected FetchPlans fetchPlans;
    protected MetadataTools metadataTools;
    protected FetchPlanRepository fetchPlanRepository;

    protected Class<?> entityClass;
    protected MetaClass metaClass;
    protected Set<String> properties = new LinkedHashSet<>();
    protected Map<String, FetchPlanBuilder> builders = new HashMap<>();
    protected Map<String, FetchPlan> propertiesToFetchPlans = new HashMap<>();
    protected Map<String, FetchMode> fetchModes = new HashMap<>();
    protected boolean systemProperties = false;
    protected boolean loadPartialEntities = false;
    protected String name = "";
    protected FetchPlan result = null;

    protected FetchPlanBuilder(
            FetchPlans fetchPlans,
            Metadata metadata,
            MetadataTools metadataTools,
            FetchPlanRepository fetchPlanRepository,
            Class<?> entityClass
    ) {
        this.fetchPlans = fetchPlans;
        this.metadataTools = metadataTools;
        this.fetchPlanRepository = fetchPlanRepository;
        this.entityClass = entityClass;
        this.metaClass = metadata.getClass(entityClass);
    }

    /**
     * Builds fetch plan and makes builder immutable.<br>
     * Subsequent method invocations returns the same object.
     *
     * @return created FetchPlan
     */
    public FetchPlan build() {
        if (result != null)
            return result;

        if (systemProperties) {
            addSystemProperties();
        }

        List<FetchPlanProperty> fetchPlanProperties = new LinkedList<>();
        for (String property : properties) {
            FetchPlanBuilder builder = builders.get(property);

            fetchPlanProperties.add(new FetchPlanProperty(property,
                    builder != null ? builder.build() : propertiesToFetchPlans.get(property),
                    fetchModes.getOrDefault(property, FetchMode.AUTO)));
        }

        result = createFetchPlan(metaClass.getJavaClass(), name, fetchPlanProperties, loadPartialEntities);
        return result;
    }

    //extended in Jmix module for legacy support
    protected FetchPlan createFetchPlan(Class<?> entityClass,
                                        String name,
                                        List<FetchPlanProperty> properties,
                                        boolean loadPartialEntities) {
        return new FetchPlan(entityClass, name, properties, loadPartialEntities);
    }

    /**
     * Adds property.
     *
     * @param property name of direct property or dot separated path to indirect property. e.g. "address.country.name"
     * @throws RuntimeException if FetchPlan has been already built
     */
    public FetchPlanBuilder add(String property) {
        checkState();

        String[] parts = property.split("\\.");
        String propName = parts[0];
        MetaProperty metaProperty = metaClass.getProperty(propName);
        properties.add(propName);
        if (metaProperty.getRange().isClass()) {
            if (!builders.containsKey(propName)) {
                Class<?> refClass = metaProperty.getRange().asClass().getJavaClass();
                builders.put(propName, fetchPlans.builder(refClass));
            }
        }
        if (parts.length > 1) {
            FetchPlanBuilder nestedBuilder = builders.get(propName);
            if (nestedBuilder == null)
                throw new IllegalStateException("Builder not found for property " + propName);
            String nestedProp = Arrays.stream(parts).skip(1).collect(Collectors.joining("."));
            nestedBuilder.add(nestedProp);
        }
        return this;
    }

    /**
     * Adds property.
     *
     * @param property property name
     * @param consumer to build property fetchPlan
     * @throws RuntimeException if FetchPlan has been already built
     */
    public FetchPlanBuilder add(String property, Consumer<FetchPlanBuilder> consumer) {
        checkState();
        properties.add(property);
        Class<?> refClass = metaClass.getProperty(property).getRange().asClass().getJavaClass();
        FetchPlanBuilder builder = fetchPlans.builder(refClass);
        consumer.accept(builder);
        builders.put(property, builder);
        return this;
    }

    /**
     * Adds property.
     *
     * @param property  property name
     * @param consumer  to build property fetchPlan
     * @param fetchMode fetch mode for property
     * @throws RuntimeException if FetchPlan has been already built
     */
    public FetchPlanBuilder add(String property, Consumer<FetchPlanBuilder> consumer, FetchMode fetchMode) {
        add(property, consumer);
        fetchModes.put(property, fetchMode);
        return this;
    }

    /**
     * Adds property with FetchPlan specified by {@code fetchPlanName}.
     * <p>
     * For example:
     * <pre>
     *   FetchPlan orderFP = fetchPlans.builder(Order.class)
     *       .addFetchPlan(FetchPlan.BASE)
     *       .add("orderLines", FetchPlan.BASE)
     *       .add("orderLines.product", FetchPlan.BASE)
     *       .build();
     * </pre>
     *
     * @param property name of immediate property or dot separated property path, e.g. "address.country.name"
     * @throws FetchPlanNotFoundException if specified by {@code fetchPlanName} FetchPlan not found for entity determined by {@code property}
     * @throws RuntimeException           if FetchPlan has been already built
     */
    public FetchPlanBuilder add(String property, String fetchPlanName) {
        checkState();

        add(property);

        FetchPlanBuilder targetBuilder = this;
        MetaPropertyPath propertyPath = getMetaPropertyPath(property);
        for (MetaProperty metaProperty : propertyPath.getMetaProperties()) {
            if (metaProperty.getRange().isClass()) {
                targetBuilder = getNestedPropertyBuilder(targetBuilder, metaProperty.getName());
            }
        }
        FetchPlanBuilder propBuilder = targetBuilder;
        propBuilder.addFetchPlan(fetchPlanName);

        return this;
    }

    /**
     * Adds property with FetchPlan specified by {@code fetchPlanName} and a specific fetch mode.
     * <p>
     * For example:
     * <pre>
     *     FetchPlan orderFP = fetchPlans.builder(Order.class)
     *         .addFetchPlan(FetchPlan.BASE)
     *         .add("orderLines", FetchPlan.BASE, FetchMode.UNDEFINED)
     *         .add("orderLines.product", FetchPlan.BASE, FetchMode.UNDEFINED)
     *         .build();
     * </pre>
     *
     * @param property name of immediate property or dot separated property path, e.g. "address.country.name"
     * @throws FetchPlanNotFoundException if specified by {@code fetchPlanName} FetchPlan not found for entity determined by {@code property}
     * @throws RuntimeException           if FetchPlan has been already built
     */
    public FetchPlanBuilder add(String property, String fetchPlanName, FetchMode fetchMode) {
        add(property, fetchPlanName);

        FetchPlanBuilder targetBuilder = this;
        MetaPropertyPath propertyPath = getMetaPropertyPath(property);
        if (propertyPath.getMetaProperties().length > 1) {
            for (MetaProperty metaProperty : Arrays.copyOf(propertyPath.getMetaProperties(), propertyPath.getMetaProperties().length - 1)) {
                if (metaProperty.getRange().isClass()) {
                    targetBuilder = getNestedPropertyBuilder(targetBuilder, metaProperty.getName());
                }
            }
        }
        targetBuilder.fetchModes.put(propertyPath.getMetaProperty().getName(), fetchMode);

        return this;
    }

    /**
     * Adds property with FetchPlan specified by {@code builder}.
     *
     * @param property property name
     * @throws RuntimeException if FetchPlan has been already built
     */
    public FetchPlanBuilder add(String property, FetchPlanBuilder builder) {
        checkState();
        properties.add(property);
        builders.put(property, builder);
        return this;
    }

    /**
     * Adds property with FetchPlan specified by {@code builder}.
     *
     * @param property  property name
     * @param fetchMode fetch mode for property
     * @throws RuntimeException if FetchPlan has been already built
     */
    public FetchPlanBuilder add(String property, FetchPlanBuilder builder, FetchMode fetchMode) {
        add(property, builder);
        fetchModes.put(property, fetchMode);
        return this;
    }

    /**
     * Adds all listed properties to FetchPlan
     *
     * @param properties list of properties determined as for simple {@code add(String)} method
     * @throws RuntimeException if FetchPlan has been already built
     * @see FetchPlanBuilder#add(String)
     */
    public FetchPlanBuilder addAll(String... properties) {
        checkState();
        for (String property : properties) {
            add(property);
        }
        return this;
    }

    /**
     * Adds all system properties determined by {@link MetadataTools#getSystemProperties(MetaClass)} to FetchPlan
     *
     * @throws RuntimeException if FetchPlan has been already built
     */
    public FetchPlanBuilder addSystem() {
        checkState();
        this.systemProperties = true;
        return this;
    }

    protected void addSystemProperties() {
        checkState();
        for (String propertyName : metadataTools.getSystemProperties(metaClass)) {
            if (!this.properties.contains(propertyName)) {
                if (metaClass.getProperty(propertyName).getRange().isClass()) {
                    add(propertyName, FetchPlan.INSTANCE_NAME);
                } else {
                    add(propertyName);
                }
            }
        }
    }

    /**
     * Adds all properties from specified {@code fetchPlan}. Replaces existing nested fetchPlans.
     *
     * @throws RuntimeException if FetchPlan has been already built
     */
    public FetchPlanBuilder addFetchPlan(FetchPlan fetchPlan) {
        checkState();
        for (FetchPlanProperty property : fetchPlan.getProperties()) {
            properties.add(property.getName());
            propertiesToFetchPlans.put(property.getName(), property.getFetchPlan());
            fetchModes.put(property.getName(), property.getFetchMode());
        }
        return this;
    }

    /**
     * Adds all properties from specified by {@code fetchPlanName} FetchPlan. Replaces existing nested fetchPlans.
     *
     * @throws RuntimeException if FetchPlan has been already built
     */
    public FetchPlanBuilder addFetchPlan(String fetchPlanName) {
        checkState();
        addFetchPlan(fetchPlanRepository.getFetchPlan(metaClass, fetchPlanName));
        return this;
    }

    /**
     * Deep merges {@code fetchPlan} into current fetchPlan by adding all properties recursively.
     *
     * @throws RuntimeException if FetchPlan has been already built
     */
    public FetchPlanBuilder merge(FetchPlan fetchPlan) {
        checkState();
        for (FetchPlanProperty property : fetchPlan.getProperties()) {
            mergeProperty(property.getName(), property.getFetchPlan(), property.getFetchMode());
        }
        return this;
    }

    /**
     * Deep merges {@code fetchPlan} into property's fetchPlan by adding all properties recursively.
     *
     * @param propName name of property to merge {@code propFetchPlan} to
     * @throws RuntimeException if FetchPlan has been already built
     */
    public FetchPlanBuilder mergeProperty(String propName, @Nullable FetchPlan propFetchPlan, @Nullable FetchMode propFetchMode) {
        boolean isNew = properties.add(propName);

        if (propFetchPlan != null) {
            if (isNew) {
                propertiesToFetchPlans.put(propName, propFetchPlan);
            } else {//property already exists
                MetaProperty metaProperty = metaClass.getProperty(propName);
                if (metaProperty.getRange().isClass()) {//ref property need to be merged with existing property
                    if (!builders.containsKey(propName)) {
                        Class<?> refClass = metaProperty.getRange().asClass().getJavaClass();
                        builders.put(propName, fetchPlans.builder(refClass));
                        builders.get(propName).merge(propertiesToFetchPlans.get(propName));
                    }
                    builders.get(propName).merge(propFetchPlan);
                }
            }
        }

        if (propFetchMode != null)
            fetchModes.put(propName, propFetchMode);

        return this;
    }

    /**
     * Deep merges {@code fetchPlan} into direct or indirect property's fetchPlan by adding all properties recursively.
     *
     * @param propertyPath name of direct property or dot separated path to indirect property to merge {@code propFetchPlan} to
     * @throws RuntimeException if FetchPlan has been already built
     */
    public FetchPlanBuilder mergeNestedProperty(String propertyPath, @Nullable FetchPlan fetchPlan) {
        checkState();

        String[] parts = propertyPath.split("\\.");
        if (parts.length > 1) {
            String propName = parts[0];
            MetaProperty metaProperty = metaClass.getProperty(propName);
            properties.add(propName);
            if (metaProperty.getRange().isClass()) {
                if (!builders.containsKey(propName)) {
                    Class<?> refClass = metaProperty.getRange().asClass().getJavaClass();

                    FetchPlanBuilder newNestedBuilder = fetchPlans.builder(refClass);
                    if (propertiesToFetchPlans.containsKey(propName)) {
                        newNestedBuilder.merge(propertiesToFetchPlans.get(propName));
                    }

                    builders.put(propName, newNestedBuilder);

                }
            }
            FetchPlanBuilder nestedBuilder = builders.get(propName);
            if (nestedBuilder == null)
                throw new IllegalStateException("Builder not found for property " + propName);
            String nestedProp = Arrays.stream(parts).skip(1).collect(Collectors.joining("."));
            nestedBuilder.mergeNestedProperty(nestedProp, fetchPlan);
        } else {
            mergeProperty(parts[0], fetchPlan, null);
        }
        return this;
    }


    /**
     * Sets {@link FetchPlan#loadPartialEntities()} to true
     *
     * @throws RuntimeException if FetchPlan has been already built
     */
    public FetchPlanBuilder partial() {
        checkState();
        loadPartialEntities = true;
        return this;
    }

    /**
     * Specifies {@link FetchPlan#loadPartialEntities()}
     *
     * @throws RuntimeException if FetchPlan has been already built
     */

    public FetchPlanBuilder partial(boolean partial) {
        checkState();
        loadPartialEntities = partial;
        return this;
    }

    /**
     * Sets {@link FetchPlan#name}
     *
     * @throws RuntimeException if FetchPlan has been already built
     */
    public FetchPlanBuilder name(String name) {
        checkState();
        this.name = name;
        return this;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    /**
     * @return {@link FetchPlan#name} for fetchPlan under construction
     */
    public String getName() {
        return name;
    }

    /**
     * @return wheser {@link FetchPlan} has been already built and builder is not modifiable anymore
     */
    public boolean isBuilt() {
        return result != null;
    }

    /**
     * Checks whether {@link FetchPlan} has been built. Builder cannot be modified after {@link FetchPlanBuilder#build()} invocation
     *
     * @throws RuntimeException if FetchPlan is already built
     */
    protected void checkState() {
        if (result != null)
            throw new RuntimeException("FetchPlanBuilder cannot be modified after build() invocation");
    }

    protected MetaPropertyPath getMetaPropertyPath(String property) {
        MetaPropertyPath propertyPath = metaClass.getPropertyPath(property);
        if (propertyPath == null) {
            throw new IllegalArgumentException("Invalid property: " + property);
        }
        return propertyPath;
    }

    protected FetchPlanBuilder getNestedPropertyBuilder(FetchPlanBuilder builder, String name) {
        FetchPlanBuilder propBuilder = builder.builders.get(name);
        if (propBuilder == null) {
            throw new IllegalStateException("Nested builder not found for property: " + name);
        }
        return propBuilder;
    }
}
