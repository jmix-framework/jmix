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

package io.jmix.flowui.sys;

import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Class provides information about view controller.
 */
public class ViewControllerMeta {

    private static final Logger log = LoggerFactory.getLogger(ViewControllerMeta.class);

    protected MetadataReaderFactory metadataReaderFactory;
    protected MetadataReader metadataReader;
    protected Class<? extends View> viewClass;

    public ViewControllerMeta(MetadataReaderFactory metadataReaderFactory, MetadataReader metadataReader) {
        this.metadataReaderFactory = metadataReaderFactory;
        this.metadataReader = metadataReader;
    }

    public ViewControllerMeta(MetadataReaderFactory metadataReaderFactory, Class<? extends View> viewClass) {
        this.metadataReaderFactory = metadataReaderFactory;
        this.viewClass = viewClass;
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
        return getViewClassName();
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
                : getControllerAnnotationAttributes(annotationName, viewClass);
    }

    protected String getControllerId() {
        Map<String, Object> viewController = getAnnotationAttributes(ViewController.class.getName());

        String id = null;
        String value = null;
        if (viewController != null) {
            id = (String) viewController.get(ViewController.ID_ATTRIBUTE);
            value = (String) viewController.get(ViewController.VALUE_ATTRIBUTE);
        }

        return ViewDescriptorUtils.getInferredViewId(id, value, getViewClassName());
    }

    protected String getViewClassName() {
        return metadataReader != null
                ? metadataReader.getClassMetadata().getClassName()
                : viewClass.getName();
    }

    @Nullable
    protected Map<String, Object> getControllerAnnotationAttributes(String annotationName,
                                                                    Class<? extends View> viewClass) {
        for (Annotation annotation : viewClass.getAnnotations()) {
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
                            annotationClass.getName(), method.getName(), viewClass.getName(), e);
                }
            }
            return annotationAttributes;
        }
        return null;
    }
}
