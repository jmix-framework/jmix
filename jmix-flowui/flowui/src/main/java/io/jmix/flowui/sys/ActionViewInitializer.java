package io.jmix.flowui.sys;

import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.flowui.view.DialogWindow.AfterCloseEvent;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.builder.DetailWindowBuilder;
import io.jmix.flowui.view.builder.LookupWindowBuilder;
import io.jmix.flowui.view.navigation.DetailViewNavigator;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ActionViewInitializer {

    protected String viewId;
    protected Class<? extends View> viewClass;
    protected RouteParameters routeParameters;
    protected QueryParameters queryParameters;
    protected Consumer<AfterCloseEvent<?>> afterCloseHandler;

    @Nullable
    public String getViewId() {
        return viewId;
    }

    public void setViewId(@Nullable String viewId) {
        this.viewId = viewId;
    }

    @Nullable
    public Class<? extends View> getViewClass() {
        return viewClass;
    }

    public void setViewClass(@Nullable Class<? extends View> viewClass) {
        this.viewClass = viewClass;
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
    public <S extends View<?>> Consumer<AfterCloseEvent<S>> getAfterCloseHandler() {
        return (Consumer) afterCloseHandler;
    }

    public <S extends View<?>> void setAfterCloseHandler(@Nullable Consumer<AfterCloseEvent<S>> afterCloseHandler) {
        this.afterCloseHandler = (Consumer) afterCloseHandler;
    }

    public <E> DetailViewNavigator<E> initNavigator(DetailViewNavigator<E> navigator) {
        if (viewClass != null) {
            navigator = navigator.withViewClass(viewClass);
        }

        if (viewId != null) {
            navigator = navigator.withViewId(viewId);
        }

        if (routeParameters != null) {
            navigator = navigator.withRouteParameters(routeParameters);
        }

        if (queryParameters != null) {
            navigator = navigator.withQueryParameters(queryParameters);
        }

        return navigator;
    }

    public <E, S extends View<?>> DetailWindowBuilder<E, S> initWindowBuilder(DetailWindowBuilder<E, S> windowBuilder) {
        if (viewClass != null) {
            windowBuilder = windowBuilder.withViewClass((Class) viewClass);
        }

        if (viewId != null) {
            windowBuilder = windowBuilder.withViewId(viewId);
        }

        if (afterCloseHandler != null) {
            windowBuilder = windowBuilder.withAfterCloseListener((Consumer) afterCloseHandler);
        }

        return windowBuilder;
    }

    public <E, S extends View<?>> LookupWindowBuilder<E, S> initWindowBuilder(LookupWindowBuilder<E, S> windowBuilder) {
        if (viewClass != null) {
            windowBuilder = windowBuilder.withViewClass((Class) viewClass);
        }

        if (viewId != null) {
            windowBuilder = windowBuilder.withViewId(viewId);
        }

        if (afterCloseHandler != null) {
            windowBuilder = windowBuilder.withAfterCloseListener((Consumer) afterCloseHandler);
        }

        return windowBuilder;
    }
}
