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

package io.jmix.securityui.screen.resourcepolicy;

import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component("sec_ResourcePolicyEditorUtils")
public class ResourcePolicyEditorUtils {

    @Autowired
    private Metadata metadata;

    @Autowired
    private MessageTools messageTools;

    @Inject
    private Messages messages;

    public Map<String, String> getEntityOptionsMap() {
        TreeMap<String, String> map = metadata.getClasses().stream()
                .collect(Collectors.toMap(
                        this::getEntityCaption,
                        MetaClass::getName,
                        (v1, v2) -> {
                            throw new RuntimeException(String.format("Duplicate key for values %s and %s", v1, v2));
                        },
                        TreeMap::new));
        map.put(messages.getMessage(ResourcePolicyEditorUtils.class, "allEntities"), "*");
        return map;
    }

    public Map<String, String> getEntityAttributeOptionsMap(String entityName) {
        MetaClass metaClass = metadata.getClass(entityName);
        TreeMap<String, String> map = metaClass.getProperties().stream()
                .collect(Collectors.toMap(
                        this::getEntityAttributeCaption,
                        MetaProperty::getName,
                        (v1, v2) -> {
                            throw new RuntimeException(String.format("Duplicate key for values %s and %s", v1, v2));
                        },
                        TreeMap::new));
        map.put(messages.getMessage(ResourcePolicyEditorUtils.class, "allAttributes"), "*");
        return map;
    }

    private String getEntityCaption(MetaClass metaClass) {
        return String.format("%s (%s)", messageTools.getEntityCaption(metaClass), metaClass.getName());
    }

    private String getEntityAttributeCaption(MetaProperty metaProperty) {
        return String.format("%s (%s)", messageTools.getPropertyCaption(metaProperty), metaProperty.getName());
    }

}