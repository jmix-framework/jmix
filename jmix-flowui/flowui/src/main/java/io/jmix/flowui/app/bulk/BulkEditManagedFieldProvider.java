/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.app.bulk;

import com.google.common.base.Strings;
import io.jmix.core.AccessManager;
import io.jmix.core.MessageTools;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.flowui.accesscontext.UiEntityAttributeContext;
import io.jmix.flowui.accesscontext.UiEntityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Component("flowui_BulkEditManagedFieldProvider")
public class BulkEditManagedFieldProvider {

    protected final BulkEditContext<?> context;
    protected final Pattern excludeRegexPattern;

    protected AccessManager accessManager;
    protected MetadataTools metadataTools;
    protected MessageTools messageTools;

    public BulkEditManagedFieldProvider(BulkEditContext<?> context) {
        this.context = context;
        String exclude = context.getExclude();
        excludeRegexPattern = exclude == null ? null : Pattern.compile(exclude);
    }

    @Autowired
    public void setAccessManager(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    @Autowired
    public void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Autowired
    public void setMessageTools(MessageTools messageTools) {
        this.messageTools = messageTools;
    }

    public List<ManagedField> getManagedFields(BulkEditContext<?> context) {
        MetaClass metaClass = context.getMetaClass();

        return getManagedFields(metaClass, null, null);
    }

    protected List<ManagedField> getManagedFields(MetaClass metaClass,
                                                  @Nullable String fqnPrefix,
                                                  @Nullable String localePrefix) {
        List<ManagedField> managedFields = new ArrayList<>();
        for (MetaProperty metaProperty : metaClass.getProperties()) {
            String fqn = generateFqn(metaProperty, fqnPrefix);
            String propertyCaption = generatePropertyCaption(metaClass, metaProperty, localePrefix);

            if (!metadataTools.isEmbedded(metaProperty)) {
                if (isManagedAttribute(metaClass, metaProperty, fqn)) {
                    managedFields.add(new ManagedField(fqn, metaProperty, propertyCaption, fqnPrefix));
                }
            } else {
                List<ManagedField> nestedFields = getManagedFields(metaProperty, fqn, propertyCaption);
                managedFields.addAll(nestedFields);
            }
        }

        return managedFields;
    }

    public String generateFqn(MetaProperty metaProperty, @Nullable String fqnPrefix) {
        String fqn = metaProperty.getName();
        if (!Strings.isNullOrEmpty(fqnPrefix)) {
            fqn = fqnPrefix + "." + fqn;
        }
        return fqn;
    }

    protected boolean isManagedAttribute(MetaClass metaClass, MetaProperty metaProperty, String fqn) {
        if (metadataTools.isSystem(metaProperty)
                || (!metadataTools.isJpa(metaProperty) && !isCrossDataStoreReference(metaProperty))
                || metadataTools.isSystemLevel(metaProperty)
                || metaProperty.getRange().getCardinality().isMany()
                || !isEntityAttributeModifyPermitted(metaClass, metaProperty)) {
            return false;
        }

        if (metaProperty.getRange().isDatatype()
                && (isByteArray(metaProperty) || isUuid(metaProperty))) {
            return false;
        }

        if (!isRangeClassPermitted(metaProperty)) {
            return false;
        }

        List<String> includeProperties = context.getIncludeProperties();
        if (!includeProperties.isEmpty()) {
            return includeProperties.contains(fqn);
        }

        return excludeRegexPattern == null || !excludeRegexPattern.matcher(fqn).matches();
    }

    protected boolean isCrossDataStoreReference(MetaProperty metaProperty) {
        return metadataTools.getCrossDataStoreReferenceIdProperty(metaProperty.getStore().getName(), metaProperty) != null;
    }

    protected boolean isEntityAttributeModifyPermitted(MetaClass metaClass, MetaProperty metaProperty) {
        UiEntityAttributeContext attributeContext =
                new UiEntityAttributeContext(metaClass, metaProperty.getName());
        accessManager.applyRegisteredConstraints(attributeContext);

        return attributeContext.canModify();
    }

    protected boolean isByteArray(MetaProperty metaProperty) {
        return metaProperty.getRange().asDatatype().getJavaClass().equals(byte[].class);
    }

    protected boolean isUuid(MetaProperty metaProperty) {
        return metaProperty.getRange().asDatatype().getJavaClass().equals(UUID.class);
    }

    protected boolean isRangeClassPermitted(MetaProperty metaProperty) {
        if (metaProperty.getRange().isClass()) {
            MetaClass propertyMetaClass = metaProperty.getRange().asClass();

            UiEntityContext entityContext = new UiEntityContext(propertyMetaClass);
            accessManager.applyRegisteredConstraints(entityContext);

            return !metadataTools.isSystemLevel(propertyMetaClass)
                    && entityContext.isViewPermitted();
        }

        return true;
    }

    protected String generatePropertyCaption(MetaClass metaClass,
                                             MetaProperty metaProperty,
                                             @Nullable String localePrefix) {
        String propertyCaption = messageTools.getPropertyCaption(metaClass, metaProperty.getName());
        if (!Strings.isNullOrEmpty(localePrefix)) {
            propertyCaption = localePrefix + " " + propertyCaption;
        }

        return propertyCaption;
    }

    protected List<ManagedField> getManagedFields(MetaProperty embeddedProperty,
                                                  String fqnPrefix,
                                                  String localePrefix) {
        MetaClass metaClass = embeddedProperty.getRange().asClass();

        return getManagedFields(metaClass, fqnPrefix, localePrefix);
    }
}
