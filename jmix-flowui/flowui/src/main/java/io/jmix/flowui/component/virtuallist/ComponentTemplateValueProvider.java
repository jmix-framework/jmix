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

package io.jmix.flowui.component.virtuallist;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.function.ValueProvider;
import io.jmix.core.Entity;
import io.jmix.core.Messages;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.flowui.model.DataComponents;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.ViewData;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.LoaderResolver;
import io.jmix.flowui.xml.layout.loader.LayoutLoader;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@org.springframework.stereotype.Component("ui_ComponentTemplateValueProvider")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ComponentTemplateValueProvider<SOURCE> implements ValueProvider<SOURCE, Component> {

    //{E}, {E}.name, {E}.user.$name etc.
    protected static final Pattern TEMPLATE_VARIABLE_PATTERN = Pattern.compile("(\\{E})((\\.[\\w$]+)*)");
    protected static final String INSTANCE_CONTAINER_ID = "_virtualListRowDc";

    protected Element templateElement;
    protected ComponentLoader.Context context;

    //attribute / property path from template variable
    protected Map<Attribute, String> propertyPathsByAttributes = new HashMap<>();
    protected boolean generateInstanceContainer = false;

    protected MetadataTools metadataTools;
    protected Messages messages;
    protected DataComponents dataComponents;
    protected LoaderResolver loaderResolver;
    protected ObjectProvider<LayoutLoader> layoutLoaderProvider;

    public ComponentTemplateValueProvider(Element templateElement, ComponentLoader.Context context) {
        this.templateElement = templateElement.createCopy();
        this.context = context;
    }

    @Autowired
    public void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setDataComponents(DataComponents dataComponents) {
        this.dataComponents = dataComponents;
    }

    @Autowired
    public void setLayoutLoaderProvider(ObjectProvider<LayoutLoader> layoutLoaderProvider) {
        this.layoutLoaderProvider = layoutLoaderProvider;
    }

    @Autowired
    public void setLoaderResolver(LoaderResolver loaderResolver) {
        this.loaderResolver = loaderResolver;
    }

    @PostConstruct
    public void init() throws Exception {
        findTemplatedAttributesRecursive(this.templateElement);
        updateContainerRecursive(this.templateElement);
    }

    protected void findTemplatedAttributesRecursive(Element element) {
        for (Attribute attribute : element.attributes()) {
            String elementAttributeValue = attribute.getValue().trim();
            Matcher matcher = TEMPLATE_VARIABLE_PATTERN.matcher(elementAttributeValue);

            if (matcher.matches()) {
                String propertyPathGroup = matcher.group(2);
                String propertyPath = StringUtils.isEmpty(propertyPathGroup)
                        ? ""
                        //remove the first dot
                        : propertyPathGroup.substring(1);
                propertyPathsByAttributes.put(attribute, propertyPath);
            }
        }
        for (Element childElement : element.elements()) {
            findTemplatedAttributesRecursive(childElement);
        }
    }

    protected void updateContainerRecursive(Element element) {
        String propertyValue = element.attributeValue("property");

        if (propertyValue != null) {
            String containerId = findContainerId(element);

            if (containerId == null) {
                element.addAttribute("dataContainer", INSTANCE_CONTAINER_ID);
                generateInstanceContainer = true;
            }
        }
        for (Element childElement : element.elements()) {
            updateContainerRecursive(childElement);
        }
    }

    @Nullable
    protected String findContainerId(Element element) {
        String containerId = element.attributeValue("dataContainer");

        // In case a component has only a property,
        // we try to obtain `dataContainer` from a parent element.
        // For instance, a component is placed within the Form component
        if (containerId == null) {
            containerId = getParentDataContainer(element);
        }
        return containerId;
    }

    @Nullable
    protected String getParentDataContainer(Element element) {
        Element parent = element.getParent();
        while (parent != null) {
            if (loaderResolver.getLoader(parent) != null && parent.attributeValue("dataContainer") != null) {
                return parent.attributeValue("dataContainer");
            }
            parent = parent.getParent();
        }
        return null;
    }

    @Override
    public Component apply(SOURCE source) {
        substituteTemplateVariablesWithValues(source);

        registerInstanceContainer(source);

        Component resultComponent = createComponent();

        unregisterInstanceContainer();
        return resultComponent;
    }

    protected void substituteTemplateVariablesWithValues(SOURCE source) {
        for (Map.Entry<Attribute, String> attributeFieldPathEntry : propertyPathsByAttributes.entrySet()) {
            Attribute attribute = attributeFieldPathEntry.getKey();
            String propertyPath = attributeFieldPathEntry.getValue();

            String attributeStringValue;
            if (propertyPath.isEmpty()) {
                attributeStringValue = convertToString(source);
            } else {
                Object attributeValue = getAttributeValue(source, propertyPath);
                attributeStringValue = convertToString(attributeValue);
            }

            attribute.setValue(attributeStringValue);
        }
    }

    protected String convertToString(@Nullable Object source) {
        if (source == null) {
            return "";
        }
        String attributeValueString;
        if (source instanceof Entity) {
            attributeValueString = metadataTools.getInstanceName(source);
        } else if (source instanceof Enum<?> enumValue) {
            attributeValueString = messages.getMessage(enumValue);
        } else {
            attributeValueString = source.toString();
        }
        return attributeValueString;
    }

    @Nullable
    protected Object getAttributeValue(Object source, String path) {
        //remove the first dot
        if (source instanceof Entity) {
            return EntityValues.getValueEx(source, path);
        } else {
            throw new IllegalArgumentException("Property path is supported only for entity items");
        }
    }

    protected void registerInstanceContainer(SOURCE source) {
        if (generateInstanceContainer) {
            //noinspection unchecked
            InstanceContainer<SOURCE> instanceContainer =
                    (InstanceContainer<SOURCE>) dataComponents.createInstanceContainer(source.getClass());
            instanceContainer.setItem(source);

            ViewData viewData = getViewData();
            viewData.registerContainer(INSTANCE_CONTAINER_ID, instanceContainer);
        }
    }

    protected ViewData getViewData() {
        if (!(context instanceof ComponentLoader.ComponentContext componentContext)) {
            throw new IllegalStateException("The context must be an instance of ComponentContext");
        }
        View<?> view = componentContext.getView();
        return ViewControllerUtils.getViewData(view);
    }

    protected Component createComponent() {
        ComponentLoader<?> componentLoader = getRootLoader();
        componentLoader.initComponent();
        componentLoader.loadComponent();
        return componentLoader.getResultComponent();
    }

    protected ComponentLoader<?> getRootLoader() {
        LayoutLoader layoutLoader = layoutLoaderProvider.getObject(context);
        return layoutLoader.createComponentLoader(templateElement);
    }

    protected void unregisterInstanceContainer() {
        if (generateInstanceContainer) {
            getViewData().getContainerIds().remove(INSTANCE_CONTAINER_ID);
        }
    }
}
