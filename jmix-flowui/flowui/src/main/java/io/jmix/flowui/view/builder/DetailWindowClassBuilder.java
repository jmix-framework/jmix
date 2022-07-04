package io.jmix.flowui.view.builder;

import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.DialogWindow.AfterCloseEvent;
import io.jmix.flowui.view.DialogWindow.AfterOpenEvent;
import io.jmix.flowui.view.DetailView;
import io.jmix.flowui.view.View;
import com.vaadin.flow.component.HasValue;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataContext;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class DetailWindowClassBuilder<E, S extends View<?> & DetailView<E>> extends DetailWindowBuilder<E, S>
        implements DialogWindowClassBuilder<S> {

    protected Class<S> viewClass;

    protected DetailWindowClassBuilder(DetailWindowBuilder<E, S> builder, Class<S> viewClass) {
        super(builder);

        this.viewClass = viewClass;
    }

    public DetailWindowClassBuilder(View<?> origin,
                                    Class<E> entityClass,
                                    Class<S> viewClass,
                                    Function<? extends DetailWindowClassBuilder<E, S>, DialogWindow<S>> handler) {
        super(origin, entityClass, handler);

        this.viewClass = viewClass;
    }

    @Override
    public DetailWindowClassBuilder<E, S> newEntity() {
        super.newEntity();
        return this;
    }

    @Override
    public DetailWindowClassBuilder<E, S> newEntity(E entity) {
        super.newEntity(entity);
        return this;
    }

    @Override
    public DetailWindowClassBuilder<E, S> editEntity(E entity) {
        super.editEntity(entity);
        return this;
    }

    @Override
    public DetailWindowClassBuilder<E, S> withViewId(@Nullable String viewId) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " doesn't support 'viewId'");
    }

    @Override
    public DetailWindowClassBuilder<E, S> withInitializer(@Nullable Consumer<E> initializer) {
        super.withInitializer(initializer);
        return this;
    }

    @Override
    public DetailWindowClassBuilder<E, S> withTransformation(@Nullable Function<E, E> transformation) {
        super.withTransformation(transformation);
        return this;
    }

    @Override
    public DetailWindowClassBuilder<E, S> withContainer(@Nullable CollectionContainer<E> container) {
        super.withContainer(container);
        return this;
    }

    @Override
    public DetailWindowClassBuilder<E, S> withParentDataContext(@Nullable DataContext parentDataContext) {
        super.withParentDataContext(parentDataContext);
        return this;
    }

    @Override
    public DetailWindowClassBuilder<E, S> withListDataComponent(@Nullable ListDataComponent<E> listDataComponent) {
        super.withListDataComponent(listDataComponent);
        return this;
    }

    @Override
    public DetailWindowClassBuilder<E, S> withField(@Nullable HasValue<?, E> field) {
        super.withField(field);
        return this;
    }

    @Override
    public DetailWindowClassBuilder<E, S> withAddFirst(@Nullable Boolean addFirst) {
        super.withAddFirst(addFirst);
        return this;
    }

    @Override
    public DetailWindowClassBuilder<E, S> withAfterOpenListener(@Nullable Consumer<AfterOpenEvent<S>> listener) {
        super.withAfterOpenListener(listener);
        return this;
    }

    @Override
    public DetailWindowClassBuilder<E, S> withAfterCloseListener(@Nullable Consumer<AfterCloseEvent<S>> listener) {
        super.withAfterCloseListener(listener);
        return this;
    }

    @Override
    public Optional<Class<S>> getViewClass() {
        return Optional.of(viewClass);
    }
}
