/*
 * Copyright (c) 2008-2022 Haulmont.
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

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.xml.layout.ComponentLoader.Context;
import io.jmix.flowui.xml.layout.loader.PropertyShortcutLoader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

@Component("flowui_ComponentLoaderSupport")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ComponentLoaderSupport {

    protected Context context;
    protected LoaderSupport loaderSupport;
    protected ApplicationContext applicationContext;
    protected Environment environment;

    public ComponentLoaderSupport() {
    }

    public ComponentLoaderSupport(Context context) {
        this.context = context;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Autowired
    public void setLoaderSupport(LoaderSupport loaderSupport) {
        this.loaderSupport = loaderSupport;
    }

    protected String loadResourceString(String message) {
        return loaderSupport.loadResourceString(message, context.getMessageGroup());
    }

    public void loadText(HasText component, Element element) {
        loaderSupport.loadString(element, "text", component::setText);
    }

    public void loadRequiredMessage(HasRequired resultComponent, Context context) {
        loaderSupport.loadResourceString("requiredMessage", context.getMessageGroup(), resultComponent::setRequiredMessage);
    }

    public void loadThemeName(HasTheme component, Element element) {
        loaderSupport.loadString(element, "themeName")
                .ifPresent(themesString -> {
                    String[] themes = themesString.split(",");
                    component.addThemeNames(themes);
                });
    }

    public void loadValueChangeMode(HasValueChangeMode component, Element element) {
        loaderSupport.loadEnum(element, ValueChangeMode.class, "valueChangeMode", component::setValueChangeMode);
        loaderSupport.loadInteger(element, "valueChangeTimeout", component::setValueChangeTimeout);
    }

    public void loadClassName(HasStyle component, Element element) {
        loaderSupport.loadString(element, "className")
                .ifPresent(styleNameString -> {
                    String[] classNames = styleNameString.split(",");
                    component.addClassNames(classNames);
                });
    }

    public void loadBadge(HasText component, Element element) {
        loaderSupport.loadString(element, "themeName")
                .ifPresent(badgeString -> {
                    String[] badgeStyles = badgeString.split(",");
                    component.getElement().getThemeList().add("badge");
                    component.getElement().getThemeList().addAll(List.of(badgeStyles));
                });
    }

    //TODO: kremnevda, components can use ReadOnly but not Required 13.04.2022
    public void loadValueAndElementAttributes(HasValueAndElement<?, ?> component, Element element) {
        loaderSupport.loadBoolean(element, "readOnly", component::setReadOnly);
        loaderSupport.loadBoolean(element, "requiredIndicatorVisible", component::setRequiredIndicatorVisible);
    }

    public void loadHelperText(HasHelper component, Element element) {
        loaderSupport.loadString(element, "helperText", component::setHelperText);
    }

    public void loadAutoComplete(HasAutocomplete component, Element element) {
        loaderSupport.loadEnum(element, Autocomplete.class, "autoComplete", component::setAutocomplete);
    }

    public void loadAutoCapitalize(HasAutocapitalize component, Element element) {
        loaderSupport.loadEnum(element, Autocapitalize.class, "autoCapitalize", component::setAutocapitalize);
    }

    public void loadAutoCorrect(HasAutocorrect resultComponent, Element element) {
        loaderSupport.loadBoolean(element, "autoCorrect", resultComponent::setAutocorrect);
    }

    public void loadEnabled(HasEnabled component, Element element) {
        loaderSupport.loadBoolean(element, "enable", component::setEnabled);
    }

    public void loadAriaLabel(HasAriaLabel component, Element element) {
        loaderSupport.loadString(element, "ariaLabel", component::setAriaLabel);
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
}
