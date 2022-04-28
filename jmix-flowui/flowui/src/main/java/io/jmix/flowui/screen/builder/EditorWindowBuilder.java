package io.jmix.flowui.screen.builder;

import com.vaadin.flow.component.HasValue;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.screen.DialogWindow;
import io.jmix.flowui.screen.DialogWindow.AfterCloseEvent;
import io.jmix.flowui.screen.DialogWindow.AfterOpenEvent;
import io.jmix.flowui.screen.EditMode;
import io.jmix.flowui.screen.EditorScreen;
import io.jmix.flowui.screen.Screen;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class EditorWindowBuilder<E, S extends Screen> extends AbstractWindowBuilder<S> {

    protected final Class<E> entityClass;

    protected E newEntity;
    protected E editedEntity;

    protected Consumer<E> initializer;
    protected Function<E, E> transformation;
    protected CollectionContainer<E> container;

    protected DataContext parentDataContext;

    protected ListDataComponent<E> listDataComponent;
    protected HasValue<?, E> field;

    protected Boolean addFirst;

    protected EditMode mode = EditMode.CREATE;

    protected EditorWindowBuilder(EditorWindowBuilder<E, S> builder) {
        super(builder.origin, builder.handler);

        this.entityClass = builder.entityClass;

        this.screenId = builder.screenId;

        this.newEntity = builder.newEntity;
        this.editedEntity = builder.editedEntity;
        this.mode = builder.mode;

        this.initializer = builder.initializer;
        this.transformation = builder.transformation;
        this.container = builder.container;
        this.parentDataContext = builder.parentDataContext;

        this.listDataComponent = builder.listDataComponent;
        this.field = builder.field;

        this.addFirst = builder.addFirst;

        this.afterOpenListener = builder.afterOpenListener;
        this.afterCloseListener = builder.afterCloseListener;
    }

    public EditorWindowBuilder(Screen origin,
                               Class<E> entityClass,
                               Function<? extends EditorWindowBuilder<E, S>, DialogWindow<S>> handler) {
        super(origin, handler);

        this.entityClass = entityClass;
    }

    public EditorWindowBuilder<E, S> newEntity() {
        this.mode = EditMode.CREATE;
        return this;
    }

    public EditorWindowBuilder<E, S> newEntity(E entity) {
        this.newEntity = entity;
        this.mode = EditMode.CREATE;
        return this;
    }

    public EditorWindowBuilder<E, S> editEntity(E entity) {
        this.editedEntity = entity;
        this.mode = EditMode.EDIT;
        return this;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T extends Screen & EditorScreen<E>> EditorWindowClassBuilder<E, T> withScreenClass(Class<T> screenClass) {
        return new EditorWindowClassBuilder(this, screenClass);
    }

    public EditorWindowBuilder<E, S> withScreenId(@Nullable String screenId) {
        this.screenId = screenId;
        return this;
    }

    public EditorWindowBuilder<E, S> withInitializer(@Nullable Consumer<E> initializer) {
        this.initializer = initializer;
        return this;
    }

    public EditorWindowBuilder<E, S> withTransformation(@Nullable Function<E, E> transformation) {
        this.transformation = transformation;
        return this;
    }

    public EditorWindowBuilder<E, S> withContainer(@Nullable CollectionContainer<E> container) {
        this.container = container;
        return this;
    }

    public EditorWindowBuilder<E, S> withParentDataContext(@Nullable DataContext parentDataContext) {
        this.parentDataContext = parentDataContext;
        return this;
    }

    public EditorWindowBuilder<E, S> withListDataComponent(@Nullable ListDataComponent<E> listDataComponent) {
        this.listDataComponent = listDataComponent;
        return this;
    }

    public EditorWindowBuilder<E, S> withField(@Nullable HasValue<?, E> field) {
        this.field = field;
        return this;
    }

    public EditorWindowBuilder<E, S> withAddFirst(@Nullable Boolean addFirst) {
        this.addFirst = addFirst;
        return this;
    }

    @Override
    public EditorWindowBuilder<E, S> withAfterOpenListener(@Nullable Consumer<AfterOpenEvent<S>> listener) {
        super.withAfterOpenListener(listener);
        return this;
    }

    @Override
    public EditorWindowBuilder<E, S> withAfterCloseListener(@Nullable Consumer<AfterCloseEvent<S>> listener) {
        super.withAfterCloseListener(listener);
        return this;
    }

    public Class<E> getEntityClass() {
        return entityClass;
    }

    public Optional<E> getNewEntity() {
        return Optional.ofNullable(newEntity);
    }

    public Optional<E> getEditedEntity() {
        return Optional.ofNullable(editedEntity);
    }

    public Optional<Consumer<E>> getInitializer() {
        return Optional.ofNullable(initializer);
    }

    public Optional<Function<E, E>> getTransformation() {
        return Optional.ofNullable(transformation);
    }

    public Optional<CollectionContainer<E>> getContainer() {
        return Optional.ofNullable(container);
    }

    public Optional<DataContext> getParentDataContext() {
        return Optional.ofNullable(parentDataContext);
    }

    public Optional<ListDataComponent<E>> getListDataComponent() {
        return Optional.ofNullable(listDataComponent);
    }

    public Optional<HasValue<?, E>> getField() {
        return Optional.ofNullable(field);
    }

    @Nullable
    public Boolean getAddFirst() {
        return addFirst;
    }

    public EditMode getMode() {
        return mode;
    }
}
