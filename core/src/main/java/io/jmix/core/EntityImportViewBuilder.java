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
 * Builds {@link EntityImportView}s.
 * <p>
 * Use {@link EntityImportViews} factory to get the builder.
 * <p>
 * See {@link EntityImportView} javadoc for usage example.
 */

@Component(EntityImportViewBuilder.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EntityImportViewBuilder {

    public static final String NAME = "core_EntityImportViewBuilder";

    private Class<? extends JmixEntity> entityClass;

    private Map<String, EntityImportViewProperty> properties = new HashMap<>();

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected MetadataTools metadataTools;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public EntityImportViewBuilder(Class<? extends JmixEntity> entityClass) {
        this.entityClass = entityClass;
    }

    public EntityImportViewBuilder addLocalProperty(String name) {
        EntityImportViewProperty property = new EntityImportViewProperty(name);
        properties.put(name, property);
        return this;
    }

    public EntityImportViewBuilder addManyToOneProperty(String name, EntityImportView view) {
        EntityImportViewProperty property = new EntityImportViewProperty(name, view);
        properties.put(name, property);
        return this;
    }

    public EntityImportViewBuilder addManyToOneProperty(String name, ReferenceImportBehaviour referenceImportBehaviour) {
        EntityImportViewProperty property = new EntityImportViewProperty(name, referenceImportBehaviour);
        properties.put(name, property);
        return this;
    }

    public EntityImportViewBuilder addOneToOneProperty(String name, EntityImportView view) {
        EntityImportViewProperty property = new EntityImportViewProperty(name, view);
        properties.put(name, property);
        return this;
    }

    public EntityImportViewBuilder addOneToOneProperty(String name, ReferenceImportBehaviour referenceImportBehaviour) {
        EntityImportViewProperty property = new EntityImportViewProperty(name, referenceImportBehaviour);
        properties.put(name, property);
        return this;
    }

    public EntityImportViewBuilder addOneToManyProperty(String name, EntityImportView view, CollectionImportPolicy collectionImportPolicy) {
        EntityImportViewProperty property = new EntityImportViewProperty(name, view, collectionImportPolicy);
        properties.put(name, property);
        return this;
    }

    public EntityImportViewBuilder addManyToManyProperty(String name, EntityImportView view, CollectionImportPolicy collectionImportPolicy) {
        EntityImportViewProperty property = new EntityImportViewProperty(name, view, collectionImportPolicy);
        properties.put(name, property);
        return this;
    }

    public EntityImportViewBuilder addManyToManyProperty(String name, ReferenceImportBehaviour referenceImportBehaviour, CollectionImportPolicy collectionImportPolicy) {
        EntityImportViewProperty property = new EntityImportViewProperty(name, referenceImportBehaviour, collectionImportPolicy);
        properties.put(name, property);
        return this;
    }

    public EntityImportViewBuilder addEmbeddedProperty(String name, EntityImportView view) {
        EntityImportViewProperty property = new EntityImportViewProperty(name, view);
        properties.put(name, property);
        return this;
    }

    public EntityImportViewBuilder addProperty(EntityImportViewProperty property) {
        properties.put(property.getName(), property);
        return this;
    }

    public EntityImportViewBuilder addLocalProperties() {
        MetaClass metaClass = metadata.getClass(entityClass);
        metaClass.getProperties().stream()
                .filter(property -> !property.getRange().isClass() && !metadataTools.isSystem(property))
                .forEach(metaProperty -> addLocalProperty(metaProperty.getName()));
        return this;
    }

    public EntityImportViewBuilder addSystemProperties() {
        MetaClass metaClass = metadata.getClass(entityClass);
        metaClass.getProperties().stream()
                .filter(metadataTools::isSystem)
                .forEach(metaProperty -> addLocalProperty(metaProperty.getName()));
        return this;
    }

    public EntityImportViewBuilder addProperties(String... names) {
        for (String name : names) {
            addLocalProperty(name);
        }
        return this;
    }

    public EntityImportView build() {
        return new EntityImportView(entityClass, properties);
    }
}
