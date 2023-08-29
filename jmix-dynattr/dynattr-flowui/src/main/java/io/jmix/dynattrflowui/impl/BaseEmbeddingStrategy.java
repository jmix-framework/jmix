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
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BaseEmbeddingStrategy implements EmbeddingStrategy {
    protected Metadata metadata;
    protected MetadataTools metadataTools;
    protected DynAttrMetadata dynAttrMetadata;
    protected AccessManager accessManager;

    @Autowired
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Autowired
    public void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Autowired
    public void setDynAttrMetadata(DynAttrMetadata dynAttrMetadata) {
        this.dynAttrMetadata = dynAttrMetadata;
    }

    @Autowired
    public void setAccessManager(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    @Override
    public void embed(Component component, Component frame) {
//        if (getWindowId(frame) != null) {
//
//            MetaClass entityMetaClass = getEntityMetaClass(component);
//            if (metadataTools.isJpaEntity(entityMetaClass)) {
//
//                List<AttributeDefinition> attributes = findVisibleAttributes(
//                        entityMetaClass,
//                        getWindowId(frame), component.getId());
//
//                if (!attributes.isEmpty()) {
//                    setLoadDynamicAttributes(component);
//                }
//
//                embed(component, frame, attributes);
//            }
//        }
    }

//    protected abstract MetaClass getEntityMetaClass(Component component);
//
//    protected abstract void setLoadDynamicAttributes(Component component);
//
//    protected abstract void embed(Component component, Frame frame, List<AttributeDefinition> attributes);
//
//    protected String getWindowId(Frame frame) {
//        Screen screen = UiControllerUtils.getScreen(frame.getFrameOwner());
//        return screen.getId();
//    }
//
//    protected void setLoadDynamicAttributes(InstanceContainer container) {
//        if (container instanceof HasLoader) {
//            DataLoader dataLoader = ((HasLoader) container).getLoader();
//            if (dataLoader instanceof InstanceLoader) {
//                ((InstanceLoader<?>) dataLoader).setHint(DynAttrQueryHints.LOAD_DYN_ATTR, true);
//            } else if (dataLoader instanceof CollectionLoader) {
//                ((CollectionLoader<?>) dataLoader).setHint(DynAttrQueryHints.LOAD_DYN_ATTR, true);
//            }
//        }
//    }
//
//    protected List<AttributeDefinition> findVisibleAttributes(MetaClass entityMetaClass, String windowId, String componentId) {
//        return dynAttrMetadata.getAttributes(entityMetaClass).stream()
//                .filter(attr -> isVisibleAttribute(attr, windowId, componentId))
//                .filter(attr -> checkPermissions(attr, entityMetaClass))
//                .sorted(Comparator.comparingInt(AttributeDefinition::getOrderNo))
//                .collect(Collectors.toList());
//    }
//
//    protected boolean isVisibleAttribute(AttributeDefinition attributeDefinition, String screen, String componentId) {
//        Set<String> screens = attributeDefinition.getConfiguration().getScreens();
//        return screens.contains(screen) || screens.contains(screen + "#" + componentId);
//    }
//
//    protected boolean checkPermissions(AttributeDefinition attributeDefinition, MetaClass entityMetaClass) {
//        UiEntityAttributeContext uiEntityAttributeContext =
//                new UiEntityAttributeContext(metadataTools.resolveMetaPropertyPath(entityMetaClass,
//                        DynAttrUtils.getPropertyFromAttributeCode(attributeDefinition.getCode())));
//
//        accessManager.applyRegisteredConstraints(uiEntityAttributeContext);
//
//        if (uiEntityAttributeContext.canView()) {
//            if (attributeDefinition.getDataType() == AttributeType.ENTITY) {
//                MetaClass referenceEntityClass = metadata.getClass(attributeDefinition.getJavaType());
//                UiEntityContext uiEntityContext = new UiEntityContext(referenceEntityClass);
//                accessManager.applyRegisteredConstraints(uiEntityContext);
//                return uiEntityContext.isViewPermitted();
//            }
//            return true;
//        }
//
//        return false;
//    }
}
