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

package io.jmix.searchflowui.view.result;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import io.jmix.core.MessageTools;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.view.MessageBundle;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SearchResultFieldFormatter {
    protected final Metadata metadata;
    protected final MessageTools messageTools;
    protected final MetadataTools metadataTools;


    protected static final Map<String, String> SYSTEM_FIELD_LABELS = ImmutableMap
            .of("_file_name", "fileName",
                    "_content", "content");

    public SearchResultFieldFormatter(Metadata metadata, MessageTools messageTools, MetadataTools metadataTools) {
        this.metadata = metadata;
        this.messageTools = messageTools;
        this.metadataTools = metadataTools;
    }

    public String formatFieldCaption(String entityName, String fieldName, MessageBundle messageBundle) {

        //StringUtils.endsWith()
        //if ()
        if (isFile)
        List<String> captionParts = new ArrayList<>();
        MetaClass currentMetaClass = metadata.getClass(entityName);
        MetaPropertyPath metaPropertyPath = metadataTools.resolveMetaPropertyPathOrNull(currentMetaClass, fieldName);
        //TODO null check
        MetaProperty[] metaProperties = metaPropertyPath.getMetaProperties();

        for (int i = 0; i < metaProperties.length; i++) {
            MetaProperty currentMetaProperty = metaProperties[i];
            captionParts.add(messageTools.getPropertyCaption(currentMetaProperty));
        }
        return Joiner.on(".").join(captionParts);
    }
}
