package io.jmix.flowui.devserver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.servlet.ServletContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AnonymousAllowed
@SuppressWarnings("unused")
@Route(value = Preview.PATH, layout = MainLayout.class)
public class Preview extends Div implements BeforeEnterObserver {

    public static final String PATH = "/flow_ui_designer/:id";
    private static final String EDITOR_PANEL_STORAGE_BEAN_ATTRIBUTE = "EditorPanelStorageBean";

    private static final Logger log = LoggerFactory.getLogger(Preview.class);

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.getRouteParameters().get("id").ifPresent(id ->
                initFor(PATH.replace(":id", id)));
    }

    public void initFor(String path) {
        if (StringUtils.isBlank(path)) {
            add(new Span("path parameter is empty"));
            return;
        }

        try {
            registerDesigner(path);
        } catch (Throwable e) {
            add(new Span(e.getMessage()));
        }
    }

    private static ServletContext getServletContext() {
        return VaadinServlet.getCurrent().getServletContext();
    }

    private static Object getEditorPanelStorage() {
        return getServletContext().getAttribute(EDITOR_PANEL_STORAGE_BEAN_ATTRIBUTE);
    }

    private void registerDesigner(String path) {
        Object editorPanelStorage = getEditorPanelStorage();
        if (editorPanelStorage != null) {
            Class<?> editorPanelStorageClass = editorPanelStorage.getClass();
            Method registerMethod = Arrays.stream(editorPanelStorageClass.getMethods())
                    .filter(it -> it.getName().equals("register"))
                    .findFirst()
                    .orElse(null);
            if (registerMethod != null) {
                try {
                    registerMethod.trySetAccessible();
                    registerMethod.invoke(editorPanelStorage, path, this);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    log.warn("Error when register panel", e);
                }
            } else {
                log.warn("Method with name 'register' not found in {}", editorPanelStorageClass);
            }
        } else {
            log.warn("EditorPanel or EditorPanelStorage is null");
        }
    }
}