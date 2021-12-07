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
     * Provides explicitly defined name of the search index.
     * <p>If it's not set index name will be based on 'searchIndexNamePrefix' property and entity name.
     *
     * @return custom index name
     */
    String indexName() default "";
}
