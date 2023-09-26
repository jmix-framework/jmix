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
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.formlayout.FormLayout;
import io.jmix.core.AccessManager;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.dynattr.DynAttrUtils;
import io.jmix.flowui.component.ComponentGenerationContext;
import io.jmix.flowui.component.UiComponentsGenerator;
import io.jmix.flowui.component.formlayout.JmixFormLayout;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.ValueSourceProvider;
import io.jmix.flowui.data.value.ContainerValueSourceProvider;
import io.jmix.flowui.view.View;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

@org.springframework.stereotype.Component("dynat_FormEmbeddingStrategy")
public class FormEmbeddingStrategy extends BaseEmbeddingStrategy {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    protected final UiComponentsGenerator uiComponentsGenerator;

    protected FormEmbeddingStrategy(Metadata metadata,
                                    MetadataTools metadataTools,
                                    DynAttrMetadata dynAttrMetadata,
                                    AccessManager accessManager,
                                    UiComponentsGenerator uiComponentsGenerator) {
        super(metadata, metadataTools, dynAttrMetadata, accessManager);
        this.uiComponentsGenerator = uiComponentsGenerator;
    }


    @Override
    public boolean supportComponent(Component component) {
        return component instanceof JmixFormLayout && ((JmixFormLayout) component).getValueSourceProvider() instanceof ContainerValueSourceProvider;
    }

    @Override
    protected void embed(Component component, View view, List<AttributeDefinition> attributes) {
        FormLayout form = (FormLayout) component;
        for (AttributeDefinition attribute : attributes) {
            addAttributeComponent((JmixFormLayout) form, attribute);
        }
    }

    protected MetaClass getEntityMetaClass(Component component) {
        ValueSourceProvider valueSourceProvider = ((JmixFormLayout) component).getValueSourceProvider();
        if (valueSourceProvider instanceof ContainerValueSourceProvider) {
            return ((ContainerValueSourceProvider<?>) valueSourceProvider).getContainer().getEntityMetaClass();
        }
        return null;
    }

    protected void setLoadDynamicAttributes(Component component) {
        ValueSourceProvider valueSourceProvider = ((JmixFormLayout) component).getValueSourceProvider();
        if (valueSourceProvider instanceof ContainerValueSourceProvider) {
            setLoadDynamicAttributes(((ContainerValueSourceProvider<?>) valueSourceProvider).getContainer());
        }
    }

    protected void addAttributeComponent(JmixFormLayout form, AttributeDefinition attribute) {
        String code = DynAttrUtils.getPropertyFromAttributeCode(attribute.getCode());

        ValueSource<?> valueSource = form.getValueSourceProvider().getValueSource(code);

        ComponentGenerationContext context = new ComponentGenerationContext(getEntityMetaClass(form), code);
        context.setValueSource(valueSource);

        Component resultComponent = uiComponentsGenerator.generate(context);

        form.add(resultComponent);

        setWidth(form, (HasSize) resultComponent, attribute);
    }

    protected void setWidth(FormLayout form, HasSize component, AttributeDefinition attributeDefinition) {
        String columnWidth = attributeDefinition.getConfiguration().getFormWidth();
        if (Strings.isNullOrEmpty(columnWidth)) {
            calculateAutoSize(form, (Component) component).ifPresent(size -> component.setWidth(size));
        } else if ("auto".equalsIgnoreCase(columnWidth)) {
            component.setWidth("AUTO");
        } else {
            component.setWidth(columnWidth);
        }
    }

    protected int findComponentColumn(FormLayout form, Component component) {
        for (int i = 0; i < form.getChildren().count(); i++) {
            if (form.getChildren().toList().contains(component)) {
                return i;
            }
        }
        throw new IllegalStateException("Unable to find component column");
    }

    protected Optional<String> calculateAutoSize(FormLayout form, Component component) {
        int column = findComponentColumn(form, component);
        OptionalDouble pixels = form.getChildren()
                .filter(c -> c != component && c instanceof HasSize)
                .filter(c -> ((HasSize) c).getWidthUnit().isPresent() && ((HasSize) c).getWidthUnit().get() == Unit.PIXELS)
                .mapToDouble(value -> Unit.getSize(((HasSize) value).getWidth()))
                .max();

        if (pixels.isPresent()) {
            return Optional.of(HasSize.getCssSize((float) pixels.getAsDouble(), Unit.PIXELS));
        }

        OptionalDouble percents = form.getChildren()
                .filter(c -> c != component && c instanceof HasSize)
                .filter(c -> ((HasSize) c).getWidthUnit().isPresent() && ((HasSize) c).getWidthUnit().get() == Unit.PERCENTAGE)
                .mapToDouble(value -> Unit.getSize(((HasSize) value).getWidth()))
                .max();

        if (percents.isPresent()) {
            return Optional.of(HasSize.getCssSize((float) percents.getAsDouble(), Unit.PERCENTAGE));
        }

        return Optional.empty();
    }
}
