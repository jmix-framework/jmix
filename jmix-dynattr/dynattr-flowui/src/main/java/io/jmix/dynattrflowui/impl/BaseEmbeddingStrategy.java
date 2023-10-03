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

import com.vaadin.flow.component.Component;
import io.jmix.core.AccessManager;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.dynattr.*;
import io.jmix.flowui.accesscontext.UiEntityAttributeContext;
import io.jmix.flowui.accesscontext.UiEntityContext;
import io.jmix.flowui.model.*;
import io.jmix.flowui.view.View;
import org.springframework.util.Assert;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BaseEmbeddingStrategy implements EmbeddingStrategy {
    protected final Metadata metadata;
    protected final MetadataTools metadataTools;
    protected final DynAttrMetadata dynAttrMetadata;
    protected final AccessManager accessManager;

    protected BaseEmbeddingStrategy(Metadata metadata,
                                    MetadataTools metadataTools,
                                    DynAttrMetadata dynAttrMetadata,
                                    AccessManager accessManager) {
        this.metadata = metadata;
        this.metadataTools = metadataTools;
        this.dynAttrMetadata = dynAttrMetadata;
        this.accessManager = accessManager;
    }

    protected abstract MetaClass getEntityMetaClass(Component component);

    protected abstract void setLoadDynamicAttributes(Component component);

    protected abstract void embed(Component component, View<?> owner, List<AttributeDefinition> attributes);

    @Override
    public void embed(Component component, View<?> owner) {
        if (getWindowId(owner) != null) {

            MetaClass entityMetaClass = getEntityMetaClass(component);
            if (metadataTools.isJpaEntity(entityMetaClass)) {

                List<AttributeDefinition> attributes = findVisibleAttributes(
                        entityMetaClass,
                        getWindowId(owner), component.getId().orElseThrow());

                if (!attributes.isEmpty()) {
                    setLoadDynamicAttributes(component);
                }

                embed(component, owner, attributes);
            }
        }
    }

    protected String getWindowId(View<?> view) {
        return view.getId().orElseThrow();
    }

    protected void setLoadDynamicAttributes(InstanceContainer<?> container) {
        if (container instanceof HasLoader) {
            DataLoader dataLoader = ((HasLoader) container).getLoader();
            if (dataLoader instanceof InstanceLoader || dataLoader instanceof CollectionLoader) {
                dataLoader.setHint(DynAttrQueryHints.LOAD_DYN_ATTR, true);
            }
        }
    }

    protected List<AttributeDefinition> findVisibleAttributes(MetaClass entityMetaClass, String windowId, String componentId) {
        return dynAttrMetadata.getAttributes(entityMetaClass).stream()
                .filter(attr -> isVisibleAttribute(attr, windowId, componentId))
                .filter(attr -> checkPermissions(attr, entityMetaClass))
                .sorted(Comparator.comparingInt(AttributeDefinition::getOrderNo))
                .collect(Collectors.toList());
    }

    protected boolean isVisibleAttribute(AttributeDefinition attributeDefinition, String screen, String componentId) {
        Set<String> screens = attributeDefinition.getConfiguration().getScreens();
        return screens.contains(screen) || screens.contains(screen + "#" + componentId);
    }

    protected boolean checkPermissions(AttributeDefinition attributeDefinition, MetaClass entityMetaClass) {
        Assert.notNull(attributeDefinition.getJavaType(), "Attribute's java type should be not null");

        UiEntityAttributeContext uiEntityAttributeContext =
                new UiEntityAttributeContext(metadataTools.resolveMetaPropertyPath(entityMetaClass,
                        DynAttrUtils.getPropertyFromAttributeCode(attributeDefinition.getCode())));

        accessManager.applyRegisteredConstraints(uiEntityAttributeContext);

        if (uiEntityAttributeContext.canView()) {
            if (attributeDefinition.getDataType() == AttributeType.ENTITY) {
                MetaClass referenceEntityClass = metadata.getClass(attributeDefinition.getJavaType());
                UiEntityContext uiEntityContext = new UiEntityContext(referenceEntityClass);
                accessManager.applyRegisteredConstraints(uiEntityContext);
                return uiEntityContext.isViewPermitted();
            }
            return true;
        }

        return false;
    }
}
