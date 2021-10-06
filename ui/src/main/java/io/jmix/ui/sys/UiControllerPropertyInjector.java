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

package io.jmix.ui.sys;

import io.jmix.ui.component.Component;
import io.jmix.ui.component.ScreenFacet;
import io.jmix.ui.component.Window;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.UiControllerUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

@SuppressWarnings("unused")
@org.springframework.stereotype.Component("ui_UiControllerPropertyInjector")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UiControllerPropertyInjector {

    private static final Logger log = LoggerFactory.getLogger(UiControllerPropertyInjector.class);

    protected final FrameOwner frameOwner;
    protected final Screen sourceScreen;
    protected final List<UiControllerProperty> properties;

    protected UiControllerReflectionInspector reflectionInspector;

    /**
     * Creates UiControllerPropertyInjector to inject properties into fragments
     *
     * @param frameOwner target screen
     * @param properties properties to inject
     */
    public UiControllerPropertyInjector(FrameOwner frameOwner, List<UiControllerProperty> properties) {
        checkNotNullArgument(frameOwner, "Frame owner cannot be null");
        checkNotNullArgument(properties, "Properties cannot be null");

        this.frameOwner = frameOwner;
        this.sourceScreen = null;
        this.properties = properties;
    }

    /**
     * Creates UiControllerPropertyInjector to inject properties into {@link ScreenFacet}.
     *
     * @param frameOwner   target screen
     * @param sourceScreen source screen that is used to load ref properties
     * @param properties   properties to inject
     */
    public UiControllerPropertyInjector(FrameOwner frameOwner, Screen sourceScreen, List<UiControllerProperty> properties) {
        checkNotNullArgument(frameOwner, "Frame owner cannot be null");
        checkNotNullArgument(properties, "Properties cannot be null");

        this.frameOwner = frameOwner;
        this.sourceScreen = sourceScreen;
        this.properties = properties;
    }

    @Autowired
    public void setReflectionInspector(UiControllerReflectionInspector reflectionInspector) {
        this.reflectionInspector = reflectionInspector;
    }

    public void inject() {
        for (UiControllerProperty property : properties) {
            String propName = property.getName();
            Method setter = findSuitableSetter(propName);
            if (setter == null) {
                log.info("Unable to find suitable setter for property '{}'. Its value will not be injected into '{}'",
                        propName, frameOwner);
                continue;
            }

            Object value = property.getValue();

            if (value instanceof String) {
                Class<?> propType = setter.getParameterTypes()[0];
                value = parseParamValue(property, propType);

                if (value == null) {
                    log.info("Unable to parse '{}' as '{}' for property '{}'. It will not be injected into '{}'",
                            property.getValue(), propType, propName, frameOwner);
                    continue;
                }
            }

            try {
                setter.invoke(frameOwner, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.info("Unable to assign value through setter '{}' in '{}' for property '{}'",
                        setter.getName(), frameOwner, propName, e);
            }
        }
    }

    @Nullable
    protected Method findSuitableSetter(String propName) {
        String setterName = getSetterName(propName);

        List<Method> propertySetters = reflectionInspector.getPropertySetters(frameOwner.getClass());

        return propertySetters.stream()
                .filter(method -> setterName.equals(method.getName()))
                .findFirst()
                .orElse(null);
    }

    @Nullable
    protected Object parseParamValue(UiControllerProperty property, Class<?> propType) {
        Object value = null;

        if (UiControllerProperty.Type.VALUE == property.getType()) {
            value = parsePrimitive(property, propType);
        } else if (UiControllerProperty.Type.REFERENCE == property.getType()) {
            value = findObjectByRef(property, propType);
        }

        return value;
    }

    @Nullable
    protected Object parsePrimitive(UiControllerProperty property, Class propType) {
        Object value = null;
        String stringProperty = ((String) property.getValue());

        try {
            if (Byte.class == propType || Byte.TYPE == propType) {
                value = Byte.valueOf(stringProperty);

            } else if (Short.class == propType || Short.TYPE == propType) {
                value = Short.valueOf(stringProperty);

            } else if (Integer.class == propType || Integer.TYPE == propType) {
                value = Integer.valueOf(stringProperty);

            } else if (Long.class == propType || Long.TYPE == propType) {
                value = Long.valueOf(stringProperty);

            } else if (Float.class == propType || Float.TYPE == propType) {
                value = Float.valueOf(stringProperty);

            } else if (Double.class == propType || Double.TYPE == propType) {
                value = Double.valueOf(stringProperty);
            }
        } catch (NumberFormatException e) {
            log.info("Unable to parse '{}' as '{}'. Property value '{}' will not be injected into '{}'",
                    property.getValue(), propType, property.getName(), frameOwner);
        }

        if (Boolean.class == propType || Boolean.TYPE == propType) {
            value = Boolean.valueOf(stringProperty);
        } else if (String.class == propType) {
            value = property.getValue();
        }

        return value;
    }

    @Nullable
    protected Object findObjectByRef(UiControllerProperty property, Class<?> propType) {
        Object value = null;
        String stringProp = (String) property.getValue();

        if (Component.class.isAssignableFrom(propType)) {
            value = findComponent(stringProp);
            if (value == null) {
                log.info("Unable to find component with id '{}'. Property value '{}' will not be injected into '{}'",
                        property.getValue(), property.getName(), frameOwner);
            }

        } else if (InstanceContainer.class.isAssignableFrom(propType)) {
            value = findDataContainer(stringProp);
            if (value == null) {
                log.info("Unable to find data container with id '{}'. Property value '{}' will not be injected into '{}'",
                        property.getValue(), property.getName(), frameOwner);
            }

        } else if (DataLoader.class.isAssignableFrom(propType)) {
            value = findLoader(stringProp);
            if (value == null) {
                log.info("Unable to find data loader with id '{}'. Property value '{}' will not be injected into '{}'",
                        property.getValue(), property.getName(), frameOwner);
            }
        }

        return value;
    }

    protected String getSetterName(String name) {
        return "set" + StringUtils.capitalize(name);
    }

    @Nullable
    protected Component findComponent(String componentId) {
        Component component = null;
        Window window = null;

        if (sourceScreen != null) {
            window = sourceScreen.getWindow();
        } else if (frameOwner instanceof ScreenFragment) {
            FrameOwner host = ((ScreenFragment) frameOwner).getHostController();

            if (host instanceof Screen) {
                window = ((Screen) host).getWindow();
            }
        } else if (frameOwner instanceof Screen) {
            window = ((Screen) frameOwner).getWindow();
        }

        if (window != null) {
            component = window.getComponent(componentId);
        }

        return component;
    }

    @Nullable
    protected InstanceContainer findDataContainer(String containerId) {
        FrameOwner host = frameOwner instanceof ScreenFragment
                ? ((ScreenFragment) frameOwner).getHostController()
                : frameOwner;

        if (sourceScreen != null) {
            return UiControllerUtils.getScreenData(sourceScreen)
                    .getContainer(containerId);
        }
        return UiControllerUtils.getScreenData(host)
                .getContainer(containerId);
    }

    @Nullable
    protected DataLoader findLoader(String loaderId) {
        FrameOwner host = frameOwner instanceof ScreenFragment
                ? ((ScreenFragment) frameOwner).getHostController()
                : frameOwner;

        if (sourceScreen != null) {
            return UiControllerUtils.getScreenData(sourceScreen)
                    .getLoader(loaderId);
        }
        return UiControllerUtils.getScreenData(host)
                .getLoader(loaderId);
    }
}
