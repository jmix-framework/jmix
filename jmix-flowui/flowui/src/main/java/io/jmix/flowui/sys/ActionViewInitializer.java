package io.jmix.flowui.sys;

import com.vaadin.flow.router.RouteParameters;
import io.jmix.flowui.action.ViewOpeningAction.QueryParametersProvider;
import io.jmix.flowui.action.ViewOpeningAction.RouteParametersProvider;
import io.jmix.flowui.view.DialogWindow.AfterCloseEvent;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.builder.DetailWindowBuilder;
import io.jmix.flowui.view.builder.LookupWindowBuilder;
import io.jmix.flowui.view.navigation.DetailViewNavigator;
import io.jmix.flowui.view.navigation.ViewNavigator;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ActionViewInitializer {

    protected String viewId;
    protected Class<? extends View> viewClass;
    protected RouteParametersProvider routeParametersProvider;
    protected QueryParametersProvider queryParametersProvider;
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
    public RouteParametersProvider getRouteParametersProvider() {
        return routeParametersProvider;
    }

    public void setRouteParametersProvider(@Nullable RouteParametersProvider provider) {
        this.routeParametersProvider = provider;
    }

    @Nullable
    public QueryParametersProvider getQueryParametersProvider() {
        return queryParametersProvider;
    }

    public void setQueryParametersProvider(@Nullable QueryParametersProvider provider) {
        this.queryParametersProvider = provider;
    }

    @Nullable
    public <S extends View<?>> Consumer<AfterCloseEvent<S>> getAfterCloseHandler() {
        return (Consumer) afterCloseHandler;
    }

    public <S extends View<?>> void setAfterCloseHandler(@Nullable Consumer<AfterCloseEvent<S>> afterCloseHandler) {
        this.afterCloseHandler = (Consumer) afterCloseHandler;
    }

    public ViewNavigator initNavigator(ViewNavigator navigator) {
        if (viewClass != null) {
            navigator = navigator.withViewClass(viewClass);
        }

        if (viewId != null) {
            navigator = navigator.withViewId(viewId);
        }

        if (routeParametersProvider != null) {
            navigator = navigator.withRouteParameters(routeParametersProvider.getRouteParameters());
        }

        if (queryParametersProvider != null) {
            navigator = navigator.withQueryParameters(queryParametersProvider.getQueryParameters());
        }

        return navigator;
    }

    public <E> DetailViewNavigator<E> initNavigator(DetailViewNavigator<E> navigator) {
        if (viewClass != null) {
            navigator = navigator.withViewClass(viewClass);
        }

        if (viewId != null) {
            navigator = navigator.withViewId(viewId);
        }

        if (routeParametersProvider != null) {
            navigator = navigator.withRouteParameters(routeParametersProvider.getRouteParameters());
        }

        if (queryParametersProvider != null) {
            navigator = navigator.withQueryParameters(queryParametersProvider.getQueryParameters());
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
