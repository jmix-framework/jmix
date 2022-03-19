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

package io.jmix.security.authentication;

import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.RowLevelPolicy;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * An granted authority that stores security policies.
 *
 * @see ResourcePolicy
 * @see RowLevelPolicy
 */
public interface PolicyAwareGrantedAuthority extends GrantedAuthority {
    Collection<String> getScopes();

    Collection<ResourcePolicy> getResourcePolicies();

    Collection<RowLevelPolicy> getRowLevelPolicies();

    <I extends ResourcePolicyIndex> Stream<ResourcePolicy> getResourcePoliciesByIndex(
            Class<I> indexClass, Function<I, Stream<ResourcePolicy>> extractor);

    <I extends RowLevelPolicyIndex> Stream<RowLevelPolicy> getRowLevelPoliciesByIndex(
            Class<I> indexClass, Function<I, Stream<RowLevelPolicy>> extractor);
}
