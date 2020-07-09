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
    protected BeanLocator beanLocator;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected FetchPlanRepository fetchPlanRepository;

    protected Class<? extends JmixEntity> entityClass;
    protected MetaClass metaClass;
    protected Set<String> properties = new LinkedHashSet<>();
    protected Map<String, FetchPlanBuilder> builders = new HashMap<>();
    protected Map<String, FetchPlan> fetchPlans = new HashMap<>();
    protected Map<String, FetchMode> fetchModes = new HashMap<>();
    protected boolean systemProperties;

    protected FetchPlanBuilder(Class<? extends JmixEntity> entityClass) {
        this.entityClass = entityClass;
    }

    @PostConstruct
    protected void postConstruct() {
        metaClass = metadata.getClass(entityClass);
    }

    public FetchPlan build() {
        FetchPlan fetchPlan = new FetchPlan(metaClass.getJavaClass(), systemProperties);
        for (String property : properties) {
            FetchPlanBuilder builder = builders.get(property);
            if (builder == null) {
                FetchPlan refView = fetchPlans.get(property);
                if (refView == null) {
                    fetchPlan.addProperty(property);
                } else {
                    FetchMode fetchMode = fetchModes.get(property);
                    if (fetchMode == null) {
                        fetchPlan.addProperty(property, refView);
                    } else {
                        fetchPlan.addProperty(property, refView, fetchMode);
                    }
                }
            } else {
                fetchPlan.addProperty(property, builder.build());
            }
        }
        return fetchPlan;
    }

    public FetchPlanBuilder add(String property) {
        String[] parts = property.split("\\.");
        String propName = parts[0];
        MetaProperty metaProperty = metaClass.getProperty(propName);
        properties.add(propName);
        if (metaProperty.getRange().isClass()) {
            if (!builders.containsKey(propName)) {
                Class<JmixEntity> refClass = metaProperty.getRange().asClass().getJavaClass();
                builders.put(propName, beanLocator.getPrototype(FetchPlanBuilder.class, refClass));
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
        properties.add(property);
        Class<JmixEntity> refClass = metaClass.getProperty(property).getRange().asClass().getJavaClass();
        FetchPlanBuilder builder = beanLocator.getPrototype(FetchPlanBuilder.class, refClass);
        consumer.accept(builder);
        builders.put(property, builder);
        return this;
    }

    public FetchPlanBuilder add(String property, String fetchPlanName) {
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

    public FetchPlanBuilder addAll(String... properties) {
        for (String property : properties) {
            add(property);
        }
        return this;
    }

    public FetchPlanBuilder addSystem() {
        this.systemProperties = true;
        return this;
    }
    
    public FetchPlanBuilder addFetchPlan(FetchPlan fetchPlan) {
        for (FetchPlanProperty property : fetchPlan.getProperties()) {
            properties.add(property.getName());
            fetchPlans.put(property.getName(), property.getFetchPlan());
            fetchModes.put(property.getName(), property.getFetchMode());
        }
        return this;
    }

    public FetchPlanBuilder addFetchPlan(String fetchPlanName) {
        addFetchPlan(fetchPlanRepository.getFetchPlan(metaClass, fetchPlanName));
        return this;
    }
}
