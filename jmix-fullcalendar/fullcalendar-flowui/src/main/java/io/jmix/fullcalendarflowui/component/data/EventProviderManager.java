package io.jmix.fullcalendarflowui.component.data;

import elemental.json.JsonValue;
import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.component.data.CalendarEventProvider.ItemSetChangeEvent;
import io.jmix.fullcalendarflowui.component.serialization.IncrementalData;
import io.jmix.fullcalendarflowui.component.serialization.serializer.FullCalendarSerializer;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class EventProviderManager extends AbstractEventProviderManager {

    protected Consumer<ItemSetChangeEvent> itemSetChangeListener;
    protected List<ItemSetChangeEvent> pendingIncrementalChanges = new ArrayList<>();

    public EventProviderManager(CalendarEventProvider eventProvider,
                                FullCalendarSerializer serializer,
                                FullCalendar fullCalendar) {
        super(eventProvider, serializer, fullCalendar, "_addItemEventSource");

        eventProvider.addItemSetChangeListener(this::onItemSetChangeListener);
    }

    @Override
    public CalendarEventProvider getEventProvider() {
        return (CalendarEventProvider) super.getEventProvider();
    }

    @Override
    public CalendarEvent getCalendarEvent(String clientId) {
        Object itemId = eventKeyMapper.get(clientId);
        return itemId == null ? null : getEventProvider().getItem(itemId);
    }

    public JsonValue serializeData() {
        return dataSerializer.serializeData(((CalendarEventProvider) eventProvider).getItems());
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
