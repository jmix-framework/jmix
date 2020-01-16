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

package io.jmix.ui.components.calendar;

import io.jmix.core.commons.events.EventHub;
import io.jmix.core.commons.events.Subscription;
import io.jmix.core.entity.Entity;
import io.jmix.core.metamodel.model.Instance;
import org.apache.commons.lang3.BooleanUtils;
import io.jmix.ui.components.data.calendar.EntityCalendarEventProvider;

import java.util.Date;
import java.util.function.Consumer;

public class EntityCalendarEvent<E extends Entity> implements CalendarEvent {

    protected final E entity;
    protected final EntityCalendarEventProvider provider;

    protected EventHub events = new EventHub();

    public EntityCalendarEvent(E entity, EntityCalendarEventProvider provider) {
        this.entity = entity;
        this.provider = provider;

        // todo bad practice, use datasource listener instead
        this.entity.addPropertyChangeListener(this::onPropertyChanged);
    }

    protected void onPropertyChanged(Instance.PropertyChangeEvent event) {
        events.publish(EventChangeEvent.class, new EventChangeEvent(this));
    }

    public E getEntity() {
        return entity;
    }

    @Override
    public Date getStart() {
        if (provider.getStartDateProperty() != null) {
            return entity.getValue(provider.getStartDateProperty());
        } else {
            return null;
        }
    }

    @Override
    public void setStart(Date start) {
        entity.setValue(provider.getStartDateProperty(), start);
    }

    @Override
    public Date getEnd() {
        if (provider.getEndDateProperty() != null) {
            return entity.getValue(provider.getEndDateProperty());
        } else {
            return null;
        }
    }

    @Override
    public void setEnd(Date end) {
        entity.setValue(provider.getEndDateProperty(), end);
    }

    @Override
    public String getCaption() {
        if (provider.getCaptionProperty() != null) {
            return entity.getValue(provider.getCaptionProperty());
        } else {
            return null;
        }
    }

    @Override
    public void setCaption(String caption) {
        entity.setValue(provider.getCaptionProperty(), caption);
    }

    @Override
    public void setDescription(String description) {
        entity.setValue(provider.getDescriptionProperty(), description);
    }

    @Override
    public String getDescription() {
        if (provider.getDescriptionProperty() != null) {
            return entity.getValue(provider.getDescriptionProperty());
        } else {
            return null;
        }
    }

    @Override
    public String getStyleName() {
        if (provider.getStyleNameProperty() != null) {
            return entity.getValue(provider.getStyleNameProperty());
        } else {
            return null;
        }
    }

    @Override
    public void setStyleName(String styleName) {
        entity.setValue(provider.getStyleNameProperty(), styleName);
    }

    @Override
    public boolean isAllDay() {
        if (provider.getIsAllDayProperty() != null) {
            return BooleanUtils.isTrue(entity.getValue(provider.getIsAllDayProperty()));
        } else {
            return false;
        }
    }

    @Override
    public void setAllDay(boolean isAllDay) {
        entity.setValue(provider.getIsAllDayProperty(), isAllDay);
    }

    @Override
    public Subscription addEventChangeListener(Consumer<EventChangeEvent> listener) {
        return events.subscribe(EventChangeEvent.class, listener);
    }

    @Override
    public void removeEventChangeListener(Consumer<EventChangeEvent> listener) {
        events.unsubscribe(EventChangeEvent.class, listener);
    }
}
