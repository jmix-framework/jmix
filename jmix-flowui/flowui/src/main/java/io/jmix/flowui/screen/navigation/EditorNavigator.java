package io.jmix.flowui.screen.navigation;

import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.flowui.screen.EditMode;
import io.jmix.flowui.screen.Screen;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public class EditorNavigator<E> extends ScreenNavigator {

    protected final Class<E> entityClass;

    protected E editedEntity;

    protected EditMode mode = EditMode.CREATE;

    public EditorNavigator(Class<E> entityClass, Consumer<? extends EditorNavigator<E>> handler) {
        super(handler);
        checkNotNullArgument(entityClass);

        this.entityClass = entityClass;
    }

    public EditorNavigator<E> newEntity() {
        this.mode = EditMode.CREATE;
        return this;
    }

    public EditorNavigator<E> editEntity(E entity) {
        checkNotNullArgument(entity);

        this.editedEntity = entity;
        this.mode = EditMode.EDIT;
        return this;
    }

    @Override
    public EditorNavigator<E> withScreenId(@Nullable String screenId) {
        super.withScreenId(screenId);
        return this;
    }

    @Override
    public EditorNavigator<E> withScreenClass(@Nullable Class<? extends Screen> screenClass) {
        super.withScreenClass(screenClass);
        return this;
    }

    @Override
    public EditorNavigator<E> withRouteParameters(RouteParameters routeParameters) {
        super.withRouteParameters(routeParameters);
        return this;
    }

    @Override
    public EditorNavigator<E> withQueryParameters(QueryParameters queryParameters) {
        super.withQueryParameters(queryParameters);
        return this;
    }

    @Override
    public EditorNavigator<E> withBackNavigationTarget(Class<? extends Screen> backNavigationTarget) {
        super.withBackNavigationTarget(backNavigationTarget);
        return this;
    }

    public Class<E> getEntityClass() {
        return entityClass;
    }

    public Optional<E> getEditedEntity() {
        return Optional.ofNullable(editedEntity);
    }

    public EditMode getMode() {
        return mode;
    }
}
