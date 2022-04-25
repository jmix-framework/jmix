package io.jmix.flowui.screen;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.PageTitle;
import io.jmix.flowui.model.ScreenData;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.Optional;

public final class UiControllerUtils {

    private UiControllerUtils() {
    }

    // TODO: gg, Screen?
    public static String getTitle(@Nullable Component component) {
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

    public static ScreenData getScreenData(Screen screen) {
        return screen.getScreenData();
    }

    public static void setScreenData(Screen screen, ScreenData screenData) {
        screen.setScreenData(screenData);
    }

    public static ScreenActions getScreenActions(Screen screen) {
        return screen.getScreenActions();
    }

    public static void setScreenActions(Screen screen, ScreenActions screenActions) {
        screen.setScreenActions(screenActions);
    }

    public static void fireEvent(Screen screen, ComponentEvent<?> event) {
        ComponentUtil.fireEvent(screen, event);
    }

    public static boolean isCommitActionPerformed(StandardEditor editor) {
        return editor.isCommitActionPerformed();
    }
}
