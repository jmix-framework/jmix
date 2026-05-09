/*
 * Copyright 2026 Haulmont.
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

package io.jmix.security.constraint;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.RowLevelPolicy;

import java.util.stream.Stream;

/**
 * Contributes additional policies to {@link PolicyStore} implementations.
 */
public interface PolicyStoreContributor {

    /**
     * Returns extra row-level policies for the specified entity.
     *
     * @param metaClass entity metadata
     * @return row-level policies
     */
    default Stream<RowLevelPolicy> getRowLevelPolicies(MetaClass metaClass) {
        return Stream.empty();
    }

    /**
     * Returns extra entity policies for the specified entity.
     *
     * @param metaClass entity metadata
     * @return entity policies
     */
    default Stream<ResourcePolicy> getEntityResourcePolicies(MetaClass metaClass) {
        return Stream.empty();
    }

    /**
     * Returns extra entity policies for the specified wildcard resource.
     *
     * @param wildcard wildcard resource name
     * @return entity policies
     */
    default Stream<ResourcePolicy> getEntityResourcePoliciesByWildcard(String wildcard) {
        return Stream.empty();
    }

    /**
     * Returns extra entity-attribute policies for the specified entity attribute.
     *
     * @param metaClass  entity metadata
     * @param attribute  attribute name
     * @return entity-attribute policies
     */
    default Stream<ResourcePolicy> getEntityAttributesResourcePolicies(MetaClass metaClass, String attribute) {
        return Stream.empty();
    }

    /**
     * Returns extra entity-attribute policies for the specified wildcard resource.
     *
     * @param entityWildcard    entity wildcard
     * @param attributeWildcard attribute wildcard
     * @return entity-attribute policies
     */
    default Stream<ResourcePolicy> getEntityAttributesResourcePoliciesByWildcard(String entityWildcard,
                                                                                 String attributeWildcard) {
        return Stream.empty();
    }

    /**
     * Returns extra specific policies for the specified resource.
     *
     * @param resourceName specific resource name
     * @return specific policies
     */
    default Stream<ResourcePolicy> getSpecificResourcePolicies(String resourceName) {
        return Stream.empty();
    }
}
