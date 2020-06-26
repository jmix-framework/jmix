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

@Component(FetchPlanBuilder.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FetchPlanBuilder {

    public static final String NAME = "core_ViewBuilder";

    @Autowired
    protected BeanLocator beanLocator;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected FetchPlanRepository viewRepository;

    protected Class<? extends Entity> entityClass;
    protected MetaClass metaClass;
    protected Set<String> properties = new LinkedHashSet<>();
    protected Map<String, FetchPlanBuilder> builders = new HashMap<>();
    protected Map<String, FetchPlan> views = new HashMap<>();
    protected Map<String, FetchMode> fetchModes = new HashMap<>();
    protected boolean systemProperties;

    public static FetchPlanBuilder of(Class<? extends Entity> entityClass) {
        return AppBeans.getPrototype(FetchPlanBuilder.class, entityClass);
    }

    protected FetchPlanBuilder(Class<? extends Entity> entityClass) {
        this.entityClass = entityClass;
    }

    @PostConstruct
    protected void postConstruct() {
        metaClass = metadata.getClass(entityClass);
    }

    public FetchPlan build() {
        FetchPlan view = new FetchPlan(metaClass.getJavaClass(), systemProperties);
        for (String property : properties) {
            FetchPlanBuilder builder = builders.get(property);
            if (builder == null) {
                FetchPlan refView = views.get(property);
                if (refView == null) {
                    view.addProperty(property);
                } else {
                    FetchMode fetchMode = fetchModes.get(property);
                    if (fetchMode == null) {
                        view.addProperty(property, refView);
                    } else {
                        view.addProperty(property, refView, fetchMode);
                    }
                }
            } else {
                view.addProperty(property, builder.build());
            }
        }
        return view;
    }

    public FetchPlanBuilder add(String property) {
        String[] parts = property.split("\\.");
        String propName = parts[0];
        MetaProperty metaProperty = metaClass.getProperty(propName);
        properties.add(propName);
        if (metaProperty.getRange().isClass()) {
            if (!builders.containsKey(propName)) {
                Class<Entity> refClass = metaProperty.getRange().asClass().getJavaClass();
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
        Class<Entity> refClass = metaClass.getProperty(property).getRange().asClass().getJavaClass();
        FetchPlanBuilder builder = beanLocator.getPrototype(FetchPlanBuilder.class, refClass);
        consumer.accept(builder);
        builders.put(property, builder);
        return this;
    }

    public FetchPlanBuilder add(String property, String viewName) {
        properties.add(property);
        FetchPlan view = viewRepository.getFetchPlan(metaClass.getProperty(property).getRange().asClass(), viewName);
        views.put(property, view);
        return this;
    }

    public FetchPlanBuilder add(String property, String viewName, FetchMode fetchMode) {
        add(property, viewName);
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

    /**
     * @deprecated replaced by {@link FetchPlanBuilder#addFetchPlan(FetchPlan)}
     */
    @Deprecated
    public FetchPlanBuilder addView(FetchPlan view) {
        return addFetchPlan(view);
    }

    /**
     * @deprecated replaced by {@link FetchPlanBuilder#addFetchPlan(String)}
     */
    @Deprecated
    public FetchPlanBuilder addView(String viewName) {
        return addFetchPlan(viewName);
    }

    public FetchPlanBuilder addFetchPlan(FetchPlan fetchPlan) {
        for (FetchPlanProperty property : fetchPlan.getProperties()) {
            properties.add(property.getName());
            views.put(property.getName(), property.getFetchPlan());
            fetchModes.put(property.getName(), property.getFetchMode());
        }
        return this;
    }

    public FetchPlanBuilder addFetchPlan(String planName) {
        addFetchPlan(viewRepository.getFetchPlan(metaClass, planName));
        return this;
    }
}
