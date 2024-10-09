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

package io.jmix.searchopensearch.index;

import io.jmix.search.index.IndexSettingsConfigurer;

/**
 * Base interface for configurers of OpenSearch index settings.
 * <p>
 * Create Spring Bean that implements this interface.
 * Index settings can be configured inside
 * {@link OpenSearchIndexSettingsConfigurer#configure(OpenSearchIndexSettingsConfigurationContext)} by using
 * settings and analysis builders acquired from {@link OpenSearchIndexSettingsConfigurationContext}.
 * <p>
 * Example:
 * <pre>
 * &#64;Component("demo_OpenSearchIndexSettingsConfigurer")
 * public class DemoOpenSearchIndexSettingsConfigurer implements OpenSearchIndexSettingsConfigurer {
 *
 *     &#64;Override
 *     public void configure(OpenSearchIndexSettingsConfigurationContext context) {
 *         context.getCommonIndexSettingsBuilder().maxResultWindow(15000);
 *         context.getEntityIndexSettingsBuilder(Person.class).maxResultWindow(20000);
 *
 *         context.getCommonAnalysisBuilder().analyzer("customized_standard", analyzerBuilder ->
 *                 analyzerBuilder.standard(stdAnalyzerBuilder ->
 *                         stdAnalyzerBuilder.maxTokenLength(100)
 *                 )
 *         );
 *         context.getEntityAnalysisBuilder(Person.class).analyzer("customized_standard", analyzerBuilder ->
 *                 analyzerBuilder.standard(stdAnalyzerBuilder ->
 *                         stdAnalyzerBuilder.maxTokenLength(150)
 *                 )
 *         );
 *     }
 * }
 * </pre>
 */
public interface OpenSearchIndexSettingsConfigurer extends IndexSettingsConfigurer<OpenSearchIndexSettingsConfigurationContext> {

    void configure(OpenSearchIndexSettingsConfigurationContext context);
}
