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

import com.google.common.base.Strings;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.AttributeType;
import io.jmix.dynattr.MsgBundleTools;
import io.jmix.ui.component.formatter.Formatter;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public abstract class ListEmbeddingStrategy extends BaseEmbeddingStrategy {

    protected MsgBundleTools msgBundleTools;
    protected CurrentAuthentication currentAuthentication;

    @Autowired
    public void setMsgBundleTools(MsgBundleTools msgBundleTools) {
        this.msgBundleTools = msgBundleTools;
    }

    @Autowired
    public void setCurrentAuthentication(CurrentAuthentication currentAuthentication) {
        this.currentAuthentication = currentAuthentication;
    }

    protected String getColumnDescription(AttributeDefinition attribute) {
        return msgBundleTools.getLocalizedValue(attribute.getDescriptionsMsgBundle(), attribute.getDescription());
    }

    protected String getColumnCaption(AttributeDefinition attribute) {
        if (!Strings.isNullOrEmpty(attribute.getConfiguration().getColumnName())) {
            return attribute.getConfiguration().getColumnName();
        } else {
            return msgBundleTools.getLocalizedValue(attribute.getNameMsgBundle(), attribute.getName());
        }
    }

    @SuppressWarnings("rawtypes")
    protected Formatter getColumnFormatter(AttributeDefinition attribute) {
        if (attribute.getDataType() == AttributeType.ENUMERATION) {
            if (!attribute.isCollection()) {
                return value -> {
                    if (value == null) {
                        return null;
                    } else {
                        return msgBundleTools.getLocalizedEnumeration(attribute.getEnumerationMsgBundle(), (String) value);
                    }
                };
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
}
