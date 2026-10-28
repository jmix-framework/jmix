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

package io.jmix.search.index.annotation;

import io.jmix.search.index.mapping.MappingDefinition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Predicate;

/**
 * Annotation to mark index definition interfaces.
 * <p>
 * <b>Mapping.</b>
 * <p>
 * Index mapping can be defined in two ways.
 * <p>
 * The first one - by using field-mapping annotation. Create one or several methods
 * and annotate them with one or more field-mapping annotations (e.g. {@link AutoMappedField}).
 * Such method should fulfil the following requirements:
 * <ul>
 *     <li>With void return type</li>
 *     <li>With any name</li>
 *     <li>Without body</li>
 *     <li>Without parameters</li>
 * </ul>
 * <p>
 * The second one - by building {@link MappingDefinition} directly.
 * Create <b>one</b> method that fulfils the following requirements:
 * <ul>
 *     <li>With default modifier</li>
 *     <li>With any name</li>
 *     <li>With return type - {@link MappingDefinition}</li>
 *     <li>With Spring beans required for custom user configuration as parameters</li>
 *     <li>Annotated with {@link ManualMappingDefinition}</li>
 * </ul>
 * Use {@link MappingDefinition#builder()} within method body to build {@link MappingDefinition}.
 * <p>
 * <b>Note:</b> if there is definition method with implementation - any field-mapping annotations will be ignored.
 * <p>
 * <b>Indexable Predicate.</b>
 * <p>
 * Indexing process can have additional instance-level condition. It can be added by configuring Indexable Predicate.
 * This predicate applies to each entity instance during indexing and defines if it should be indexed or not.
 * <p>
 * It doesn't apply during deletion.
 * <p>
 * To configure Indexable Predicate add method that fulfils the following requirements:
 * <ul>
 *     <li>With default modifier</li>
 *     <li>With any name</li>
 *     <li>With return type - {@link Predicate}&lt;TargetEntity&gt;, where 'TargetEntity' is a value of {@link #entity()}
 *     parameter of current annotation. Or it just can be {@link Object}</li>
 *     <li>With Spring beans required for predicate logic as parameters</li>
 *     <li>Annotated with {@link IndexablePredicate}</li>
 * </ul>
 * Create and return your predicate within method body.
 * <p>
 * <b>Note:</b> instance passed to predicate includes only declared indexable properties, others are unfetched.
 * To get access to them you need to reload instance with proper fetch plan within predicate.
 * <p>
 * Example:
 * <p>
 * 'status' property is not declared as indexable but required for predicate - instance should be reloaded.
 * <pre>
 * &#64;JmixEntitySearchIndex(entity = MyEntity.class)
 * public interface MyEntityIndexDefinition {
 *
 *     &#64;AutoMappedField(includeProperties = "name")
 *     void mapping();
 *
 *     &#64;IndexablePredicate
 *     default Predicate&lt;MyEntity&gt; indexOpenOnlyPredicate(DataManager dataManager) {
 *         return (instance) -&gt; {
 *             Id&lt;MyEntity&gt; id = Id.of(instance);
 *             MyEntity reloadedInstance = dataManager.load(id)
 *                     .fetchPlanProperties("status")
 *                     .one();
 *             Status status = reloadedInstance.getStatus();
 *             return Status.OPEN.equals(status);
 *         };
 *     }
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JmixEntitySearchIndex {

    /**
     * Provides entity that should be indexed using this index definition interface.
     * <p>All properties defined in further field-mapping annotation related to this entity.
     *
     * @return entity class
     */
    Class<?> entity();

    /**
     * Names the index of this entity, overriding the application-wide pattern.
     * <p>
     * The value is a pattern: the placeholders {@code {entityName}} and {@code {tenantId}} are replaced, and
     * everything else is taken as written. A value without placeholders is therefore the index name itself:
     * {@code indexName = "orders"} puts the entity into the index {@code orders}.
     * <p>
     * A placeholder is required exactly where one pattern has to produce several names. The data of a tenant-aware
     * entity is stored in a separate index per tenant, so its pattern must contain {@code {tenantId}}, e.g.
     * {@code "orders_{tenantId}"}; the pattern of any other entity must not contain it. {@code {entityName}} is
     * never required here — the pattern already belongs to one entity — but it is allowed.
     * <p>
     * If not set, the index name is built from the {@code jmix.search.tenantless-index-name-pattern} or
     * {@code jmix.search.tenant-index-name-pattern} application property.
     *
     * @return pattern of the index name of this entity
     */
    String indexName() default "";
}
