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

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Use {@link EntityImportPlanBuilder} for building instances of {@link EntityImportPlan}. {@link
 * EntityImportPlanBuilder} may be obtained with the {@link EntityImportPlans} factory.
 * <p>
 * {@link EntityImportPlan} describes how entity fields should be saved during the import performed by {@link
 * EntityImportExport}.
 * <p>
 * Only fields that are added as properties to the {@code EntityImportPlan} will be saved.</p> <p> For local entity
 * property the rule is simple: if property name is added to the plan, then the property will be saved. Use {@link
 * EntityImportPlanBuilder#addLocalProperty(String)} method for adding local property to the plan. <p> For
 * <b>many-to-one</b> references there are two possible options: <ul> <li>Create or update the referenced entity. Use
 * the {@link EntityImportPlanBuilder#addManyToOneProperty(String, EntityImportPlan)} method. The referenced entity will
 * be saved according to the {@code EntityImportPlan} passed as parameter</li> <li>Try to find the reference in the
 * database and put it to the property value. {@link EntityImportPlanBuilder#addManyToOneProperty(String,
 * ReferenceImportBehaviour)} must be used for this. {@link ReferenceImportBehaviour} parameter specifies the behaviour
 * in case when referenced entity is missed in the database: missing entity can be ignored or import may fail with an
 * error.</li> </ul>
 * <p>
 * For <b>one-to-one</b> references behavior is the same as for the many-to-one references. Just use the corresponding
 * methods for adding properties to the plan: {@link EntityImportPlanBuilder#addOneToOneProperty(String,
 * EntityImportPlan)} or {@link EntityImportPlanBuilder#addOneToOneProperty(String, ReferenceImportBehaviour)}.
 * <p>
 * For <b>one-to-many</b> references you must specify the {@link EntityImportPlan} which defines how entities from the
 * collection must be saved. The second parameter is the {@link CollectionImportPolicy} which specifies what to do with
 * collection items that weren't passed to the import: they can be removed or remained.
 * <p>
 * For <b>many-to-many</b> references the following things must be defined: <ul> <li>Whether the passed collection
 * members must be created/updated or just searched in the database</li> <li>Whether the collection items not passed to
 * the import must be removed or remain. Keep in mind that for many-to-many properties missing collection members will
 * be removed from the collection only, not from the database</li> </ul>
 * <p>
 * You can invoke methods for adding plan properties in fluent interface style. There are also useful methods like
 * {@link EntityImportPlanBuilder#addLocalProperties()}, {@link EntityImportPlanBuilder#addSystemProperties()} or {@link
 * EntityImportPlanBuilder#addProperties(String...)}
 * <p>
 * Example of creating the EntityImportPlan object:
 * <pre>
 * EntityImportPlan importPlan = entityImportPlans.builder(Group.class)
 *           .addLocalProperties()
 *           .addOneToManyProperty("constraints",
 *                  entityImportPlans.builder(Constraint.class).addLocalProperties().build(),
 *                  CollectionImportPolicy.KEEP_ABSENT_ITEMS)
 *           .addManyToOneProperty("parent", ReferenceImportBehaviour.ERROR_ON_MISSING)
 *           .build();
 * </pre>
 */
public class EntityImportPlan implements Serializable {

    private Map<String, EntityImportPlanProperty> properties = new HashMap<>();

    private Class<?> entityClass;

    public EntityImportPlan(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    public EntityImportPlan(Class<?> entityClass, Map<String, EntityImportPlanProperty> properties) {
        this.entityClass = entityClass;
        this.properties = properties;
    }

    public EntityImportPlan addProperty(EntityImportPlanProperty property) {
        properties.put(property.getName(), property);
        return this;
    }

    @Nullable
    public EntityImportPlanProperty getProperty(String name) {
        return properties.get(name);
    }

    public Collection<EntityImportPlanProperty> getProperties() {
        return properties.values();
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }
}
