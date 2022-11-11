/*
 * Copyright 2022 Haulmont.
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

package io.jmix.datatoolsflowui.view.entityinspector.assistant;

import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.datatoolsflowui.view.entityinspector.EntityFormLayoutUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component("datatl_EntityInspectorFetchPlanBuilder")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class InspectorFetchPlanBuilder {

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected FetchPlans fetchPlans;

    protected FetchPlanBuilder fetchPlanBuilder;

    protected Class<?> entityClass;
    protected MetaClass metaClass;

    protected boolean withCollections = false;
    protected boolean withEmbedded = false;
    protected boolean withSystemProperties = false;

    public static InspectorFetchPlanBuilder of(ApplicationContext applicationContext, Class<?> entityClass) {
        return applicationContext.getBean(InspectorFetchPlanBuilder.class, entityClass);
    }

    protected InspectorFetchPlanBuilder(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    @PostConstruct
    protected void postConstruct() {
        metaClass = metadata.getClass(entityClass);
        fetchPlanBuilder = fetchPlans.builder(entityClass);
    }

    public InspectorFetchPlanBuilder withCollections(boolean withCollections) {
        this.withCollections = withCollections;
        return this;
    }

    public InspectorFetchPlanBuilder withEmbedded(boolean withEmbedded) {
        this.withEmbedded = withEmbedded;
        return this;
    }

    public InspectorFetchPlanBuilder withSystemProperties(boolean withSystemProperties) {
        this.withSystemProperties = withSystemProperties;
        return this;
    }

    public FetchPlan build() {
        if (withSystemProperties) {
            fetchPlanBuilder.addSystem();
        }
        fetchPlanBuilder.addFetchPlan(FetchPlan.LOCAL);
        for (MetaProperty metaProperty : metaClass.getProperties()) {
            switch (metaProperty.getType()) {
                case EMBEDDED:
                    if (withEmbedded) {
                        MetaClass metaPropertyClass = metaProperty.getRange().asClass();
                        fetchPlanBuilder.add(
                                metaProperty.getName(),
                                builder -> createEmbeddedPlan(metaPropertyClass, builder)
                        );
                    }
                    break;
                case ASSOCIATION:
                case COMPOSITION:
                    if (withEmbedded) {
                        break;
                    }
                    if (EntityFormLayoutUtils.isMany(metaProperty)) {
                        if (withCollections) {
                            fetchPlanBuilder.add(metaProperty.getName(),
                                    builder -> builder.addFetchPlan(FetchPlan.LOCAL)
                                            .addSystem());
                        }
                    } else {
                        fetchPlanBuilder.add(metaProperty.getName(),
                                builder -> builder.addFetchPlan(FetchPlan.INSTANCE_NAME));
                    }
                    break;
                default:
                    break;
            }
        }
        return fetchPlanBuilder.build();
    }

    protected void createEmbeddedPlan(MetaClass metaClass, FetchPlanBuilder builder) {
        builder.addFetchPlan(FetchPlan.BASE);
        for (MetaProperty embeddedNestedProperty : metaClass.getProperties()) {
            if (embeddedNestedProperty.getRange().isClass() && !EntityFormLayoutUtils.isMany(embeddedNestedProperty)) {
                builder.add(embeddedNestedProperty.getName(), FetchPlan.INSTANCE_NAME);
            }
        }
    }
}
