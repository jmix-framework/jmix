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

/**
 * Represents a dataset constraint. For instance:
 * <pre>
 * new DatasetConstraint(1, "country_name = 'United States'")
 * </pre>
 *
 * @param dataset an integer ID of Superset dataset. The dataset ID can be found through the URL address of dataset
 *                in datasets list. Other way is to get all datasets from API, it will return datasets with ids.
 * @param clause  native SQL condition that will be appended to "WHERE" clause
 */
public record DatasetConstraint(Integer dataset, String clause) {
}
