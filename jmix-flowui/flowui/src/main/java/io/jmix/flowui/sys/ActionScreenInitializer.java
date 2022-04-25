package io.jmix.flowui.sys;

import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.flowui.screen.DialogWindow.AfterCloseEvent;
import io.jmix.flowui.screen.Screen;
import io.jmix.flowui.screen.builder.EditorWindowBuilder;
import io.jmix.flowui.screen.builder.LookupWindowBuilder;
import io.jmix.flowui.screen.navigation.EditorNavigator;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ActionScreenInitializer {

    protected String screenId;
    protected Class<? extends Screen> screenClass;
    protected RouteParameters routeParameters;
    protected QueryParameters queryParameters;
    protected Consumer<AfterCloseEvent<?>> afterCloseHandler;

    @Nullable
    public String getScreenId() {
        return screenId;
    }

    public void setScreenId(@Nullable String screenId) {
        this.screenId = screenId;
    }

    @Nullable
    public Class<? extends Screen> getScreenClass() {
        return screenClass;
    }

    public void setScreenClass(@Nullable Class<? extends Screen> screenClass) {
        this.screenClass = screenClass;
    }

    @Nullable
    public RouteParameters getRouteParameters() {
        return routeParameters;
    }

    public void setRouteParameters(@Nullable RouteParameters routeParameters) {
        this.routeParameters = routeParameters;
    }

    @Nullable
    public QueryParameters getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(@Nullable QueryParameters queryParameters) {
        this.queryParameters = queryParameters;
    }

    @Nullable
    public <S extends Screen> Consumer<AfterCloseEvent<S>> getAfterCloseHandler() {
        return (Consumer) afterCloseHandler;
    }

    public <S extends Screen> void setAfterCloseHandler(@Nullable Consumer<AfterCloseEvent<S>> afterCloseHandler) {
        this.afterCloseHandler = (Consumer) afterCloseHandler;
    }

    public <E> EditorNavigator<E> initNavigator(EditorNavigator<E> navigator) {
        if (screenClass != null) {
            navigator = navigator.withScreenClass(screenClass);
        }

        if (screenId != null) {
            navigator = navigator.withScreenId(screenId);
        }

        if (routeParameters != null) {
            navigator = navigator.withRouteParameters(routeParameters);
        }

        if (queryParameters != null) {
            navigator = navigator.withQueryParameters(queryParameters);
        }

        return navigator;
    }

    public <E, S extends Screen> EditorWindowBuilder<E, S> initWindowBuilder(EditorWindowBuilder<E, S> windowBuilder) {
        if (screenClass != null) {
            windowBuilder = windowBuilder.withScreenClass((Class) screenClass);
        }

        if (screenId != null) {
            windowBuilder = windowBuilder.withScreenId(screenId);
        }

        if (afterCloseHandler != null) {
            windowBuilder = windowBuilder.withAfterCloseListener((Consumer) afterCloseHandler);
        }

        return windowBuilder;
    }

    public <E, S extends Screen> LookupWindowBuilder<E, S> initWindowBuilder(LookupWindowBuilder<E, S> windowBuilder) {
        if (screenClass != null) {
            windowBuilder = windowBuilder.withScreenClass((Class) screenClass);
        }

        if (screenId != null) {
            windowBuilder = windowBuilder.withScreenId(screenId);
        }

        if (afterCloseHandler != null) {
            windowBuilder = windowBuilder.withAfterCloseListener((Consumer) afterCloseHandler);
        }

        return windowBuilder;
    }
}
