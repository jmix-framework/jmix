package io.jmix.flowui.screen.navigation;

import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.flowui.screen.Screen;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public class BrowserNavigator<E> extends ScreenNavigator {

    protected final Class<E> entityClass;

    public BrowserNavigator(Class<E> entityClass,
                            Consumer<? extends BrowserNavigator<E>> handler) {
        super(handler);
        checkNotNullArgument(entityClass);

        this.entityClass = entityClass;
    }

    @Override
    public BrowserNavigator<E> withScreenId(@Nullable String screenId) {
        super.withScreenId(screenId);
        return this;
    }

    @Override
    public BrowserNavigator<E> withScreenClass(@Nullable Class<? extends Screen> screenClass) {
        super.withScreenClass(screenClass);
        return this;
    }

    @Override
    public BrowserNavigator<E> withRouteParameters(RouteParameters routeParameters) {
        super.withRouteParameters(routeParameters);
        return this;
    }

    @Override
    public BrowserNavigator<E> withQueryParameters(QueryParameters queryParameters) {
        super.withQueryParameters(queryParameters);
        return this;
    }

    @Override
    public BrowserNavigator<E> withBackNavigationTarget(Class<? extends Screen> backNavigationTarget) {
        super.withBackNavigationTarget(backNavigationTarget);
        return this;
    }

    public Class<E> getEntityClass() {
        return entityClass;
    }
}
