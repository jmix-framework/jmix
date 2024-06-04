/*
 * Copyright 2024 Haulmont.
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

package io.jmix.supersetflowui.component.dataconstraint;

import io.jmix.supersetflowui.component.SupersetDashboard;

import java.util.List;

/**
 * Interface to be implemented by Spring beans that should be used in {@link SupersetDashboard} component.
 * <p>
 * For instance:
 * <pre>
 * &#64;Component("app_CovidDatasetConstraint")
 * public class DashboardDatasetConstraintsProvider implements DatasetConstraintsProvider {
 *
 *     &#64;Override
 *     public List&lt;DatasetConstraint&gt; getConstraints() {
 *         return List.of(new DatasetConstraint(13, "country_name = 'United States'"));
 *     }
 * }
 * </pre>
 * And view descriptor:
 * <pre>
 * &lt;superset:dashboard id="dashboard"
 *                     height="100%"
 *                     width="100%"
 *                     datasetConstraintsProviderBean="app_CovidDatasetConstraint"
 *                     embeddedId="d0ec568a-04cb-4408-a072-f2ba4e011f20"/&gt;
 * </pre>
 */
public interface DatasetConstraintsProvider {

    /**
     * @return list of dataset constraints
     */
    List<DatasetConstraint> getConstraints();
}
