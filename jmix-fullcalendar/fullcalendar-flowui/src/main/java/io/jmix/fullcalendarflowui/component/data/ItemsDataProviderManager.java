package io.jmix.fullcalendarflowui.component.data;

import elemental.json.JsonValue;
import io.jmix.core.annotation.Internal;
import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.component.data.ItemsCalendarDataProvider.ItemSetChangeEvent;
import io.jmix.fullcalendarflowui.component.model.IncrementalData;
import io.jmix.fullcalendarflowui.component.serialization.FullCalendarSerializer;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * INTERNAL.
 * <p>
 * Data provider manager that works with {@link ItemsCalendarDataProvider}.
 */
@Internal
public class ItemsDataProviderManager extends AbstractDataProviderManager {

    protected Consumer<ItemSetChangeEvent> itemSetChangeListener;
    protected List<ItemSetChangeEvent> pendingIncrementalChanges = new ArrayList<>();

    public ItemsDataProviderManager(ItemsCalendarDataProvider dataProvider,
                                    FullCalendarSerializer serializer,
                                    FullCalendar fullCalendar) {
        super(dataProvider, serializer, fullCalendar, "_addItemEventSource");

        dataProvider.addItemSetChangeListener(this::onItemSetChangeListener);
    }

    @Override
    public ItemsCalendarDataProvider getDataProvider() {
        return (ItemsCalendarDataProvider) super.getDataProvider();
    }

    @Override
    public CalendarEvent getCalendarEvent(String clientId) {
        Object itemId = eventKeyMapper.get(clientId);
        return itemId == null ? null : getDataProvider().getItem(itemId);
    }

    public JsonValue serializeData() {
        return dataSerializer.serializeData(((ItemsCalendarDataProvider) dataProvider).getItems());
    }

    public List<JsonValue> serializeIncrementalData() {
        if (pendingIncrementalChanges == null || pendingIncrementalChanges.isEmpty()) {
            return Collections.emptyList();
        }
        return pendingIncrementalChanges.stream()
                .map(change -> dataSerializer.serializeIncrementalData(
                        new IncrementalData(sourceId, change.getOperation(), change.getItems())))
                .toList();
    }

    public void addIncrementalChange(ItemSetChangeEvent event) {
        pendingIncrementalChanges.add(event);
    }

    public void clearIncrementalData() {
        pendingIncrementalChanges.clear();
    }

    @Nullable
    public Consumer<ItemSetChangeEvent> getItemSetChangeListener() {
        return itemSetChangeListener;
    }

    public void setItemSetChangeListener(@Nullable Consumer<ItemSetChangeEvent> itemSetChangeListener) {
        this.itemSetChangeListener = itemSetChangeListener;
    }

    protected void onItemSetChangeListener(ItemSetChangeEvent event) {
        if (itemSetChangeListener != null) {
            itemSetChangeListener.accept(event);
        }
    }
}
