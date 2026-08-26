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

package io.jmix.reports.test_support.role;

import io.jmix.reports.test_support.entity.Publisher;
import io.jmix.security.role.annotation.JpqlRowLevelPolicy;
import io.jmix.security.role.annotation.RowLevelRole;

/**
 * Shows one publisher and hides the rest, as an application's own row-level role would. Lets a test check the
 * whole chain — a role assigned to a user, the policies it puts into {@code PolicyStore}, and what a query then
 * returns — rather than only the half a test can set up directly.
 * <p>
 * Lives here because annotated roles are found by the classpath scanner in the base packages of the Jmix modules,
 * which a test's own package is not one of.
 */
@RowLevelRole(name = "Test publisher row-level role", code = TestPublisherRowLevelRole.CODE)
public interface TestPublisherRowLevelRole {

    String CODE = "test-publisher-row-level";

    /**
     * The publisher this role leaves visible. Prefixed like the rows of {@code LlmQueryExecutionTest}, whose data
     * this role is written for.
     */
    String VISIBLE_PUBLISHER = "LlmQueryExecution Nintendo";

    @JpqlRowLevelPolicy(entityClass = Publisher.class, where = "{E}.name = '" + VISIBLE_PUBLISHER + "'")
    void publisher();
}
