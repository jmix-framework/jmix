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

package io.jmix.data.impl.jpql.model;

public class EntityBuilder {
    private JpqlEntityModelImpl entityModel;

    private EntityBuilder() {
    }

    public static EntityBuilder create() {
        return new EntityBuilder();
    }

    public JpqlEntityModel produceImmediately(String entityName) {
        return new JpqlEntityModelImpl(entityName);
    }

    public JpqlEntityModel produceImmediately(String entityName, String... stringAttributeNames) {
        JpqlEntityModelImpl result = new JpqlEntityModelImpl(entityName);
        for (String stringAttributeName : stringAttributeNames) {
            result.addSingleValueAttribute(String.class, stringAttributeName);
        }
        return result;
    }

    public EntityBuilder startNewEntity(String name) {
        entityModel = new JpqlEntityModelImpl(name);
        return this;
    }

    public EntityBuilder addStringAttribute(String name) {
        addSingleValueAttribute(String.class, name);
        return this;
    }

    public EntityBuilder addSingleValueAttribute(Class clazz, String name) {
        entityModel.addSingleValueAttribute(clazz, name);
        return this;
    }

    public EntityBuilder addSingleValueAttribute(Class clazz, String name, String userFriendlyName) {
        entityModel.addSingleValueAttribute(clazz, name, userFriendlyName);
        return this;
    }

    public EntityBuilder addReferenceAttribute(String name, String referencedEntityName) {
        entityModel.addReferenceAttribute(referencedEntityName, name);
        return this;
    }

    public EntityBuilder addReferenceAttribute(String name, String referencedEntityName, String userFriendlyName, boolean isEmbedded) {
        entityModel.addReferenceAttribute(referencedEntityName, name, userFriendlyName, isEmbedded);
        return this;
    }

    public EntityBuilder addCollectionReferenceAttribute(String name, String referencedEntityName) {
        entityModel.addCollectionReferenceAttribute(referencedEntityName, name);
        return this;
    }

    public EntityBuilder addCollectionReferenceAttribute(String name, String referencedEntityName, String userFriendlyName) {
        entityModel.addCollectionReferenceAttribute(referencedEntityName, name, userFriendlyName);
        return this;
    }

    public JpqlEntityModel produce() {
        JpqlEntityModelImpl returnedEntity = entityModel;
        entityModel = null;
        return returnedEntity;
    }
}
