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

package io.jmix.dynattrui.propertyfilter;

import io.jmix.core.DataManager;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.AttributeType;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.dynattr.DynAttrUtils;
import io.jmix.ui.app.propertyfilter.dateinterval.DateIntervalUtils;
import io.jmix.ui.component.PropertyFilter;
import io.jmix.ui.component.propertyfilter.PropertyFilterSupport;

import java.util.EnumSet;

import static io.jmix.ui.component.PropertyFilter.Operation.EQUAL;
import static io.jmix.ui.component.PropertyFilter.Operation.IN_LIST;
import static io.jmix.ui.component.PropertyFilter.Operation.IS_SET;
import static io.jmix.ui.component.PropertyFilter.Operation.NOT_EQUAL;
import static io.jmix.ui.component.PropertyFilter.Operation.NOT_IN_LIST;

public class DynAttrPropertyFilterSupport extends PropertyFilterSupport {

    protected DynAttrMetadata dynAttrMetadata;

    public DynAttrPropertyFilterSupport(Messages messages,
                                        MessageTools messageTools,
                                        MetadataTools metadataTools,
                                        DataManager dataManager,
                                        DatatypeRegistry datatypeRegistry,
                                        DynAttrMetadata dynAttrMetadata,
                                        DateIntervalUtils dateIntervalUtils) {
        super(messages, messageTools, metadataTools, dataManager, datatypeRegistry, dateIntervalUtils);
        this.dynAttrMetadata = dynAttrMetadata;
    }

    @Override
    protected boolean isStringDatatype(MetaPropertyPath mpp) {
        return super.isStringDatatype(mpp) && !isEnumerationAttribute(mpp);
    }

    @Override
    public EnumSet<PropertyFilter.Operation> getAvailableOperations(MetaPropertyPath mpp) {
        if (isEnumerationAttribute(mpp)) {
            return EnumSet.of(EQUAL, NOT_EQUAL, IS_SET, IN_LIST, NOT_IN_LIST);
        }

        return super.getAvailableOperations(mpp);
    }

    protected boolean isEnumerationAttribute(MetaPropertyPath mpp) {
        MetaProperty metaProperty = mpp.getMetaProperty();
        if (DynAttrUtils.isDynamicAttributeProperty(metaProperty.getName())) {
            AttributeDefinition attribute = dynAttrMetadata.getAttributeByCode(metaProperty.getDomain(),
                    DynAttrUtils.getAttributeCodeFromProperty(metaProperty.getName()))
                    .orElse(null);
            return attribute != null && attribute.getDataType() == AttributeType.ENUMERATION;
        }

        return false;
    }
}
