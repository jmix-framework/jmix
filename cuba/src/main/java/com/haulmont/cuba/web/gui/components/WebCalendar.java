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

package com.haulmont.cuba.web.gui.components;

import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.impl.CollectionDsHelper;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.components.calendar.CalendarEventProvider;
import com.haulmont.cuba.web.gui.components.calendar.EntityCalendarEventProvider;
import io.jmix.ui.components.calendar.ContainerCalendarEventProvider;

import javax.annotation.Nullable;

public class WebCalendar extends io.jmix.ui.components.impl.WebCalendar {

    /**
     * Set collection datasource for the calendar component with a collection of events.
     *
     * @param datasource a datasource to set
     * @deprecated @deprecated Use {@link #setEventProvider(CalendarEventProvider)}
     * with {@link EntityCalendarEventProvider} instead
     */
    @Deprecated
    void setDatasource(CollectionDatasource datasource) {
        if (datasource == null) {
            setEventProvider(null);
        } else {
            CollectionDsHelper.autoRefreshInvalid(datasource, true);
            setEventProvider(new EntityCalendarEventProvider(datasource));
        }
    }

    /**
     * @return a datasource
     * @deprecated Use {@link #getEventProvider()} instead
     */

    @Nullable
    @Deprecated
    CollectionDatasource getDatasource() {
        return (calendarEventProvider instanceof EntityCalendarEventProvider)
                ? ((EntityCalendarEventProvider) calendarEventProvider)
                .getDatasource()
                : null;
    }

    @Nullable
    @Override
    protected MetaProperty getMetaProperty() {
        if (getEventProvider() instanceof io.jmix.ui.components.data.calendar.EntityCalendarEventProvider) {
            io.jmix.ui.components.data.calendar.EntityCalendarEventProvider eventProvider = (io.jmix.ui.components.data.calendar.EntityCalendarEventProvider) getEventProvider();
            String property = eventProvider.getStartDateProperty().isEmpty()
                    ? eventProvider.getEndDateProperty()
                    : eventProvider.getStartDateProperty();

            if (!property.isEmpty()) {
                CollectionDatasource datasource = getDatasource();
                MetaClass metaClass = datasource != null
                        ? datasource.getMetaClass()
                        : ((ContainerCalendarEventProvider) eventProvider).getContainer().getEntityMetaClass();

                return metaClass.getProperty(property);
            }
        }
        return null;
    }
}
