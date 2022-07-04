package io.jmix.flowui.view;

import javax.annotation.Nullable;
import java.util.Optional;

public class ViewInfo {

    protected String id;
    protected String controllerClassName;
    protected Class<? extends View<?>> controllerClass;
    protected String templatePath;

    public ViewInfo(String id,
                    String controllerClassName,
                    Class<? extends View<?>> controllerClass,
                    @Nullable String templatePath) {
        this.id = id;
        this.controllerClassName = controllerClassName;
        this.controllerClass = controllerClass;
        this.templatePath = templatePath;
    }

    public String getId() {
        return id;
    }

    public String getControllerClassName() {
        return controllerClassName;
    }

    public Class<? extends View<?>> getControllerClass() {
        return controllerClass;
    }

    public Optional<String> getTemplatePath() {
        return Optional.ofNullable(templatePath);
    }
}
