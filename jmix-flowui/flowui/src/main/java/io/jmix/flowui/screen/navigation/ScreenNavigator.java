package io.jmix.flowui.screen.navigation;

import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.flowui.screen.Screen;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public class ScreenNavigator {

    protected final Consumer<ScreenNavigator> handler;

    protected String screenId;
    protected Class<? extends Screen> screenClass;

    protected RouteParameters routeParameters;
    protected QueryParameters queryParameters;

    protected Class<? extends Screen> backNavigationTarget;

    public ScreenNavigator(Consumer<? extends ScreenNavigator> handler) {
        checkNotNullArgument(handler);

        this.handler = (Consumer<ScreenNavigator>) handler;
    }

    public ScreenNavigator withScreenId(@Nullable String screenId) {
        this.screenId = screenId;
        return this;
    }

    public ScreenNavigator withScreenClass(@Nullable Class<? extends Screen> screenClass) {
        this.screenClass = screenClass;
        return this;
    }

    public ScreenNavigator withRouteParameters(RouteParameters routeParameters) {
        this.routeParameters = routeParameters;
        return this;
    }

    public ScreenNavigator withQueryParameters(QueryParameters queryParameters) {
        this.queryParameters = queryParameters;
        return this;
    }

    public ScreenNavigator withBackNavigationTarget(Class<? extends Screen> backNavigationTarget) {
        this.backNavigationTarget = backNavigationTarget;
        return this;
    }

    public Optional<String> getScreenId() {
        return Optional.ofNullable(screenId);
    }

    public Optional<Class<? extends Screen>> getScreenClass() {
        return Optional.ofNullable(screenClass);
    }

    public Optional<RouteParameters> getRouteParameters() {
        return Optional.ofNullable(routeParameters);
    }

    public Optional<QueryParameters> getQueryParameters() {
        return Optional.ofNullable(queryParameters);
    }

    public Optional<Class<? extends Screen>> getBackNavigationTarget() {
        return Optional.ofNullable(backNavigationTarget);
    }

    public void navigate() {
        handler.accept(this);
    }
}
