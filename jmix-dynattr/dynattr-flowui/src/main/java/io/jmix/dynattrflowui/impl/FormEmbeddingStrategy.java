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
import com.vaadin.flow.component.Component;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.DynAttrUtils;
import io.jmix.flowui.component.UiComponentsGenerator;
//import io.jmix.ui.component.*;
//import io.jmix.ui.component.data.ValueSource;
//import io.jmix.ui.component.data.ValueSourceProvider;
//import io.jmix.ui.component.data.value.ContainerValueSourceProvider;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.OptionalDouble;
//
//@org.springframework.stereotype.Component("dynat_FormEmbeddingStrategy")
//public class FormEmbeddingStrategy extends BaseEmbeddingStrategy {
//
//    protected UiComponentsGenerator uiComponentsGenerator;
//
//    @Autowired
//    public void setUiComponentsGenerator(UiComponentsGenerator uiComponentsGenerator) {
//        this.uiComponentsGenerator = uiComponentsGenerator;
//    }
//
//    @Override
//    public boolean supportComponent(Component component) {
//        return component instanceof Form && ((Form) component).getValueSourceProvider() instanceof ContainerValueSourceProvider;
//    }
//
//    @Override
//    protected void embed(Component component, Frame frame, List<AttributeDefinition> attributes) {
//        Form form = (Form) component;
//        for (AttributeDefinition attribute : attributes) {
//            addAttributeComponent(form, attribute);
//        }
//    }
//
//    protected MetaClass getEntityMetaClass(Component component) {
//        ValueSourceProvider valueSourceProvider = ((Form) component).getValueSourceProvider();
//        if (valueSourceProvider instanceof ContainerValueSourceProvider) {
//            return ((ContainerValueSourceProvider<?>) valueSourceProvider).getContainer().getEntityMetaClass();
//        }
//        return null;
//    }
//
//    protected void setLoadDynamicAttributes(Component component) {
//        ValueSourceProvider valueSourceProvider = ((Form) component).getValueSourceProvider();
//        if (valueSourceProvider instanceof ContainerValueSourceProvider) {
//            setLoadDynamicAttributes(((ContainerValueSourceProvider<?>) valueSourceProvider).getContainer());
//        }
//    }
//
//    protected void addAttributeComponent(Form form, AttributeDefinition attribute) {
//        String code = DynAttrUtils.getPropertyFromAttributeCode(attribute.getCode());
//
//        ValueSource<?> valueSource = form.getValueSourceProvider().getValueSource(code);
//
//        ComponentGenerationContext context = new ComponentGenerationContext(getEntityMetaClass(form), code);
//        context.setValueSource(valueSource);
//
//        Component resultComponent = uiComponentsGenerator.generate(context);
//
//        form.add(resultComponent);
//
//        setWidth(form, resultComponent, attribute);
//    }
//
//    protected void setWidth(Form form, Component component, AttributeDefinition attributeDefinition) {
//        String columnWidth = attributeDefinition.getConfiguration().getFormWidth();
//        if (Strings.isNullOrEmpty(columnWidth)) {
//            calculateAutoSize(form, component)
//                    .ifPresent(size -> component.setWidth(size.stringValue()));
//        } else if ("auto".equalsIgnoreCase(columnWidth)) {
//            component.setWidth(Component.AUTO_SIZE);
//        } else {
//            component.setWidth(columnWidth);
//        }
//    }
//
//    protected int findComponentColumn(Form form, Component component) {
//        for (int i = 0; i < form.getColumns(); i++) {
//            if (form.getComponents(i).contains(component)) {
//                return i;
//            }
//        }
//        throw new IllegalStateException("Unable to find component column");
//    }
//
//    protected Optional<SizeWithUnit> calculateAutoSize(Form form, Component component) {
//        int column = findComponentColumn(form, component);
//        OptionalDouble pixels = form.getComponents(column)
//                .stream()
//                .filter(c -> c != component)
//                .filter(c -> c.getWidthSizeUnit() == SizeUnit.PIXELS)
//                .mapToDouble(Component::getWidth)
//                .max();
//
//        if (pixels.isPresent()) {
//            return Optional.of(new SizeWithUnit((float) pixels.getAsDouble(), SizeUnit.PIXELS));
//        }
//
//        OptionalDouble percents = form.getComponents(column)
//                .stream()
//                .filter(c -> c != component)
//                .filter(c -> c.getWidthSizeUnit() == SizeUnit.PERCENTAGE)
//                .mapToDouble(Component::getWidth)
//                .max();
//
//        if (percents.isPresent()) {
//            return Optional.of(new SizeWithUnit((float) percents.getAsDouble(), SizeUnit.PERCENTAGE));
//        }
//
//        return Optional.empty();
//    }
//}
