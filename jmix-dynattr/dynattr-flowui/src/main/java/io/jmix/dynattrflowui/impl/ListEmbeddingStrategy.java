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

package io.jmix.dynattrflowui.impl;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import io.jmix.core.AccessManager;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.AttributeType;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.dynattr.MsgBundleTools;
import io.jmix.flowui.kit.component.formatter.Formatter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.util.Collection;

public abstract class ListEmbeddingStrategy extends BaseEmbeddingStrategy {

    protected final MsgBundleTools msgBundleTools;
    protected final CurrentAuthentication currentAuthentication;
    protected final DataManager dataManager;
    protected final AttributeRecalculationManager attributeRecalculationManager;

    protected ListEmbeddingStrategy(Metadata metadata,
                                    MetadataTools metadataTools,
                                    DynAttrMetadata dynAttrMetadata,
                                    AccessManager accessManager,
                                    MsgBundleTools msgBundleTools,
                                    CurrentAuthentication currentAuthentication,
                                    DataManager dataManager,
                                    AttributeRecalculationManager attributeRecalculationManager) {
        super(metadata, metadataTools, dynAttrMetadata, accessManager);
        this.msgBundleTools = msgBundleTools;
        this.currentAuthentication = currentAuthentication;
        this.dataManager = dataManager;
        this.attributeRecalculationManager = attributeRecalculationManager;
    }

    protected String getColumnDescription(AttributeDefinition attribute) {
        //noinspection DataFlowIssue
        return msgBundleTools.getLocalizedValue(attribute.getDescriptionsMsgBundle(), attribute.getDescription());
    }

    protected String getColumnCaption(AttributeDefinition attribute) {
        if (!Strings.isNullOrEmpty(attribute.getConfiguration().getColumnName())) {
            return attribute.getConfiguration().getColumnName();
        } else {
            //noinspection DataFlowIssue
            return msgBundleTools.getLocalizedValue(attribute.getNameMsgBundle(), attribute.getName());
        }
    }

    protected <T> Formatter<T> getColumnFormatter(AttributeDefinition attribute) {
        if (attribute.getDataType() == AttributeType.ENUMERATION) {
            if (!attribute.isCollection()) {
                return value -> {
                    if (attribute.getEnumerationMsgBundle() == null) {
                        return value == null ? "" : value.toString();
                    } else {
                        return msgBundleTools.getLocalizedEnumeration(attribute.getEnumerationMsgBundle(), (String) value);
                    }
                };
            } else {
                return value -> MessageFormat.format("[{0}]", Joiner.on(", ")
                        .join(((Collection) value)
                                .stream()
                                .map(item -> msgBundleTools.getLocalizedEnumeration(attribute.getEnumerationMsgBundle(), (String) item))
                                .toList()));
            }
        } else if (!Strings.isNullOrEmpty(attribute.getConfiguration().getNumberFormatPattern())) {
            return value -> {
                if (value == null) {
                    return null;
                } else {
                    DecimalFormatSymbols symbols = new DecimalFormatSymbols(currentAuthentication.getLocale());
                    DecimalFormat format = new DecimalFormat(attribute.getConfiguration().getNumberFormatPattern(), symbols);
                    return format.format(value);
                }
            };
        }
        return null;
    }

    protected <T> Renderer<T> getColumnRenderer(AttributeDefinition attribute) {
        return new ComponentRenderer<>(() -> new Text(""), (c, rowEntity) -> {
            Formatter<Object> formatter = getColumnFormatter(attribute);

            Object propertyValue = EntityValues.getValue(rowEntity, attribute.getMetaProperty().getName());

            if (propertyValue != null) {
                if (attribute.getDataType().equals(AttributeType.ENTITY)) {
                    // todo poor performance
                    if (propertyValue instanceof Collection) {
                        propertyValue = ((Collection<?>) propertyValue).stream()
                                .map(metadataTools::getInstanceName)
                                .toList();
                    } else {
                        propertyValue = metadataTools.getInstanceName(propertyValue);
                    }
                }
                ((Text) c).setText(formatter != null ? formatter.apply(propertyValue) : propertyValue.toString());
            }
        });
    }
}
