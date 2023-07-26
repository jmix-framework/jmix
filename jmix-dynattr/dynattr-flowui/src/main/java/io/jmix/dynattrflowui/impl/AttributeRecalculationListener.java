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

import io.jmix.core.entity.EntityValues;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.DynAttrUtils;
import io.jmix.flowui.data.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import java.util.function.Consumer;

@org.springframework.stereotype.Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AttributeRecalculationListener implements Consumer<ValueSource.ValueChangeEvent> {

    protected final AttributeDefinition attribute;

    @Autowired
    protected AttributeRecalculationManager recalculationManager;

    private static final ThreadLocal<Boolean> recalculationInProgress = new ThreadLocal<>();

    public AttributeRecalculationListener(AttributeDefinition attribute) {
        this.attribute = attribute;
    }

    @Override
    public void accept(ValueSource.ValueChangeEvent valueChangeEvent) {
        // todo: implement
    }

//    @Override
//    public void accept(ValueSource.ValueChangeEvent valueChangeEvent) {
//        if (Boolean.TRUE.equals(recalculationInProgress.get())) {
//            return;
//        }
//        try {
//            recalculationInProgress.set(true);
//
//            Component component = valueChangeEvent.getComponent();
//            if (component instanceof HasValueSource
//                    && ((HasValueSource<?>) component).getValueSource() instanceof ContainerValueSource) {
//
//                ContainerValueSource<?, ?> valueSource = (ContainerValueSource<?, ?>) ((HasValueSource<?>) component).getValueSource();
//                InstanceContainer<?> container = valueSource.getContainer();
//
//                Object entity = container.getItem();
//
//                EntityValues.setValue(entity, DynAttrUtils.getPropertyFromAttributeCode(attribute.getCode()), valueChangeEvent.getValue());
//
//                recalculationManager.recalculateByAttribute(entity, attribute);
//            }
//        } finally {
//            recalculationInProgress.remove();
//        }
//    }
}
