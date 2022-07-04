package io.jmix.flowui.view.navigation;

import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.flowui.view.View;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public class ListViewNavigator<E> extends ViewNavigator {

    protected final Class<E> entityClass;

    public ListViewNavigator(Class<E> entityClass,
                             Consumer<? extends ListViewNavigator<E>> handler) {
        super(handler);
        checkNotNullArgument(entityClass);

        this.entityClass = entityClass;
    }

    @Override
    public ListViewNavigator<E> withViewId(@Nullable String viewId) {
        super.withViewId(viewId);
        return this;
    }

    @Override
    public ListViewNavigator<E> withViewClass(@Nullable Class<? extends View> viewClass) {
        super.withViewClass(viewClass);
        return this;
    }

    @Override
    public ListViewNavigator<E> withRouteParameters(RouteParameters routeParameters) {
        super.withRouteParameters(routeParameters);
        return this;
    }

    @Override
    public ListViewNavigator<E> withQueryParameters(QueryParameters queryParameters) {
        super.withQueryParameters(queryParameters);
        return this;
    }

    @Override
    public ListViewNavigator<E> withBackNavigationTarget(Class<? extends View> backNavigationTarget) {
        super.withBackNavigationTarget(backNavigationTarget);
        return this;
    }

    public Class<E> getEntityClass() {
        return entityClass;
    }
}
