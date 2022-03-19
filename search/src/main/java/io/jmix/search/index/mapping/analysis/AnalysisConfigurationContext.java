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

import static io.jmix.search.index.mapping.analysis.AnalysisConfigurationStages.*;

/**
 * Allows to configure Elasticsearch analysis elements.
 */
public interface AnalysisConfigurationContext {

    /**
     * Init definition of new analyzer.
     *
     * @param name name of the new analyzer
     * @return Initial stage of analyzer configuration
     */
    DefineAnalyzer defineAnalyzer(String name);

    /**
     * Init definition of new normalizer.
     *
     * @param name name of the new normalizer
     * @return Initial stage of normalizer configuration
     */
    DefineNormalizer defineNormalizer(String name);

    /**
     * Init definition of new tokenizer.
     *
     * @param name name of the new tokenizer
     * @return Initial stage of tokenizer configuration
     */
    DefineTokenizer defineTokenizer(String name);

    /**
     * Init definition of new character filter.
     *
     * @param name name of the new character filter
     * @return Initial stage of character filter configuration
     */
    DefineCharacterFilter defineCharacterFilter(String name);

    /**
     * Init definition of new token filter
     *
     * @param name name of the new token filter
     * @return Initial stage of token filter configuration
     */
    DefineTokenFilter defineTokenFilter(String name);
}
