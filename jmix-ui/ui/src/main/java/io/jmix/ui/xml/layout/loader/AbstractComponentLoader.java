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

import com.google.common.base.Strings;
import io.jmix.core.ClassManager;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.EntityOp;
import io.jmix.ui.Actions;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.UiComponents;
import io.jmix.ui.UiProperties;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.action.ItemTrackingAction;
import io.jmix.ui.component.*;
import io.jmix.ui.component.Component.Alignment;
import io.jmix.ui.component.data.HasValueSource;
import io.jmix.ui.component.data.value.ContainerValueSource;
import io.jmix.ui.component.formatter.Formatter;
import io.jmix.ui.component.formatter.FormatterLoadFactory;
import io.jmix.ui.component.validation.Validator;
import io.jmix.ui.component.validation.ValidatorLoadFactory;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.model.ScreenData;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.theme.ThemeConstants;
import io.jmix.ui.theme.ThemeConstantsManager;
import io.jmix.ui.xml.PropertyShortcutLoader;
import io.jmix.ui.xml.layout.ComponentLoader;
import io.jmix.ui.xml.layout.LayoutLoaderConfig;
import io.jmix.ui.xml.layout.LoaderResolver;
import io.jmix.ui.xml.layout.LoaderSupport;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkState;
import static io.jmix.ui.icon.Icons.ICON_NAME_REGEX;
import static org.apache.commons.lang3.StringUtils.trimToNull;

public abstract class AbstractComponentLoader<T extends Component> implements ComponentLoader<T> {

    protected Context context;

    protected UiComponents factory;
    @Deprecated
    protected LayoutLoaderConfig layoutLoaderConfig;
    protected LoaderResolver loaderResolver;
    protected LoaderSupport loaderSupport;

    protected Element element;

    protected T resultComponent;

    protected ApplicationContext applicationContext;
    protected Environment environment;

    protected AbstractComponentLoader() {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    protected ComponentContext getComponentContext() {
        checkState(context instanceof ComponentContext,
                "'context' must implement io.jmix.ui.xml.layout.ComponentLoader.ComponentContext");

        return (ComponentContext) context;
    }

    protected CompositeComponentContext getCompositeComponentContext() {
        checkState(context instanceof CompositeComponentContext,
                "'context' must implement io.jmix.ui.xml.layout.ComponentLoader.CompositeComponentContext");

        return (CompositeComponentContext) context;
    }

    @Override
    public UiComponents getFactory() {
        return factory;
    }

    @Override
    public void setFactory(UiComponents factory) {
        this.factory = factory;
    }

    @Override
    public void setElement(Element element) {
        this.element = element;
    }

    @Override
    public Element getElement(Element element) {
        return element;
    }

    @Override
    public T getResultComponent() {
        return resultComponent;
    }

    @Override
    public LoaderResolver getLoaderResolver() {
        return loaderResolver;
    }

    @Override
    public void setLoaderResolver(LoaderResolver loaderResolver) {
        this.loaderResolver = loaderResolver;
    }

    @Override
    public LoaderSupport getLoaderSupport() {
        return loaderSupport;
    }

    @Override
    public void setLoaderSupport(LoaderSupport loaderSupport) {
        this.loaderSupport = loaderSupport;
    }

    @Deprecated
    @Override
    public LayoutLoaderConfig getLayoutLoaderConfig() {
        return layoutLoaderConfig;
    }

    @Deprecated
    @Override
    public void setLayoutLoaderConfig(LayoutLoaderConfig layoutLoaderConfig) {
        this.layoutLoaderConfig = layoutLoaderConfig;
    }

    protected Messages getMessages() {
        return applicationContext.getBean(Messages.class);
    }

    protected Actions getActions() {
        return applicationContext.getBean(Actions.class);
    }

    protected MessageTools getMessageTools() {
        return applicationContext.getBean(MessageTools.class);
    }

    protected ClassManager getClassManager() {
        return applicationContext.getBean(ClassManager.class);
    }

    protected UiProperties getProperties() {
        return applicationContext.getBean(UiProperties.class);
    }

    protected MeterRegistry getMeterRegistry() {
        return applicationContext.getBean(MeterRegistry.class);
    }

    protected ThemeConstants getTheme() {
        ThemeConstantsManager manager = applicationContext.getBean(ThemeConstantsManager.class);
        return manager.getConstants();
    }

    protected LayoutLoader getLayoutLoader() {
        return applicationContext.getBean(LayoutLoader.class, context);
    }

    protected LayoutLoader getLayoutLoader(Context context) {
        return applicationContext.getBean(LayoutLoader.class, context);
    }

    protected void loadId(Component component, Element element) {
        String id = element.attributeValue("id");
        component.setId(id);
    }

    protected void loadStyleName(Component component, Element element) {
        if (element.attribute("stylename") != null) {
            component.setStyleName(element.attributeValue("stylename"));
        }
    }

    protected void loadCss(Component component, Element element) {
        String css = element.attributeValue("css");
        if (StringUtils.isNotEmpty(css)) {
            HtmlAttributes htmlAttributes = (HtmlAttributes) applicationContext.getBean(HtmlAttributes.NAME);
            htmlAttributes.applyCss(component, css);
        }
    }

    protected void loadResponsive(Component component, Element element) {
        String responsive = element.attributeValue("responsive");
        if (StringUtils.isNotEmpty(responsive)) {
            component.setResponsive(Boolean.parseBoolean(responsive));
        }
    }

    protected void assignXmlDescriptor(Component component, Element element) {
        if (component instanceof Component.HasXmlDescriptor) {
            ((Component.HasXmlDescriptor) component).setXmlDescriptor(element);
        }
    }

    protected void loadEditable(Component component, Element element) {
        if (component instanceof Component.Editable) {
            String editable = element.attributeValue("editable");
            if (!StringUtils.isEmpty(editable)) {
                ((Component.Editable) component).setEditable(Boolean.parseBoolean(editable));
            }
        }
    }

    protected void loadCaption(Component.HasCaption component, Element element) {
        if (element.attribute("caption") != null) {
            String caption = element.attributeValue("caption");

            caption = loadResourceString(caption);
            component.setCaption(caption);

            if (component instanceof HasHtmlCaption) {
                loadCaptionAsHtml((HasHtmlCaption) component, element);
            }
        }
    }

    protected void loadCaptionAsHtml(HasHtmlCaption component, Element element) {
        String captionAsHtml = element.attributeValue("captionAsHtml");
        if (!Strings.isNullOrEmpty(captionAsHtml)) {
            component.setCaptionAsHtml(Boolean.parseBoolean(captionAsHtml));
        }
    }

    protected void loadDescription(Component.HasDescription component, Element element) {
        if (element.attribute("description") != null) {
            String description = element.attributeValue("description");

            description = loadResourceString(description);
            component.setDescription(description);

            if (component instanceof HasHtmlDescription) {
                loadDescriptionAsHtml((HasHtmlDescription) component, element);
            }
        }
    }

    protected void loadDescriptionAsHtml(HasHtmlDescription component, Element element) {
        String descriptionAsHtml = element.attributeValue("descriptionAsHtml");
        if (!Strings.isNullOrEmpty(descriptionAsHtml)) {
            component.setDescriptionAsHtml(Boolean.parseBoolean(descriptionAsHtml));
        }
    }

    protected void loadContextHelp(HasContextHelp component, Element element) {
        String contextHelpText = element.attributeValue("contextHelpText");
        if (StringUtils.isNotEmpty(contextHelpText)) {
            contextHelpText = loadResourceString(contextHelpText);
            component.setContextHelpText(contextHelpText);
        }

        String htmlEnabled = element.attributeValue("contextHelpTextHtmlEnabled");
        if (StringUtils.isNotEmpty(htmlEnabled)) {
            component.setContextHelpTextHtmlEnabled(Boolean.parseBoolean(htmlEnabled));
        }
    }

    protected void loadRequired(Requirable component, Element element) {
        String required = element.attributeValue("required");
        if (StringUtils.isNotEmpty(required)) {
            component.setRequired(Boolean.parseBoolean(required));
        }

        String requiredMessage = element.attributeValue("requiredMessage");
        if (requiredMessage != null) {
            component.setRequiredMessage(loadResourceString(requiredMessage));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void loadValidation(HasValidator component, Element element) {
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

    protected void loadVisible(Component component, Element element) {
        String visible = element.attributeValue("visible");
        if (StringUtils.isNotEmpty(visible)) {
            boolean visibleValue = Boolean.parseBoolean(visible);
            component.setVisible(visibleValue);
        }
    }

    protected void loadEnable(Component component, Element element) {
        String enable = element.attributeValue("enable");
        if (StringUtils.isNotEmpty(enable)) {
            boolean enabled = Boolean.parseBoolean(enable);
            component.setEnabled(enabled);
        }
    }

    protected String loadResourceString(String caption) {
        if (StringUtils.isEmpty(caption)) {
            return caption;
        }

        return getMessageTools().loadString(context.getMessageGroup(), caption);
    }

    @Nullable
    protected String loadThemeString(@Nullable String value) {
        if (value != null && value.startsWith(ThemeConstants.PREFIX)) {
            value = getTheme().get(value.substring(ThemeConstants.PREFIX.length()));
        }
        return value;
    }

    protected int loadThemeInt(@Nullable String value) {
        if (value != null && value.startsWith(ThemeConstants.PREFIX)) {
            value = getTheme().get(value.substring(ThemeConstants.PREFIX.length()));
        }
        return value == null ? 0 : Integer.parseInt(value);
    }

    protected void loadAlign(Component component, Element element) {
        String align = element.attributeValue("align");
        if (!StringUtils.isBlank(align)) {
            component.setAlignment(Alignment.valueOf(align));
        }
    }

    protected void loadHeight(Component component, Element element) {
        String height = element.attributeValue("height");
        if (StringUtils.isNotEmpty(height)) {
            if ("auto".equalsIgnoreCase(height)) {
                component.setHeight(Component.AUTO_SIZE);
            } else {
                component.setHeight(loadThemeString(height));
            }
        }
    }

    protected void loadHeight(Component component, Element element, @Nullable String defaultValue) {
        String height = element.attributeValue("height");
        if ("auto".equalsIgnoreCase(height)) {
            component.setHeight(Component.AUTO_SIZE);
        } else if (!StringUtils.isBlank(height)) {
            component.setHeight(loadThemeString(height));
        } else if (!StringUtils.isBlank(defaultValue)) {
            component.setHeight(defaultValue);
        }
    }

    protected void loadWidth(Component component, Element element) {
        String width = element.attributeValue("width");
        if (StringUtils.isNotEmpty(width)) {
            if ("auto".equalsIgnoreCase(width)) {
                component.setWidth(Component.AUTO_SIZE);
            } else {
                component.setWidth(loadThemeString(width));
            }
        }
    }

    protected void loadTabIndex(Component.Focusable component, Element element) {
        String tabIndex = element.attributeValue("tabIndex");
        if (StringUtils.isNotEmpty(tabIndex)) {
            component.setTabIndex(Integer.parseInt(tabIndex));
        }
    }

    protected void loadWidth(Component component, Element element, @Nullable String defaultValue) {
        String width = element.attributeValue("width");
        if ("auto".equalsIgnoreCase(width)) {
            component.setWidth(Component.AUTO_SIZE);
        } else if (!StringUtils.isBlank(width)) {
            component.setWidth(loadThemeString(width));
        } else if (!StringUtils.isBlank(defaultValue)) {
            component.setWidth(defaultValue);
        }
    }

    protected void loadCollapsible(Collapsable component, Element element, boolean defaultCollapsable) {
        String collapsable = element.attributeValue("collapsable");
        boolean b = Strings.isNullOrEmpty(collapsable) ? defaultCollapsable : Boolean.parseBoolean(collapsable);
        component.setCollapsable(b);
        if (b) {
            String collapsed = element.attributeValue("collapsed");
            if (!StringUtils.isBlank(collapsed)) {
                component.setExpanded(!Boolean.parseBoolean(collapsed));
            }
        }
    }

    protected void loadBorder(HasBorder component, Element element) {
        String border = element.attributeValue("border");
        if (!StringUtils.isEmpty(border)) {
            if ("visible".equalsIgnoreCase(border)) {
                component.setBorderVisible(true);
            } else if ("hidden".equalsIgnoreCase(border)) {
                component.setBorderVisible(false);
            }
        }
    }

    protected void loadMargin(HasMargin layout, Element element) {
        String margin = element.attributeValue("margin");
        if (!StringUtils.isEmpty(margin)) {
            MarginInfo marginInfo = parseMarginInfo(margin);
            layout.setMargin(marginInfo);
        }
    }

    protected MarginInfo parseMarginInfo(String margin) {
        if (margin.contains(";") || margin.contains(",")) {
            String[] margins = margin.split("[;,]");
            if (margins.length != 4) {
                throw new GuiDevelopmentException(
                        "Margin attribute must contain 1 or 4 boolean values separated by ',' or ';", context);
            }

            return new MarginInfo(
                    Boolean.parseBoolean(StringUtils.trimToEmpty(margins[0])),
                    Boolean.parseBoolean(StringUtils.trimToEmpty(margins[1])),
                    Boolean.parseBoolean(StringUtils.trimToEmpty(margins[2])),
                    Boolean.parseBoolean(StringUtils.trimToEmpty(margins[3]))
            );
        } else {
            return new MarginInfo(Boolean.parseBoolean(margin));
        }
    }

    protected void assignFrame(Component.BelongToFrame component) {
        if (context instanceof ComponentContext
                && ((ComponentContext) context).getFrame() != null) {
            component.setFrame(((ComponentContext) context).getFrame());
        }
    }

    protected void loadAction(ActionOwner component, Element element) {
        String actionId = element.attributeValue("action");
        if (!StringUtils.isEmpty(actionId)) {
            ComponentContext componentContext = getComponentContext();
            componentContext.addPostInitTask(
                    new ActionOwnerAssignActionPostInitTask(component, actionId, componentContext.getFrame())
            );
        }
    }

    protected void loadIcon(Component.HasIcon component, Element element) {
        if (element.attribute("icon") != null) {
            String icon = element.attributeValue("icon");
            component.setIcon(getIconPath(icon));
        }
    }

    @Nullable
    protected String getIconPath(@Nullable String icon) {
        if (icon == null || icon.isEmpty()) {
            return null;
        }

        String iconPath = null;

        if (ICON_NAME_REGEX.matcher(icon).matches()) {
            Icons icons = applicationContext.getBean(Icons.class);
            iconPath = icons.get(icon);
        }

        if (StringUtils.isEmpty(iconPath)) {
            String themeValue = loadThemeString(icon);
            iconPath = loadResourceString(themeValue);
        }

        return iconPath;
    }

    protected void loadActions(ActionsHolder actionsHolder, Element element) {
        Element actionsEl = element.element("actions");
        if (actionsEl == null) {
            return;
        }

        for (Element actionEl : actionsEl.elements("action")) {
            actionsHolder.addAction(loadDeclarativeAction(actionsHolder, actionEl));
        }
    }

    protected Action loadDeclarativeAction(ActionsHolder actionsHolder, Element element) {
        return loadDeclarativeActionDefault(actionsHolder, element);
    }

    protected Action loadDeclarativeActionDefault(ActionsHolder actionsHolder, Element element) {
        String id = loadActionId(element);

        String trackSelection = element.attributeValue("trackSelection");
        boolean shouldTrackSelection = Boolean.parseBoolean(trackSelection);

        Action targetAction;

        if (shouldTrackSelection) {
            Actions actions = getActions();
            targetAction = actions.create(ItemTrackingAction.ID, id);
            loadActionConstraint(targetAction, element);
        } else {
            targetAction = new BaseAction(id);
        }

        initAction(element, targetAction);

        return targetAction;
    }

    protected String loadActionId(Element element) {
        String id = element.attributeValue("id");
        if (id == null) {
            Element component = element;
            for (int i = 0; i < 2; i++) {
                if (component.getParent() != null)
                    component = component.getParent();
                else
                    throw new GuiDevelopmentException("No action ID provided", context);
            }
            throw new GuiDevelopmentException("No action ID provided", context,
                    "Component ID", component.attributeValue("id"));
        }
        return id;
    }

    @Nullable
    protected Action loadDeclarativeActionByType(ActionsHolder actionsHolder, Element element) {
        String id = loadActionId(element);

        String actionTypeId = element.attributeValue("type");
        if (StringUtils.isNotEmpty(actionTypeId)) {
            Actions actions = applicationContext.getBean(Actions.class);
            Action instance = actions.create(actionTypeId, id);

            initAction(element, instance);
            loadActionConstraint(instance, element);

            return instance;
        }

        return null;
    }

    protected void initAction(Element element, Action targetAction) {
        String caption = element.attributeValue("caption");
        if (StringUtils.isNotEmpty(caption)) {
            targetAction.setCaption(loadResourceString(caption));
        }

        String description = element.attributeValue("description");
        if (StringUtils.isNotEmpty(description)) {
            targetAction.setDescription(loadResourceString(description));
        }

        String icon = element.attributeValue("icon");
        if (StringUtils.isNotEmpty(icon)) {
            targetAction.setIcon(getIconPath(icon));
        }

        String enable = element.attributeValue("enable");
        if (StringUtils.isNotEmpty(enable)) {
            targetAction.setEnabled(Boolean.parseBoolean(enable));
        }

        String visible = element.attributeValue("visible");
        if (StringUtils.isNotEmpty(visible)) {
            targetAction.setVisible(Boolean.parseBoolean(visible));
        }

        String shortcut = trimToNull(element.attributeValue("shortcut"));
        if (shortcut != null) {
            targetAction.setShortcut(loadShortcut(shortcut));
        }

        if (targetAction instanceof Action.HasPrimaryState) {
            String primary = element.attributeValue("primary");
            if (!Strings.isNullOrEmpty(primary)) {
                ((Action.HasPrimaryState) targetAction).setPrimary(Boolean.parseBoolean(primary));
            }
        }

        Element propertiesEl = element.element("properties");
        if (propertiesEl != null) {
            ActionCustomPropertyLoader propertyLoader = applicationContext.getBean(ActionCustomPropertyLoader.class);
            for (Element propertyEl : propertiesEl.elements("property")) {
                propertyLoader.load(targetAction,
                        propertyEl.attributeValue("name"), propertyEl.attributeValue("value"));
            }
        }
    }

    protected void loadActionConstraint(Action action, Element element) {
        if (action instanceof Action.HasSecurityConstraint) {
            Action.HasSecurityConstraint itemTrackingAction = (Action.HasSecurityConstraint) action;

            Attribute operationTypeAttribute = element.attribute("constraintEntityOp");
            if (operationTypeAttribute != null) {
                EntityOp operationType = EntityOp.fromId(operationTypeAttribute.getValue());
                itemTrackingAction.setConstraintEntityOp(operationType);
            }
        }
    }

    protected String loadShortcut(String shortcut) {
        if (StringUtils.isNotEmpty(shortcut) && shortcut.startsWith("${") && shortcut.endsWith("}")) {
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

    @Nullable
    protected String loadShortcutFromConfig(String shortcut) {
        if (shortcut.contains(".")) {
            String shortcutPropertyKey = shortcut.substring(2, shortcut.length() - 1);
            String shortcutValue = environment.getProperty(shortcutPropertyKey);
            if (StringUtils.isNotEmpty(shortcutValue)) {
                return shortcutValue;
            } else {
                String message = String.format("Action shortcut property \"%s\" doesn't exist", shortcutPropertyKey);
                throw new GuiDevelopmentException(message, context);
            }
        }
        return null;
    }

    protected Action loadValuePickerDeclarativeAction(ActionsHolder actionsHolder, Element element) {
        String type = element.attributeValue("type");
        if (StringUtils.isNotEmpty(type)) {
            Actions actions = getActions();

            String id = loadActionId(element);
            Action action = actions.create(type, id);
            initAction(element, action);

            return action;
        }

        return loadDeclarativeActionDefault(actionsHolder, element);
    }

    @Nullable
    protected Formatter<?> loadFormatter(Element element) {
        Element formatterElement = element.element("formatter");
        if (formatterElement == null) {
            return null;
        }

        int size = formatterElement.elements().size();
        if (size != 1) {
            throw new GuiDevelopmentException("Only one formatter needs to be defined. " +
                    "The current number of formatters is " + size, getContext(),
                    "Component ID", resultComponent.getId());
        }

        Element childElement = formatterElement.elements().get(0);
        FormatterLoadFactory loadFactory = applicationContext.getBean(FormatterLoadFactory.class);
        if (loadFactory.isFormatter(childElement)) {
            return loadFactory.createFormatter(childElement);
        }

        return null;
    }

    protected void loadFormatter(HasFormatter component, Element element) {
        Formatter formatter = loadFormatter(element);
        if (formatter != null) {
            component.setFormatter(formatter);
        }
    }

    protected void loadOrientation(HasOrientation component, Element element) {
        String orientation = element.attributeValue("orientation");
        if (orientation == null) {
            return;
        }

        if ("horizontal".equalsIgnoreCase(orientation)) {
            component.setOrientation(HasOrientation.Orientation.HORIZONTAL);
        } else if ("vertical".equalsIgnoreCase(orientation)) {
            component.setOrientation(HasOrientation.Orientation.VERTICAL);
        } else {
            throw new GuiDevelopmentException("Invalid orientation value: " + orientation, context,
                    "Component ID", ((Component) component).getId());
        }
    }

    protected void loadInputPrompt(HasInputPrompt component, Element element) {
        String inputPrompt = element.attributeValue("inputPrompt");
        if (StringUtils.isNotBlank(inputPrompt)) {
            component.setInputPrompt(loadResourceString(inputPrompt));
        }
    }

    protected void loadFocusable(Component.Focusable component, Element element) {
        String focusable = element.attributeValue("focusable");
        if (StringUtils.isNotBlank(focusable)) {
            component.setFocusable(Boolean.parseBoolean(focusable));
        }
    }

    protected void loadData(T component, Element element) {
        loadContainer(component, element);
    }

    @SuppressWarnings("unchecked")
    protected void loadContainer(T component, Element element) {
        if (component instanceof HasValueSource) {
            String property = element.attributeValue("property");
            loadContainer(element, property).ifPresent(container ->
                    ((HasValueSource) component).setValueSource(new ContainerValueSource<>(container, property)));
        }
    }

    protected Optional<InstanceContainer> loadContainer(Element element, @Nullable String property) {
        String containerId = element.attributeValue("dataContainer");

        // In case a component has only a property,
        // we try to obtain `dataContainer` from a parent element.
        // For instance, a component is placed within the Form component
        if (Strings.isNullOrEmpty(containerId) && property != null) {
            containerId = getParentDataContainer(element);
        }

        if (!Strings.isNullOrEmpty(containerId)) {
            if (property == null) {
                throw new GuiDevelopmentException(
                        String.format("Can't set container '%s' for component '%s' because 'property' " +
                                "attribute is not defined", containerId, element.attributeValue("id")), context);
            }

            FrameOwner frameOwner = getComponentContext().getFrame().getFrameOwner();
            ScreenData screenData = UiControllerUtils.getScreenData(frameOwner);

            return Optional.of(screenData.getContainer(containerId));
        }

        return Optional.empty();
    }

    protected Optional<CollectionContainer> loadOptionsContainer(Element element) {
        String containerId = element.attributeValue("optionsContainer");
        if (containerId != null) {
            FrameOwner frameOwner = getComponentContext().getFrame().getFrameOwner();
            ScreenData screenData = UiControllerUtils.getScreenData(frameOwner);
            InstanceContainer container = screenData.getContainer(containerId);
            if (!(container instanceof CollectionContainer)) {
                throw new GuiDevelopmentException("Not a CollectionContainer: " + containerId, context);
            }
            return Optional.of((CollectionContainer) container);
        }

        return Optional.empty();
    }

    @Nullable
    protected String getParentDataContainer(Element element) {
        Element parent = element.getParent();
        while (parent != null) {
            if (loaderResolver.getLoader(parent) != null) {
                return parent.attributeValue("dataContainer");
            }
            parent = parent.getParent();
        }
        return null;
    }

    @Nullable
    protected Component findComponent(String componentId) {
        if (context instanceof ComponentContext) {
            return getComponentContext().getFrame().getComponent(componentId);
        } else if (context instanceof CompositeComponentContext) {
            // We assume that CompositeComponent has only one root component
            Component current = resultComponent;
            while (current.getParent() != null) {
                current = current.getParent();
            }

            if (current instanceof HasComponents) {
                return ((HasComponents) current).getComponent(componentId);
            }
        }

        return null;
    }

    protected void loadRequiredIndicatorVisible(HasRequiredIndicator component, Element element) {
        String requiredIndicatorVisible = element.attributeValue("requiredIndicatorVisible");
        if (!Strings.isNullOrEmpty(requiredIndicatorVisible)) {
            component.setRequiredIndicatorVisible(Boolean.parseBoolean(requiredIndicatorVisible));
        }
    }

    protected void loadHtmlSanitizerEnabled(HasHtmlSanitizer component, Element element) {
        String htmlSanitizerEnabled = element.attributeValue("htmlSanitizerEnabled");
        if (StringUtils.isNotEmpty(htmlSanitizerEnabled)) {
            component.setHtmlSanitizerEnabled(Boolean.parseBoolean(htmlSanitizerEnabled));
        }
    }

    protected Optional<String> loadString(Element element, String attributeName) {
        return getLoaderSupport().loadString(element, attributeName);
    }

    protected void loadString(Element element, String attributeName, Consumer<String> setter) {
        loadString(element, attributeName)
                .ifPresent(setter);
    }

    protected Optional<Boolean> loadBoolean(Element element, String attributeName) {
        return getLoaderSupport().loadBoolean(element, attributeName);
    }

    protected void loadBoolean(Element element, String attributeName, Consumer<Boolean> setter) {
        loadBoolean(element, attributeName)
                .ifPresent(setter);
    }

    protected Optional<Integer> loadInteger(Element element, String attributeName) {
        return getLoaderSupport().loadInteger(element, attributeName);
    }

    protected void loadInteger(Element element, String attributeName, Consumer<Integer> setter) {
        loadInteger(element, attributeName)
                .ifPresent(setter);
    }

    protected <T extends Enum<T>> Optional<T> loadEnum(Element element, Class<T> type, String attributeName) {
        return getLoaderSupport().loadEnum(element, type, attributeName);
    }

    protected <T extends Enum<T>> void loadEnum(Element element, Class<T> type, String attributeName,
                                                Consumer<T> setter) {
        loadEnum(element, type, attributeName)
                .ifPresent(setter);
    }

    protected Optional<MetaClass> loadMetaClass(Element element) {
        return loadString(element, "metaClass")
                .map(metaClassStr -> applicationContext.getBean(Metadata.class).getClass(metaClassStr));
    }

    protected void loadMetaClass(Element element, Consumer<MetaClass> setter) {
        loadString(element, "metaClass", metaClassStr ->
                setter.accept(applicationContext.getBean(Metadata.class).getClass(metaClassStr)));
    }

    protected Optional<String> loadMinHeight(Element element) {
        return loadString(element, "minHeight");
    }

    protected void loadMinHeight(Element element, Consumer<String> setter) {
        loadMinHeight(element)
                .ifPresent(setter);
    }

    protected Optional<String> loadMinWidth(Element element) {
        return loadString(element, "minWidth");
    }

    protected void loadMinWidth(Element element, Consumer<String> setter) {
        loadMinWidth(element)
                .ifPresent(setter);
    }
}
