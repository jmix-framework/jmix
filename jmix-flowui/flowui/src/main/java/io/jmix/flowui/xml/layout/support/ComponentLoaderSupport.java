/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.xml.layout.support;

import com.google.common.base.Strings;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.ThemableLayout;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.core.impl.DatatypeRegistryImpl;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.SupportsDatatype;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.formatter.FormatterLoadFactory;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.component.validation.ValidatorLoadFactory;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.kit.component.HasAutofocus;
import io.jmix.flowui.kit.component.HasPlaceholder;
import io.jmix.flowui.kit.component.HasTitle;
import io.jmix.flowui.kit.component.SupportsFormatter;
import io.jmix.flowui.kit.component.formatter.Formatter;
import io.jmix.flowui.xml.layout.ComponentLoader.Context;
import io.jmix.flowui.xml.layout.loader.PropertyShortcutLoader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Component("flowui_ComponentLoaderSupport")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ComponentLoaderSupport implements ApplicationContextAware {

    protected Context context;
    protected LoaderSupport loaderSupport;
    protected ApplicationContext applicationContext;
    protected Environment environment;
    protected DatatypeRegistryImpl datatypeRegistry;

    public ComponentLoaderSupport(Context context) {
        this.context = context;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Autowired
    public void setLoaderSupport(LoaderSupport loaderSupport) {
        this.loaderSupport = loaderSupport;
    }

    @Autowired
    public void setDatatypeRegistry(DatatypeRegistryImpl datatypeRegistry) {
        this.datatypeRegistry = datatypeRegistry;
    }

    @Nullable
    protected String loadResourceString(@Nullable String message) {
        return loaderSupport.loadResourceString(message, context.getMessageGroup());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void loadDatatype(SupportsDatatype<?> component, Element element) {
        String dataTypeString = loaderSupport.loadString(element, "datatype").orElse(null);

        if (dataTypeString == null) {
            return;
        }

        Datatype datatype = datatypeRegistry.find(dataTypeString);
        component.setDatatype(datatype);
    }

    public void loadSpacing(ThemableLayout layout, Element element) {
        loaderSupport.loadBoolean(element, "spacing", layout::setSpacing);
    }

    public void loadMargin(ThemableLayout layout, Element element) {
        loaderSupport.loadBoolean(element, "margin", layout::setMargin);
    }

    public void loadPadding(ThemableLayout layout, Element element) {
        loaderSupport.loadBoolean(element, "padding", layout::setPadding);
    }

    public void loadBoxSizing(ThemableLayout layout, Element element) {
        loaderSupport.loadEnum(element, BoxSizing.class, "boxSizing", layout::setBoxSizing);
    }

    public void loadThemableAttributes(ThemableLayout layout, Element element) {
        loadSpacing(layout, element);
        loadMargin(layout, element);
        loadPadding(layout, element);
        loadBoxSizing(layout, element);
    }

    public void loadAlignItems(FlexComponent component, Element element) {
        loaderSupport.loadEnum(element, FlexComponent.Alignment.class, "alignItems", component::setAlignItems);
    }

    public void loadJustifyContent(FlexComponent component, Element element) {
        loaderSupport.loadEnum(element, FlexComponent.JustifyContentMode.class, "justifyContent", component::setJustifyContentMode);
    }

    public void loadFlexibleAttributes(FlexComponent component, Element element) {
        loadAlignItems(component, element);
        loadJustifyContent(component, element);
        loadEnabled(component, element);
        loadClassName(component, element);
        loadSizeAttributes(component, element);
    }

    public void loadText(HasText component, Element element) {
        loaderSupport.loadResourceString(element, "text", context.getMessageGroup(), component::setText);
    }

    public void loadTitle(HasTitle component, Element element, Context context) {
        loaderSupport.loadResourceString(element, "title", context.getMessageGroup(), component::setTitle);
    }

    public void loadLabel(HasLabel component, Element element) {
        loaderSupport.loadResourceString(element, "label", context.getMessageGroup(), component::setLabel);
    }

    public void loadRequired(HasRequired resultComponent, Element element, Context context) {
        loaderSupport.loadResourceString(element,
                "requiredMessage",
                context.getMessageGroup(),
                resultComponent::setRequiredMessage
        );
        loaderSupport.loadBoolean(element, "required", resultComponent::setRequired);
    }

    public void loadValueChangeMode(HasValueChangeMode component, Element element) {
        loaderSupport.loadEnum(element, ValueChangeMode.class, "valueChangeMode", component::setValueChangeMode);
        loaderSupport.loadInteger(element, "valueChangeTimeout", component::setValueChangeTimeout);
    }

    public void loadThemeName(HasTheme component, Element element) {
        loaderSupport.loadString(element, "themeName")
                .ifPresent(themeString -> split(themeString, component::addThemeName));
    }

    public void loadClassName(HasStyle component, Element element) {
        loaderSupport.loadString(element, "className")
                .ifPresent(classNameString -> split(classNameString, component::addClassName));
    }

    public void loadBadge(HasText component, Element element) {
        loaderSupport.loadString(element, "themeName")
                .ifPresent(badgeString -> {
                    component.getElement().getThemeList().add("badge");
                    split(badgeString, component.getElement().getThemeList()::add);
                });
    }

    public void loadValueAndElementAttributes(HasValueAndElement<?, ?> component, Element element) {
        loaderSupport.loadBoolean(element, "readOnly", component::setReadOnly);
        loaderSupport.loadBoolean(element, "requiredIndicatorVisible", component::setRequiredIndicatorVisible);
    }

    public void loadValidationAttributes(HasValidation component, Element element, Context context) {
        loaderSupport.loadBoolean(element, "invalid", component::setInvalid);
        loaderSupport.loadResourceString(element, "errorMessage", context.getMessageGroup(),
                component::setErrorMessage);

        if (component instanceof SupportsValidation<?>) {
            loadValidation((SupportsValidation<?>) component, element);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void loadFormatter(SupportsFormatter component, Element element) {
        loadFormatter(element)
                .ifPresent(component::setFormatter);
    }

    public void loadHelperText(HasHelper component, Element element) {
        loaderSupport.loadResourceString(element, "helperText", context.getMessageGroup(), component::setHelperText);
    }

    public void loadPlaceholder(HasPlaceholder component, Element element) {
        loaderSupport.loadResourceString(element, "placeholder", context.getMessageGroup(), component::setPlaceholder);
    }

    public void loadAutofocus(HasAutofocus component, Element element) {
        loaderSupport.loadBoolean(element, "autofocus", component::setAutofocus);
    }

    public void loadAutocomplete(HasAutocomplete component, Element element) {
        loaderSupport.loadEnum(element, Autocomplete.class, "autocomplete", component::setAutocomplete);
    }

    public void loadAutocapitalize(HasAutocapitalize component, Element element) {
        loaderSupport.loadEnum(element, Autocapitalize.class, "autocapitalize", component::setAutocapitalize);
    }

    public void loadAutocorrect(HasAutocorrect component, Element element) {
        loaderSupport.loadBoolean(element, "autocorrect", component::setAutocorrect);
    }

    public void loadEnabled(HasEnabled component, Element element) {
        loaderSupport.loadBoolean(element, "enabled", component::setEnabled);
    }

    public void loadAriaLabel(HasAriaLabel component, Element element) {
        loaderSupport.loadResourceString(element, "ariaLabel", context.getMessageGroup(), component::setAriaLabel);
    }

    public void loadWhiteSpace(HasText component, Element element) {
        loaderSupport.loadEnum(element, HasText.WhiteSpace.class, "whiteSpace", component::setWhiteSpace);
    }

    public void loadWidth(HasSize component, Element element) {
        loaderSupport.loadString(element, "width")
                .ifPresent(component::setWidth);
    }

    public void loadMaxWidth(HasSize component, Element element) {
        loaderSupport.loadString(element, "maxWidth")
                .ifPresent(component::setMaxWidth);
    }

    public void loadMinWidth(HasSize component, Element element) {
        loaderSupport.loadString(element, "minWidth")
                .ifPresent(component::setMinWidth);
    }

    public void loadHeight(HasSize component, Element element) {
        loaderSupport.loadString(element, "height")
                .ifPresent(component::setHeight);
    }

    public void loadMaxHeight(HasSize component, Element element) {
        loaderSupport.loadString(element, "maxHeight")
                .ifPresent(component::setMaxHeight);
    }

    public void loadMinHeight(HasSize component, Element element) {
        loaderSupport.loadString(element, "minHeight")
                .ifPresent(component::setMinHeight);
    }

    public void loadSizeAttributes(HasSize component, Element element) {
        loadWidth(component, element);
        loadMaxWidth(component, element);
        loadMinWidth(component, element);
        loadHeight(component, element);
        loadMaxHeight(component, element);
        loadMinHeight(component, element);
    }

    public Optional<VaadinIcon> loadIcon(com.vaadin.flow.component.Component component, Element element) {
        return loaderSupport.loadEnum(element, VaadinIcon.class, "icon");
    }

    public Optional<String> loadShortcut(Element element) {
        return loaderSupport.loadString(element, "shortcut")
                .map(shortcut -> {
                    if (shortcut.startsWith("${") && shortcut.endsWith("}")) {
                        String fqnShortcut = loadShortcutFromFQNConfig(shortcut);
                        if (fqnShortcut != null) {
                            return fqnShortcut;
                        }

                        String configShortcut = loadShortcutFromConfig(shortcut);
                        if (configShortcut != null) {
                            return configShortcut;
                        }

                        String aliasShortcut = loadShortcutFromAlias(shortcut);
                        if (aliasShortcut != null) {
                            return aliasShortcut;
                        }
                    }

                    return shortcut;
                });
    }

    @Nullable
    protected String loadShortcutFromFQNConfig(String shortcut) {
        if (shortcut.contains("#")) {
            String[] splittedShortcut = shortcut.split("#");
            if (splittedShortcut.length != 2) {
                String message = "An error occurred while loading shortcut: incorrect format of shortcut.";
                throw new GuiDevelopmentException(message, context);
            }

            String classFqn = splittedShortcut[0].substring(2);
            String methodName = splittedShortcut[1].substring(0, splittedShortcut[1].length() - 1);

            //noinspection rawtypes
            Class beanClass;
            try {
                beanClass = ReflectionHelper.loadClass(classFqn);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }

            //noinspection unchecked
            Object bean = applicationContext.getBean(beanClass);

            try {
                String shortcutValue = (String) MethodUtils.invokeMethod(bean, methodName);
                if (StringUtils.isNotEmpty(shortcutValue)) {
                    return shortcutValue;
                }
            } catch (NoSuchMethodException e) {
                String message = String.format("An error occurred while loading shortcut: " +
                        "can't find method \"%s\" in \"%s\"", methodName, classFqn);
                throw new GuiDevelopmentException(message, context);
            } catch (IllegalAccessException | InvocationTargetException e) {
                String message = String.format("An error occurred while loading shortcut: " +
                        "can't invoke method \"%s\" in \"%s\"", methodName, classFqn);
                throw new GuiDevelopmentException(message, context);
            }
        }
        return null;
    }

    @Nullable
    protected String loadShortcutFromConfig(String shortcut) {
        if (shortcut.contains(".")) {
            String shortcutPropertyKey = shortcut.substring(2, shortcut.length() - 1);
            String shortcutValue = environment.getProperty(shortcutPropertyKey);
            if (StringUtils.isNotEmpty(shortcutValue)) {
                return shortcutValue;
            } else {
                String message = String.format("Component shortcut property \"%s\" doesn't exist", shortcutPropertyKey);
                throw new GuiDevelopmentException(message, context);
            }
        }
        return null;
    }

    @Nullable
    protected String loadShortcutFromAlias(String shortcut) {
        if (shortcut.endsWith("_SHORTCUT}")) {
            PropertyShortcutLoader propertyShortcutLoader = applicationContext.getBean(PropertyShortcutLoader.class);

            String alias = shortcut.substring(2, shortcut.length() - 1);
            if (propertyShortcutLoader.contains(alias)) {
                return propertyShortcutLoader.getShortcut(alias);
            } else {
                String message = String.format("An error occurred while loading shortcut. " +
                        "Can't find shortcut for alias \"%s\"", alias);
                throw new GuiDevelopmentException(message, context);
            }
        }
        return null;
    }

    protected void split(String names, Consumer<String> setter) {
        for (String split : names.split(",")) {
            String trimmed = split.trim();

            if (!Strings.isNullOrEmpty(trimmed)) {
                setter.accept(trimmed);
            }
        }
    }

    protected Optional<Formatter<?>> loadFormatter(Element element) {
        Element formatterElement = element.element("formatter");
        if (formatterElement == null) {
            return Optional.empty();
        }

        int size = formatterElement.elements().size();
        if (size != 1) {
            throw new GuiDevelopmentException("Only one formatter needs to be defined. " +
                    "The current number of formatters is " + size, context);
        }

        Element childElement = formatterElement.elements().get(0);
        FormatterLoadFactory loadFactory = applicationContext.getBean(FormatterLoadFactory.class, context);
        if (loadFactory.isFormatter(childElement)) {
            return Optional.ofNullable(loadFactory.createFormatter(childElement));
        }

        return Optional.empty();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void loadValidation(SupportsValidation<?> component, Element element) {
        Element validatorsHolder = element.element("validators");
        if (validatorsHolder != null) {
            List<Element> validators = validatorsHolder.elements();

            ValidatorLoadFactory loadFactory = applicationContext.getBean(ValidatorLoadFactory.class);

            for (Element validatorElem : validators) {
                Validator validator = loadFactory.createValidator(validatorElem, context.getMessageGroup());
                if (validator != null) {
                    component.addValidator(validator);
                }
            }
        }
    }
}
