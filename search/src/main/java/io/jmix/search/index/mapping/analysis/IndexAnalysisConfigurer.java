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

package io.jmix.search.index.mapping.analysis;

/**
 * Base interface for configurers of Elasticsearch analysis elements (analyzers, normalizers, etc).
 * <p>
 * Create Spring Bean that implements this interface.
 * <p>
 * Analysis elements can be configured inside {@link IndexAnalysisConfigurer#configure(AnalysisConfigurationContext)}
 * <p>
 * Example:<pre>
 * &#64;Component
 * public class CustomIndexAnalysisConfigurer implements IndexAnalysisConfigurer {
 *      &#64;Override
 *     public void configure(AnalysisConfigurationContext context) {
 *         // Analyzer
 *         context.defineAnalyzer("configured_builtin_analyzer")
 *                 .configureBuiltIn("standard")
 *                 .withParameter("max_token_length", 100)
 *                 .withParameter("stopwords", "_english_");
 *
 *         context.defineAnalyzer("custom_analyzer")
 *                 .createCustom()
 *                 .withTokenizer("whitespace")
 *                 .withCharacterFilters("html_strip")
 *                 .withTokenFilters("stop");
 *
 *         context.defineAnalyzer("analyzer_with_native_config")
 *                 .withNativeConfiguration(
 *                         "{" +
 *                                 "\"type\": \"standard\"," +
 *                                 "  \"max_token_length\": 100," +
 *                                 "  \"stopwords\": \"_english_\"" +
 *                                 "}"
 *                 );
 *
 *         // Normalizer
 *         context.defineNormalizer("custom_normalizer")
 *                 .createCustom()
 *                 .withCharacterFilters("html_strip")
 *                 .withTokenFilters("lowercase");
 *
 *         context.defineNormalizer("normalizer_with_native_config")
 *                 .withNativeConfiguration(
 *                         "{" +
 *                                 "  \"type\": \"custom\"," +
 *                                 "  \"filter\": [" +
 *                                 "    \"lowercase\"," +
 *                                 "    \"asciifolding\"" +
 *                                 "  ]" +
 *                                 "}"
 *                 );
 *
 *         // Tokenizer
 *         context.defineTokenizer("configured_tokenizer")
 *                 .configureBuiltIn("whitespace")
 *                 .withParameter("max_token_length", 100);
 *
 *         context.defineTokenizer("tokenizer_with_native_config")
 *                 .withNativeConfiguration(
 *                         "{" +
 *                                 "  \"type\": \"standard\"," +
 *                                 "  \"max_token_length\": 100" +
 *                                 "}"
 *                 );
 *
 *         // Character Filters
 *         context.defineCharacterFilter("configured_char_filter")
 *                 .configureBuiltIn("html_strip")
 *                 .withParameter("escaped_tags", Arrays.asList("b", "i"));
 *
 *         context.defineCharacterFilter("char_filter_with_native_config")
 *                 .withNativeConfiguration(
 *                         "{" +
 *                                 "  \"type\": \"html_strip\"," +
 *                                 "  \"escaped_tags\": [" +
 *                                 "    \"b\"" +
 *                                 "  ]" +
 *                                 "}"
 *                 );
 *
 *         // Token Filter
 *         context.defineTokenFilter("configured_token_filter")
 *                 .configureBuiltIn("stop")
 *                 .withParameter("stopwords", "_russian_")
 *                 .withParameter("ignore_case", "true");
 *
 *         context.defineTokenFilter("filter_with_native_config")
 *                 .withNativeConfiguration(
 *                         "{" +
 *                                 "  \"type\": \"ngram\"," +
 *                                 "  \"min_gram\": 3," +
 *                                 "  \"max_gram\": 5" +
 *                                 "}"
 *                 );
 *     }
 * }
 * </pre>
 */
public interface IndexAnalysisConfigurer {

    void configure(AnalysisConfigurationContext context);
}
