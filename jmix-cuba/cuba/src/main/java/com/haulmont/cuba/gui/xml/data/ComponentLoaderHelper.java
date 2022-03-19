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
import com.haulmont.cuba.gui.WindowManager.OpenType;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.ListActionType;
import com.haulmont.cuba.gui.components.validators.*;
import com.haulmont.cuba.gui.screen.compatibility.LegacyFrame;
import com.haulmont.cuba.gui.xml.DeclarativeAction;
import com.haulmont.cuba.gui.xml.DeclarativeTrackingAction;
import io.jmix.core.ClassManager;
import io.jmix.core.Messages;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.security.EntityOp;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.component.ActionsHolder;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.formatter.Formatter;
import io.jmix.ui.xml.layout.ComponentLoader;
import com.haulmont.cuba.gui.xml.layout.loaders.LoadPresentationsPostInitTask;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
                && ((ComponentLoader.ComponentContext) context).getFrame().getFrameOwner() instanceof LegacyFrame;
    }

    public static void loadSettingsEnabled(HasSettings component, Element element) {
        String settingsEnabled = element.attributeValue("settingsEnabled");
        if (StringUtils.isNotEmpty(settingsEnabled)) {
            component.setSettingsEnabled(Boolean.parseBoolean(settingsEnabled));
        }
    }

    @Nullable
    public static DataGrid.Renderer loadLegacyRenderer(Element rendererElement, ComponentLoader.Context context,
                                                       ClassManager classManager, ApplicationContext applicationContext) {
        String rendererType = rendererElement.attributeValue("type");
        if (StringUtils.isEmpty(rendererType)) {
            return null;
        }

        Class<?> rendererClass = classManager.loadClass(rendererType);

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

        return (DataGrid.Renderer) applicationContext.getBean(rendererClass);
    }

    @SuppressWarnings("unchecked")
    public static void loadValidators(Field component, Element element, ComponentLoader.Context context,
                                      ClassManager classManager, Messages messages) {
        List<Element> validatorElements = element.elements("validator");

        if (!validatorElements.isEmpty()) {
            for (Element validatorElement : validatorElements) {
                Consumer<?> validator = loadValidator(validatorElement, context, classManager);
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
    public static Consumer<?> loadValidator(Element validatorElement, ComponentLoader.Context context, ClassManager classManager) {
        String className = validatorElement.attributeValue("class");
        String scriptPath = validatorElement.attributeValue("script");
        String script = validatorElement.getText();

        Consumer<?> validator = null;

        if (StringUtils.isNotBlank(scriptPath) || StringUtils.isNotBlank(script)) {
            validator = new ScriptValidator(validatorElement, context.getMessageGroup());
        } else {
            Class aClass = classManager.findClass(className);
            if (aClass == null)
                throw new GuiDevelopmentException(String.format("Class %s is not found", className), context);
            if (!StringUtils.isBlank(context.getMessageGroup()))
                try {
                    validator = (Consumer<?>) ReflectionHelper.newInstance(aClass, validatorElement, context.getMessageGroup());
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

    @Nullable
    public static Formatter<?> loadFormatter(Element element, ClassManager classManager, ComponentLoader.Context context) {
        Element formatterElement = element.element("formatter");
        if (formatterElement != null) {
            String className = formatterElement.attributeValue("class");

            if (StringUtils.isEmpty(className)) {
                throw new GuiDevelopmentException("Formatter's attribute 'class' is not specified", context);
            }

            Class<?> aClass = classManager.findClass(className);
            if (aClass == null) {
                throw new GuiDevelopmentException(String.format("Class %s is not found", className), context);
            }

            try {
                Constructor<?> constructor = aClass.getConstructor(Element.class);
                try {
                    return (Formatter<?>) constructor.newInstance(formatterElement);
                } catch (Throwable e) {
                    throw new GuiDevelopmentException(
                            String.format("Unable to instantiate class %s: %s", className, e), context);
                }
            } catch (NoSuchMethodException e) {
                try {
                    return (Formatter<?>) aClass.getDeclaredConstructor().newInstance();
                } catch (Exception e1) {
                    throw new GuiDevelopmentException(
                            String.format("Unable to instantiate class %s: %s", className, e1), context);
                }
            }
        } else {
            return null;
        }
    }

    public static Optional<Action> loadInvokeAction(ComponentLoader.Context context,
                                                    ActionsHolder actionsHolder,
                                                    Element element,
                                                    String id,
                                                    String caption,
                                                    String description,
                                                    String iconPath,
                                                    @Nullable String shortcut) {
        String invokeMethod = element.attributeValue("invoke");
        if (StringUtils.isEmpty(invokeMethod) || !isLegacyFrame(context)) {
            return Optional.empty();
        }

        String trackSelection = element.attributeValue("trackSelection");
        boolean shouldTrackSelection = Boolean.parseBoolean(trackSelection);

        BaseAction action;
        if (shouldTrackSelection) {
            action = new DeclarativeTrackingAction(
                    id,
                    caption,
                    description,
                    iconPath,
                    element.attributeValue("enable"),
                    element.attributeValue("visible"),
                    invokeMethod,
                    shortcut,
                    actionsHolder
            );

            loadActionConstraint(action, element);

            return Optional.of(action);
        } else {
            action = new DeclarativeAction(
                    id,
                    caption,
                    description,
                    iconPath,
                    element.attributeValue("enable"),
                    element.attributeValue("visible"),
                    invokeMethod,
                    shortcut,
                    actionsHolder
            );
        }

        action.setPrimary(Boolean.parseBoolean(element.attributeValue("primary")));

        return Optional.of(action);
    }

    public static Optional<Action> loadLegacyListAction(ComponentLoader.Context context,
                                                        ActionsHolder actionsHolder,
                                                        Element element,
                                                        @Nullable String caption,
                                                        @Nullable String description,
                                                        @Nullable String iconPath,
                                                        @Nullable String shortcut) {
        Boolean isInvokeMethod = Boolean.parseBoolean(element.attributeValue("invoke"));
        if (isLegacyFrame(context) && !isInvokeMethod) {
            String id = element.attributeValue("id");
            // Try to create a standard list action
            for (ListActionType type : ListActionType.values()) {
                if (type.getId().equals(id)) {
                    Action instance = type.createAction((ListComponent) actionsHolder);

                    String enable = element.attributeValue("enable");
                    if (StringUtils.isNotEmpty(enable)) {
                        instance.setEnabled(Boolean.parseBoolean(enable));
                    }

                    String visible = element.attributeValue("visible");
                    if (StringUtils.isNotEmpty(visible)) {
                        instance.setVisible(Boolean.parseBoolean(visible));
                    }

                    if (StringUtils.isNotBlank(caption)) {
                        instance.setCaption(caption);
                    }

                    if (StringUtils.isNotBlank(description)) {
                        instance.setDescription(description);
                    }

                    if (StringUtils.isNotBlank(iconPath)) {
                        instance.setIcon(iconPath);
                    }

                    if (StringUtils.isNotBlank(shortcut)) {
                        instance.setShortcut(shortcut);
                    }

                    loadActionOpenType(instance, element, context);

                    loadActionConstraint(instance, element);

                    return Optional.of(instance);
                }
            }
        }
        return Optional.empty();
    }

    public static void loadActionOpenType(Action action, Element element, ComponentLoader.Context context) {
        if (action instanceof HasOpenType) {
            String openTypeString = element.attributeValue("openType");
            if (StringUtils.isNotEmpty(openTypeString)) {
                OpenType openType;
                try {
                    openType = OpenType.valueOf(openTypeString);
                } catch (IllegalArgumentException e) {
                    throw new GuiDevelopmentException(
                            String.format("Unknown open type: '%s' for action: '%s'", openTypeString, action.getId()),
                            context);
                }

                ((HasOpenType) action).setOpenType(openType);
            }
        }
    }

    /*
     * Caution! Copied from io.jmix.ui.xml.layout.loader.AbstractComponentLoader
     */
    public static void loadActionConstraint(Action action, Element element) {
        if (action instanceof Action.HasSecurityConstraint) {
            Action.HasSecurityConstraint itemTrackingAction = (Action.HasSecurityConstraint) action;

            Attribute operationTypeAttribute = element.attribute("constraintOperationType");
            if (operationTypeAttribute != null) {
                EntityOp operationType = EntityOp.fromId(operationTypeAttribute.getValue());
                itemTrackingAction.setConstraintEntityOp(operationType);
            }
        }
    }

    public static void loadCaptionProperty(HasCaptionMode component, Element element) {
        String captionProperty = element.attributeValue("captionProperty");
        if (!StringUtils.isEmpty(captionProperty)) {
            component.setCaptionProperty(captionProperty);
            component.setCaptionMode(CaptionMode.PROPERTY);
        }
    }

    public static void loadRowsCount(RowsCount.RowsCountTarget listComponent, Element element,
                                     Supplier<RowsCount> rowsCountCreator) {
        Element rowsCountElement = element.element("rowsCount");
        if (rowsCountElement != null) {
            RowsCount rowsCount = rowsCountCreator.get();

            String autoLoad = rowsCountElement.attributeValue("autoLoad");
            if (StringUtils.isNotEmpty(autoLoad)) {
                rowsCount.setAutoLoad(Boolean.parseBoolean(autoLoad));
            }

            rowsCount.setRowsCountTarget(listComponent);
            ((HasRowsCount) listComponent).setRowsCount(rowsCount);
        }
    }

    public static void loadTableColumnType(io.jmix.ui.component.Table.Column column,
                                           Element element,
                                           ApplicationContext applicationContext) {
        if (!(column instanceof Table.Column)) {
            return;
        }

        if (column.getMetaPropertyPath() != null) {
            ((Table.Column<?>) column).setType(column.getMetaPropertyPath().getRangeJavaClass());
        }

        String type = element.attributeValue("type");
        if (StringUtils.isNotEmpty(type)) {
            DatatypeRegistry datatypeRegistry = applicationContext.getBean(DatatypeRegistry.class);
            Datatype datatype = datatypeRegistry.get(type);
            ((Table.Column<?>) column).setType(datatype.getJavaClass());
        }
    }

    public static void loadPresentations(HasPresentations component,
                                     Element element,
                                     ApplicationContext applicationContext,
                                     ComponentLoader.ComponentContext context) {
        String presentations = element.attributeValue("presentations");
        if (StringUtils.isNotEmpty(presentations)) {
            if (applicationContext.containsBean("ui_Presentations")) {
                component.usePresentations(Boolean.parseBoolean(presentations));
                context.addPostInitTask(new LoadPresentationsPostInitTask(component));
            }
        }
    }
}
