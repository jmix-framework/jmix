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

package test_support.settings.analysis;

import io.jmix.search.index.mapping.analysis.AnalysisConfigurationContext;
import io.jmix.search.index.mapping.analysis.IndexAnalysisConfigurer;

import java.util.Arrays;

public class TestIndexAnalysisConfigurer implements IndexAnalysisConfigurer {

    @Override
    public void configure(AnalysisConfigurationContext context) {
        // Analyzer
        context.defineAnalyzer("modified_built_in_analyzer")
                .configureBuiltIn("standard")
                .withParameter("max_token_length", 100)
                .withParameter("stopwords", "_english_");

        context.defineAnalyzer("custom_analyzer_built_in_elements")
                .createCustom()
                .withTokenizer("whitespace")
                .withCharacterFilters("html_strip")
                .withTokenFilters("stop");

        context.defineAnalyzer("custom_analyzer_custom_elements")
                .createCustom()
                .withTokenizer("modified_tokenizer")
                .withCharacterFilters("modified_char_filter", "native_modified_char_filter")
                .withTokenFilters("modified_token_filter", "native_modified_token_filter");

        context.defineAnalyzer("native_modified_built_in_analyzer")
                .withNativeConfiguration(
                        "{" +
                                "\"type\": \"standard\"," +
                                "  \"max_token_length\": 100," +
                                "  \"stopwords\": \"_english_\"" +
                                "}"
                );

        context.defineAnalyzer("native_custom_analyzer_built_in_elements")
                .withNativeConfiguration(
                        "{\n" +
                                "    \"type\": \"custom\",\n" +
                                "    \"tokenizer\": \"whitespace\",\n" +
                                "    \"char_filter\": [\n" +
                                "        \"html_strip\"\n" +
                                "    ],\n" +
                                "    \"filter\": [\n" +
                                "        \"stop\"\n" +
                                "    ]\n" +
                                "}"
                );

        context.defineAnalyzer("native_custom_analyzer_custom_elements")
                .withNativeConfiguration(
                        "{\n" +
                                "    \"type\": \"custom\",\n" +
                                "    \"tokenizer\": \"native_modified_tokenizer\",\n" +
                                "    \"char_filter\": [\n" +
                                "        \"modified_char_filter\",\n" +
                                "        \"native_modified_char_filter\"\n" +
                                "    ],\n" +
                                "    \"filter\": [\n" +
                                "        \"modified_token_filter\",\n" +
                                "        \"native_modified_token_filter\"\n" +
                                "    ]\n" +
                                "}"
                );

        context.defineAnalyzer("not_used_analyzer")
                .configureBuiltIn("standard")
                .withParameter("max_token_length", 100);

        // Tokenizer
        context.defineTokenizer("modified_tokenizer")
                .configureBuiltIn("whitespace")
                .withParameter("max_token_length", 100);

        context.defineTokenizer("native_modified_tokenizer")
                .withNativeConfiguration(
                        "{" +
                                "  \"type\": \"whitespace\"," +
                                "  \"max_token_length\": 100" +
                                "}"
                );

        context.defineTokenizer("not_used_tokenizer")
                .configureBuiltIn("whitespace")
                .withParameter("max_token_length", 50);

        // Character Filters
        context.defineCharacterFilter("modified_char_filter")
                .configureBuiltIn("html_strip")
                .withParameter("escaped_tags", Arrays.asList("b", "i"));

        context.defineCharacterFilter("native_modified_char_filter")
                .withNativeConfiguration(
                        "{" +
                                "  \"type\": \"html_strip\"," +
                                "  \"escaped_tags\": [" +
                                "    \"b\", \"i\"" +
                                "  ]" +
                                "}"
                );

        context.defineCharacterFilter("not_used_char_filter")
                .configureBuiltIn("html_strip")
                .withParameter("escaped_tags", Arrays.asList("div", "p"));

        // Token Filter
        context.defineTokenFilter("modified_token_filter")
                .configureBuiltIn("stop")
                .withParameter("stopwords", "_english_")
                .withParameter("ignore_case", true);

        context.defineTokenFilter("native_modified_token_filter")
                .withNativeConfiguration(
                        "{" +
                                "  \"type\": \"stop\"," +
                                "  \"stopwords\": \"_english_\"," +
                                "  \"ignore_case\": true" +
                                "}"
                );

        context.defineTokenFilter("not_used_token_filter")
                .configureBuiltIn("stop")
                .withParameter("stopwords", "_english_");
    }
}
