/*
 * Copyright 2021 Haulmont.
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

import io.jmix.ui.navigation.Route;
import io.jmix.ui.navigation.RouteDefinition;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Class provides information about screen controller.
 */
public class UiControllerMeta {

    private static final Logger log = LoggerFactory.getLogger(UiControllerMeta.class);

    protected MetadataReaderFactory metadataReaderFactory;
    protected MetadataReader metadataReader;
    protected Class<? extends FrameOwner> screenClass;

    public UiControllerMeta(MetadataReaderFactory metadataReaderFactory, MetadataReader metadataReader) {
        this.metadataReaderFactory = metadataReaderFactory;
        this.metadataReader = metadataReader;
    }

    public UiControllerMeta(MetadataReaderFactory metadataReaderFactory, Class<? extends FrameOwner> screenClass) {
        this.metadataReaderFactory = metadataReaderFactory;
        this.screenClass = screenClass;
    }

    /**
     * @return controller id
     */
    public String getId() {
        return getControllerId();
    }

    /**
     * @return fully qualified controller class name
     */
    public String getControllerClass() {
        return getScreenClassName();
    }

    /**
     * @return route definition configured by the {@link Route} annotation
     */
    @Nullable
    public RouteDefinition getRouteDefinition() {
        return extractRouteDefinition();
    }

    /**
     * @return the resource reference for the class file
     */
    @Nullable
    public Resource getResource() {
        return metadataReader != null ? metadataReader.getResource() : null;
    }

    /**
     * @param annotationName fully qualified annotation class name
     * @return key-value pairs of annotation properties and their values
     */
    @Nullable
    public Map<String, Object> getAnnotationAttributes(String annotationName) {
        return metadataReader != null
                ? metadataReader.getAnnotationMetadata().getAnnotationAttributes(annotationName)
                : getControllerAnnotationAttributes(annotationName, screenClass);
    }

    protected String getControllerId() {
        Map<String, Object> uiController = getAnnotationAttributes(UiController.class.getName());

        String id = null;
        String value = null;
        if (uiController != null) {
            id = (String) uiController.get(UiController.ID_ATTRIBUTE);
            value = (String) uiController.get(UiController.VALUE_ATTRIBUTE);
        }

        return UiDescriptorUtils.getInferredScreenId(id, value, getScreenClassName());
    }

    protected String getScreenClassName() {
        return metadataReader != null
                ? metadataReader.getClassMetadata().getClassName()
                : screenClass.getName();
    }

    @Nullable
    protected RouteDefinition extractRouteDefinition() {
        Map<String, Object> route = getAnnotationAttributes(Route.class.getName());

        if (route == null) {
            route = traverseForRoute();
        }

        RouteDefinition routeDefinition = null;

        if (route != null) {
            String pathAttr = (String) route.get(Route.PATH_ATTRIBUTE);
            String parentPrefixAttr = (String) route.get(Route.PARENT_PREFIX_ATTRIBUTE);
            boolean rootRoute = (boolean) route.get(Route.ROOT_ATTRIBUTE);

            routeDefinition = new RouteDefinition(pathAttr, parentPrefixAttr, rootRoute);
        }

        return routeDefinition;
    }

    @Nullable
    protected Map<String, Object> traverseForRoute() {
        return metadataReader != null
                ? traverseForRoute(metadataReader)
                : traverseForRoute(screenClass);
    }

    @Nullable
    protected Map<String, Object> traverseForRoute(Class screenClass) {
        //noinspection unchecked
        Class<? extends FrameOwner> superClass = screenClass.getSuperclass();
        if (superClass == null || Screen.class.getName().equals(superClass.getName())) {
            return null;
        }

        Route route = superClass.getAnnotation(Route.class);

        return route != null
                ? getControllerAnnotationAttributes(Route.class.getName(), superClass)
                : traverseForRoute(superClass);
    }

    @Nullable
    protected Map<String, Object> traverseForRoute(MetadataReader metadataReader) {
        String superClazz = metadataReader.getClassMetadata().getSuperClassName();

        if (Screen.class.getName().equals(superClazz) || superClazz == null) {
            return null;
        }

        try {
            MetadataReader superReader = metadataReaderFactory.getMetadataReader(superClazz);
            Map<String, Object> routeAttributes =
                    superReader.getAnnotationMetadata()
                            .getAnnotationAttributes(Route.class.getName());

            return routeAttributes != null
                    ? routeAttributes
                    : traverseForRoute(superReader);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read class: %s" + superClazz);
        }
    }

    @Nullable
    protected Map<String, Object> getControllerAnnotationAttributes(String annotationName, Class<?> screenClass) {
        for (Annotation annotation : screenClass.getAnnotations()) {
            Class<? extends Annotation> annotationClass = annotation.getClass();
            if (!annotationClass.getName().equals(annotationName)) {
                continue;
            }

            Map<String, Object> annotationAttributes = new HashMap<>();
            for (Method method : annotationClass.getDeclaredMethods()) {
                try {
                    annotationAttributes.put(method.getName(), method.invoke(annotation));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    log.warn("Failed to get '{}#{}' property value for class '{}'",
                            annotationClass.getName(), method.getName(), screenClass.getName(), e);
                }
            }
            return annotationAttributes;
        }
        return null;
    }
}
