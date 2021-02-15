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

package io.jmix.search.index.mapping;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;

public class IndexMappingConfigSerializer extends StdSerializer<IndexMappingConfig> {

    private static final long serialVersionUID = -6118284896645343097L;

    private static final Logger log = LoggerFactory.getLogger(IndexMappingConfigSerializer.class);

    protected IndexMappingConfigSerializer() {
        super(IndexMappingConfig.class);
    }

    @Override
    public void serialize(IndexMappingConfig value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        log.trace("Start serialize Index Mapping Config: {}", value);

        ObjectNode mergedMappingStructure = mergeFields(value.getFields().values());
        log.trace("Result mapping structure = {}", mergedMappingStructure);
        gen.writeTree(mergedMappingStructure);
    }

    //todo check merger from EntityIndexer
    protected ObjectNode mergeFields(Collection<MappingFieldDescriptor> fields) {
        ObjectNode root = JsonNodeFactory.instance.objectNode();
        ObjectNode rootProperties = root.putObject("properties");
        rootProperties.putObject("meta")
                .putObject("properties")
                .putObject("entityClass")
                .put("type", "keyword");
        ObjectNode contentProperties = rootProperties.putObject("content").putObject("properties");

        for(MappingFieldDescriptor field : fields) {
            String path = field.getIndexPropertyFullName();
            ObjectNode config = field.getFieldConfiguration().asJson();
            mergeField(contentProperties, path, config);
        }

        return root;
    }

    protected void mergeField(ObjectNode root, String fieldName, ObjectNode configValue) {
        log.trace("Merge field '{}' ({}) with object {}", fieldName, configValue, root);
        String[] parts = fieldName.split("\\.", 2);

        String localFieldName = parts[0];
        if(root.has(localFieldName)) {
            JsonNode localField = root.get(localFieldName);
            if(localField.isObject() && localField.has("properties")) {
                ObjectNode nextRoot = (ObjectNode)localField.get("properties");
                if(parts.length > 1) {
                    mergeField(nextRoot, parts[1], configValue);
                } else {
                    if(configValue.has("properties")) {
                        nextRoot.setAll((ObjectNode)configValue.get("properties"));
                    }
                }
            } else {
                throw new RuntimeException("Unable to merge field '" + fieldName + "' with existing object " + root);
            }
        } else {
            if(parts.length > 1) {
                ObjectNode nextRoot = root.putObject(localFieldName).putObject("properties");
                mergeField(nextRoot, parts[1], configValue);
            } else {
                root.set(localFieldName, configValue);
            }
        }
    }
}
