package io.jmix.flowui.screen;

import javax.annotation.Nullable;
import java.util.Optional;

public class ScreenInfo {

    protected String id;
    protected String controllerClassName;
    protected Class<? extends Screen> controllerClass;
    protected String templatePath;

    public ScreenInfo(String id,
                      String controllerClassName,
                      Class<? extends Screen> controllerClass,
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

    public Class<? extends Screen> getControllerClass() {
        return controllerClass;
    }

    public Optional<String> getTemplatePath() {
        return Optional.ofNullable(templatePath);
    }
}
