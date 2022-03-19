/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.rest.impl.service;

import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.rest.impl.RestControllerUtils;
import io.jmix.rest.exception.RestAPIException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;

/**
 * Class that executes business logic required by the {@link io.jmix.rest.impl.controller.MessagesController}.
 */
@Component("rest_LocalizationControllerManager")
public class MessagesControllerManager {

    @Autowired
    protected RestControllerUtils restControllerUtils;

    @Autowired
    protected MessageTools messageTools;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected Messages messages;

    public Map<String, String> getLocalizationForEntity(String entityName) {
        MetaClass metaClass = restControllerUtils.getMetaClass(entityName);
        return getLocalizationForEntity(metaClass);
    }

    public Map<String, String> getLocalizationForAllEntities() {
        Map<String, String> locMap = new TreeMap<>();
        metadata.getSession().getClasses().forEach(metaClass -> locMap.putAll(getLocalizationForEntity(metaClass)));
        return locMap;
    }

    protected Map<String, String> getLocalizationForEntity(MetaClass metaClass) {
        Map<String, String> locMap = new TreeMap<>();
        locMap.put(metaClass.getName(), messageTools.getEntityCaption(metaClass));
        metaClass.getProperties().forEach(metaProperty -> {
            String msgKey = metaClass.getName() + "." + metaProperty.getName();
            String msgValue = messageTools.getPropertyCaption(metaProperty);
            locMap.put(msgKey, msgValue);
        });
        return locMap;
    }

    public Map<String, String> getLocalizationForAllEnums() {
        Map<String, String> locMap = new TreeMap<>();
        metadataTools.getAllEnums().forEach(enumClass -> locMap.putAll(getLocalizationForEnum(enumClass)));
        return locMap;
    }

    public Map<String, String> getLocalizationForEnum(String enumClassName) {
        Class<?> enumClass;
        try {
            enumClass = Class.forName(enumClassName);
        } catch (ClassNotFoundException e) {
            throw new RestAPIException("Enum not found", "Enum with class name " + enumClassName + " not found", HttpStatus.NOT_FOUND);
        }
        return getLocalizationForEnum(enumClass);
    }

    public Map<String, String> getLocalizationForEnum(Class enumClass) {
        Map<String, String> locMap = new TreeMap<>();
        locMap.put(enumClass.getName(), messages.getMessage(enumClass, enumClass.getSimpleName()));
        Object[] enumConstants = enumClass.getEnumConstants();
        for (Object enumConstant : enumConstants) {
            Enum enumValue = (Enum) enumConstant;
            String msgKey = enumClass.getName() + "." + enumValue.name();
            String msgValue = messages.getMessage(enumValue);
            locMap.put(msgKey, msgValue);
        }
        return locMap;
    }
}
