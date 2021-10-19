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

package io.jmix.ui.facet;

import io.jmix.core.MessageTools;
import io.jmix.core.Metadata;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.datatype.impl.EnumClass;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.app.inputdialog.DialogActions;
import io.jmix.ui.app.inputdialog.InputParameter;
import io.jmix.ui.component.ActionsAwareDialogFacet;
import io.jmix.ui.component.Field;
import io.jmix.ui.component.InputDialogFacet;
import io.jmix.ui.component.impl.InputDialogFacetImpl;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.theme.ThemeConstants;
import io.jmix.ui.theme.ThemeConstantsManager;
import io.jmix.ui.xml.FacetProvider;
import io.jmix.ui.xml.layout.ComponentLoader;
import io.jmix.ui.xml.layout.loader.LayoutLoader;
import org.dom4j.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.jmix.ui.icon.Icons.ICON_NAME_REGEX;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Component("ui_InputDialogFacetProvider")
public class InputDialogFacetProvider implements FacetProvider<InputDialogFacet> {

    protected static final Pattern PARAM_TYPE_REGEX = Pattern.compile("^(\\w+)Parameter$");

    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected DatatypeRegistry datatypeRegistry;
    @Autowired
    protected Icons icons;
    @Autowired
    protected ThemeConstantsManager themeConstantsManager;

    @Override
    public Class<InputDialogFacet> getFacetClass() {
        return InputDialogFacet.class;
    }

    @Override
    public InputDialogFacet create() {
        return new InputDialogFacetImpl();
    }

    @Override
    public String getFacetTag() {
        return "inputDialog";
    }

    @Override
    public void loadFromXml(InputDialogFacet facet, Element element,
                            ComponentLoader.ComponentContext context) {
        loadId(facet, element);
        loadCaption(facet, element, context);

        loadWidth(facet, element);
        loadHeight(facet, element);

        loadTarget(facet, element, context);

        loadInputParameters(facet, element, context);
        loadDialogActions(facet, element, context);
    }

    protected void loadId(InputDialogFacet facet, Element element) {
        String id = element.attributeValue("id");
        if (isNotEmpty(id)) {
            facet.setId(id);
        }
    }

    protected void loadCaption(InputDialogFacet facet, Element element,
                               ComponentLoader.ComponentContext context) {
        String caption = element.attributeValue("caption");
        if (isNotEmpty(caption)) {
            facet.setCaption(loadResourceString(context, caption));
        }
    }

    protected void loadWidth(InputDialogFacet facet, Element element) {
        String width = element.attributeValue("width");
        if (isNotEmpty(width)) {
            facet.setWidth(width);
        }
    }

    protected void loadHeight(InputDialogFacet facet, Element element) {
        String height = element.attributeValue("height");
        if (isNotEmpty(height)) {
            facet.setHeight(height);
        }
    }

    protected void loadTarget(InputDialogFacet facet, Element element,
                              ComponentLoader.ComponentContext context) {
        String actionTarget = element.attributeValue("onAction");
        String buttonTarget = element.attributeValue("onButton");

        if (isNotEmpty(actionTarget)
                && isNotEmpty(buttonTarget)) {
            throw new GuiDevelopmentException(
                    "InputDialog facet should have either action or button target",
                    context);
        }

        if (isNotEmpty(actionTarget)) {
            facet.setActionTarget(actionTarget);
        } else if (isNotEmpty(buttonTarget)) {
            facet.setButtonTarget(buttonTarget);
        }
    }

    protected void loadDialogActions(InputDialogFacet facet, Element element,
                                     ComponentLoader.ComponentContext context) {
        loadDialogActions(facet, element);

        Element actions = element.element("actions");
        if (actions != null) {
            if (facet.getDialogActions() == null) {
                loadActions(facet, element, context);
            } else {
                throw new GuiDevelopmentException(
                        "Predefined and custom actions cannot be used for InputDialog at the same time",
                        context);
            }
        }
    }

    protected void loadActions(InputDialogFacet facet, Element element,
                               ComponentLoader.ComponentContext context) {
        Element actionsEl = element.element("actions");
        if (actionsEl == null) {
            return;
        }

        List<Element> actionElements = actionsEl.elements("action");

        List<ActionsAwareDialogFacet.DialogAction<InputDialogFacet>> actions = new ArrayList<>(actionElements.size());
        for (Element actionElement : actionElements) {
            actions.add(loadAction(actionElement, context));
        }

        facet.setActions(actions);
    }

    protected ActionsAwareDialogFacet.DialogAction<InputDialogFacet> loadAction(Element element,
                                                                                ComponentLoader.ComponentContext context) {
        String id = element.attributeValue("id");
        String caption = loadResourceString(context, element.attributeValue("caption"));
        String description = loadResourceString(context, element.attributeValue("description"));
        String icon = getIconPath(context, element.attributeValue("icon"));
        boolean primary = Boolean.parseBoolean(element.attributeValue("primary"));

        return new ActionsAwareDialogFacet.DialogAction<>(id, caption, description, icon, primary);
    }

    protected void loadDialogActions(InputDialogFacet facet, Element element) {
        String actions = element.attributeValue("dialogActions");
        if (isNotEmpty(actions)) {
            facet.setDialogActions(DialogActions.valueOf(actions));
        }
    }

    protected void loadInputParameters(InputDialogFacet facet, Element element,
                                       ComponentLoader.ComponentContext context) {
        List<InputParameter> inputParameters = new ArrayList<>();
        Set<String> paramIds = new HashSet<>();

        Element paramsEl = element.element("parameters");
        if (paramsEl == null) {
            return;
        }

        for (Element paramEl : paramsEl.elements()) {
            String paramId = paramEl.attributeValue("id");

            if (!paramIds.contains(paramId)) {
                inputParameters.add(loadInputParameter(paramEl, context));
                paramIds.add(paramId);
            } else {
                throw new GuiDevelopmentException("InputDialog parameters should have unique ids", context);
            }
        }

        if (inputParameters.isEmpty()) {
            throw new GuiDevelopmentException("InputDialog Facet cannot be used without parameters", context);
        }

        facet.setParameters(inputParameters.toArray(new InputParameter[]{}));
    }

    protected InputParameter loadInputParameter(Element paramEl,
                                                ComponentLoader.ComponentContext context) {
        String paramName = paramEl.getName();
        if ("entityParameter".equals(paramName)) {
            return loadEntityParameter(paramEl, context);
        } else if ("enumParameter".equals(paramName)) {
            return loadEnumParameter(paramEl, context);
        } else if (PARAM_TYPE_REGEX.matcher(paramName).matches()) {
            return loadPrimitiveParameter(paramEl, context);
        } else {
            throw new GuiDevelopmentException(
                    String.format("Unsupported type '%s' of InputDialog parameter '%s'",
                            paramName, paramEl.attributeValue("id")),
                    context);
        }
    }

    protected InputParameter loadPrimitiveParameter(Element paramEl,
                                                    ComponentLoader.ComponentContext context) {
        String paramId = paramEl.attributeValue("id");
        String paramName = paramEl.getName();

        InputParameter inputParameter;

        if ("bigDecimalParameter".equals(paramName)) {
            // Handle BigDecimal explicitly because its datatype id doesn't match with pattern "typeParameter"
            inputParameter = InputParameter.bigDecimalParameter(paramId)
                    .withCaption(loadParamCaption(paramEl, context))
                    .withRequired(loadParamRequired(paramEl))
                    .withRequiredMessage(loadRequiredMessage(paramEl, context))
                    .withDefaultValue(
                            loadDefaultValue(paramEl, datatypeRegistry.get(BigDecimal.class), context));
        } else {
            Datatype datatype = loadDatatype(paramEl, context);

            inputParameter = InputParameter.parameter(paramId)
                    .withCaption(loadParamCaption(paramEl, context))
                    .withRequired(loadParamRequired(paramEl))
                    .withRequiredMessage(loadRequiredMessage(paramEl, context))
                    .withDatatype(datatype)
                    .withDefaultValue(
                            loadDefaultValue(paramEl, datatype, context));
        }

        Field<?> field = loadField(paramId, paramEl, context);
        if (field != null) {
            inputParameter.withField(() -> field);
        }

        return inputParameter;
    }

    @SuppressWarnings("unchecked")
    protected InputParameter loadEntityParameter(Element paramEl,
                                                 ComponentLoader.ComponentContext context) {
        String paramId = paramEl.attributeValue("id");
        String classFqn = paramEl.attributeValue("entityClass");

        InputParameter parameter;

        Class clazz = loadParamClass(paramEl, classFqn, context);
        MetaClass entityClass = metadata.findClass(clazz);

        if (entityClass != null) {
            parameter = InputParameter.entityParameter(paramId, clazz)
                    .withCaption(loadParamCaption(paramEl, context))
                    .withRequired(loadParamRequired(paramEl))
                    .withRequiredMessage(loadRequiredMessage(paramEl, context));

            Field<?> field = loadField(paramId, paramEl, context);
            if (field != null) {
                parameter.withField(() -> field);
            }
        } else {
            throw new GuiDevelopmentException(
                    String.format(
                            "Unable to create InputDialog parameter '%s'. Class '%s' is not entity class",
                            paramId, classFqn),
                    context);
        }

        return parameter;
    }

    @SuppressWarnings("unchecked")
    protected InputParameter loadEnumParameter(Element paramEl,
                                               ComponentLoader.ComponentContext context) {
        String paramId = paramEl.attributeValue("id");
        String classFqn = paramEl.attributeValue("enumClass");

        InputParameter parameter;

        Class clazz = loadParamClass(paramEl, classFqn, context);

        if (EnumClass.class.isAssignableFrom(clazz)) {
            parameter = InputParameter.enumParameter(paramId, clazz)
                    .withCaption(loadParamCaption(paramEl, context))
                    .withRequired(loadParamRequired(paramEl))
                    .withRequiredMessage(loadRequiredMessage(paramEl, context));

            Field<?> field = loadField(paramId, paramEl, context);
            if (field != null) {
                parameter.withField(() -> field);
            }
        } else {
            throw new GuiDevelopmentException(
                    String.format(
                            "Unable to create InputDialog parameter '%s'. Class '%s' is not enum class",
                            paramId, classFqn),
                    context);
        }

        return parameter;
    }

    @Nullable
    protected Field<?> loadField(String paramId, Element element, ComponentLoader.ComponentContext context) {
        List<Element> elements = element.elements();
        if (elements.size() == 0) {
            return null;
        } else if (elements.size() > 1) {
            throw new GuiDevelopmentException(
                    String.format("InputParameter '%s' element cannot contain " +
                                    "two or more custom field definitions", paramId), context);
        }

        Element customFieldElement = elements.get(0);
        ComponentLoader loader = getLayoutLoader(context).createComponent(customFieldElement);
        io.jmix.ui.component.Component component = loader.getResultComponent();

        // Check field type before loading attributes
        if (!(component instanceof Field)) {
            throw new GuiDevelopmentException(
                    String.format("InputParameter '%s' custom field must implement " +
                            "io.jmix.ui.component.Field", paramId), context);
        }

        loader.loadComponent();
        return ((Field<?>) component);
    }

    protected LayoutLoader getLayoutLoader(ComponentLoader.Context context) {
        return applicationContext.getBean(LayoutLoader.class, context);
    }

    protected Datatype loadDatatype(Element element,
                                    ComponentLoader.ComponentContext context) {
        String paramName = element.getName();

        Matcher matcher = PARAM_TYPE_REGEX.matcher(paramName);
        if (matcher.matches()) {
            String typeName = matcher.group(1);
            return datatypeRegistry.get(typeName);
        } else {
            throw new GuiDevelopmentException(
                    String.format("Unsupported InputDialog parameter type: '%s'", paramName),
                    context);
        }
    }

    @Nullable
    protected String loadParamCaption(Element paramEl,
                                      ComponentLoader.ComponentContext context) {
        String caption = paramEl.attributeValue("caption");
        if (isNotEmpty(caption)) {
            return loadResourceString(context, caption);
        }
        return null;
    }

    protected boolean loadParamRequired(Element paramEl) {
        String required = paramEl.attributeValue("required");
        if (isNotEmpty(required)) {
            return Boolean.parseBoolean(required);
        }
        return false;
    }

    @Nullable
    protected String loadRequiredMessage(Element paramEl,
                                         ComponentLoader.ComponentContext context) {
        String requiredMessage = paramEl.attributeValue("requiredMessage");
        if (isNotEmpty(requiredMessage)) {
            return loadResourceString(context, requiredMessage);
        }
        return null;
    }

    @Nullable
    protected Object loadDefaultValue(Element paramEl, Datatype datatype,
                                      ComponentLoader.ComponentContext context) {
        String defaultValue = paramEl.attributeValue("defaultValue");
        if (isNotEmpty(defaultValue)) {
            try {
                return datatype.parse(defaultValue);
            } catch (ParseException e) {
                throw new GuiDevelopmentException(
                        String.format("Unable to parse default value '%s' as '%s' for InputDialog parameter '%s'",
                                defaultValue, datatype, paramEl.attributeValue("id")),
                        context);
            }
        }
        return null;
    }

    protected Class loadParamClass(Element paramEl, String classFqn,
                                   ComponentLoader.ComponentContext context) {
        try {
            return ReflectionHelper.loadClass(classFqn);
        } catch (ClassNotFoundException e) {
            throw new GuiDevelopmentException(
                    String.format(
                            "Unable to create InputDialog parameter '%s'. Class '%s' not found",
                            paramEl.attributeValue("id"), classFqn),
                    context);
        }
    }

    @Nullable
    protected String loadResourceString(ComponentLoader.ComponentContext context, @Nullable String caption) {
        if (isEmpty(caption)) {
            return caption;
        }

        Class screenClass = context.getFrame()
                .getFrameOwner()
                .getClass();

        return messageTools.loadString(screenClass.getPackage().getName(), caption);
    }

    @Nullable
    protected String getIconPath(ComponentLoader.ComponentContext context, @Nullable String icon) {
        if (icon == null || icon.isEmpty()) {
            return null;
        }

        String iconPath = null;

        if (ICON_NAME_REGEX.matcher(icon).matches()) {
            iconPath = icons.get(icon);
        }

        if (isEmpty(iconPath)) {
            String themeValue = loadThemeString(icon);
            iconPath = loadResourceString(context, themeValue);
        }

        return iconPath;
    }

    @Nullable
    protected String loadThemeString(@Nullable String value) {
        if (value != null && value.startsWith(ThemeConstants.PREFIX)) {
            value = themeConstantsManager.getConstants()
                    .get(value.substring(ThemeConstants.PREFIX.length()));
        }
        return value;
    }
}
