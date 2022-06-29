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

package io.jmix.ui.menu;

import com.google.common.collect.ImmutableMap;
import io.jmix.core.*;
import io.jmix.ui.component.DialogWindow;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.Screens;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.logging.UserActionsLogger;
import io.jmix.ui.monitoring.UiMonitoring;
import io.jmix.ui.screen.*;
import io.jmix.ui.sys.UiControllerProperty;
import io.jmix.ui.sys.UiControllerPropertyInjector;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.jmix.ui.screen.UiControllerUtils.getScreenContext;
import static io.jmix.ui.sys.UiControllerProperty.Type.REFERENCE;
import static io.jmix.ui.sys.UiControllerProperty.Type.VALUE;

@Component("ui_MenuItemCommands")
public class MenuItemCommands {

    private static final Logger userActionsLog = LoggerFactory.getLogger(UserActionsLogger.class);
    private static final Logger log = LoggerFactory.getLogger(MenuItemCommands.class);

    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected MenuConfig menuConfig;
    @Autowired
    protected WindowConfig windowConfig;
    @Autowired
    protected ClassManager classManager;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected FetchPlanRepository fetchPlanRepository;
    @Autowired
    protected ScreenBuilders screenBuilders;
    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected MeterRegistry meterRegistry;

    /**
     * Create menu command.
     *
     * @param item menu item
     * @return command
     */
    @Nullable
    public MenuItemCommand create(FrameOwner origin, MenuItem item) {
        if (StringUtils.isNotEmpty(item.getScreen())) {
            return createScreenCommand(origin, item);
        }

        if (StringUtils.isNotEmpty(item.getRunnableClass())) {
            return createRunnableClassCommand(origin, item, item.getRunnableClass());
        }

        if (StringUtils.isNotEmpty(item.getBean())) {
            return createBeanCommand(origin, item, item.getBean(), item.getBeanMethod());
        }

        return null;
    }

    protected MenuItemCommand createScreenCommand(FrameOwner origin, MenuItem item) {
        return new ScreenCommand(origin, item, item.getScreen(), item.getDescriptor());
    }

    protected MenuItemCommand createRunnableClassCommand(FrameOwner origin, MenuItem item, String runnableClass) {
        return new RunnableClassCommand(origin, item, runnableClass);
    }

    protected MenuItemCommand createBeanCommand(FrameOwner origin, MenuItem item, String bean, String beanMethod) {
        return new BeanCommand(origin, item, bean, beanMethod);
    }

    protected Map<String, Object> buildMethodParametersMap(List<MenuItem.MenuItemProperty> properties) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        for (MenuItem.MenuItemProperty property : properties) {
            builder.put(property.getName(),
                    property.getValue() != null ? property.getValue() : loadEntity(property));
        }

        return builder.build();
    }

    protected Object loadEntity(MenuItem.MenuItemProperty property) {
        LoadContext<Object> ctx = new LoadContext<>(property.getEntityClass())
                .setId(property.getEntityId());

        if (StringUtils.isNotEmpty(property.getFetchPlanName())) {
            ctx.setFetchPlan(fetchPlanRepository.getFetchPlan(
                    property.getEntityClass(), property.getFetchPlanName()));
        }

        Object entity = dataManager.load(ctx);

        if (entity == null) {
            throw new RuntimeException(String.format("Unable to load entity of class '%s' with id '%s'",
                    property.getEntityClass(), property.getEntityId()));
        }
        return entity;
    }

    protected class ScreenCommand implements MenuItemCommand {
        protected FrameOwner origin;
        protected MenuItem item;

        protected String screen;
        protected Element descriptor;

        protected ScreenCommand(FrameOwner origin, MenuItem item, String screen, Element descriptor) {
            this.origin = origin;
            this.item = item;
            this.screen = screen;
            this.descriptor = descriptor;
        }

        @Override
        public void run() {
            userActionsLog.trace("Menu item {} triggered", item.getId());

            List<UiControllerProperty> controllerProperties = convertToUiControllerProperties(item.getProperties());

            Timer.Sample sample = Timer.start(meterRegistry);

            WindowInfo windowInfo = windowConfig.getWindowInfo(this.screen);
            Screen screen = createScreen(windowInfo, this.screen, controllerProperties);

            if (screen != null && screen.getWindow() instanceof DialogWindow) {
                Boolean resizable = getResizable(descriptor);
                if (resizable != null) {
                    ((DialogWindow) screen.getWindow()).setResizable(resizable);
                }
            }

            // inject declarative properties

            List<UiControllerProperty> properties = controllerProperties;
            if (windowInfo.getDescriptor() != null) {
                properties = properties.stream()
                        .filter(prop -> !"entityToEdit".equals(prop.getName()))
                        .collect(Collectors.toList());
            }

            UiControllerPropertyInjector propertyInjector = applicationContext.getBean(UiControllerPropertyInjector.class,
                    screen, properties);
            propertyInjector.inject();

            if (screen != null) {
                Screens screens = getScreenContext(origin).getScreens();
                screens.showFromNavigation(screen);
            }

            sample.stop(UiMonitoring.createMenuTimer(meterRegistry, item.getId()));
        }

        protected Object getEntityToEdit(String screenId, List<UiControllerProperty> controllerProperties) {
            Object entityItem;

            Object entityToEdit = controllerProperties.stream()
                    .filter(prop -> "entityToEdit".equals(prop.getName()))
                    .findFirst()
                    .map(UiControllerProperty::getValue)
                    .orElse(null);
            if (entityToEdit instanceof Entity) {
                entityItem = entityToEdit;
            } else {
                String[] strings = screenId.split("[.]");
                String metaClassName;
                if (strings.length == 2) {
                    metaClassName = strings[0];
                } else if (strings.length == 3) {
                    metaClassName = strings[1];
                } else {
                    throw new UnsupportedOperationException("Incorrect screen parameters in menu item " + item.getId());
                }

                entityItem = metadata.create(metaClassName);
            }
            return entityItem;
        }

        @Nullable
        protected Screen createScreen(WindowInfo windowInfo, String screenId,
                                      List<UiControllerProperty> controllerProperties) {
            Screens screens = getScreenContext(origin).getScreens();

            Screen screen = screens.create(screenId, getOpenMode(descriptor),
                    new MapScreenOptions(Collections.emptyMap()));
            if (screen instanceof EditorScreen) {
                //noinspection unchecked
                ((EditorScreen<Object>) screen).setEntityToEdit(getEntityToEdit(screenId, controllerProperties));
            }
            return screen;
        }

        @Override
        public String getDescription() {
            return String.format("Opening window: \"%s\"", screen);
        }

        protected OpenMode getOpenMode(Element descriptor) {
            String openModeStr = descriptor.attributeValue("openMode");
            if (StringUtils.isNotEmpty(openModeStr)) {
                return OpenMode.valueOf(openModeStr);
            }
            return OpenMode.NEW_TAB;
        }

        @Nullable
        protected Boolean getResizable(Element descriptor) {
            String resizable = descriptor.attributeValue("resizable");
            if (StringUtils.isNotEmpty(resizable)) {
                return Boolean.parseBoolean(resizable);
            }
            return null;
        }

        protected List<UiControllerProperty> convertToUiControllerProperties(List<MenuItem.MenuItemProperty> properties) {
            return properties.stream()
                    .map(property -> property.getValue() != null
                            ? new UiControllerProperty(property.getName(), property.getValue(), VALUE)
                            : new UiControllerProperty(property.getName(), loadEntity(property), REFERENCE))
                    .collect(Collectors.toList());
        }
    }

    protected class BeanCommand implements MenuItemCommand {

        protected FrameOwner origin;
        protected MenuItem item;

        protected String bean;
        protected String beanMethod;

        protected BeanCommand(FrameOwner origin, MenuItem item, String bean, String beanMethod) {
            this.origin = origin;
            this.item = item;
            this.bean = bean;
            this.beanMethod = beanMethod;
        }

        @Override
        public void run() {
            userActionsLog.trace("Menu item {} triggered", item.getId());

            Timer.Sample sample = Timer.start(meterRegistry);

            Map<String, Object> methodParameters = getMethodParameters(item);

            Object beanInstance = applicationContext.getBean(bean);
            try {
                Method methodWithParams = MethodUtils.getAccessibleMethod(beanInstance.getClass(), beanMethod, Map.class);
                if (methodWithParams != null) {
                    methodWithParams.invoke(beanInstance, methodParameters);
                    return;
                }

                Method methodWithScreen = MethodUtils.getAccessibleMethod(beanInstance.getClass(), beanMethod, FrameOwner.class);
                if (methodWithScreen != null) {
                    methodWithScreen.invoke(beanInstance, origin);
                    return;
                }

                MethodUtils.invokeMethod(beanInstance, beanMethod, (Object[]) null);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Unable to execute bean method", e);
            }

            sample.stop(UiMonitoring.createMenuTimer(meterRegistry, item.getId()));
        }

        protected Map<String, Object> getMethodParameters(MenuItem item) {
            return buildMethodParametersMap(item.getProperties());
        }

        @Override
        public String getDescription() {
            return String.format("Calling bean method: %s#%s", bean, beanMethod);
        }
    }

    protected class RunnableClassCommand implements MenuItemCommand {

        protected FrameOwner origin;
        protected MenuItem item;

        protected String runnableClass;

        protected RunnableClassCommand(FrameOwner origin, MenuItem item, String runnableClass) {
            this.origin = origin;
            this.item = item;
            this.runnableClass = runnableClass;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            userActionsLog.trace("Menu item {} triggered", item.getId());

            Timer.Sample sample = Timer.start(meterRegistry);

            Map<String, Object> methodParameters = getMethodParameters(item);

            Class<?> clazz = classManager.findClass(runnableClass);
            if (clazz == null) {
                throw new IllegalStateException(String.format("Can't load class: %s", runnableClass));
            }

            if (!Runnable.class.isAssignableFrom(clazz)
                    && !Consumer.class.isAssignableFrom(clazz)
                    && !MenuItemRunnable.class.isAssignableFrom(clazz)) {

                throw new IllegalStateException(
                        String.format("Class \"%s\" must implement Runnable or Consumer<Map<String, Object>> or MenuItemRunnable",
                                runnableClass));
            }

            Constructor<?> constructor;
            try {
                constructor = clazz.getConstructor();
            } catch (NoSuchMethodException e) {
                throw new DevelopmentException(String.format("Unable to get constructor of %s", runnableClass));
            }

            Object classInstance;
            try {
                classInstance = constructor.newInstance();
            } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                throw new DevelopmentException(String.format("Failed to get a new instance of %s", runnableClass));
            }

            if (classInstance instanceof ApplicationContextAware) {
                ((ApplicationContextAware) classInstance).setApplicationContext(applicationContext);
            }

            if (classInstance instanceof MenuItemRunnable) {
                ((MenuItemRunnable) classInstance).run(origin, item);
            } else if (classInstance instanceof Consumer) {
                ((Consumer<Object>) classInstance).accept(methodParameters);
            } else {
                ((Runnable) classInstance).run();
            }

            sample.stop(UiMonitoring.createMenuTimer(meterRegistry, item.getId()));
        }

        protected Map<String, Object> getMethodParameters(MenuItem item) {
            return buildMethodParametersMap(item.getProperties());
        }

        @Override
        public String getDescription() {
            return String.format("Running \"%s\"", runnableClass);
        }
    }
}
