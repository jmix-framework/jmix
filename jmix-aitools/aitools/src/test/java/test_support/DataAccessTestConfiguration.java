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

package test_support;

import io.jmix.core.AccessConstraintsRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Test configuration that adds a {@link DenyingLoadValuesConstraint} registered with the real
 * {@link AccessConstraintsRegistry}, so {@code AccessManager.applyRegisteredConstraints} exercises it
 * exactly like a production security constraint.
 */
@Configuration
@Import(AiToolsTestConfiguration.class)
public class DataAccessTestConfiguration {

    @Bean
    public DenyingLoadValuesConstraint denyingLoadValuesConstraint(AccessConstraintsRegistry accessConstraintsRegistry) {
        DenyingLoadValuesConstraint constraint = new DenyingLoadValuesConstraint();
        accessConstraintsRegistry.register(constraint);
        return constraint;
    }
}
