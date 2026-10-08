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

import io.jmix.core.AccessConstraintsRegistry;
import io.jmix.security.constraint.PolicyStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Leaves the loader Reports declares in place — a test on this configuration executes queries for real — and
 * adds a constraint a test can use to narrow what the current user may read.
 */
@Configuration
public class LlmQueryExecutionTestConfiguration {

    @Bean
    public DenyingLoadValuesConstraint denyingLoadValuesConstraint(AccessConstraintsRegistry registry) {
        DenyingLoadValuesConstraint constraint = new DenyingLoadValuesConstraint();
        registry.register(constraint);
        // Entity READ is decided by CrudEntityContext, which the value-load constraint above cannot answer, so
        // both halves are registered and driven by the same denied set.
        registry.register(new CrudEntityDenial(constraint));
        return constraint;
    }

    /**
     * Puts the row-level policies a test sets up where the platform and the loader read them from. Primary, so
     * that both the platform's own constraint and the loader see the same store; the platform's own store is
     * asked for by name, since asking for the type here would ask for this bean.
     */
    @Bean
    @Primary
    public TestRowLevelPolicies testRowLevelPolicies(
            @Qualifier("sec_AuthenticationPolicyStore") PolicyStore delegate) {
        return new TestRowLevelPolicies(delegate);
    }
}
