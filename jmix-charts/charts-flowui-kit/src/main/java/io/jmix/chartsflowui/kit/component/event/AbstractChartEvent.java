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

package io.jmix.chartsflowui.kit.component.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.vaadin.flow.component.ComponentEvent;
import elemental.json.JsonObject;
import io.jmix.chartsflowui.kit.component.JmixChart;
import io.jmix.chartsflowui.kit.component.event.dto.BaseChartEventDetail;

/**
 * Base chart event class. Stores an {@link AbstractChartEvent#detailJson} object containing the event context.
 *
 * @param <T> origin event class type
 */
public abstract class AbstractChartEvent<T extends BaseChartEventDetail> extends ComponentEvent<JmixChart> {

    public static final String EVENT_NAME_PREFIX = "jmix-chart:";

    private final static ObjectMapper mapper = JsonMapper.builder()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true)
            .build();

    protected JsonObject detailJson;

    protected T detail;
    protected Class<T> detailClass;

    public AbstractChartEvent(JmixChart source, boolean fromClient,
                              JsonObject detail, Class<T> detailClass) {
        super(source, fromClient);

        this.detailJson = detail;
        this.detailClass = detailClass;
    }

    /**
     * {@code detailJson} can be used for independent parsing if the predefined mapping doesn't match.
     *
     * @return {@link JsonObject} describing the context of the event that occurred on the client-side.
     */
    public JsonObject getDetailJson() {
        return detailJson;
    }

    /**
     * Lazily returns an event detail object. Implemented in a lazy way due to the fact that the event stream
     * can be loaded and because of this, parsing all the details can cause performance issues.
     *
     * @return POJO describing the event
     */
    public T getDetail() {
        if (detail == null) {
            detail = convertDetail(detailClass);
        }
        return detail;
    }

    /**
     * Deserializes the incoming event JSON object into a POJO.
     *
     * @param detailClass the class of the resulting POJO object
     * @param <M>         resulting POJO class type
     * @return POJO describing the event if the parse was successful
     * @throws IllegalArgumentException if parsing fails
     */
    protected <M> M convertDetail(Class<M> detailClass) {
        M converted;
        try {
            converted = mapper.readValue(detailJson.toJson(), detailClass);
        } catch (JsonProcessingException e) {
            String message = String.format("Unparsable JsonObject for the '%s'", detailClass.getSimpleName());
            throw new IllegalArgumentException(message, e);
        }

        return converted;
    }
}
