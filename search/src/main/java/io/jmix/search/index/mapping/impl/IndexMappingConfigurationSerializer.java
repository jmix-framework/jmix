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

package io.jmix.search.index.mapping.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.jmix.search.index.mapping.DisplayedNameDescriptor;
import io.jmix.search.index.mapping.IndexMappingConfiguration;
import io.jmix.search.index.mapping.MappingFieldDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class IndexMappingConfigurationSerializer extends StdSerializer<IndexMappingConfiguration> {

    private static final long serialVersionUID = -6118284896645343097L;

    private static final Logger log = LoggerFactory.getLogger(IndexMappingConfigurationSerializer.class);

    protected IndexMappingConfigurationSerializer() {
        super(IndexMappingConfiguration.class);
    }

    @Override
    public void serialize(IndexMappingConfiguration configuration, JsonGenerator gen, SerializerProvider provider) throws IOException {
        log.trace("Start serialize Index Mapping Config: {}", configuration);

        ObjectNode mergedMappingStructure = mergeFields(configuration);
        log.trace("Result mapping structure = {}", mergedMappingStructure);
        gen.writeTree(mergedMappingStructure);
    }

    protected ObjectNode mergeFields(IndexMappingConfiguration configuration) {
        ObjectNode root = JsonNodeFactory.instance.objectNode();
        ObjectNode rootProperties = root.putObject("properties");

        for (MappingFieldDescriptor field : configuration.getFields().values()) {
            String path = field.getIndexPropertyFullName();
            ObjectNode config = field.getFieldConfiguration().asJson();
            mergeField(rootProperties, path, config);
        }

        DisplayedNameDescriptor displayedNameDescriptor = configuration.getDisplayedNameDescriptor();
        rootProperties.set(displayedNameDescriptor.getIndexPropertyFullName(), displayedNameDescriptor.getFieldConfiguration().asJson());

        return root;
    }

    protected void mergeField(ObjectNode root, String fieldName, ObjectNode configValue) {
        log.trace("Merge field '{}' ({}) with object {}", fieldName, configValue, root);
        String[] parts = fieldName.split("\\.", 2);

        String localFieldName = parts[0];
        if (root.has(localFieldName)) {
            JsonNode localField = root.get(localFieldName);
            if (localField.isObject() && localField.has("properties")) {
                ObjectNode nextRoot = (ObjectNode) localField.get("properties");
                if (parts.length > 1) {
                    mergeField(nextRoot, parts[1], configValue);
                } else {
                    if (configValue.has("properties")) {
                        nextRoot.setAll((ObjectNode) configValue.get("properties"));
                    }
                }
            } else {
                throw new RuntimeException("Unable to merge field '" + fieldName + "' with existing object " + root);
            }
        } else {
            if (parts.length > 1) {
                ObjectNode nextRoot = root.putObject(localFieldName).putObject("properties");
                mergeField(nextRoot, parts[1], configValue);
            } else {
                root.set(localFieldName, configValue);
            }
        }
    }
}
