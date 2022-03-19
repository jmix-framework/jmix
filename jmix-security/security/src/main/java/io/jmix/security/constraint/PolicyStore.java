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

package io.jmix.security.constraint;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.RowLevelPolicy;

import java.util.stream.Stream;

/**
 * Provides all security policies of the current user.
 */
public interface PolicyStore {

    Stream<RowLevelPolicy> getRowLevelPolicies(MetaClass entityClass);

    Stream<ResourcePolicy> getEntityResourcePolicies(MetaClass metaClass);

    Stream<ResourcePolicy> getEntityResourcePoliciesByWildcard(String wildcard);

    Stream<ResourcePolicy> getEntityAttributesResourcePolicies(MetaClass metaClass, String attribute);

    Stream<ResourcePolicy> getEntityAttributesResourcePoliciesByWildcard(String entityWildcard, String attributeWildcard);

    Stream<ResourcePolicy> getSpecificResourcePolicies(String resourceName);

    Stream<ResourcePolicy> getGraphQLResourcePolicies(String resourceName);
}
