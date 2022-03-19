/*
 * Copyright 2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.ui.component.calendar;

import io.jmix.core.common.event.EventHub;
import io.jmix.core.common.event.Subscription;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class SimpleCalendarEvent<V> implements CalendarEvent<V> {

    protected V start;
    protected V end;
    protected String caption;
    protected String description;
    protected String styleName;
    protected boolean isAllDay;

    protected EventHub events = new EventHub();

    protected void fireDataChanged() {
        events.publish(EventChangeEvent.class, new EventChangeEvent(this));
    }

    @Nullable
    @Override
    public V getStart() {
        return start;
    }

    @Override
    public void setStart(V start) {
        this.start = start;
        fireDataChanged();
    }

    @Nullable
    @Override
    public V getEnd() {
        return end;
    }

    @Override
    public void setEnd(V end) {
        this.end = end;
        fireDataChanged();
    }

    @Nullable
    @Override
    public String getCaption() {
        return caption;
    }

    @Override
    public void setCaption(String caption) {
        this.caption = caption;
        fireDataChanged();
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
        fireDataChanged();
    }

    @Nullable
    @Override
    public String getDescription() {
        return description;
    }

    @Nullable
    @Override
    public String getStyleName() {
        return styleName;
    }

    @Override
    public void setStyleName(String styleName) {
        this.styleName = styleName;
        fireDataChanged();
    }

    @Override
    public boolean isAllDay() {
        return isAllDay;
    }

    @Override
    public void setAllDay(boolean isAllDay) {
        this.isAllDay = isAllDay;
        fireDataChanged();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Subscription addEventChangeListener(Consumer<EventChangeEvent<V>> listener) {
        return events.subscribe(EventChangeEvent.class, (Consumer) listener);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void removeEventChangeListener(Consumer<EventChangeEvent<V>> listener) {
        events.unsubscribe(EventChangeEvent.class, (Consumer) listener);
    }
}
