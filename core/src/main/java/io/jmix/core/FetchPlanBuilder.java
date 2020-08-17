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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Builds {@link FetchPlan}s.
 * <p>
 * Use {@link FetchPlans} factory to get the builder.
 */
@Component(FetchPlanBuilder.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FetchPlanBuilder {

    public static final String NAME = "core_FetchPlanBuilder";

    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected FetchPlanRepository fetchPlanRepository;

    protected Class<? extends JmixEntity> entityClass;
    protected MetaClass metaClass;
    protected Set<String> properties = new LinkedHashSet<>();
    protected Map<String, FetchPlanBuilder> builders = new HashMap<>();
    protected Map<String, FetchPlan> fetchPlans = new HashMap<>();
    protected Map<String, FetchMode> fetchModes = new HashMap<>();
    protected boolean systemProperties = false;
    protected boolean loadPartialEntities = false;
    protected String name = "";
    protected FetchPlan result = null;

    protected FetchPlanBuilder(Class<? extends JmixEntity> entityClass) {
        this.entityClass = entityClass;
    }

    @PostConstruct
    protected void postConstruct() {
        metaClass = metadata.getClass(entityClass);
    }

    /**
     * Builds fetch plan and makes builder immutable.<br/>
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
                    builder != null ? builder.build() : fetchPlans.get(property),
                    fetchModes.getOrDefault(property, FetchMode.AUTO)));
        }

        result = createFetchPlan(metaClass.getJavaClass(), name, fetchPlanProperties, loadPartialEntities);
        return result;
    }

    //extended in CUBA module for legacy support
    protected FetchPlan createFetchPlan(Class<? extends JmixEntity> entityClass,
                                        String name,
                                        List<FetchPlanProperty> properties,
                                        boolean loadPartialEntities) {
        return new FetchPlan(entityClass, name, properties, loadPartialEntities);
    }

    public FetchPlanBuilder add(String property) {
        checkState();

        String[] parts = property.split("\\.");
        String propName = parts[0];
        MetaProperty metaProperty = metaClass.getProperty(propName);
        properties.add(propName);
        if (metaProperty.getRange().isClass()) {
            if (!builders.containsKey(propName)) {
                Class<JmixEntity> refClass = metaProperty.getRange().asClass().getJavaClass();
                builders.put(propName, applicationContext.getBean(FetchPlanBuilder.class, refClass));
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

    public FetchPlanBuilder add(String property, Consumer<FetchPlanBuilder> consumer) {
        checkState();
        properties.add(property);
        Class<JmixEntity> refClass = metaClass.getProperty(property).getRange().asClass().getJavaClass();
        FetchPlanBuilder builder = applicationContext.getBean(FetchPlanBuilder.class, refClass);
        consumer.accept(builder);
        builders.put(property, builder);
        return this;
    }

    public FetchPlanBuilder add(String property, Consumer<FetchPlanBuilder> consumer, FetchMode fetchMode) {
        add(property, consumer);
        fetchModes.put(property, fetchMode);
        return this;
    }

    public FetchPlanBuilder add(String property, String fetchPlanName) {
        checkState();
        properties.add(property);
        FetchPlan fetchPlan = fetchPlanRepository.getFetchPlan(metaClass.getProperty(property).getRange().asClass(), fetchPlanName);
        fetchPlans.put(property, fetchPlan);
        return this;
    }

    public FetchPlanBuilder add(String property, String fetchPlanName, FetchMode fetchMode) {
        add(property, fetchPlanName);
        fetchModes.put(property, fetchMode);
        return this;
    }

    public FetchPlanBuilder add(String property, FetchPlanBuilder builder, FetchMode fetchMode) {
        properties.add(property);
        builders.put(property, builder);
        fetchModes.put(property, fetchMode);
        return this;
    }


    public FetchPlanBuilder addAll(String... properties) {
        checkState();
        for (String property : properties) {
            add(property);
        }
        return this;
    }

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
     */
    public FetchPlanBuilder addFetchPlan(FetchPlan fetchPlan) {
        checkState();
        for (FetchPlanProperty property : fetchPlan.getProperties()) {
            properties.add(property.getName());
            fetchPlans.put(property.getName(), property.getFetchPlan());
            fetchModes.put(property.getName(), property.getFetchMode());
        }
        return this;
    }

    /**
     * Adds all properties from specified by {@code fetchPlanName} FetchPlan. Replaces existing nested fetchPlans.
     */
    public FetchPlanBuilder addFetchPlan(String fetchPlanName) {
        checkState();
        addFetchPlan(fetchPlanRepository.getFetchPlan(metaClass, fetchPlanName));
        return this;
    }

    /**
     * Deep merges {@code fetchPlan} into current fetchPlan by adding all properties recursively.
     *
     * @param fetchPlan
     * @return
     */
    public FetchPlanBuilder mergeFetchPlan(FetchPlan fetchPlan) {
        checkState();
        for (FetchPlanProperty property : fetchPlan.getProperties()) {
            String propName = property.getName();
            boolean isNew = properties.add(propName);
            if (isNew) {
                fetchPlans.put(propName, property.getFetchPlan());
            } else if (property.getFetchPlan() != null) {//property already exists
                MetaProperty metaProperty = metaClass.getProperty(propName);
                if (metaProperty.getRange().isClass()) {//ref property need to be merged with existing property
                    if (!builders.containsKey(propName)) {
                        Class<JmixEntity> refClass = metaProperty.getRange().asClass().getJavaClass();
                        builders.put(propName, applicationContext.getBean(FetchPlanBuilder.class, refClass));
                        builders.get(propName).mergeFetchPlan(fetchPlans.get(propName));
                    }
                    builders.get(propName).mergeFetchPlan(property.getFetchPlan());
                }
            }
            fetchModes.put(propName, property.getFetchMode());
        }

        return this;
    }

    public FetchPlanBuilder partial() {
        checkState();
        loadPartialEntities = true;
        return this;
    }

    public FetchPlanBuilder partial(boolean partial) {
        checkState();
        loadPartialEntities = partial;
        return this;
    }

    public FetchPlanBuilder name(String name) {
        checkState();
        this.name = name;
        return this;
    }

    public Class<? extends JmixEntity> getEntityClass() {
        return entityClass;
    }

    public String getName() {
        return name;
    }

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


}
