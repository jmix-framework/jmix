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

package io.jmix.flowui.xml.layout.loader;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasText;
import io.jmix.core.ClassManager;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.core.security.EntityOp;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.SecuredBaseAction;
import io.jmix.flowui.action.SecurityConstraintAction;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.data.value.ContainerValueSource;
import io.jmix.flowui.component.HasActions;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.ScreenData;
import io.jmix.flowui.screen.Screen;
import io.jmix.flowui.screen.UiControllerUtils;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.LoaderResolver;
import io.jmix.flowui.xml.layout.LoaderSupport;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.dom4j.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkState;

public abstract class AbstractComponentLoader<T extends Component> implements ComponentLoader<T> {

    protected Context context;

    protected UiComponents factory;
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
                "'context' must implement " + ComponentContext.class.getName());

        return (ComponentContext) context;
    }

    /*protected CompositeComponentContext getCompositeComponentContext() {
        checkState(context instanceof CompositeComponentContext,
                "'context' must implement io.jmix.ui.xml.layout.ComponentLoader.CompositeComponentContext");

        return (CompositeComponentContext) context;
    }*/

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

    /*protected UiProperties getProperties() {
        return applicationContext.getBean(UiProperties.class);
    }*/

    protected MeterRegistry getMeterRegistry() {
        return applicationContext.getBean(MeterRegistry.class);
    }

    protected LayoutLoader getLayoutLoader() {
        return applicationContext.getBean(LayoutLoader.class, context);
    }

    protected LayoutLoader getLayoutLoader(Context context) {
        return applicationContext.getBean(LayoutLoader.class, context);
    }

    protected void loadId(Component component, Element element) {
        loadString(element, "id", component::setId);
    }

    protected void loadHeight(HasSize component, Element element) {
        String height = element.attributeValue("height");
        if (StringUtils.isNotEmpty(height)) {
            if ("auto".equalsIgnoreCase(height)) {
                // TODO: gg, create a constant. Do we need it at all?
//                component.setHeight(Component.AUTO_SIZE);
                component.setHeight("-1px");
            } else {
                component.setHeight(height);
            }
        }
    }

    protected void loadHeight(HasSize component, Element element, @Nullable String defaultValue) {
        String height = element.attributeValue("height");
        if ("auto".equalsIgnoreCase(height)) {
            // TODO: gg, create a constant. Do we need it at all?
//            component.setHeight(Component.AUTO_SIZE);
            component.setHeight("-1px");
        } else if (!StringUtils.isBlank(height)) {
            component.setHeight(height);
        } else if (!StringUtils.isBlank(defaultValue)) {
            component.setHeight(defaultValue);
        }
    }

    protected void loadWidth(HasSize component, Element element) {
        String width = element.attributeValue("width");
        if (StringUtils.isNotEmpty(width)) {
            if ("auto".equalsIgnoreCase(width)) {
                // TODO: gg, create a constant. Do we need it at all?
//                component.setWidth(Component.AUTO_SIZE);
                component.setWidth("-1px");
            } else {
                component.setWidth(width);
            }
        }
    }

    protected void loadWidth(HasSize component, Element element, @Nullable String defaultValue) {
        String width = element.attributeValue("width");
        if ("auto".equalsIgnoreCase(width)) {
            // TODO: gg, create a constant. Do we need it at all?
//            component.setWidth(Component.AUTO_SIZE);
            component.setWidth("-1px");
        } else if (!StringUtils.isBlank(width)) {
            component.setWidth(width);
        } else if (!StringUtils.isBlank(defaultValue)) {
            component.setWidth(defaultValue);
        }
    }

    protected void loadText(HasText component, Element element) {
        loadString(element, "text", component::setText);
    }

    protected void loadData(T component, Element element) {
        loadContainer(component, element);
    }

    @SuppressWarnings("unchecked")
    protected void loadContainer(T component, Element element) {
        if (component instanceof SupportsValueSource) {
            String property = element.attributeValue("property");
            loadContainer(element, property).ifPresent(container ->
                    ((SupportsValueSource) component).setValueSource(new ContainerValueSource<>(container, property)));
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

            Screen screen = getComponentContext().getScreen();
            ScreenData screenData = UiControllerUtils.getScreenData(screen);

            return Optional.of(screenData.getContainer(containerId));
        }

        return Optional.empty();
    }

    /*protected Optional<CollectionContainer> loadOptionsContainer(Element element) {
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
    }*/

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

    /*@Nullable
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
    }*/

    protected void loadActions(HasActions hasActions, Element element) {
        Element actionsEl = element.element("actions");
        if (actionsEl == null) {
            return;
        }

        for (Element actionEl : actionsEl.elements("action")) {
            hasActions.addAction(loadDeclarativeAction(hasActions, actionEl));
        }
    }

    protected Action loadDeclarativeAction(HasActions hasActions, Element element) {
        return loadDeclarativeActionDefault(element);
    }

    protected Action loadDeclarativeActionDefault(Element element) {
        String id = loadActionId(element);

        //String trackSelection = element.attributeValue("trackSelection");
        //boolean shouldTrackSelection = Boolean.parseBoolean(trackSelection);

        Action targetAction;

        //if (shouldTrackSelection) {
        //    Actions actions = getActions();
        //    targetAction = actions.create(ItemTrackingAction.ID, id);
        //loadActionConstraint(targetAction, element);
        //} else {
        targetAction = new SecuredBaseAction(id);
        // }

        initAction(element, targetAction);

        return targetAction;
    }

    protected String loadActionId(Element element) {
        Optional<String> id = loadString(element, "id");
        if (id.isPresent()) {
            return id.get();
        } else {
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
    }

    protected Optional<Action> loadDeclarativeActionByType(Element element) {
        String id = loadActionId(element);

        return loadString(element, "type")
                .map(typeId -> {
                    Action instance = getActions().create(typeId, id);
                    initAction(element, instance);
                    loadActionConstraint(instance, element);
                    return instance;
                });
    }

    protected void initAction(Element element, Action targetAction) {
        loadString(element, "text", targetAction::setText);
        loadBoolean(element, "enable", targetAction::setEnabled);
        loadBoolean(element, "visible", targetAction::setVisible);
        loadEnum(element, ActionVariant.class, "variant", targetAction::setVariant);

        //todo gd refactor icon loading mechanism
        loadString(element, "icon", targetAction::setIcon);

        loadShortcut(element).ifPresent(shortcut ->
                targetAction.setShortcutCombination(KeyCombination.create(shortcut)));

        Element propertiesEl = element.element("properties");
        if (propertiesEl != null) {
            ActionCustomPropertyLoader propertyLoader = applicationContext.getBean(ActionCustomPropertyLoader.class);
            for (Element propertyEl : propertiesEl.elements("property")) {
                loadString(propertiesEl, "name",
                        name -> propertyLoader.load(targetAction, name, propertyEl.attributeValue("value")));
            }
        }
    }

    protected void loadActionConstraint(Action action, Element element) {
        if (action instanceof SecurityConstraintAction) {
            SecurityConstraintAction securityConstraintAction = (SecurityConstraintAction) action;
            loadEnum(element, EntityOp.class, "constraintEntityOp",
                    securityConstraintAction::setConstraintEntityOp);
        }
    }

    protected Optional<String> loadShortcut(Element element) {
        return loadString(element, "shortcut")
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

    protected String loadResourceString(String caption) {
        if (Strings.isNullOrEmpty(caption)) {
            return caption;
        }

        return getMessageTools().loadString(context.getMessageGroup(), caption);
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
}
