/*
 * Copyright 2020 Haulmont.
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

package io.jmix.search.index.mapping.fieldmapper.impl;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.search.index.mapping.ExtendedSearchSettings;

import java.util.Map;

public abstract class SimpleFieldMapper extends AbstractFieldMapper {

    @Override
    public ObjectNode createJsonConfiguration(Map<String, Object> parameters, ExtendedSearchSettings extendedSearchSettings) {
        Map<String, Object> nativeParameters = createEffectiveNativeParameters(parameters);
        nativeParameters.put("type", getSearchPlatformDatatype());
        ObjectNode baseConfig = objectMapper.convertValue(nativeParameters, ObjectNode.class);

        ObjectNode resultConfig = baseConfig;
        if(isExtendedSearchSupported()) {
            if (extendedSearchSettings.isEnabled()) {
                resultConfig = applyExtendedSearch(baseConfig.deepCopy(), extendedSearchSettings);
            }
        }

        return resultConfig;
    }

    abstract boolean isExtendedSearchSupported();

    protected ObjectNode applyExtendedSearch(ObjectNode config, ExtendedSearchSettings extendedSearchSettings) {
        return config;
    }

    protected abstract String getSearchPlatformDatatype();
}
