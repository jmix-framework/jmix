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

package io.jmix.flowui.view;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.model.ViewData;
import io.jmix.flowui.view.View.QueryParametersChangeEvent;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.Optional;

public final class ViewControllerUtils {

    private ViewControllerUtils() {
    }

    public static String getPageTitle(@Nullable Component component) {
        if (component == null) {
            return "";
        }

        if (component instanceof View) {
            return ((View<?>) component).getPageTitle();
        }

        if (component instanceof HasDynamicTitle) {
            return Strings.nullToEmpty(((HasDynamicTitle) component).getPageTitle());
        } else {
            return findAnnotation(component, PageTitle.class)
                    .map(PageTitle::value)
                    .orElse("");
        }
    }

    public static <A extends Annotation> Optional<A> findAnnotation(Component component, Class<A> annotationClass) {
        return findAnnotation(component.getClass(), annotationClass);
    }

    public static <A extends Annotation> Optional<A> findAnnotation(Class<? extends Component> componentClass,
                                                                    Class<A> annotationClass) {
        return Optional.ofNullable(componentClass.getAnnotation(annotationClass));
    }

    public static String getPackage(Class<?> controllerClass) {
        Package javaPackage = controllerClass.getPackage();
        if (javaPackage != null) {
            return javaPackage.getName();
        }

        // infer from FQN, hot-deployed classes do not have package
        // see JDK-8189231
        String canonicalName = controllerClass.getCanonicalName();
        int dotIndex = canonicalName.lastIndexOf('.');

        if (dotIndex >= 0) {
            return canonicalName.substring(0, dotIndex);
        }

        return "";
    }

    public static ViewData getViewData(View<?> view) {
        return view.getViewData();
    }

    public static void setViewData(View<?> view, ViewData viewData) {
        view.setViewData(viewData);
    }

    public static ViewActions getViewActions(View<?> view) {
        return view.getViewActions();
    }

    public static void setViewActions(View<?> view, ViewActions viewActions) {
        view.setViewActions(viewActions);
    }

    public static ViewFacets getViewFacets(View<?> view) {
        return view.getViewFacets();
    }

    public static void setViewFacets(View<?> view, ViewFacets viewFacets) {
        view.setViewFacets(viewFacets);
    }

    public static void fireEvent(View<?> view, ComponentEvent<?> event) {
        ComponentUtil.fireEvent(view, event);
    }

    public static boolean isSaveActionPerformed(StandardDetailView<?> detailView) {
        return detailView.isSaveActionPerformed();
    }

    public static Registration addQueryParametersChangeListener(View<?> view,
                                                                ComponentEventListener<QueryParametersChangeEvent> listener) {
        return view.addQueryParametersChangeListener(listener);
    }
}
