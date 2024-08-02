package io.jmix.fullcalendarflowui.component.data;

import com.vaadin.flow.data.provider.KeyMapper;
import elemental.json.JsonValue;
import io.jmix.fullcalendarflowui.component.serialization.IncrementalData;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class EventProviderManager extends AbstractEventProviderManager {

    protected Consumer<CalendarEventProvider.ItemSetChangeEvent> itemSetChangeListener;
    protected List<Pair<ItemChangeOperation, Collection<?>>> pendingIncrementalChanges = new ArrayList<>();

    public EventProviderManager(CalendarEventProvider eventProvider) {
        super(eventProvider, "_addItemEventSource");

        eventProvider.addItemSetChangeListener(this::onItemSetChangeListener);
    }

    @Override
    public void setCrossEventProviderKeyMapper(@Nullable KeyMapper<Object> crossEventProviderKeyMapper) {
        super.setCrossEventProviderKeyMapper(crossEventProviderKeyMapper);

        dataSerializer = createDataSerializer(sourceId, eventKeyMapper, crossEventProviderKeyMapper);
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
        if (pendingIncrementalChanges== null || pendingIncrementalChanges.isEmpty()) {
            return Collections.emptyList();
        }
        return pendingIncrementalChanges.stream()
                .map(change -> dataSerializer.serializeIncrementalData(
                        new IncrementalData(sourceId, change.getFirst(), change.getSecond())))
                .toList();
    }

    public void addIncrementalChange(ItemChangeOperation operation, Collection<?> items) {
        pendingIncrementalChanges.add(new Pair<>(operation, items));
    }

    public void clearIncrementalData() {
        pendingIncrementalChanges.clear();
    }

    @Nullable
    public Consumer<CalendarEventProvider.ItemSetChangeEvent> getItemSetChangeListener() {
        return itemSetChangeListener;
    }

    public void setItemSetChangeListener(@Nullable Consumer<CalendarEventProvider.ItemSetChangeEvent> itemSetChangeListener) {
        this.itemSetChangeListener = itemSetChangeListener;
    }

    protected void onItemSetChangeListener(CalendarEventProvider.ItemSetChangeEvent event) {
        if (itemSetChangeListener != null) {
            itemSetChangeListener.accept(event);
        }
    }

    // todo rp rework
    protected static class Pair<F, S> {
        protected F first;
        protected S second;

        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }

        public F getFirst() {
            return first;
        }

        public S getSecond() {
            return second;
        }
    }
}
