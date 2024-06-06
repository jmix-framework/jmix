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

package io.jmix.searchelasticsearch.index;

/**
 * Base interface for configurers of Elasticsearch index settings.
 * <p>
 * Create Spring Bean that implements this interface.
 * Index settings can be configured inside
 * {@link ElasticsearchIndexSettingsConfigurer#configure(ElasticsearchIndexSettingsConfigurationContext)} by using
 * settings builders.
 * <p>
 * See {@link ElasticsearchIndexSettingsConfigurationContext}.
 * <p>
 * Example:
 * <pre>
 * &#64;Component("demo_ElasticsearchIndexSettingsConfigurer")
 * public class DemoElasticsearchIndexSettingsConfigurer implements ElasticsearchIndexSettingsConfigurer {
 *
 *     &#64;Override
 *     public void configure(ElasticsearchIndexSettingsConfigurationContext context) {
 *         IndexSettings.Builder commonSettingsBuilder = context.getCommonSettingsBuilder();
 *         commonSettingsBuilder
 *                 .maxResultWindow(15000)
 *                 .analysis(analysisBuilder ->
 *                         analysisBuilder.analyzer("customized_standard", analyzerBuilder ->
 *                                 analyzerBuilder.standard(stdAnalyzerBuilder ->
 *                                         stdAnalyzerBuilder.maxTokenLength(100)
 *                                 )
 *                         )
 *                 );
 *
 *         IndexSettings.Builder personSettingsBuilder = context.getEntitySettingsBuilder(Person.class);
 *         personSettingsBuilder
 *                 .maxResultWindow(20000)
 *                 .analysis(analysisBuilder ->
 *                         analysisBuilder.analyzer("customized_standard", analyzerBuilder ->
 *                                 analyzerBuilder.standard(stdAnalyzerBuilder ->
 *                                         stdAnalyzerBuilder.maxTokenLength(100)
 *                                 )
 *                         )
 *                 );
 *     }
 * }
 * </pre>
 */
public interface ElasticsearchIndexSettingsConfigurer {

    void configure(ElasticsearchIndexSettingsConfigurationContext context);
}
