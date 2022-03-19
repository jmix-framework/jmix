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

import io.jmix.core.common.util.Preconditions;
import io.jmix.search.index.mapping.analysis.AnalysisConfigurationContext;
import io.jmix.search.index.mapping.analysis.AnalysisConfigurationStages;

import java.util.ArrayList;
import java.util.List;

public class AnalysisConfigurationContextImpl implements AnalysisConfigurationContext {

    protected List<AnalyzerConfigurer> analyzerConfigurers;
    protected List<NormalizerConfigurer> normalizerConfigurers;
    protected List<TokenizerConfigurer> tokenizerConfigurers;
    protected List<CharacterFilterConfigurer> characterFilterConfigurers;
    protected List<TokenFilterConfigurer> tokenFilterConfigurers;

    protected AnalysisConfigurationContextImpl() {
        analyzerConfigurers = new ArrayList<>();
        normalizerConfigurers = new ArrayList<>();
        tokenizerConfigurers = new ArrayList<>();
        characterFilterConfigurers = new ArrayList<>();
        tokenFilterConfigurers = new ArrayList<>();
    }

    @Override
    public AnalysisConfigurationStages.DefineAnalyzer defineAnalyzer(String name) {
        Preconditions.checkNotEmptyString(name, "Analyzer name is not specified");
        AnalyzerConfigurer configurer = new AnalyzerConfigurer(name);
        analyzerConfigurers.add(configurer);
        return configurer;
    }

    @Override
    public AnalysisConfigurationStages.DefineNormalizer defineNormalizer(String name) {
        Preconditions.checkNotEmptyString(name, "Normalizer name is not specified");
        NormalizerConfigurer configurer = new NormalizerConfigurer(name);
        normalizerConfigurers.add(configurer);
        return configurer;
    }

    @Override
    public AnalysisConfigurationStages.DefineTokenizer defineTokenizer(String name) {
        Preconditions.checkNotEmptyString(name, "Tokenizer name is not specified");
        TokenizerConfigurer configurer = new TokenizerConfigurer(name);
        tokenizerConfigurers.add(configurer);
        return configurer;
    }

    @Override
    public AnalysisConfigurationStages.DefineCharacterFilter defineCharacterFilter(String name) {
        Preconditions.checkNotEmptyString(name, "Character Filter name is not specified");
        CharacterFilterConfigurer configurer = new CharacterFilterConfigurer(name);
        characterFilterConfigurers.add(configurer);
        return configurer;
    }

    @Override
    public AnalysisConfigurationStages.DefineTokenFilter defineTokenFilter(String name) {
        Preconditions.checkNotEmptyString(name, "Token Filter name is not specified");
        TokenFilterConfigurer configurer = new TokenFilterConfigurer(name);
        tokenFilterConfigurers.add(configurer);
        return configurer;
    }

    public List<AnalyzerConfigurer> getAnalyzerConfigurers() {
        return analyzerConfigurers;
    }

    public List<NormalizerConfigurer> getNormalizerConfigurers() {
        return normalizerConfigurers;
    }

    public List<TokenizerConfigurer> getTokenizerConfigurers() {
        return tokenizerConfigurers;
    }

    public List<CharacterFilterConfigurer> getCharacterFilterConfigurers() {
        return characterFilterConfigurers;
    }

    public List<TokenFilterConfigurer> getTokenFilterConfigurers() {
        return tokenFilterConfigurers;
    }
}
