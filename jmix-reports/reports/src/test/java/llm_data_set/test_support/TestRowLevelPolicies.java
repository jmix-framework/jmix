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

package llm_data_set.test_support;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.RowLevelPolicy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Gives the current user row-level policies a test chose, and leaves every other policy question to the real
 * store it wraps.
 * <p>
 * A row-level policy normally comes from a role of the authenticated user, and these tests run as the system
 * user, whom no policy applies to. Wrapping the store puts the policies where the platform and the loader both
 * read them from — {@code PolicyStore} — so what a test sets up is answered to the production code paths
 * unchanged.
 */
public class TestRowLevelPolicies implements PolicyStore {

    protected final PolicyStore delegate;

    protected final Map<String, List<RowLevelPolicy>> policies = new LinkedHashMap<>();

    public TestRowLevelPolicies(PolicyStore delegate) {
        this.delegate = delegate;
    }

    /**
     * Adds a policy of the current user, as a role would.
     */
    public void add(String entityName, RowLevelPolicy policy) {
        policies.computeIfAbsent(entityName, key -> new ArrayList<>()).add(policy);
    }

    public void reset() {
        policies.clear();
    }

    @Override
    public Stream<RowLevelPolicy> getRowLevelPolicies(MetaClass entityClass) {
        return Stream.concat(delegate.getRowLevelPolicies(entityClass),
                policies.getOrDefault(entityClass.getName(), List.of()).stream());
    }

    @Override
    public Stream<ResourcePolicy> getEntityResourcePolicies(MetaClass metaClass) {
        return delegate.getEntityResourcePolicies(metaClass);
    }

    @Override
    public Stream<ResourcePolicy> getEntityResourcePoliciesByWildcard(String wildcard) {
        return delegate.getEntityResourcePoliciesByWildcard(wildcard);
    }

    @Override
    public Stream<ResourcePolicy> getEntityAttributesResourcePolicies(MetaClass metaClass, String attribute) {
        return delegate.getEntityAttributesResourcePolicies(metaClass, attribute);
    }

    @Override
    public Stream<ResourcePolicy> getEntityAttributesResourcePoliciesByWildcard(String entityWildcard,
                                                                               String attributeWildcard) {
        return delegate.getEntityAttributesResourcePoliciesByWildcard(entityWildcard, attributeWildcard);
    }

    @Override
    public Stream<ResourcePolicy> getSpecificResourcePolicies(String resourceName) {
        return delegate.getSpecificResourcePolicies(resourceName);
    }
}
