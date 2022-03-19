/*
 * Copyright 2021 Haulmont.
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

package io.jmix.search.index;

/**
 * Base interface for configurers of Elasticsearch index settings.
 * <p>
 * Create Spring Bean that implements this interface.
 * Index settings can be configured inside {@link IndexSettingsConfigurer#configure(IndexSettingsConfigurationContext)}.
 * <p>
 * See {@link IndexSettingsConfigurationContext}.
 * <p>
 * Example:
 * <p>
 * Settings of all search indexes will have common values "index.max_result_window"=15000 and "index.mapping.total_fields.limit"=1500
 * but settings of index related to entity class 'DemoEntity' will have common value "index.max_result_window"=15000 and explicit value "index.mapping.total_fields.limit"=2000.
 * <p>
 * Configurer:<pre>
 * &#64;Component("demo_IndexSettingsConfigurer")
 * public class DemoIndexSettingsConfigurer implements IndexSettingsConfigurer {
 *
 *     &#64;Override
 *     public void configure(&#64;Nonnull IndexSettingsConfigurationContext context) {
 *         context.getCommonSettingsBuilder()
 *                 .put("index.max_result_window", 15000)
 *                 .put("index.mapping.total_fields.limit", 1500);
 *
 *         context.getEntitySettingsBuilder(DemoEntity.class)
 *                 .put("index.mapping.total_fields.limit", 2000);
 *     }
 * }
 * </pre>
 */
public interface IndexSettingsConfigurer {

    void configure(IndexSettingsConfigurationContext context);
}
