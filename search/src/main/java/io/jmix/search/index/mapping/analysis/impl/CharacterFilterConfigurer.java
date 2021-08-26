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

import com.fasterxml.jackson.databind.node.ObjectNode;

import static io.jmix.search.index.mapping.analysis.impl.AnalysisElementType.CHAR_FILTER;

public class CharacterFilterConfigurer extends AnalysisElementConfigurer implements CharacterFilterConfigurationStages {

    protected CharacterFilterConfigurer(String name) {
        super(name);
    }

    @Override
    protected AnalysisElementType getType() {
        return CHAR_FILTER;
    }

    @Override
    protected ObjectNode createCustomConfig() {
        throw new RuntimeException("Unable to build custom character filter");
    }
}
