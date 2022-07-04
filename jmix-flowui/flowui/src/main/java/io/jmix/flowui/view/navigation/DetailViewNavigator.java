package io.jmix.flowui.view.navigation;

import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.flowui.view.DetailViewMode;
import io.jmix.flowui.view.View;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public class DetailViewNavigator<E> extends ViewNavigator {

    protected final Class<E> entityClass;

    protected E editedEntity;

    protected DetailViewMode mode = DetailViewMode.CREATE;

    public DetailViewNavigator(Class<E> entityClass, Consumer<? extends DetailViewNavigator<E>> handler) {
        super(handler);
        checkNotNullArgument(entityClass);

        this.entityClass = entityClass;
    }

    public DetailViewNavigator<E> newEntity() {
        this.mode = DetailViewMode.CREATE;
        return this;
    }

    public DetailViewNavigator<E> editEntity(E entity) {
        checkNotNullArgument(entity);

        this.editedEntity = entity;
        this.mode = DetailViewMode.EDIT;
        return this;
    }

    @Override
    public DetailViewNavigator<E> withViewId(@Nullable String viewId) {
        super.withViewId(viewId);
        return this;
    }

    @Override
    public DetailViewNavigator<E> withViewClass(@Nullable Class<? extends View> viewClass) {
        super.withViewClass(viewClass);
        return this;
    }

    @Override
    public DetailViewNavigator<E> withRouteParameters(RouteParameters routeParameters) {
        super.withRouteParameters(routeParameters);
        return this;
    }

    @Override
    public DetailViewNavigator<E> withQueryParameters(QueryParameters queryParameters) {
        super.withQueryParameters(queryParameters);
        return this;
    }

    @Override
    public DetailViewNavigator<E> withBackNavigationTarget(Class<? extends View> backNavigationTarget) {
        super.withBackNavigationTarget(backNavigationTarget);
        return this;
    }

    public Class<E> getEntityClass() {
        return entityClass;
    }

    public Optional<E> getEditedEntity() {
        return Optional.ofNullable(editedEntity);
    }

    public DetailViewMode getMode() {
        return mode;
    }
}
