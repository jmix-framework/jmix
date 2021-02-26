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

package io.jmix.search.index.mapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.core.FileRef;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.impl.FileRefDatatype;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.search.exception.FileParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class FileValueMapper implements ValueMapper {

    private static final Logger log = LoggerFactory.getLogger(FileValueMapper.class);

    protected final boolean mapFileContent;
    protected final FileProcessor fileProcessor;

    public FileValueMapper(boolean mapFileContent, FileProcessor fileProcessor) {
        this.mapFileContent = mapFileContent;
        this.fileProcessor = fileProcessor;
    }

    @Override
    public JsonNode getValue(Object entity, MetaPropertyPath propertyPath, Map<String, Object> parameters) {
        JsonNode result = NullNode.getInstance();
        if(propertyPath.getRange().isDatatype()) {
            Datatype<?> datatype = propertyPath.getRange().asDatatype();
            if(datatype instanceof FileRefDatatype) {
                FileRef fileRef = EntityValues.getValueEx(entity, propertyPath);
                if(fileRef != null) {
                    result = processFileRef(fileRef);
                }
            }
        }
        return result;
    }

    protected ObjectNode processFileRef(FileRef fileRef) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("_file_name", fileRef.getFileName());
        if(mapFileContent) {
            addFileContent(node, fileRef);
        }
        return node;
    }

    protected void addFileContent(ObjectNode node, FileRef fileRef) {
        try {
            String content = fileProcessor.extractFileContent(fileRef);
            node.put("_content", content);
        } catch (FileParseException e) {
            log.error("Unable to index file content", e);
        }
    }
}
