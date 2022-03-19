/*
 * Copyright 2020 Haulmont.
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds {@link EntityImportPlan}s.
 * <p>
 * Use {@link EntityImportPlans} factory to get the builder.
 * <p>
 * See {@link EntityImportPlan} javadoc for usage example.
 */

@Component("core_EntityImportPlanBuilder")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EntityImportPlanBuilder {

    private Class<?> entityClass;

    private Map<String, EntityImportPlanProperty> properties = new HashMap<>();

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected MetadataTools metadataTools;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public EntityImportPlanBuilder(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    public EntityImportPlanBuilder addLocalProperty(String name) {
        EntityImportPlanProperty property = new EntityImportPlanProperty(name);
        properties.put(name, property);
        return this;
    }

    public EntityImportPlanBuilder addManyToOneProperty(String name, EntityImportPlan plan) {
        EntityImportPlanProperty property = new EntityImportPlanProperty(name, plan);
        properties.put(name, property);
        return this;
    }

    public EntityImportPlanBuilder addManyToOneProperty(String name, ReferenceImportBehaviour referenceImportBehaviour) {
        EntityImportPlanProperty property = new EntityImportPlanProperty(name, referenceImportBehaviour);
        properties.put(name, property);
        return this;
    }

    public EntityImportPlanBuilder addOneToOneProperty(String name, EntityImportPlan plan) {
        EntityImportPlanProperty property = new EntityImportPlanProperty(name, plan);
        properties.put(name, property);
        return this;
    }

    public EntityImportPlanBuilder addOneToOneProperty(String name, ReferenceImportBehaviour referenceImportBehaviour) {
        EntityImportPlanProperty property = new EntityImportPlanProperty(name, referenceImportBehaviour);
        properties.put(name, property);
        return this;
    }

    public EntityImportPlanBuilder addOneToManyProperty(String name, EntityImportPlan plan, CollectionImportPolicy collectionImportPolicy) {
        EntityImportPlanProperty property = new EntityImportPlanProperty(name, plan, collectionImportPolicy);
        properties.put(name, property);
        return this;
    }

    public EntityImportPlanBuilder addManyToManyProperty(String name, EntityImportPlan plan, CollectionImportPolicy collectionImportPolicy) {
        EntityImportPlanProperty property = new EntityImportPlanProperty(name, plan, collectionImportPolicy);
        properties.put(name, property);
        return this;
    }

    public EntityImportPlanBuilder addManyToManyProperty(String name, ReferenceImportBehaviour referenceImportBehaviour, CollectionImportPolicy collectionImportPolicy) {
        EntityImportPlanProperty property = new EntityImportPlanProperty(name, referenceImportBehaviour, collectionImportPolicy);
        properties.put(name, property);
        return this;
    }

    public EntityImportPlanBuilder addEmbeddedProperty(String name, EntityImportPlan plan) {
        EntityImportPlanProperty property = new EntityImportPlanProperty(name, plan);
        properties.put(name, property);
        return this;
    }

    public EntityImportPlanBuilder addProperty(EntityImportPlanProperty property) {
        properties.put(property.getName(), property);
        return this;
    }

    public EntityImportPlanBuilder addLocalProperties() {
        MetaClass metaClass = metadata.getClass(entityClass);
        metaClass.getProperties().stream()
                .filter(property -> !property.getRange().isClass() &&
                        !metadataTools.isSystem(property) &&
                        metadataTools.isJpa(property))
                .forEach(metaProperty -> addLocalProperty(metaProperty.getName()));
        return this;
    }

    public EntityImportPlanBuilder addSystemProperties() {
        MetaClass metaClass = metadata.getClass(entityClass);
        metaClass.getProperties().stream()
                .filter(metadataTools::isSystem)
                .forEach(metaProperty -> addLocalProperty(metaProperty.getName()));
        return this;
    }

    public EntityImportPlanBuilder addProperties(String... names) {
        for (String name : names) {
            addLocalProperty(name);
        }
        return this;
    }

    public EntityImportPlan build() {
        return new EntityImportPlan(entityClass, properties);
    }
}
