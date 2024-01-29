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

public abstract class AbstractChartEvent<T extends BaseChartEventDetail> extends ComponentEvent<JmixChart> {

    public static final String EVENT_NAME_PREFIX = "jmix-chart:";

    protected JsonObject detailJson;

    protected T detail;
    protected Class<T> detailClass;

    private final static ObjectMapper mapper = JsonMapper.builder()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true)
            .build();

    public AbstractChartEvent(JmixChart source, boolean fromClient,
                              JsonObject detail, Class<T> detailClass) {
        super(source, fromClient);

        this.detailJson = detail;
        this.detailClass = detailClass;
    }

    public JsonObject getDetailJson() {
        return detailJson;
    }

    public T getDetail() {
        if (detail == null) {
            detail = convertDetail(detailClass);
        }
        return detail;
    }

    public <M> M convertDetail(Class<M> detailClass) {
        M converted;
        try {
            converted = mapper.readValue(detailJson.toJson(), detailClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return converted;
    }
}
