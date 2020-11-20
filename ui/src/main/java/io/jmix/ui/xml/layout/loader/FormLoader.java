/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.xml.layout.loader;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.jmix.core.MessageTools;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.component.*;
import io.jmix.ui.component.SupportsCaptionPosition.CaptionPosition;
import io.jmix.ui.component.data.HasValueSource;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.component.data.options.ContainerOptions;
import io.jmix.ui.component.data.value.ContainerValueSource;
import io.jmix.ui.component.data.value.ContainerValueSourceProvider;
import io.jmix.ui.component.form.ComponentPosition;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.model.ScreenData;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.xml.layout.ComponentLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FormLoader extends AbstractComponentLoader<Form> {

    @Override
    public void createComponent() {
        resultComponent = factory.create(Form.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        assignFrame(resultComponent);

        loadVisible(resultComponent, element);
        loadWidth(resultComponent, element);
        loadHeight(resultComponent, element);

        loadEditable(resultComponent, element);
        loadEnable(resultComponent, element);

        loadStyleName(resultComponent, element);

        loadHtmlSanitizerEnabled(resultComponent, element);

        loadCaption(resultComponent, element);
        loadDescription(resultComponent, element);
        loadIcon(resultComponent, element);
        loadContextHelp(resultComponent, element);
        loadCss(resultComponent, element);

        loadAlign(resultComponent, element);

        loadCaptionPosition(resultComponent, element);
        loadChildrenCaptionAlignment(resultComponent, element);
        loadChildrenCaptionWidth(resultComponent, element);

        loadDataContainer(resultComponent, element);

        loadColumns(resultComponent, element);
    }

    protected void loadDataContainer(Form resultComponent, Element element) {
        String containerId = element.attributeValue("dataContainer");
        if (!Strings.isNullOrEmpty(containerId)) {
            FrameOwner frameOwner = getComponentContext().getFrame().getFrameOwner();
            ScreenData screenData = UiControllerUtils.getScreenData(frameOwner);
            InstanceContainer container = screenData.getContainer(containerId);
            //noinspection unchecked
            resultComponent.setValueSourceProvider(new ContainerValueSourceProvider(container));
        }
    }

    protected void loadColumns(Form resultComponent, Element element) {
        if (element.elements("column").isEmpty()) {
            Iterable<ComponentPosition> rootComponents = loadComponents(element, null, null);
            for (ComponentPosition component : rootComponents) {
                resultComponent.add(component.getComponent(), 0,
                        component.getColSpan(), component.getRowSpan());
            }
        } else {
            List<Element> columnElements = element.elements("column");
            if (element.elements().size() > columnElements.size()) {
                String fieldGroupId = resultComponent.getId();
                Map<String, Object> params = Strings.isNullOrEmpty(fieldGroupId)
                        ? Collections.emptyMap()
                        : ParamsMap.of("Form ID", fieldGroupId);
                throw new GuiDevelopmentException("Form component elements have to be placed within its column.",
                        context, params);
            }

            resultComponent.setColumns(columnElements.size());

            int colIndex = 0;
            for (Element columnElement : columnElements) {
                String columnWidth = loadThemeString(columnElement.attributeValue("width"));

                String flexAttr = columnElement.attributeValue("flex");
                Float flex = null;
                if (!Strings.isNullOrEmpty(flexAttr)) {
                    flex = Float.parseFloat(flexAttr);
                    resultComponent.setColumnFlex(colIndex, flex);
                }

                Iterable<ComponentPosition> columnComponents = loadComponents(columnElement, columnWidth, flex);
                for (ComponentPosition component : columnComponents) {
                    resultComponent.add(component.getComponent(), colIndex,
                            component.getColSpan(), component.getRowSpan());
                }

                loadChildrenCaptionAlignment(resultComponent, columnElement, colIndex);
                loadChildrenCaptionWidth(resultComponent, columnElement, colIndex);

                colIndex++;
            }
        }
    }

    protected List<ComponentPosition> loadComponents(Element element, @Nullable String columnWidth, @Nullable Float flex) {
        List<Element> elements = element.elements();
        if (elements.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<ComponentPosition> components = new ArrayList<>(elements.size());
            for (Element componentElement : elements) {
                ComponentPosition component = loadComponent(componentElement, columnWidth, flex);
                components.add(component);
            }
            return components;
        }
    }

    protected ComponentPosition loadComponent(Element element, @Nullable String columnWidth, @Nullable Float flex) {
        Component component;
        if ("field".equals(element.getName())) {
            component = loadField(element);
        } else {
            LayoutLoader loader = getLayoutLoader();

            ComponentLoader childComponentLoader = loader.createComponent(element);
            childComponentLoader.loadComponent();

            component = childComponentLoader.getResultComponent();
        }

        // Set default width
        String componentWidth = element.attributeValue("width");
        if (Strings.isNullOrEmpty(componentWidth)) {
            if (columnWidth != null) {
                component.setWidth(columnWidth);
            } else if (flex != null) {
                component.setWidthFull();
            }
        }

        // Set default caption
        if (component instanceof HasValueSource
                && ((HasValueSource) component).getValueSource() instanceof EntityValueSource
                && component instanceof Component.HasCaption
                && ((Component.HasCaption) component).getCaption() == null) {
            EntityValueSource valueSource = ((EntityValueSource) ((HasValueSource) component).getValueSource());

            MetaPropertyPath metaPropertyPath = valueSource.getMetaPropertyPath();

            String propertyName = metaPropertyPath != null ? metaPropertyPath.getMetaProperty().getName() : null;
            if (metaPropertyPath != null) {
                MetaClass propertyMetaClass = getMetadataTools().getPropertyEnclosingMetaClass(metaPropertyPath);
                String propertyCaption = getMessageTools().getPropertyCaption(propertyMetaClass, propertyName);
                ((Component.HasCaption) component).setCaption(propertyCaption);
            }
        }

        int colspan = loadSpan(element, "colspan");
        int rowspan = loadSpan(element, "rowspan");

        return new ComponentPosition(component, colspan, rowspan);
    }

    protected int loadSpan(Element element, String spanName) {
        String spanValue = element.attributeValue(spanName);
        if (!Strings.isNullOrEmpty(spanValue)) {
            int span = Integer.parseInt(spanValue);
            if (span < 1) {
                throw new GuiDevelopmentException("Form " + spanName + " can not be less than 1",
                        context, spanName, span);
            }
            if (span == 1) {
                LoggerFactory.getLogger(FormLoader.class).warn("Do not use " + spanName +
                        "=\"1\", it will have no effect");
            }
            return span;
        }

        return 1;
    }

    protected Field loadField(Element element) {
        Field component = createField(element);

        loadId(component, element);

        assignFrame(component);
        assignXmlDescriptor(component, element);

        loadEditable(component, element);
        loadEnable(component, element);
        loadVisible(component, element);

        loadCaption(component, element);
        loadCaptionAsHtml(component, element);
        loadDescription(component, element);
        loadDescriptionAsHtml(component, element);

        loadIcon(component, element);
        loadContextHelp(component, element);

        loadHeight(component, element);
        loadWidth(component, element);

        loadResponsive(component, element);
        loadStyleName(component, element);
        loadCss(component, element);

        return component;
    }

    @SuppressWarnings("unchecked")
    protected Field createField(Element element) {
        String property = element.attributeValue("property");
        if (Strings.isNullOrEmpty(property)) {
            throw new GuiDevelopmentException("Field element has no 'property' attribute", context);
        }

        InstanceContainer container = loadContainer(element, property).orElseThrow(() ->
                new GuiDevelopmentException(String.format("Can't infer component for field '%s'. " +
                        "No data container associated with it", property), context));
        MetaClass metaClass = container.getEntityMetaClass();

        ComponentGenerationContext context = new ComponentGenerationContext(metaClass, property);
        context.setTargetClass(Form.class);
        context.setValueSource(new ContainerValueSource<>(container, property));
        context.setXmlDescriptor(element);

        loadOptionsContainer(element).ifPresent(optionsContainer ->
                context.setOptions(new ContainerOptions(optionsContainer)));

        Component component = getUiComponentsGenerator().generate(context);
        Preconditions.checkState(component instanceof Field,
                "Form field '%s' must implement io.jmix.ui.component.Field", property);
        return (Field) component;
    }


    protected void loadWidth(Component component, String width) {
        if ("auto".equalsIgnoreCase(width)) {
            component.setWidth(Component.AUTO_SIZE);
        } else if (StringUtils.isNotBlank(width)) {
            component.setWidth(loadThemeString(width));
        }
    }

    protected MetadataTools getMetadataTools() {
        return applicationContext.getBean(MetadataTools.class);
    }

    protected UiComponentsGenerator getUiComponentsGenerator() {
        return (UiComponentsGenerator) applicationContext.getBean(UiComponentsGenerator.NAME);
    }

    protected void loadCaptionPosition(Form resultComponent, Element element) {
        String captionAlignment = element.attributeValue("captionPosition");
        if (!Strings.isNullOrEmpty(captionAlignment)) {
            resultComponent.setCaptionPosition(CaptionPosition.valueOf(captionAlignment));
        }
    }

    @Nullable
    protected String loadChildrenCaptionWidth(Element element) {
        String childCaptionWidth = element.attributeValue("childrenCaptionWidth");
        if (!Strings.isNullOrEmpty(childCaptionWidth)) {
            if (childCaptionWidth.startsWith(MessageTools.MARK)) {
                childCaptionWidth = loadResourceString(childCaptionWidth);
            }

            if ("auto".equalsIgnoreCase(childCaptionWidth)) {
                childCaptionWidth = Component.AUTO_SIZE;
            }

            if (childCaptionWidth.endsWith("px")) {
                childCaptionWidth = childCaptionWidth.substring(0, childCaptionWidth.indexOf("px"));
            }

            return childCaptionWidth;
        }

        return null;
    }

    protected void loadChildrenCaptionWidth(Form resultComponent, Element element) {
        String childrenCaptionWidth = loadChildrenCaptionWidth(element);
        if (childrenCaptionWidth != null) {
            resultComponent.setChildrenCaptionWidth(Integer.parseInt(childrenCaptionWidth));
        }
    }

    protected void loadChildrenCaptionWidth(Form resultComponent, Element element, int colIndex) {
        String childrenCaptionWidth = loadChildrenCaptionWidth(element);
        if (childrenCaptionWidth != null) {
            resultComponent.setChildrenCaptionWidth(colIndex, Integer.parseInt(childrenCaptionWidth));
        }
    }

    @Nullable
    protected Form.CaptionAlignment loadChildrenCaptionAlignment(Element element) {
        String childrenCaptionAlignment = element.attributeValue("childrenCaptionAlignment");
        if (!Strings.isNullOrEmpty(childrenCaptionAlignment)) {
            return Form.CaptionAlignment.valueOf(childrenCaptionAlignment);
        }

        return null;
    }

    protected void loadChildrenCaptionAlignment(Form resultComponent, Element element) {
        Form.CaptionAlignment childrenCaptionAlignment = loadChildrenCaptionAlignment(element);
        if (childrenCaptionAlignment != null) {
            resultComponent.setChildrenCaptionAlignment(childrenCaptionAlignment);
        }
    }

    protected void loadChildrenCaptionAlignment(Form resultComponent, Element element, int colIndex) {
        Form.CaptionAlignment childrenCaptionAlignment = loadChildrenCaptionAlignment(element);
        if (childrenCaptionAlignment != null) {
            resultComponent.setChildrenCaptionAlignment(colIndex, childrenCaptionAlignment);
        }
    }
}
