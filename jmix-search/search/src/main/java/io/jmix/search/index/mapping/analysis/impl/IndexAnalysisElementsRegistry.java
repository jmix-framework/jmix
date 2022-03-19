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

package io.jmix.search.index.mapping.analysis.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.search.index.mapping.analysis.IndexAnalysisConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Contains configurations of all user-defined analysis elements.
 */
@Component("search_IndexAnalysisElementsRegistry")
public class IndexAnalysisElementsRegistry {

    protected final Map<String, AnalysisElementConfiguration> analyzers;
    protected final Map<String, AnalysisElementConfiguration> normalizers;
    protected final Map<String, AnalysisElementConfiguration> tokenizers;
    protected final Map<String, AnalysisElementConfiguration> characterFilters;
    protected final Map<String, AnalysisElementConfiguration> tokenFilters;

    @Autowired
    public IndexAnalysisElementsRegistry(List<IndexAnalysisConfigurer> indexAnalysisConfigurers) {
        AnalysisConfigurationContextImpl context = new AnalysisConfigurationContextImpl();
        indexAnalysisConfigurers.forEach(configurer -> configurer.configure(context));

        this.analyzers = buildAnalysisElements(
                context.getAnalyzerConfigurers(),
                "Multiple analyzers declared with the same name"
        );
        this.normalizers = buildAnalysisElements(
                context.getNormalizerConfigurers(),
                "Multiple normalizers declared with the same name"
        );
        this.tokenizers = buildAnalysisElements(
                context.getTokenizerConfigurers(),
                "Multiple tokenizers declared with the same name"
        );
        this.characterFilters = buildAnalysisElements(
                context.getCharacterFilterConfigurers(),
                "Multiple character filters declared with the same name"
        );
        this.tokenFilters = buildAnalysisElements(
                context.getTokenFilterConfigurers(),
                "Multiple token filters declared with the same name"
        );
    }

    @Nullable
    public AnalysisElementConfiguration getAnalyzer(String name) {
        return analyzers.get(name);
    }

    @Nullable
    public AnalysisElementConfiguration getNormalizer(String name) {
        return normalizers.get(name);
    }

    @Nullable
    public AnalysisElementConfiguration getTokenizer(String name) {
        return tokenizers.get(name);
    }

    @Nullable
    public AnalysisElementConfiguration getCharacterFilter(String name) {
        return characterFilters.get(name);
    }

    @Nullable
    public AnalysisElementConfiguration getTokenFilter(String name) {
        return tokenFilters.get(name);
    }

    /**
     * Resolves all user-defined analysis elements required for provided analyzer (including itself).
     * <ul>
     *     <li>If name of modified analyzer is provided - result contains configuration of this analyzer only.</li>
     *     <li>If name of completely custom analyzer is provided - result contains configurations of this analyzer
     *     and user-defined tokenizer, character filters and token filters.
     *     Built-in analysis elements included into user-defined custom analyzer is not present in result.</li>
     *     <li>If name of built-in analyzer is provided - result is empty</li>
     * </ul>
     *
     * @param name name of analyzer
     * @return set of analysis element configurations
     */
    public Set<AnalysisElementConfiguration> resolveAllUsedCustomElementsForAnalyzer(String name) {
        AnalysisElementConfiguration analyzer = getAnalyzer(name);
        return resolveAllUsedCustomElements(analyzer);
    }

    /**
     * Resolves all user-defined analysis elements required for provided normalizer (including itself).
     * <ul>
     *     <li>If name of modified normalizer is provided - result contains configuration of this normalizer only.</li>
     *     <li>If name of completely custom normalizer is provided - result contains configurations of this normalizer
     *     and user-defined character filters and token filters.
     *     Built-in analysis elements included into user-defined custom normalizer is not present in result.</li>
     *     <li>If name of built-in normalizer is provided - result is empty</li>
     * </ul>
     *
     * @param name name of normalizer
     * @return set of analysis element configurations
     */
    public Set<AnalysisElementConfiguration> resolveAllUsedCustomElementsForNormalizer(String name) {
        AnalysisElementConfiguration normalizer = getNormalizer(name);
        return resolveAllUsedCustomElements(normalizer);
    }

    protected Set<AnalysisElementConfiguration> resolveAllUsedCustomElements(AnalysisElementConfiguration rootElement) {
        Set<AnalysisElementConfiguration> result = new HashSet<>();
        if (rootElement != null) {
            result.add(rootElement);
            ObjectNode config = rootElement.getConfig();
            if ("custom".equals(config.path("type").textValue())) {
                processCustomRootElementConfig(config, result);
            }
        }

        return result;
    }

    protected void processCustomRootElementConfig(ObjectNode config, Set<AnalysisElementConfiguration> result) {
        fillCustomTokenizer(config, result);
        fillCustomCharFilters(config, result);
        fillCustomTokenFilters(config, result);
    }

    protected void fillCustomTokenizer(ObjectNode config, Set<AnalysisElementConfiguration> result) {
        JsonNode tokenizer = config.path("tokenizer");
        if (tokenizer.isTextual()) {
            AnalysisElementConfiguration customTokenizer = getTokenizer(tokenizer.textValue());
            if (customTokenizer != null) {
                result.add(customTokenizer);
            }
        }
    }

    protected void fillCustomCharFilters(ObjectNode config, Set<AnalysisElementConfiguration> result) {
        Set<String> charFilterNames = new HashSet<>();

        JsonNode charFilter = config.path("char_filter");
        if (charFilter.isArray()) {
            charFilter.forEach(item -> {
                if (item.isTextual()) {
                    charFilterNames.add(item.textValue());
                    AnalysisElementConfiguration customCharFilter = getCharacterFilter(item.textValue());
                    if (customCharFilter != null) {
                        result.add(customCharFilter);
                    }
                }
            });
        } else if (charFilter.isTextual()) {
            charFilterNames.add(charFilter.textValue());
        }
        charFilterNames.forEach(charFilterName -> {
            AnalysisElementConfiguration customCharFilter = getCharacterFilter(charFilterName);
            if (customCharFilter != null) {
                result.add(customCharFilter);
            }
        });
    }

    protected void fillCustomTokenFilters(ObjectNode config, Set<AnalysisElementConfiguration> result) {
        Set<String> tokenFilterNames = new HashSet<>();

        JsonNode tokenFilter = config.path("filter");
        if (tokenFilter.isArray()) {
            tokenFilter.forEach(item -> {
                if (item.isTextual()) {
                    tokenFilterNames.add(item.textValue());
                }
            });
        } else if (tokenFilter.isTextual()) {
            tokenFilterNames.add(tokenFilter.textValue());
        }
        tokenFilterNames.forEach(tokenFilterName -> {
            AnalysisElementConfiguration customTokenFilter = getTokenFilter(tokenFilterName);
            if (customTokenFilter != null) {
                result.add(customTokenFilter);
            }
        });
    }

    protected Map<String, AnalysisElementConfiguration> buildAnalysisElements(
            List<? extends AnalysisElementConfigurer> elementConfigurers,
            String duplicateNameErrorMessage) {
        return elementConfigurers.stream()
                .map(AnalysisElementConfigurer::build)
                .collect(Collectors.toMap(
                        AnalysisElementConfiguration::getName,
                        Function.identity(),
                        (config1, config2) -> {
                            throw new RuntimeException(duplicateNameErrorMessage);
                        })
                );
    }
}
