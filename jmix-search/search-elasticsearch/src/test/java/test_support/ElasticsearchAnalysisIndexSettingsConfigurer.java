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

package test_support;

import co.elastic.clients.elasticsearch.indices.IndexSettings;
import io.jmix.searchelasticsearch.index.ElasticsearchIndexSettingsConfigurationContext;
import io.jmix.searchelasticsearch.index.ElasticsearchIndexSettingsConfigurer;
import org.springframework.stereotype.Component;
import test_support.entity.TestRootEntity;

@Component
public class ElasticsearchAnalysisIndexSettingsConfigurer implements ElasticsearchIndexSettingsConfigurer {

    @Override
    public void configure(ElasticsearchIndexSettingsConfigurationContext context) {
        IndexSettings.Builder commonSettingsBuilder = context.getCommonSettingsBuilder();
        commonSettingsBuilder
                .maxResultWindow(15000)
                .analysis(analysisBuilder ->
                        analysisBuilder.analyzer("customized_standard", analyzerBuilder ->
                                analyzerBuilder.standard(stdAnalyzerBuilder ->
                                        stdAnalyzerBuilder.maxTokenLength(100)
                                )
                        )
                );

        IndexSettings.Builder rootEntitySettingsBuilder = context.getEntitySettingsBuilder(TestRootEntity.class);
        rootEntitySettingsBuilder
                .maxResultWindow(15000)
                .maxRegexLength(2000)
                .analysis(analysisBuilder ->
                        analysisBuilder.analyzer("customized_standard", analyzerBuilder ->
                                analyzerBuilder.standard(stdAnalyzerBuilder ->
                                        stdAnalyzerBuilder.maxTokenLength(150)
                                )
                        )
                );
    }
}