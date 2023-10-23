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

package io.jmix.dynattrflowui.impl;

import io.jmix.core.MessageResolver;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.dynattr.DynAttrUtils;
import io.jmix.dynattr.MsgBundleTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Locale;

@Component("dynat_DynAttrMessageResolver")
public class DynAttrMessageResolver implements MessageResolver {

    protected final DynAttrMetadata dynAttrMetadata;
    protected final MsgBundleTools msgBundleTools;

    public DynAttrMessageResolver(DynAttrMetadata dynAttrMetadata, MsgBundleTools msgBundleTools) {
        this.dynAttrMetadata = dynAttrMetadata;
        this.msgBundleTools = msgBundleTools;
    }

    @Nullable
    @Override
    public String getPropertyCaption(MetaProperty property, @Nullable Locale locale) {
        if (!DynAttrUtils.isDynamicAttributeProperty(property.getName())) {
            return null;
        }

        AttributeDefinition attribute = dynAttrMetadata.getAttributeByCode(property.getDomain(),
                        DynAttrUtils.getAttributeCodeFromProperty(property.getName()))
                .orElse(null);

        if (attribute == null) {
            return null;
        }

        //noinspection DataFlowIssue
        return msgBundleTools.getLocalizedValue(attribute.getNameMsgBundle(), attribute.getName());
    }
}
