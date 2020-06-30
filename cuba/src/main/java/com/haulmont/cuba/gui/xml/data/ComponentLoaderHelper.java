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

package com.haulmont.cuba.gui.xml.data;

import com.google.common.collect.ImmutableList;
import com.haulmont.cuba.gui.components.HasSettings;
import com.haulmont.cuba.gui.components.Field;
import com.haulmont.cuba.gui.components.PickerField;
import io.jmix.core.BeanLocator;
import io.jmix.core.HotDeployManager;
import com.haulmont.cuba.gui.components.validators.DateValidator;
import com.haulmont.cuba.gui.components.validators.DoubleValidator;
import com.haulmont.cuba.gui.components.validators.IntegerValidator;
import com.haulmont.cuba.gui.components.validators.LongValidator;
import com.haulmont.cuba.gui.components.validators.ScriptValidator;
import io.jmix.core.HotDeployManager;
import io.jmix.core.Messages;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.gui.OpenType;
import io.jmix.ui.screen.compatibility.CubaLegacyFrame;
import io.jmix.ui.xml.layout.ComponentLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import javax.annotation.Nullable;
import java.util.List;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public final class ComponentLoaderHelper {

    protected static final List<Class<?>> UNSUPPORTED_DECLARATIVE_RENDERERS = ImmutableList.of(
            io.jmix.ui.component.DataGrid.ButtonRenderer.class,
            io.jmix.ui.component.DataGrid.ClickableTextRenderer.class,
            io.jmix.ui.component.DataGrid.ImageRenderer.class
    );

    protected static final List<Class<?>> UNSUPPORTED_PARAMETERIZED_RENDERERS = ImmutableList.of(
            io.jmix.ui.component.DataGrid.DateRenderer.class,
            io.jmix.ui.component.DataGrid.LocalDateRenderer.class,
            io.jmix.ui.component.DataGrid.LocalDateTimeRenderer.class,
            io.jmix.ui.component.DataGrid.NumberRenderer.class
    );

    public static Optional<Action> loadLegacyPickerAction(PickerField actionsHolder,
                                                          Element element, ComponentLoader.Context context,
                                                          String actionId) {
        if (StringUtils.isBlank(element.attributeValue("invoke")) && isLegacyFrame(context)) {
            // Try to create a standard picker action
            for (PickerField.ActionType type : PickerField.ActionType.values()) {
                if (type.getId().equals(actionId)) {
                    Action action = type.createAction(actionsHolder);
                    if (type != PickerField.ActionType.LOOKUP && type != PickerField.ActionType.OPEN) {
                        return Optional.of(action);
                    }

                    String openTypeString = element.attributeValue("openType");
                    if (openTypeString == null) {
                        return Optional.of(action);
                    }

                    OpenType openType;
                    try {
                        openType = OpenType.valueOf(openTypeString);
                    } catch (IllegalArgumentException e) {
                        throw new GuiDevelopmentException(
                                String.format("Unknown open type: '%s' for action: '%s'", openTypeString, actionId),
                                context);
                    }

                    if (action instanceof PickerField.LookupAction) {
                        ((PickerField.LookupAction) action).setLookupScreenOpenType(openType);
                    } else if (action instanceof PickerField.OpenAction) {
                        ((PickerField.OpenAction) action).setEditScreenOpenType(openType);
                    }
                    return Optional.of(action);
                }
            }
        }

        return Optional.empty();
    }

    public static boolean isLegacyFrame(ComponentLoader.Context context) {
        return context instanceof ComponentLoader.ComponentContext
                && ((ComponentLoader.ComponentContext) context).getFrame().getFrameOwner() instanceof CubaLegacyFrame;
    }

    public static void loadSettingsEnabled(HasSettings component, Element element) {
        String settingsEnabled = element.attributeValue("settingsEnabled");
        if (StringUtils.isNotEmpty(settingsEnabled)) {
            component.setSettingsEnabled(Boolean.parseBoolean(settingsEnabled));
        }
    }

    @Nullable
    public static DataGrid.Renderer loadLegacyRenderer(Element rendererElement, ComponentLoader.Context context,
                                                       HotDeployManager hotDeployManager, BeanLocator beanLocator) {
        String rendererType = rendererElement.attributeValue("type");
        if (StringUtils.isEmpty(rendererType)) {
            return null;
        }

        Class<?> rendererClass = hotDeployManager.loadClass(rendererType);

        if (UNSUPPORTED_PARAMETERIZED_RENDERERS.contains(rendererClass)) {
            throw new GuiDevelopmentException(String.format(
                    "DataGrid doesn't support renderer of type '%s' without required parameters. " +
                            "Use special XML elements for parameterized renderers.",
                    rendererType), context);
        }

        if (UNSUPPORTED_DECLARATIVE_RENDERERS.contains(rendererClass)) {
            throw new GuiDevelopmentException(String.format(
                    "DataGrid doesn't support declarative configuration of renderer of type '%s'. " +
                            "Define it in screen controller.",
                    rendererType), context);
        }

        return (DataGrid.Renderer) beanLocator.getPrototype(rendererClass);
    }

    public static void loadTableValidators(io.jmix.ui.component.Table component, Element element, ComponentLoader.Context context,
                                           HotDeployManager hotDeployManager) {
        List<Element> validatorElements = element.elements("validator");

        for (Element validatorElement : validatorElements) {
            Consumer<?> validator = loadValidator(validatorElement, context, hotDeployManager);
            component.addValidator(validator);
        }
    }

    public static void loadTableColumnValidators(io.jmix.ui.component.Table component, io.jmix.ui.component.Table.Column column,
                                                 ComponentLoader.Context context, HotDeployManager hotDeployManager, Messages messages) {
        List<Element> validatorElements = column.getXmlDescriptor().elements("validator");

        if (!validatorElements.isEmpty()) {
            for (Element validatorElement : validatorElements) {
                Consumer<?> validator = loadValidator(validatorElement, context, hotDeployManager);
                component.addValidator(column, validator);
            }
        } else if (column.isEditable()) {
            if (!(column.getId() instanceof MetaPropertyPath)) {
                throw new GuiDevelopmentException(String.format("Column '%s' has editable=true, but there is no " +
                        "property of an entity with this id", column.getId()), context);
            }

            MetaPropertyPath propertyPath = (MetaPropertyPath) column.getId();
            Consumer<?> validator = getDefaultValidator(propertyPath.getMetaProperty(), messages);
            if (validator != null) {
                component.addValidator(column, validator);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void loadValidators(Field component, Element element, ComponentLoader.Context context,
                                      HotDeployManager hotDeployManager, Messages messages) {
        List<Element> validatorElements = element.elements("validator");

        if (!validatorElements.isEmpty()) {
            for (Element validatorElement : validatorElements) {
                Consumer<?> validator = loadValidator(validatorElement, context, hotDeployManager);
                component.addValidator(validator);
            }
        } else if (component.getDatasource() != null) {
            MetaProperty property = component.getMetaProperty();
            if (property != null) {
                Consumer<?> validator = ComponentLoaderHelper.getDefaultValidator(property, messages);
                if (validator != null) {
                    component.addValidator(validator);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static Consumer<?> loadValidator(Element validatorElement, ComponentLoader.Context context, HotDeployManager hotDeployManager) {
        String className = validatorElement.attributeValue("class");
        String scriptPath = validatorElement.attributeValue("script");
        String script = validatorElement.getText();

        Consumer<?> validator = null;

        if (StringUtils.isNotBlank(scriptPath) || StringUtils.isNotBlank(script)) {
            validator = new ScriptValidator(validatorElement, context.getMessagesPack());
        } else {
            Class aClass = hotDeployManager.findClass(className);
            if (aClass == null)
                throw new GuiDevelopmentException(String.format("Class %s is not found", className), context);
            if (!StringUtils.isBlank(context.getMessagesPack()))
                try {
                    validator = (Consumer<?>) ReflectionHelper.newInstance(aClass, validatorElement, context.getMessagesPack());
                } catch (NoSuchMethodException e) {
                    //
                }
            if (validator == null) {
                try {
                    validator = (Consumer<?>) ReflectionHelper.newInstance(aClass, validatorElement);
                } catch (NoSuchMethodException e) {
                    try {
                        validator = (Consumer<?>) ReflectionHelper.newInstance(aClass);
                    } catch (NoSuchMethodException e1) {
                        // todo log warn
                    }
                }
            }
            if (validator == null) {
                throw new GuiDevelopmentException(
                        String.format("Validator class %s has no supported constructors", aClass), context);
            }
        }
        return validator;
    }

    public static Consumer<?> getDefaultValidator(MetaProperty property, Messages messages) {
        Consumer<?> validator = null;
        if (property.getRange().isDatatype()) {
            Class type = property.getRange().asDatatype().getJavaClass();
            if (type.equals(Integer.class)) {
                validator = new IntegerValidator(messages.getMessage("validation.invalidNumber"));

            } else if (type.equals(Long.class)) {
                validator = new LongValidator(messages.getMessage("validation.invalidNumber"));

            } else if (type.equals(Double.class) || type.equals(BigDecimal.class)) {
                validator = new DoubleValidator(messages.getMessage("validation.invalidNumber"));

            } else if (type.equals(java.sql.Date.class)) {
                validator = new DateValidator(messages.getMessage("validation.invalidDate"));
            }
        }
        return validator;
    }
}
