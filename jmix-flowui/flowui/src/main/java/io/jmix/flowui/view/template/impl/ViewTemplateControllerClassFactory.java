/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.view.template.impl;

import com.vaadin.flow.router.Route;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.view.*;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Generates dedicated controller classes for template-based views.
 */
@Component("flowui_ViewTemplateControllerClassFactory")
public class ViewTemplateControllerClassFactory {

    protected static final String GENERATED_PACKAGE_SUFFIX = ".generated_view";
    protected static final Pattern NON_ROUTE_CHARS = Pattern.compile("[^a-z0-9]+");
    protected static final Pattern EDGE_SEPARATORS = Pattern.compile("(^[-_]+|[-_]+$)");

    protected Map<String, Class<? extends View<?>>> controllerClasses = new ConcurrentHashMap<>();

    /**
     * Returns a generated controller class for the specified template view.
     *
     * @param entityMetaClass entity meta-class that owns the generated view
     * @param viewId         view id to expose through {@link ViewController}
     * @param type           template view type
     * @param descriptorPath descriptor path exposed through {@link ViewDescriptor}
     * @param routePath      route path exposed through {@link Route}
     * @return generated controller class
     */
    public Class<? extends View<?>> createControllerClass(MetaClass entityMetaClass,
                                                         String viewId,
                                                         ViewTemplateType type,
                                                         String descriptorPath,
                                                         String routePath) {
        String key = type.name() + ":" + viewId + ":" + routePath;
        return controllerClasses.computeIfAbsent(key, __ ->
                createControllerClassInternal(entityMetaClass, viewId, type, descriptorPath, routePath));
    }

    @SuppressWarnings("unchecked")
    protected Class<? extends View<?>> createControllerClassInternal(MetaClass entityMetaClass,
                                                                    String viewId,
                                                                    ViewTemplateType type,
                                                                    String descriptorPath,
                                                                    String routePath) {
        Class<? extends View<?>> superclass = type == ViewTemplateType.LIST
                ? TemplateListView.class
                : TemplateDetailView.class;
        String className = createClassName(entityMetaClass, type);
        Class<?> loadedClass = findLoadedClass(className, superclass.getClassLoader());
        if (loadedClass != null) {
            return (Class<? extends View<?>>) loadedClass;
        }

        AnnotationDescription viewController = AnnotationDescription.Builder.ofType(ViewController.class)
                .define(ViewController.ID_ATTRIBUTE, viewId)
                .build();
        AnnotationDescription viewDescriptor = AnnotationDescription.Builder.ofType(ViewDescriptor.class)
                .define("path", descriptorPath)
                .build();
        AnnotationDescription route = AnnotationDescription.Builder.ofType(Route.class)
                .define("value", routePath)
                .define("layout", DefaultMainViewParent.class)
                .build();

        try (DynamicType.Unloaded<? extends View<?>> unloaded = new ByteBuddy()
                .subclass(superclass)
                .name(className)
                .annotateType(viewController, viewDescriptor, route)
                .make()) {
            return unloaded.load(superclass.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded();
        }
    }

    /**
     * Creates the default route path for a template view when the annotation does not provide one.
     *
     * @param viewId template view id
     * @param type   template view type
     * @return generated route path
     */
    public String createDefaultRoutePath(String viewId, ViewTemplateType type) {
        String routePath = createRouteSlug(viewId);
        return type == ViewTemplateType.DETAIL
                ? routePath + "/:" + StandardDetailView.DEFAULT_ROUTE_PARAM
                : routePath;
    }

    protected String createClassName(MetaClass entityMetaClass, ViewTemplateType type) {
        Class<?> entityClass = entityMetaClass.getJavaClass();
        String controllerType = type == ViewTemplateType.LIST ? "ListView" : "DetailView";
        return entityClass.getPackageName()
                + GENERATED_PACKAGE_SUFFIX
                + "."
                + entityClass.getSimpleName()
                + controllerType;
    }

    protected String createRouteSlug(String value) {
        String slug = NON_ROUTE_CHARS.matcher(value.toLowerCase(Locale.ROOT)).replaceAll("-");
        slug = EDGE_SEPARATORS.matcher(slug).replaceAll("");

        if (slug.isEmpty()) {
            slug = "view";
        }

        return slug;
    }

    protected Class<?> findLoadedClass(String className, ClassLoader classLoader) {
        try {
            return Class.forName(className, false, classLoader);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
