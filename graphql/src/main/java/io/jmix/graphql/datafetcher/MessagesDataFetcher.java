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

package io.jmix.graphql.datafetcher;

import graphql.schema.DataFetcher;
import io.jmix.core.AccessManager;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.core.accesscontext.EntityAttributeContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.graphql.schema.messages.MessageDetail;
import org.apache.commons.lang3.LocaleUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component(value = MessagesDataFetcher.NAME)
public class MessagesDataFetcher {
    public static final String NAME = "gql_MessagesDataFetcher";

    @Autowired
    private Metadata metadata;
    @Autowired
    private MetadataTools metadataTools;
    @Autowired
    private AccessManager accessManager;
    @Autowired
    private MessageTools messageTools;
    @Autowired
    private Messages messages;

    public DataFetcher<?> loadEntityMessages() {
        return environment -> {
            String className = environment.getArgument("className");
            String strLocale = environment.getArgument("locale");
            Locale locale = null;
            if (strLocale != null) {
                locale = LocaleUtils.toLocale(strLocale);
            }
            if (className == null) {
                Locale finalLocale = locale;
                return metadata.getSession().getClasses().stream()
                        .flatMap(metaClass -> this.getEntityMessages(metaClass, finalLocale).stream())
                        .collect(Collectors.toList());
            }

            try {
                MetaClass queryMetaClass = metadata.getClass(className);
                return getEntityMessages(queryMetaClass, locale);
            } catch (IllegalArgumentException exception) {
                throw new GqlEntityValidationException(exception, exception.getMessage());
            }
        };
    }

    public DataFetcher<?> loadEnumMessages() {
        return environment -> {
            String className = environment.getArgument("className");
            String strLocale = environment.getArgument("locale");
            Locale locale = null;
            if (strLocale != null) {
                locale = LocaleUtils.toLocale(strLocale);
            }
            if (className == null) {
                Locale finalLocale = locale;
                return metadataTools.getAllEnums().stream()
                        .flatMap(enumClass -> this.getEnumMessages(enumClass, finalLocale).stream())
                        .collect(Collectors.toList());
            }

            try {
                Class<?> queryEnum = Class.forName(className);
                return getEnumMessages(queryEnum, locale);
            } catch (IllegalArgumentException exception) {
                throw new GqlEntityValidationException(exception, exception.getMessage());
            }
        };
    }

    protected List<MessageDetail> getEntityMessages(MetaClass metaClass, Locale locale) {
        List<MessageDetail> messages = new ArrayList<>();
        CrudEntityContext entityContext = new CrudEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(entityContext);

        String metaClassName = metaClass.getName();
        if (entityContext.isReadPermitted()) {
            String entityCaption = messageTools.getEntityCaption(metaClass, locale);
            messages.add(new MessageDetail(metaClassName, entityCaption));
        }

        for (MetaProperty metaProperty : metaClass.getProperties()) {
            EntityAttributeContext attributeContext = new EntityAttributeContext(metaClass, metaProperty.getName());
            accessManager.applyRegisteredConstraints(attributeContext);
            if (attributeContext.canView()) {
                String propertyCaption = messageTools.getPropertyCaption(metaProperty, locale);
                messages.add(
                        new MessageDetail(
                                metaClassName + "." + metaProperty.getName(),
                                propertyCaption
                        )
                );
            }
        }
        return messages;
    }

    protected List<MessageDetail> getEnumMessages(Class<?> enumClass, Locale locale) {
        List<MessageDetail> enumMessages = new ArrayList<>();
        String classCaption;
        if (locale == null) {
            classCaption = messages.getMessage(enumClass, enumClass.getSimpleName());
        } else {
            classCaption = messages.getMessage(enumClass, enumClass.getSimpleName(), locale);
        }
        enumMessages.add(new MessageDetail(enumClass.getName(), classCaption));

        Object[] enumConstants = enumClass.getEnumConstants();
        for (Object enumConstant : enumConstants) {
            Enum<?> enumValue = (Enum<?>) enumConstant;
            String msgKey = enumClass.getName() + "." + enumValue.name();
            String msgValue;
            if (locale == null) {
                msgValue = messages.getMessage(enumValue);
            } else {
                msgValue = messages.getMessage(enumValue, locale);
            }
            enumMessages.add(new MessageDetail(msgKey, msgValue));
        }
        return enumMessages;
    }
}
