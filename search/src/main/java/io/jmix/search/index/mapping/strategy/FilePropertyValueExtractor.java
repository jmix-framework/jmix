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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.core.FileRef;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.impl.FileRefDatatype;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.search.exception.FileParseException;
import io.jmix.search.index.mapping.ParameterKeys;
import io.jmix.search.utils.FileProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("search_FilePropertyValueExtractor")
public class FilePropertyValueExtractor extends AbstractPropertyValueExtractor {

    private static final Logger log = LoggerFactory.getLogger(FilePropertyValueExtractor.class);

    protected final FileProcessor fileProcessor;

    @Autowired
    public FilePropertyValueExtractor(FileProcessor fileProcessor) {
        this.fileProcessor = fileProcessor;
    }

    @Override
    protected boolean isSupported(Object entity, MetaPropertyPath propertyPath, Map<String, Object> parameters) {
        if (propertyPath.getRange().isDatatype()) {
            Datatype<?> datatype = propertyPath.getRange().asDatatype();
            return datatype instanceof FileRefDatatype;
        } else {
            return false;
        }
    }

    @Override
    protected JsonNode transformSingleValue(Object value, Map<String, Object> parameters) {
        return processFileRef((FileRef) value, parameters);
    }

    @Override
    protected JsonNode transformMultipleValues(Iterable<?> values, Map<String, Object> parameters) {
        ArrayNode result = JsonNodeFactory.instance.arrayNode();
        for (Object value : (values)) {
            result.add(processFileRef((FileRef) value, parameters));
        }
        return result;
    }

    protected ObjectNode processFileRef(FileRef fileRef, Map<String, Object> parameters) {
        ObjectNode result = JsonNodeFactory.instance.objectNode();
        result.put("_file_name", fileRef.getFileName());
        if (isIndexFileContent(parameters)) {
            addFileContent(result, fileRef);
        }
        return result;
    }

    protected boolean isIndexFileContent(Map<String, Object> parameters) {
        return (boolean) parameters.getOrDefault(ParameterKeys.INDEX_FILE_CONTENT, true);
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
