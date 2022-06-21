package io.jmix.flowui.component.grid;

import com.vaadin.flow.data.provider.AbstractDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.data.binding.JmixBinding;
import io.jmix.flowui.data.BindingState;
import io.jmix.flowui.data.ContainerDataUnit;
import io.jmix.flowui.data.grid.EntityGridDataItems;
import io.jmix.flowui.kit.event.EventBus;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.InstanceContainer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public abstract class AbstractContainerGridDataProvider<T> extends AbstractDataProvider<T, Void>
        implements ContainerDataUnit<T>, EntityGridDataItems<T>, JmixBinding {

    protected CollectionContainer<T> container;

    protected Subscription containerItemChangeSubscription;
    protected Subscription containerItemPropertyChangeSubscription;
    protected Subscription containerCollectionChangeSubscription;

    protected List<Consumer<SelectedItemChangeEvent<T>>> selectedItemChangeListeners = new ArrayList<>();
    protected List<Consumer<ValueChangeEvent<T>>> valueChangeListeners = new ArrayList<>();
    protected List<Consumer<ItemSetChangeEvent<T>>> itemSetChangeListeners = new ArrayList<>();

    protected EventBus eventBus = new EventBus();

    protected BindingState state = BindingState.INACTIVE;

    public AbstractContainerGridDataProvider(CollectionContainer<T> container) {
        Preconditions.checkNotNullArgument(container);

        this.container = container;
    }

    protected void onContainerItemChanged(InstanceContainer.ItemChangeEvent<T> itemChangeEvent) {
        if (!selectedItemChangeListeners.isEmpty()) {
            SelectedItemChangeEvent<T> event =
                    new SelectedItemChangeEvent<>(this, itemChangeEvent.getItem());
            selectedItemChangeListeners.forEach(listener -> listener.accept(event));
        }
    }

    protected void onContainerCollectionChanged(CollectionContainer.CollectionChangeEvent<T> collectionChangeEvent) {
        refreshAll();

        if (!itemSetChangeListeners.isEmpty()) {
            ItemSetChangeEvent<T> event = new ItemSetChangeEvent<>(this);
            itemSetChangeListeners.forEach(listener -> listener.accept(event));
        }
    }

    protected void containerItemPropertyChanged(InstanceContainer.ItemPropertyChangeEvent<T> event) {
        refreshItem(event.getItem());

        if (!valueChangeListeners.isEmpty()) {
            ValueChangeEvent<T> valueChangeEvent = new ValueChangeEvent<>(
                    this, event.getItem(), event.getProperty(), event.getPrevValue(), event.getValue());
            valueChangeListeners.forEach(listener -> listener.accept(valueChangeEvent));
        }
    }

    @Override
    public void bind() {
        containerItemChangeSubscription =
                this.container.addItemChangeListener(this::onContainerItemChanged);
        containerCollectionChangeSubscription =
                this.container.addCollectionChangeListener(this::onContainerCollectionChanged);
        containerItemPropertyChangeSubscription =
                this.container.addItemPropertyChangeListener(this::containerItemPropertyChanged);

        setState(BindingState.ACTIVE);
    }

    @Override
    public void unbind() {
        if (containerItemChangeSubscription != null) {
            containerItemChangeSubscription.remove();
            containerItemChangeSubscription = null;
        }
        if (containerCollectionChangeSubscription != null) {
            containerCollectionChangeSubscription.remove();
            containerCollectionChangeSubscription = null;
        }
        if (containerItemPropertyChangeSubscription != null) {
            containerItemPropertyChangeSubscription.remove();
            containerItemPropertyChangeSubscription = null;
        }

        setState(BindingState.INACTIVE);
    }

    @Override
    public Registration addItemSetChangeListener(Consumer<ItemSetChangeEvent<T>> listener) {
        return Registration.addAndRemove(itemSetChangeListeners, listener);
    }

    @Override
    public Registration addValueChangeListener(Consumer<ValueChangeEvent<T>> listener) {
        return Registration.addAndRemove(valueChangeListeners, listener);
    }

    @Override
    public Registration addSelectedItemChangeListener(Consumer<SelectedItemChangeEvent<T>> listener) {
        return Registration.addAndRemove(selectedItemChangeListeners, listener);
    }

    @Override
    public boolean isInMemory() {
        return true;
    }

    @Override
    public int size(Query<T, Void> query) {
        return size();
    }

    @Override
    public Stream<T> fetch(Query<T, Void> query) {
        return getItems()
                .skip(query.getOffset())
                .limit(query.getLimit());
    }

    @Override
    public BindingState getState() {
        return state;
    }

    protected void setState(BindingState state) {
        if (this.state != state) {
            this.state = state;

            eventBus.fireEvent(new StateChangeEvent(this, state));
        }
    }

    @Override
    public Registration addStateChangeListener(Consumer<StateChangeEvent> listener) {
        return eventBus.addListener(StateChangeEvent.class, listener);
    }

    @Override
    public MetaClass getEntityMetaClass() {
        return container.getEntityMetaClass();
    }

    @Override
    public CollectionContainer<T> getContainer() {
        return container;
    }

    @Nullable
    @Override
    public Object getItemId(T item) {
        return EntityValues.getId(item);
    }

    @Nullable
    @Override
    public T getItem(@Nullable Object itemId) {
        return itemId == null ? null : container.getItemOrNull(itemId);
    }

    @Nullable
    @Override
    public Object getItemValue(Object itemId, MetaPropertyPath propertyId) {
        return EntityValues.getValueEx(container.getItem(itemId), propertyId);
    }

    @Override
    public int indexOfItem(T item) {
        return container.getItemIndex(EntityValues.getId(item));
    }

    @Nullable
    @Override
    public T getItemByIndex(int index) {
        return container.getItems().get(index);
    }

    @Override
    public Stream<T> getItems() {
        return container.getItems().stream();
    }

    @Override
    public List<T> getItems(int startIndex, int numberOfItems) {
        return container.getItems().subList(startIndex, startIndex + numberOfItems);
    }

    @Override
    public boolean containsItem(T item) {
        return container.containsItem(item);
    }

    @Override
    public int size() {
        return container.getItems().size();
    }

    @Nullable
    @Override
    public T getSelectedItem() {
        return container.getItemOrNull();
    }

    @Override
    public void setSelectedItem(@Nullable T item) {
        container.setItem(item);
    }

    @Override
    public Class<T> getType() {
        return getEntityMetaClass().getJavaClass();
    }
}
