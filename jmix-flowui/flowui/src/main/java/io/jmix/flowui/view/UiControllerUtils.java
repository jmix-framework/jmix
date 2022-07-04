package io.jmix.flowui.view;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.PageTitle;
import io.jmix.flowui.model.ViewData;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.Optional;

public final class UiControllerUtils {

    private UiControllerUtils() {
    }

    public static String getPageTitle(@Nullable Component component) {
        if (component == null) {
            return "";
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
        return Optional.ofNullable(component.getClass().getAnnotation(annotationClass));
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

    public static boolean isCommitActionPerformed(StandardDetailView<?> detailView) {
        return detailView.isCommitActionPerformed();
    }
}
