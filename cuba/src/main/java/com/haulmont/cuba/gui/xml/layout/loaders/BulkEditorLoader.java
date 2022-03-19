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

package com.haulmont.cuba.gui.xml.layout.loaders;

import com.google.common.base.Splitter;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Security;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.WindowManager.OpenType;
import com.haulmont.cuba.gui.components.BulkEditor;
import com.haulmont.cuba.gui.components.Field;
import com.haulmont.cuba.gui.components.ListComponent;
import com.haulmont.cuba.gui.screen.compatibility.LegacyFrame;
import com.haulmont.cuba.gui.xml.data.ComponentLoaderHelper;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.Window;
import io.jmix.ui.app.bulk.ColumnsMode;
import io.jmix.ui.xml.layout.loader.AbstractComponentLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class BulkEditorLoader extends AbstractComponentLoader<BulkEditor> {

    protected void loadValidators(BulkEditor component, Element element) {
        List<Element> validatorElements = element.elements("validator");
        if (!validatorElements.isEmpty()) {
            List<Field.Validator> modelValidators = new ArrayList<>();
            Map<String, Field.Validator> fieldValidators = new LinkedHashMap<>();

            for (Element validatorElement : validatorElements) {
                Field.Validator validator = loadValidator(validatorElement);
                String field = validatorElement.attributeValue("field");

                if (StringUtils.isNotBlank(field)) {
                    fieldValidators.put(field, validator);
                } else {
                    modelValidators.add(validator);
                }
            }

            if (!fieldValidators.isEmpty()) {
                component.setFieldValidators(fieldValidators);
            }

            if (!modelValidators.isEmpty()) {
                component.setModelValidators(modelValidators);
            }
        }
    }

    protected Field.Validator loadValidator(Element validatorElement) {
        Consumer<?> consumer = ComponentLoaderHelper.loadValidator(validatorElement, context, getClassManager());
        if (!(consumer instanceof Field.Validator)) {
            throw new GuiDevelopmentException(
                    "BulkEditor validator must implement io.jmix.ui.component.Field.Validator", context);
        }
        return (Field.Validator) consumer;
    }

    @Override
    public void createComponent() {
        UiComponents uiComponents = applicationContext.getBean(UiComponents.class);
        resultComponent = uiComponents.create(BulkEditor.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        assignXmlDescriptor(resultComponent, element);
        assignFrame(resultComponent);

        Window window = ComponentsHelper.getWindow(resultComponent);
        if (window != null && !(window.getFrameOwner() instanceof LegacyFrame)) {
            throw new GuiDevelopmentException(
                    "BulkEditor component can be used only in legacy screens based on AbstractWindow",
                    context);
        }

        loadEnable(resultComponent, element);
        loadVisible(resultComponent, element);

        loadStyleName(resultComponent, element);

        loadHtmlSanitizerEnabled(resultComponent, element);

        loadCaption(resultComponent, element);
        loadDescription(resultComponent, element);
        loadIcon(resultComponent, element);

        loadWidth(resultComponent, element);
        loadAlign(resultComponent, element);
        loadResponsive(resultComponent, element);
        loadCss(resultComponent, element);

        loadColumns(resultComponent, element);

        loadTabIndex(resultComponent, element);

        Security security = AppBeans.get(Security.class);
        if (!security.isSpecificPermitted(BulkEditor.PERMISSION)) {
            resultComponent.setVisible(false);
        }

        String openType = element.attributeValue("openType");
        if (StringUtils.isNotEmpty(openType)) {
            resultComponent.setOpenType(OpenType.valueOf(openType));
        }

        String exclude = element.attributeValue("exclude");
        String includeProperties = element.attributeValue("includeProperties");

        if (StringUtils.isNotBlank(exclude) && StringUtils.isNotBlank(includeProperties)) {
            throw new GuiDevelopmentException(
                    "BulkEditor cannot define simultaneously exclude and includeProperties attributes", getContext());
        }

        if (StringUtils.isNotBlank(exclude)) {
            resultComponent.setExcludePropertiesRegex(exclude.replace(" ", ""));
        }

        if (StringUtils.isNotBlank(includeProperties)) {
            resultComponent.setIncludeProperties(
                    Splitter.on(',').omitEmptyStrings().trimResults().splitToList(includeProperties)
            );
        }

        String listComponent = element.attributeValue("for");
        if (StringUtils.isEmpty(listComponent)) {
            throw new GuiDevelopmentException("'for' attribute of bulk editor is not specified",
                    context, "componentId", resultComponent.getId());
        }

        String loadDynamicAttributes = element.attributeValue("loadDynamicAttributes");
        if (StringUtils.isNotEmpty(loadDynamicAttributes)) {
            resultComponent.setLoadDynamicAttributes(Boolean.parseBoolean(loadDynamicAttributes));
        }

        String useConfirmDialog = element.attributeValue("useConfirmDialog");
        if (StringUtils.isNotEmpty(useConfirmDialog)) {
            resultComponent.setUseConfirmDialog(Boolean.parseBoolean(useConfirmDialog));
        }

        getComponentContext().addPostInitTask((c, w) -> {
            if (resultComponent.getListComponent() == null) {
                Component bindComponent = resultComponent.getFrame().getComponent(listComponent);
                if (!(bindComponent instanceof ListComponent)) {
                    throw new GuiDevelopmentException("Specify 'for' attribute: id of table or tree",
                            context, "componentId", resultComponent.getId());
                }

                resultComponent.setListComponent((ListComponent) bindComponent);
            }
        });

        loadValidators(resultComponent, element);

        loadFocusable(resultComponent, element);
    }

    protected void loadColumns(BulkEditor component, Element element) {
        String value = element.attributeValue("columnsMode");
        if (StringUtils.isNotEmpty(value)) {
            ColumnsMode columnsMode = ColumnsMode.valueOf(value);
            component.setColumnsMode(columnsMode);
        }
    }

    protected UserSessionSource getUserSessionSource() {
        return (UserSessionSource) applicationContext.getBean(UserSessionSource.NAME);
    }
}
