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
import io.jmix.core.entity.EntityPropertyChangeEvent;
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.core.entity.EntityValues;
import io.jmix.ui.component.data.calendar.EntityCalendarEventProvider;
import org.apache.commons.lang3.BooleanUtils;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class EntityCalendarEvent<E, V> implements CalendarEvent<V> {

    protected final E entity;
    protected final EntityCalendarEventProvider provider;

    protected EventHub events = new EventHub();

    public EntityCalendarEvent(E entity, EntityCalendarEventProvider provider) {
        this.entity = entity;
        this.provider = provider;

        // todo bad practice, use datasource listener instead
        EntitySystemAccess.addPropertyChangeListener(this.entity, this::onPropertyChanged);
    }

    protected void onPropertyChanged(EntityPropertyChangeEvent event) {
        events.publish(EventChangeEvent.class, new EventChangeEvent<>(this));
    }

    public E getEntity() {
        return entity;
    }

    @Nullable
    @Override
    public V getStart() {
        if (provider.getStartDateProperty() != null) {
            return EntityValues.getValue(entity, provider.getStartDateProperty());
        } else {
            return null;
        }
    }

    @Override
    public void setStart(V start) {
        if (provider.getStartDateProperty() != null) {
            EntityValues.setValue(entity, provider.getStartDateProperty(), start);
        }
    }

    @Nullable
    @Override
    public V getEnd() {
        if (provider.getEndDateProperty() != null) {
            return EntityValues.getValue(entity, provider.getEndDateProperty());
        } else {
            return null;
        }
    }

    @Override
    public void setEnd(V end) {
        if (provider.getEndDateProperty() != null) {
            EntityValues.setValue(entity, provider.getEndDateProperty(), end);
        }
    }

    @Nullable
    @Override
    public String getCaption() {
        if (provider.getCaptionProperty() != null) {
            return EntityValues.getValue(entity, provider.getCaptionProperty());
        } else {
            return null;
        }
    }

    @Override
    public void setCaption(String caption) {
        if (provider.getCaptionProperty() != null) {
            EntityValues.setValue(entity, provider.getCaptionProperty(), caption);
        }
    }

    @Override
    public void setDescription(String description) {
        if (provider.getDescriptionProperty() != null) {
            EntityValues.setValue(entity, provider.getDescriptionProperty(), description);
        }
    }

    @Nullable
    @Override
    public String getDescription() {
        if (provider.getDescriptionProperty() != null) {
            return EntityValues.getValue(entity, provider.getDescriptionProperty());
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public String getStyleName() {
        if (provider.getStyleNameProperty() != null) {
            return EntityValues.getValue(entity, provider.getStyleNameProperty());
        } else {
            return null;
        }
    }

    @Override
    public void setStyleName(String styleName) {
        if (provider.getStyleNameProperty() != null) {
            EntityValues.setValue(entity, provider.getStyleNameProperty(), styleName);
        }
    }

    @Override
    public boolean isAllDay() {
        if (provider.getIsAllDayProperty() != null) {
            return BooleanUtils.isTrue(EntityValues.getValue(entity, provider.getIsAllDayProperty()));
        } else {
            return false;
        }
    }

    @Override
    public void setAllDay(boolean isAllDay) {
        if (provider.getIsAllDayProperty() != null) {
            EntityValues.setValue(entity, provider.getIsAllDayProperty(), isAllDay);
        }
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
