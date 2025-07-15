/*
 * Copyright 2025 Haulmont.
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

package io.jmix.search.index.mapping.processor.impl;


import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.search.index.mapping.AttributesConfigurationGroup;
import io.jmix.search.utils.PropertyTools;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractAttributesGroupProcessor<Group extends AttributesConfigurationGroup>
        implements AttributesGroupProcessor<Group> {
    protected final PropertyTools propertyTools;

    protected AbstractAttributesGroupProcessor(PropertyTools propertyTools) {
        this.propertyTools = propertyTools;
    }
}
