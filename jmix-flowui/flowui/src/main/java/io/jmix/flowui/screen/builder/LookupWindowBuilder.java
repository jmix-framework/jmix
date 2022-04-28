package io.jmix.flowui.screen.builder;

import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.screen.DialogWindow;
import io.jmix.flowui.screen.DialogWindow.AfterCloseEvent;
import io.jmix.flowui.screen.DialogWindow.AfterOpenEvent;
import io.jmix.flowui.screen.LookupScreen;
import io.jmix.flowui.screen.LookupScreen.ValidationContext;
import io.jmix.flowui.screen.Screen;
import com.vaadin.flow.component.HasValue;
import io.jmix.flowui.model.CollectionContainer;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class LookupWindowBuilder<E, S extends Screen> extends AbstractWindowBuilder<S> {

    protected final Class<E> entityClass;

    protected Consumer<Collection<E>> selectHandler;
    protected Predicate<ValidationContext<E>> selectValidator;
    protected Function<Collection<E>, Collection<E>> transformation;

    protected CollectionContainer<E> container;

    protected ListDataComponent<E> listDataComponent;
    protected HasValue field;

    protected boolean fieldCollectionValue = false;

    protected LookupWindowBuilder(LookupWindowBuilder<E, S> builder) {
        super(builder.origin, builder.handler);

        this.entityClass = builder.entityClass;

        this.screenId = builder.screenId;

        this.selectHandler = builder.selectHandler;
        this.selectValidator = builder.selectValidator;
        this.transformation = builder.transformation;

        this.container = builder.container;

        this.listDataComponent = builder.listDataComponent;
        this.field = builder.field;

        this.fieldCollectionValue = builder.fieldCollectionValue;

        this.afterOpenListener = builder.afterOpenListener;
        this.afterCloseListener = builder.afterCloseListener;
    }

    public LookupWindowBuilder(Screen origin,
                               Class<E> entityClass,
                               Function<? extends LookupWindowBuilder<E, S>, DialogWindow<S>> handler) {
        super(origin, handler);

        this.entityClass = entityClass;
    }

    public LookupWindowBuilder<E, S> withScreenId(@Nullable String screenId) {
        this.screenId = screenId;
        return this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends Screen & LookupScreen<E>> LookupWindowClassBuilder<E, T> withScreenClass(@Nullable Class<T> screenClass) {
        return new LookupWindowClassBuilder(this, screenClass);
    }

    public LookupWindowBuilder<E, S> withSelectHandler(Consumer<Collection<E>> selectHandler) {
        this.selectHandler = selectHandler;
        return this;
    }

    public LookupWindowBuilder<E, S> withSelectValidator(Predicate<ValidationContext<E>> selectValidator) {
        this.selectValidator = selectValidator;
        return this;
    }

    public LookupWindowBuilder<E, S> withTransformation(@Nullable Function<Collection<E>, Collection<E>> transformation) {
        this.transformation = transformation;
        return this;
    }

    public LookupWindowBuilder<E, S> withContainer(@Nullable CollectionContainer<E> container) {
        this.container = container;
        return this;
    }

    public LookupWindowBuilder<E, S> withListDataComponent(@Nullable ListDataComponent<E> listDataComponent) {
        this.listDataComponent = listDataComponent;
        return this;
    }

    public <T extends HasValue<?, E>> LookupWindowBuilder<E, S> withField(@Nullable T field) {
        this.field = field;
        this.fieldCollectionValue = false;
        return this;
    }

    public <T extends HasValue<?, Collection<E>>> LookupWindowBuilder<E, S> withValuesField(@Nullable T field) {
        this.field = field;
        this.fieldCollectionValue = true;
        return this;
    }

    public LookupWindowBuilder<E, S> withAfterOpenListener(@Nullable Consumer<AfterOpenEvent<S>> listener) {
        super.withAfterOpenListener(listener);
        return this;
    }

    public LookupWindowBuilder<E, S> withAfterCloseListener(@Nullable Consumer<AfterCloseEvent<S>> listener) {
        super.withAfterCloseListener(listener);
        return this;
    }

    public Screen getOrigin() {
        return origin;
    }

    public Class<E> getEntityClass() {
        return entityClass;
    }

    public Optional<Consumer<Collection<E>>> getSelectHandler() {
        return Optional.ofNullable(selectHandler);
    }

    public Optional<Predicate<ValidationContext<E>>> getSelectValidator() {
        return Optional.ofNullable(selectValidator);
    }

    public Optional<Function<Collection<E>, Collection<E>>> getTransformation() {
        return Optional.ofNullable(transformation);
    }

    public Optional<CollectionContainer<E>> getContainer() {
        return Optional.ofNullable(container);
    }

    public Optional<ListDataComponent<E>> getListDataComponent() {
        return Optional.ofNullable(listDataComponent);
    }

    public Optional<HasValue> getField() {
        return Optional.ofNullable(field);
    }

    public boolean isFieldCollectionValue() {
        return fieldCollectionValue;
    }
}
