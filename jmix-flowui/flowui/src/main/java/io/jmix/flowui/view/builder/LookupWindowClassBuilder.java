package io.jmix.flowui.view.builder;

import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.DialogWindow.AfterCloseEvent;
import io.jmix.flowui.view.DialogWindow.AfterOpenEvent;
import io.jmix.flowui.view.LookupView;
import io.jmix.flowui.view.View;
import com.vaadin.flow.component.HasValue;
import io.jmix.flowui.model.CollectionContainer;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class LookupWindowClassBuilder<E, S extends View<?> & LookupView<E>> extends LookupWindowBuilder<E, S>
        implements DialogWindowClassBuilder<S> {

    protected Class<S> viewClass;

    protected LookupWindowClassBuilder(LookupWindowBuilder<E, S> builder, Class<S> viewClass) {
        super(builder);

        this.viewClass = viewClass;
    }

    public LookupWindowClassBuilder(View<?> origin,
                                    Class<E> entityClass,
                                    Class<S> viewClass,
                                    Function<? extends LookupWindowClassBuilder<E, S>, DialogWindow<S>> handler) {
        super(origin, entityClass, handler);

        this.viewClass = viewClass;
    }

    @Override
    public LookupWindowClassBuilder<E, S> withViewId(@Nullable String viewId) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " doesn't support 'viewId'");
    }

    @Override
    public LookupWindowClassBuilder<E, S> withSelectHandler(Consumer<Collection<E>> selectHandler) {
        super.withSelectHandler(selectHandler);
        return this;
    }

    @Override
    public LookupWindowClassBuilder<E, S> withSelectValidator(Predicate<LookupView.ValidationContext<E>> selectValidator) {
        super.withSelectValidator(selectValidator);
        return this;
    }

    @Override
    public LookupWindowClassBuilder<E, S> withTransformation(@Nullable Function<Collection<E>, Collection<E>> transformation) {
        super.withTransformation(transformation);
        return this;
    }

    @Override
    public LookupWindowClassBuilder<E, S> withContainer(@Nullable CollectionContainer<E> container) {
        super.withContainer(container);
        return this;
    }

    @Override
    public LookupWindowClassBuilder<E, S> withListDataComponent(@Nullable ListDataComponent<E> listDataComponent) {
        super.withListDataComponent(listDataComponent);
        return this;
    }

    @Override
    public <T extends HasValue<?, E>> LookupWindowClassBuilder<E, S> withField(@Nullable T field) {
        super.withField(field);
        return this;
    }

    @Override
    public <T extends HasValue<?, Collection<E>>> LookupWindowClassBuilder<E, S> withValuesField(@Nullable T field) {
        super.withValuesField(field);
        return this;
    }

    @Override
    public LookupWindowClassBuilder<E, S> withAfterOpenListener(@Nullable Consumer<AfterOpenEvent<S>> listener) {
        super.withAfterOpenListener(listener);
        return this;
    }

    @Override
    public LookupWindowClassBuilder<E, S> withAfterCloseListener(@Nullable Consumer<AfterCloseEvent<S>> listener) {
        super.withAfterCloseListener(listener);
        return this;
    }

    @Override
    public Optional<Class<S>> getViewClass() {
        return Optional.ofNullable(viewClass);
    }
}
