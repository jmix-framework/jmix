package io.jmix.fullcalendarflowui.component.data;

import com.google.common.base.Strings;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.flowui.data.BindingState;
import io.jmix.flowui.data.EntityDataUnit;
import io.jmix.flowui.kit.event.EventBus;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.InstanceContainer;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ContainerCalendarEventProvider<E> extends AbstractEntityEventProvider<Void> implements EntityDataUnit,
        CalendarEventProvider {

    protected Set<CalendarEvent> itemsCache;

    protected InstanceContainer<E> container;

    private EventBus eventBus;

    public ContainerCalendarEventProvider(InstanceContainer<E> container) {
        this(EventProviderUtils.generateId(), container);
    }

    public ContainerCalendarEventProvider(String id, InstanceContainer<E> container) {
        super(id);

        Preconditions.checkNotNullArgument(container);

        this.container = container;
        initContainer(container);
    }

    protected void initContainer(InstanceContainer<E> container) {
        container.addItemChangeListener(this::containerItemChanged);
        container.addItemPropertyChangeListener(this::containerItemPropertyChanged);

        if (container instanceof CollectionContainer<E> collectionContainer) {
            collectionContainer.addCollectionChangeListener(this::containerCollectionChanged);
        }
    }

    protected void containerItemChanged(InstanceContainer.ItemChangeEvent<E> event) {
        if (!(container instanceof CollectionContainer)) {
            refreshCache();
            fireItemSetChangeEvent(DataChangeOperation.REFRESH, getItems());
        }
    }

    @SuppressWarnings({"unchecked"})
    protected void containerCollectionChanged(CollectionContainer.CollectionChangeEvent<E> event) {
        switch (event.getChangeType()) {
            case ADD_ITEMS -> {
                List<CalendarEvent> addedItems = addToCache((Collection<E>) event.getChanges());
                fireItemSetChangeEvent(DataChangeOperation.ADD, addedItems);
            }
            case REMOVE_ITEMS -> {
                List<CalendarEvent> removedItems = removeFromCache((Collection<E>) event.getChanges());
                fireItemSetChangeEvent(DataChangeOperation.REMOVE, removedItems);
            }
            case REFRESH -> {
                refreshCache();
                fireItemSetChangeEvent(DataChangeOperation.REFRESH, getItems());
            }
        }
    }

    protected void containerItemPropertyChanged(CollectionContainer.ItemPropertyChangeEvent<E> event) {
        if (isEventPropertyChanged(event.getProperty())) {
            replaceInCache(event.getItem());

            Object itemId = EntityValues.getId(event.getItem());
            if (itemId == null || getItem(itemId) == null) {
                // Should not happen. If item is not in event collection - skip.
                return;
            }

            fireItemSetChangeEvent(DataChangeOperation.UPDATE, Collections.singletonList(getItem(itemId)));
        }
    }

    public InstanceContainer<E> getContainer() {
        return container;
    }

    @Override
    public MetaClass getEntityMetaClass() {
        return container.getEntityMetaClass();
    }

    @Override
    public BindingState getState() {
        return BindingState.ACTIVE;
    }

    @Override
    public Registration addStateChangeListener(Consumer<StateChangeEvent> listener) {
        return getEventBus().addListener(StateChangeEvent.class, listener);
    }

    @Override
    public boolean isInMemory() {
        return false;
    }

    @Override
    public int size(Query<CalendarEvent, Void> query) {
        if (getState() == BindingState.INACTIVE) {
            return 0;
        }
        return Math.toIntExact(fetch(query).count());
    }

    @Override
    public Stream<CalendarEvent> fetch(Query<CalendarEvent, Void> query) {
        if (getState() == BindingState.INACTIVE) {
            return Stream.empty();
        }

        return getItems().stream()
                .skip(query.getOffset())
                .limit(query.getLimit());
    }

    @Override
    public List<CalendarEvent> getItems() {
        if (itemsCache == null) {
            itemsCache = new HashSet<>();
            for (E item : getContainerItems()) {
                itemsCache.add(new EntityCalendarEvent<>(item, this));
            }
        }
        return itemsCache.stream().toList();
    }

    @Nullable
    @Override
    public CalendarEvent getItem(Object itemId) {
        return getItems().stream()
                .filter(e -> e.getId().equals(itemId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Registration addItemSetChangeListener(Consumer<ItemSetChangeEvent> listener) {
        return getEventBus().addListener(ItemSetChangeEvent.class, listener);
    }

    @Override
    public Class<?> getStartPropertyJavaType() {
        if (Strings.isNullOrEmpty(getStartDateTimeProperty())) {
            return null;
        }
        MetaProperty property = getEntityMetaClass().getProperty(getStartDateTimeProperty());
        return property.getJavaType();
    }

    @Override
    public Class<?> getEndPropertyJavaType() {
        if (Strings.isNullOrEmpty(getEndDateTimeProperty())) {
            return null;
        }
        MetaProperty property = getEntityMetaClass().getProperty(getEndDateTimeProperty());
        return property.getJavaType();
    }

    protected EventBus getEventBus() {
        if (eventBus == null) {
            eventBus = new EventBus();
        }

        return eventBus;
    }

    protected void replaceInCache(E item) {
        if (itemsCache == null) {
            refreshCache();
        } else {
            itemsCache.add(new EntityCalendarEvent<>(item, this));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected List<CalendarEvent> addToCache(Collection<E> items) {
        List<EntityCalendarEvent<E>> addedItems = items.stream()
                .map(item -> new EntityCalendarEvent<>(item, this))
                .toList();
        if (itemsCache == null) {
            refreshCache();
        } else {
            itemsCache.addAll(addedItems);
        }
        return (List) addedItems;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected List<CalendarEvent> removeFromCache(Collection<E> items) {
        List<EntityCalendarEvent<E>> removedItems = items.stream()
                .map(item -> new EntityCalendarEvent<>(item, this))
                .toList();
        if (itemsCache == null) {
            refreshCache();
        } else {
            removedItems.forEach(itemsCache::remove);
        }
        return (List) removedItems;
    }

    protected void refreshCache() {
        if (itemsCache != null) {
            itemsCache.clear();
        }
        itemsCache = new HashSet<>();
        for (E item : getContainerItems()) {
            itemsCache.add(new EntityCalendarEvent<>(item, this));
        }
    }

    protected List<E> getContainerItems() {
        if (container instanceof CollectionContainer<E> collectionContainer) {
            return collectionContainer.getItems();
        } else {
            E item = container.getItemOrNull();
            return item != null
                    ? Collections.singletonList(item)
                    : Collections.emptyList();
        }
    }

    protected void fireItemSetChangeEvent(DataChangeOperation operation, List<CalendarEvent> items) {
        getEventBus().fireEvent(new ItemSetChangeEvent(this, operation, items));
    }
}
