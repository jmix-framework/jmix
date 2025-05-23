/*
 * Copyright 2025 Haulmont.
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

package io.jmix.masquerade;

import com.codeborne.selenide.SelenideElement;
import com.google.common.base.Preconditions;
import io.jmix.masquerade.component.Notification;
import io.jmix.masquerade.config.ComponentConfig;
import io.jmix.masquerade.config.DefaultComponentConfig;
import io.jmix.masquerade.sys.Composite;
import io.jmix.masquerade.sys.DialogWindow;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.pagefactory.Annotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static com.codeborne.selenide.Selenide.$;
import static io.jmix.masquerade.JSelectors.*;
import static io.jmix.masquerade.Masquerade.UI_TEST_ID;
import static org.openqa.selenium.By.xpath;

/**
 * Factory class for creating and wiring wrappers for Masquerade components.
 */
public class JComponents {

    private static final Logger log = LoggerFactory.getLogger(JComponents.class);

    protected static final Map<Class<?>, Function<By, ?>> components = new ConcurrentHashMap<>();

    static {
        DefaultComponentConfig defaultConfig = new DefaultComponentConfig();

        // import default implementations
        components.putAll(defaultConfig.getComponents());

        // import implementations from project
        ServiceLoader<ComponentConfig> configs = ServiceLoader.load(ComponentConfig.class);
        for (ComponentConfig componentConfig : configs) {
            log.info("Loading component config for {}", componentConfig.getClass().getSimpleName());

            components.putAll(componentConfig.getComponents());
        }
    }

    private JComponents() {
        throw new UnsupportedOperationException();
    }

    /**
     * Wires and returns a composite wrapper for a web-element by class type.
     *
     * @param clazz class of the composite wrapper to wire
     * @param <T>   type of the composite
     * @return composite wrapper for the web-element with wired fields
     */
    protected static <T extends Composite<?>> T wireComposite(Class<T> clazz) {
        TestView testView = clazz.getAnnotation(TestView.class);

        // in case of view-like-composite: dialogs or views
        if (testView != null && !testView.id().isEmpty()) {
            if (DialogWindow.class.isAssignableFrom(clazz)) {
                By xpathBy = xpath("//vaadin-dialog-overlay[vaadin-scroller[vaadin-vertical-layout[@%s='%s']]]"
                        .formatted(UI_TEST_ID, testView.id()));

                return wireClassBy(clazz, xpathBy);
            } else {
                return wireClassBy(clazz, byUiTestId(testView.id()));
            }
        }

        if (Notification.class.isAssignableFrom(clazz)) {
            By xpathBy = xpath(".//vaadin-notification-card");
            return wireClassBy(clazz, xpathBy);
        }

        // in case of layout composite: any layouts, forms or fragments
        TestComponent testComponent = clazz.getAnnotation(TestComponent.class);
        if (testComponent != null && testComponent.path().length != 0) {
            return wireClassBy(clazz, byPath(testComponent.path()));
        }

        return wireClassBy(clazz, byJavaClassName(clazz));
    }

    /**
     * Wires and returns a component wrapper for a web-element by passed class type and {@link By} selector.
     *
     * @param clazz class of the component wrapper to wire
     * @param by    {@link By} selector by which the current web-element can be found
     * @param <T>   type of the component
     * @return component wrapper for the web-element
     */
    @SuppressWarnings("unchecked")
    protected static <T> T wireClassBy(Class<T> clazz, By by) {
        Preconditions.checkArgument(clazz != null);
        Preconditions.checkArgument(by != null);

        Function<By, ?> byFunction = components.get(clazz);
        if (byFunction != null) {
            return clazz.cast(byFunction.apply(by));
        }

        if (SelenideElement.class.isAssignableFrom(clazz)) {
            SelenideElement element = $(by);

            if (element != null) {
                return (T) element;
            }
        }

        // custom composite
        T instance;
        try {
            instance = clazz.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException("Unable to instantiate composite %s".formatted(clazz.getName()), e);
        }

        // assign fields
        Field[] allFields = FieldUtils.getAllFields(clazz);
        for (Field field : allFields) {
            Object fieldValue = getTargetFieldValue(clazz, field, by);

            if (fieldValue != null) {
                try {
                    field.setAccessible(true);
                    field.set(instance, fieldValue);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Unable to inject field " + field.getName(), e);
                }
            }
        }

        return instance;
    }

    /**
     * Computes and returns a value to wire to a field in a web-element's wrapper.
     *
     * @param clazz    origin class for field wiring
     * @param field    field fir wiring
     * @param parentBy {@link By} selector by which the origin class can be found
     * @return the value of the passed field
     * @see TestComponent
     * @see FindBy
     */
    protected static Object getTargetFieldValue(Class<?> clazz, Field field, By parentBy) {
        String fieldName = field.getName();

        TestComponent annotation = field.getAnnotation(TestComponent.class);

        if (annotation != null) {

            if (field.getType() == SelenideElement.class) {
                return $(parentBy);
            }

            if (field.getType() == By.class) {
                return parentBy;
            }

            if (field.getType() == Logger.class) {
                return LoggerFactory.getLogger(clazz);
            }

            String[] path = annotation.path();
            if (path.length == 0) {
                path = new String[]{fieldName};
            }

            By fieldBy = parentBy instanceof ByJavaClassName
                    ? byPath(path)
                    : byChained(parentBy, byPath(path));

            return wireClassBy(field.getType(), fieldBy);
        }

        FindBy findBy = field.getAnnotation(FindBy.class);
        if (findBy != null) {
            By selector = new Annotations(field).buildBy();

            By fieldBy;
            if (parentBy instanceof ByJavaClassName) {
                fieldBy = selector;
            } else {
                fieldBy = byChained(parentBy, selector);
            }

            return wireClassBy(field.getType(), fieldBy);
        }

        return null;
    }
}
