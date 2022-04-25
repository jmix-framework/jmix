package io.jmix.flowui.screen.builder;

import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.screen.DialogWindow;
import io.jmix.flowui.screen.DialogWindow.AfterCloseEvent;
import io.jmix.flowui.screen.DialogWindow.AfterOpenEvent;
import io.jmix.flowui.screen.EditorScreen;
import io.jmix.flowui.screen.Screen;
import com.vaadin.flow.component.HasValue;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataContext;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class EditorWindowClassBuilder<E, S extends Screen & EditorScreen<E>> extends EditorWindowBuilder<E, S>
        implements DialogWindowClassBuilder<S> {

    protected Class<S> screenClass;

    protected EditorWindowClassBuilder(EditorWindowBuilder<E, S> builder, Class<S> screenClass) {
        super(builder);

        this.screenClass = screenClass;
    }

    public EditorWindowClassBuilder(Screen origin,
                                    Class<E> entityClass,
                                    Class<S> screenClass,
                                    Function<? extends EditorWindowClassBuilder<E, S>, DialogWindow<S>> handler) {
        super(origin, entityClass, handler);

        this.screenClass = screenClass;
    }

    @Override
    public EditorWindowClassBuilder<E, S> newEntity() {
        super.newEntity();
        return this;
    }

    @Override
    public EditorWindowClassBuilder<E, S> newEntity(E entity) {
        super.newEntity(entity);
        return this;
    }

    @Override
    public EditorWindowClassBuilder<E, S> editEntity(E entity) {
        super.editEntity(entity);
        return this;
    }

    @Override
    public EditorWindowClassBuilder<E, S> withScreenId(@Nullable String screenId) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " doesn't support 'screenId'");
    }

    @Override
    public EditorWindowClassBuilder<E, S> withInitializer(@Nullable Consumer<E> initializer) {
        super.withInitializer(initializer);
        return this;
    }

    @Override
    public EditorWindowClassBuilder<E, S> withTransformation(@Nullable Function<E, E> transformation) {
        super.withTransformation(transformation);
        return this;
    }

    @Override
    public EditorWindowClassBuilder<E, S> withContainer(@Nullable CollectionContainer<E> container) {
        super.withContainer(container);
        return this;
    }

    @Override
    public EditorWindowClassBuilder<E, S> withParentDataContext(@Nullable DataContext parentDataContext) {
        super.withParentDataContext(parentDataContext);
        return this;
    }

    @Override
    public EditorWindowClassBuilder<E, S> withListDataComponent(@Nullable ListDataComponent<E> listDataComponent) {
        super.withListDataComponent(listDataComponent);
        return this;
    }

    @Override
    public EditorWindowClassBuilder<E, S> withField(@Nullable HasValue<?, E> field) {
        super.withField(field);
        return this;
    }

    @Override
    public EditorWindowClassBuilder<E, S> withAddFirst(@Nullable Boolean addFirst) {
        super.withAddFirst(addFirst);
        return this;
    }

    @Override
    public EditorWindowClassBuilder<E, S> withAfterOpenListener(@Nullable Consumer<AfterOpenEvent<S>> listener) {
        super.withAfterOpenListener(listener);
        return this;
    }

    @Override
    public EditorWindowClassBuilder<E, S> withAfterCloseListener(@Nullable Consumer<AfterCloseEvent<S>> listener) {
        super.withAfterCloseListener(listener);
        return this;
    }

    @Override
    public Optional<Class<S>> getScreenClass() {
        return Optional.of(screenClass);
    }
}
