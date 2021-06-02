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

package io.jmix.search.index.mapping.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Map;

@Component("search_DisplayedNameValueExtractor")
public class DisplayedNameValueExtractor implements PropertyValueExtractor {

    protected final MetadataTools metadataTools;

    @Autowired
    public DisplayedNameValueExtractor(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Override
    public JsonNode getValue(Object entity, @Nullable MetaPropertyPath propertyPath, Map<String, Object> parameters) {
        String instanceName = metadataTools.getInstanceName(entity);
        return TextNode.valueOf(instanceName);
    }
}
