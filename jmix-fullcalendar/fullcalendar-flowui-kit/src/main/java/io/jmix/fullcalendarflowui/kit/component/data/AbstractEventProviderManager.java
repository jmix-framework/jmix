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

package io.jmix.fullcalendarflowui.kit.component.data;

import com.vaadin.flow.data.provider.KeyMapper;
import io.jmix.fullcalendarflowui.kit.component.serialization.serializer.JmixEventProviderDataSerializer;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.RandomStringUtils;

public abstract class AbstractEventProviderManager {

    protected final String sourceId;
    protected final String jsFunctionName;
    protected final CalendarEventProvider eventProvider;
    protected final KeyMapper<Object> keyMapper = new KeyMapper<>();

    protected KeyMapper<Object> crossEventProviderKeyMapper;
    protected JmixEventProviderDataSerializer dataSerializer;

    public AbstractEventProviderManager(CalendarEventProvider eventProvider, String jsFunctionName) {
        this.eventProvider = eventProvider;

        this.jsFunctionName = jsFunctionName;
        this.sourceId = generateSourceId(eventProvider);
        this.dataSerializer = createDataSerializer(keyMapper, sourceId, crossEventProviderKeyMapper);
    }

    public CalendarEventProvider getEventProvider() {
        return eventProvider;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getJsFunctionName() {
        return this.jsFunctionName;
    }

    @Nullable
    public KeyMapper<Object> getCrossEventProviderKeyMapper() {
        return crossEventProviderKeyMapper;
    }

    public void setCrossEventProviderKeyMapper(@Nullable KeyMapper<Object> crossEventProviderKeyMapper) {
        this.crossEventProviderKeyMapper = crossEventProviderKeyMapper;
    }

    @Nullable
    public abstract CalendarEvent getCalendarEvent(String clientId);

    protected String generateSourceId(CalendarEventProvider eventProvider) {
        return eventProvider.getId() + "-" + RandomStringUtils.randomAlphabetic(5);
    }

    protected JmixEventProviderDataSerializer createDataSerializer(KeyMapper<Object> keyMapper, String sourceId,
                                                                   @Nullable KeyMapper<Object> crossEventProviderKeyMapper) {
        return new JmixEventProviderDataSerializer(keyMapper, sourceId);
    }
}
