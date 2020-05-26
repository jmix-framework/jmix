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

package io.jmix.dynattrui.impl;

import io.jmix.core.BeanLocator;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.EntityOp;
import io.jmix.core.security.Security;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.AttributeType;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Frame;
import io.jmix.ui.model.*;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiControllerUtils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseEmbeddingStrategy implements EmbeddingStrategy {
    protected BeanLocator beanLocator;

    public BaseEmbeddingStrategy(BeanLocator beanLocator) {
        this.beanLocator = beanLocator;
    }

    @Override
    public void embed(Component component, Frame frame) {
        if (getWindowId(frame) != null) {
            MetadataTools metadataTools = beanLocator.get(MetadataTools.class);

            MetaClass entityMetaClass = getEntityMetaClass(component);
            if (metadataTools.isPersistent(entityMetaClass)) {

                List<AttributeDefinition> attributes = findVisibleAttributes(
                        entityMetaClass,
                        getWindowId(frame), component.getId());

                if (!attributes.isEmpty()) {
                    setLoadDynamicAttributes(component);
                }

                embed(component, attributes);
            }
        }
    }

    protected abstract MetaClass getEntityMetaClass(Component component);

    protected abstract void setLoadDynamicAttributes(Component component);

    protected abstract void embed(Component component, List<AttributeDefinition> attributes);

    protected String getWindowId(Frame frame) {
        Screen screen = UiControllerUtils.getScreen(frame.getFrameOwner());
        return screen.getId();
    }

    protected void setLoadDynamicAttributes(InstanceContainer container) {
        if (container instanceof HasLoader) {
            DataLoader dataLoader = ((HasLoader) container).getLoader();
            if (dataLoader instanceof InstanceLoader) {
                ((InstanceLoader<?>) dataLoader).setLoadDynamicAttributes(true);
            } else if (dataLoader instanceof CollectionLoader) {
                ((CollectionLoader<?>) dataLoader).setLoadDynamicAttributes(true);
            }
        }
    }

    protected List<AttributeDefinition> findVisibleAttributes(MetaClass entityMetaClass, String windowId, String componentId) {
        DynAttrMetadata dynAttrMetadata = beanLocator.get(DynAttrMetadata.class);
        return dynAttrMetadata.getAttributes(entityMetaClass).stream()
                .filter(attr -> isVisibleAttribute(attr, windowId, componentId))
                .filter(this::checkPermissions)
                .sorted(Comparator.comparingInt(AttributeDefinition::getOrderNo))
                .collect(Collectors.toList());
    }

    protected boolean isVisibleAttribute(AttributeDefinition attributeDefinition, String screen, String componentId) {
        return true;
//        Set<String> screens = attributeDefinition.getConfiguration().getScreens();
//        return screens.contains(screen) || screens.contains(screen + "#" + componentId);
    }

    protected boolean checkPermissions(AttributeDefinition attributeDefinition) {
        if (attributeDefinition.getDataType() != AttributeType.ENTITY) {
            return true;
        }

        Metadata metadata = beanLocator.get(Metadata.class);
        Security security = beanLocator.get(Security.class);

        MetaClass entityClass = metadata.getClass(attributeDefinition.getJavaType());
        return security.isEntityOpPermitted(entityClass, EntityOp.READ);
    }
}
