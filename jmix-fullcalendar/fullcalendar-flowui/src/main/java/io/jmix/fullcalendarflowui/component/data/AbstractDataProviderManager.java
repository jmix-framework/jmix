/*
 * Copyright 2024 Haulmont.
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

package io.jmix.fullcalendarflowui.component.data;

import com.vaadin.flow.data.provider.KeyMapper;
import io.jmix.core.annotation.Internal;
import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.component.serialization.FullCalendarSerializer;
import io.jmix.fullcalendarflowui.component.serialization.FullCalendarSerializer.FullCalendarDataSerializer;
import org.springframework.lang.Nullable;

import java.util.Objects;

/**
 * INTERNAL.
 * <p>
 * The manager of concrete data provider. It is a connector between event source in client-side and server's
 * {@link CalendarDataProvider}.
 */
@Internal
public abstract class AbstractDataProviderManager {

    protected final CalendarDataProvider dataProvider;
    protected final String sourceId;

    protected final String jsFunctionName;
    protected final KeyMapper<Object> eventKeyMapper = new KeyMapper<>();

    protected FullCalendarDataSerializer dataSerializer;

    public AbstractDataProviderManager(CalendarDataProvider dataProvider,
                                       FullCalendarSerializer serializer,
                                       FullCalendar fullCalendar,
                                       String jsFunctionName) {
        this.dataProvider = dataProvider;
        this.jsFunctionName = jsFunctionName;

        this.sourceId = generateSourceId(dataProvider);

        this.dataSerializer = serializer.createDataSerializer(sourceId, eventKeyMapper);
        this.dataSerializer.setTimeZoneSupplier(fullCalendar::getTimeZone);
    }

    /**
     * @return data provider
     */
    public CalendarDataProvider getDataProvider() {
        return dataProvider;
    }

    /**
     * @return data provider's ID that is used in client-side
     */
    public String getSourceId() {
        return sourceId;
    }

    /**
     * @return a JS function that should be invoked to add data provider to component at the client-side
     */
    public String getJsFunctionName() {
        return this.jsFunctionName;
    }

    /**
     * @param clientId ID of event from client-side
     * @return calendar event or {@code null} if there is no event with the provided ID
     */
    @Nullable
    public abstract CalendarEvent getCalendarEvent(String clientId);

    protected String generateSourceId(CalendarDataProvider dataProvider) {
        return dataProvider.getId() + "-" + DataProviderUtils.generateId();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof AbstractDataProviderManager mngr
                && mngr.sourceId.equals(this.sourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(sourceId);
    }
}
