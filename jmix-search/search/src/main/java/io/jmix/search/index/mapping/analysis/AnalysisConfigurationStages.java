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
 * Stages of analysis configuration fluent API
 */
public interface AnalysisConfigurationStages {

    interface DefineAnalyzer extends SetupNativeConfiguration, ConfigureBuiltIn {
        SetupTokenizer createCustom();
    }

    interface DefineNormalizer extends SetupNativeConfiguration, ConfigureBuiltIn {
        SetupFilters createCustom();
    }

    interface DefineTokenizer extends SetupNativeConfiguration, ConfigureBuiltIn {
    }

    interface DefineCharacterFilter extends SetupNativeConfiguration, ConfigureBuiltIn {
    }

    interface DefineTokenFilter extends SetupNativeConfiguration, ConfigureBuiltIn {
    }

    interface SetupNativeConfiguration {
        void withNativeConfiguration(String nativeConfiguration);
    }

    interface ConfigureBuiltIn {
        SetupParameters configureBuiltIn(String builtInTypeName);
    }

    interface SetupParameters {
        SetupParameters withParameter(String key, Object value);
    }

    interface SetupTokenizer {
        SetupFilters withTokenizer(String tokenizerName);
    }

    interface SetupCharacterFilters {
        SetupFilters withCharacterFilters(String... charFilterNames);
    }

    interface SetupTokenFilters {
        SetupFilters withTokenFilters(String... tokenFilterNames);
    }

    interface SetupFilters extends SetupCharacterFilters, SetupTokenFilters {
    }
}
